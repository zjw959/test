package logic;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;

import ui.WindowMain;
import util.Log4jClientManager;


public class LogicListener {

    static ExecutorService poolService;

    volatile static boolean isStart = false;

    final static AtomicLong currentThread = new AtomicLong(0);
    static ScheduledExecutorService scheduledSinglePoolLog;

    private static final HashSet<Integer> setTestNums = new HashSet<Integer>();


    public static void start(String[] args) {
        if (!Log4jClientManager.getInstance().isTerm) {
            WindowMain.btnStop.setVisible(true);
            WindowMain.btnStop.setEnabled(false);
            WindowMain.cbIsDecode.setEnabled(false);
            WindowMain.cbIsNum.setEnabled(false);
        }

        // 初始化测试数据
        final StringBuilder url = new StringBuilder();
        if (!Log4jClientManager.getInstance().isTerm) {
            url.append(WindowMain.tfURL.getText());
        } else {
            url.append("http://");
            url.append(args[0]);
            Log4jClientManager.getInstance().info("url:" + url);
        }

        final AtomicLong currentHold = new AtomicLong(0);

        // 线程数
        final AtomicInteger configThread = new AtomicInteger();
        if (!Log4jClientManager.getInstance().isTerm) {
            configThread.set(Integer.valueOf((WindowMain.tfThread.getText())));
        } else {
            configThread.set(Integer.valueOf(args[1]));
            Log4jClientManager.getInstance().info("configThread:" + configThread);
        }

        // 保持连接数
        AtomicLong configHold = new AtomicLong(0);
        if (!Log4jClientManager.getInstance().isTerm) {
            configHold.set(Long.valueOf(WindowMain.tfHoldNum.getText()));
        } else {
            configHold.set(Long.valueOf(args[2]));
            Log4jClientManager.getInstance().info("configHold:" + configHold);
        }

        // 发送间隔
        AtomicLong configDelay = new AtomicLong(0);
        if (!Log4jClientManager.getInstance().isTerm) {
            configDelay.set(Long.valueOf(WindowMain.tfSendDelayed.getText()));
        } else {
            configDelay.set(Long.valueOf(args[3]));
            Log4jClientManager.getInstance().info("configDelay:" + configDelay);
        }

        final AtomicLong currentTotal = new AtomicLong(0);
        final AtomicLong currentSuccTotal = new AtomicLong(0);

        // 发送总数
        final AtomicLong configTotal = new AtomicLong(0);
        if (!Log4jClientManager.getInstance().isTerm) {
            configTotal.set(Long.valueOf(WindowMain.tfTimes.getText()));
        } else {
            configTotal.set(Long.valueOf(args[4]));
            Log4jClientManager.getInstance().info("configTotal:" + configTotal);
        }

        // 创建线程
        poolService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("poolService");
                return thread;
            }
        });

        if (!Log4jClientManager.getInstance().isTerm) {
            WindowMain.btnStop.setEnabled(true);
        }

        // 开始测试流程

        // 监听线程
        scheduledSinglePoolLog = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("scheduledSinglePoolMin1");
                return thread;
            }
        });

        Runnable r = new Runnable() {
            long lastcurrent = 0;
            long lastsucc = 0;
            boolean isStop = false;


            @Override
            public void run() {
                try {
                    if (isStart && !isStop) {
                        long _recv = currentTotal.get();
                        long _recv2 = currentSuccTotal.get();
                        long delta = _recv - lastcurrent;
                        long delta2 = _recv2 - lastsucc;
                        lastcurrent = _recv;
                        lastsucc = _recv2;
                        String info = "累计发送次数:" + _recv + ",增量:" + delta + ",实际发送成功的次数:"
                                + currentSuccTotal.get() + ",增量:" + delta2 + ",配置发送的次数:"
                                + configTotal.get() + ",当前保持的连接数:" + currentHold.get()
                                + ",配置当前最大连接数目:" + configHold + ",当前的线程数:" + currentThread.get()
                                + ",配置的线程数:" + configThread;
                        Log4jClientManager.getInstance().info(info);

                        if (configTotal.get() != 0 && currentSuccTotal.get() >= configTotal.get()) {
                            Log4jClientManager.getInstance().info("发送数量已满,开始停止...");
                            if (isStop == false) {
                                isStop = true;
                                stop(false);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log4jClientManager.getInstance().error(e);
                }
            }
        };
        scheduledSinglePoolLog.scheduleWithFixedDelay(r, 1000, 1000, TimeUnit.MILLISECONDS);

        // 发送指令线程创建
        isStart = true;
        for (int i = 0; i < configThread.get(); i++) {
            poolService.execute(new Runnable() {
                @Override
                public void run() {
                    currentThread.incrementAndGet();

                    // Create I/O reactor configuration
                    IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(1)
                            .setConnectTimeout(1000 * 5).setSoTimeout(1000 * 5).build();

                    // Create a custom I/O reactort
                    ConnectingIOReactor ioReactor = null;
                    try {
                        ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
                    } catch (IOReactorException e1) {
                        e1.printStackTrace();
                    }
                    PoolingNHttpClientConnectionManager connManager =
                            new PoolingNHttpClientConnectionManager(ioReactor);

                    CloseableHttpAsyncClient client =
                            HttpAsyncClients.custom().setConnectionManager(connManager).build();
                    client.start();
                    try {
                        while (isStart) {
                            Thread.sleep(configDelay.get());
                            if (((configHold.get() == 0) || (currentHold.get() < configHold.get()))
                                    && (configTotal.get() == 0
                                            || (currentSuccTotal.get() < configTotal.get()))) {
                                // currentSuccTotal.incrementAndGet();

                                // 发送http
                                RequestConfig requestConfig = RequestConfig.custom()
                                        .setSocketTimeout(1).setConnectTimeout(1).build();

                                long _currentTotal = currentTotal.incrementAndGet();
                                currentHold.incrementAndGet();

                                String _url = url.toString();
                                if (WindowMain.cbIsNum.isSelected()
                                        || WindowMain.cbIsDecode.isSelected()) {
                                    _url = _url + "?num=" + _currentTotal + "&pw="
                                            + "j~x4k31l!55#$om43";
                                }
                                if (WindowMain.cbIsDecode.isSelected()) {
                                    _url += "&isD=true";
                                }
                                // System.out.println(_url);
                                HttpGet get = new HttpGet(_url);
                                get.setConfig(requestConfig);

                                client.execute(get, new FutureCallback<HttpResponse>() {
                                    @Override
                                    public void completed(HttpResponse t) {
                                        try {
                                            currentHold.decrementAndGet();
                                            currentSuccTotal.incrementAndGet();
                                            // 回调完成数据装填
                                            complete(EntityUtils.toString(t.getEntity()));
                                        } catch (Exception ex) {
                                            Log4jClientManager.getInstance().error(ex);
                                        }
                                    }

                                    @Override
                                    public void failed(Exception excptn) {
                                        currentHold.decrementAndGet();
                                    }

                                    @Override
                                    public void cancelled() {
                                        currentHold.decrementAndGet();
                                    }

                                });
                            }
                        }


                    } catch (Exception e) {
                        Log4jClientManager.getInstance().error(e);
                    } finally {
                        currentThread.decrementAndGet();
                        try {
                            client.close();
                        } catch (IOException e) {
                            Log4jClientManager.getInstance().error(e);
                        }
                    }
                }
            });
        }
    }

    public static void complete(String result) {
        // TODO 正式部署需要大量测试. 复杂解码以及 自增量是否重复接受
        if (WindowMain.cbIsDecode.isSelected()) {
            if (!result.equals(
                    "@#%@#%asf23546y6%^#$%af@sfsdfsd2$$$342$3432$$3243424444445324324fs#$%^&*(ghvjB33^&567#2#&efsdsd)pscs./fsg")) {
                Log4jClientManager.getInstance().error("严重错误,解码错误");
                stop(true);
            }
        } else if (WindowMain.cbIsNum.isSelected()) {
            Integer testNum = Integer.valueOf(result);
            if (setTestNums.contains(testNum)) {
                Log4jClientManager.getInstance().error("严重错误,重复数字出现:" + testNum);
            } else {
                setTestNums.add(testNum);
                stop(true);
            }
        }
    }

    public static void stop(boolean force) {
        try {
            if (!Log4jClientManager.getInstance().isTerm) {
                WindowMain.btnStart.setVisible(true);
            }

            // 处理流程
            // poolService.shutdownNow();
            if (force) {
                isStart = false;
                if (scheduledSinglePoolLog != null) {
                    scheduledSinglePoolLog.shutdownNow();
                }
                poolService.shutdownNow();
                if (!Log4jClientManager.getInstance().isTerm) {
                    WindowMain.btnStart.setEnabled(true);
                }
            } else {
                if (isStart) {
                    isStart = false;
                    while (currentThread.get() != 0) {
                        Thread.sleep(1000);
                        Log4jClientManager.getInstance()
                                .info("等待关闭...,当前线程:" + currentThread.get());
                    }
                    if (scheduledSinglePoolLog != null) {
                        scheduledSinglePoolLog.shutdownNow();
                    }
                    poolService.shutdownNow();
                    if (!Log4jClientManager.getInstance().isTerm) {
                        WindowMain.btnStart.setEnabled(true);
                    }
                }
            }
            setTestNums.clear();

            if (!Log4jClientManager.getInstance().isTerm) {
                if (WindowMain.cbIsDecode.isSelected()) {
                    WindowMain.cbIsDecode.setEnabled(true);
                }
                if (WindowMain.cbIsNum.isSelected()) {
                    WindowMain.cbIsNum.setEnabled(true);
                }
            }

            Log4jClientManager.getInstance().info("关闭成功");
        } catch (Exception e) {
            Log4jClientManager.getInstance().error(e);
        }
    }

}
