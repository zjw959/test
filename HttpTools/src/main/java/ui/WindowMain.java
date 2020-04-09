package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import logic.LogicListener;


public class WindowMain extends JFrame {

    private static final long serialVersionUID = 1L;
    public static JTextField tfURL;
    public static JTextField tfTimes;
    public static JTextField tfSendDelayed;
    public static JCheckBox cbScroll;
    public static JButton btnStart;
    public static JButton btnStop;
    public static JTextArea textArea;
    public static JTextField tfHoldNum;
    public static JLabel label;
    public static JTextField tfThread;
    public static JCheckBox cbIsNum;
    public static JCheckBox cbIsDecode;

    private ExecutorService backGround = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("backGround");
            return thread;
        }
    });

    /**
     * Create the frame.
     */
    public WindowMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 709, 706);
        getContentPane().setLayout(null);

        JLabel lblIp = new JLabel("URL");
        lblIp.setBounds(26, 12, 26, 16);
        getContentPane().add(lblIp);

        tfURL = new JTextField();
        tfURL.setText("http://");
        tfURL.setBounds(65, 6, 608, 28);
        getContentPane().add(tfURL);
        tfURL.setColumns(10);

        JLabel lblSenddelayed = new JLabel("单线程发送时间间隔(ms)");
        lblSenddelayed.setBounds(262, 92, 159, 16);
        getContentPane().add(lblSenddelayed);

        tfSendDelayed = new JTextField("1000");
        tfSendDelayed.setBounds(433, 86, 134, 28);
        getContentPane().add(tfSendDelayed);
        tfSendDelayed.setColumns(10);

        btnStart = new JButton("Start");
        btnStart.setBounds(26, 162, 117, 29);
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (btnStart) {
                    if (btnStart.isEnabled()) {
                        btnStart.setVisible(false);
                        btnStart.setEnabled(false);
                        backGround.execute(new Runnable() {
                            public void run() {
                                LogicListener.start(null);
                            }
                        });
                    }
                }
            }
        });
        getContentPane().add(btnStart);

        btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (btnStart) {
                    if (btnStop.isEnabled()) {
                        btnStop.setVisible(false);
                        btnStop.setEnabled(false);
                        backGround.execute(new Runnable() {
                            public void run() {
                                LogicListener.stop(false);
                            }
                        });
                    }
                }
            }
        });
        btnStop.setBounds(26, 162, 117, 29);
        getContentPane().add(btnStop);

        JButton btnClear = new JButton("清屏");
        btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backGround.execute(new Runnable() {
                    @Override
                    public void run() {
                        textArea.setText("");
                    }
                });
            }
        });
        btnClear.setBounds(594, 164, 86, 29);
        getContentPane().add(btnClear);

        JLabel lblTimes = new JLabel("发送次数");
        lblTimes.setBounds(26, 51, 62, 16);
        getContentPane().add(lblTimes);

        tfTimes = new JTextField("100");
        tfTimes.setBounds(100, 46, 134, 28);
        getContentPane().add(tfTimes);
        tfTimes.setColumns(10);

        textArea = new JTextArea();
        textArea.setBounds(2, 325, 643, 148);
        getContentPane().add(textArea);
        textArea.setLineWrap(true);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                backGround.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (cbScroll.isSelected()) {
                            textArea.setSelectionStart(WindowMain.textArea.getText().length());
                        }
                    }
                });
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                backGround.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (cbScroll.isSelected()) {
                            textArea.setSelectionStart(WindowMain.textArea.getText().length());
                        }
                    }
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                backGround.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (cbScroll.isSelected()) {
                            textArea.setSelectionStart(WindowMain.textArea.getText().length());
                        }
                    }
                });
            }
        });
        
        JScrollPane jScrollPane = new JScrollPane(textArea);
        jScrollPane.setBounds(26, 203, 647, 475);
        getContentPane().add(jScrollPane);

        cbScroll = new JCheckBox("是否滚屏");
        cbScroll.setSelected(true);
        cbScroll.setBounds(465, 162, 117, 29);
        getContentPane().add(cbScroll);

        JLabel lblHoldNum = new JLabel("同时保持最大连接数");
        lblHoldNum.setBounds(262, 51, 143, 16);
        getContentPane().add(lblHoldNum);

        tfHoldNum = new JTextField("1");
        tfHoldNum.setColumns(10);
        tfHoldNum.setBounds(433, 46, 134, 28);
        getContentPane().add(tfHoldNum);

        label = new JLabel("线程数");
        label.setBounds(26, 92, 62, 16);
        getContentPane().add(label);

        tfThread = new JTextField("1");
        tfThread.setColumns(10);
        tfThread.setBounds(100, 86, 134, 28);
        getContentPane().add(tfThread);

        cbIsNum = new JCheckBox("数字测试");
        cbIsNum.setBounds(26, 121, 117, 29);
        getContentPane().add(cbIsNum);
        cbIsNum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbIsNum.isSelected()) {
                    cbIsDecode.setSelected(false);
                    cbIsDecode.setEnabled(false);
                } else {
                    cbIsDecode.setSelected(false);
                    cbIsDecode.setEnabled(true);
                }
            }
        });

        cbIsDecode = new JCheckBox("解码测试");
        cbIsDecode.setBounds(155, 120, 117, 29);
        getContentPane().add(cbIsDecode);
        cbIsDecode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbIsDecode.isSelected()) {
                    cbIsNum.setSelected(false);
                    cbIsNum.setEnabled(false);
                } else {
                    cbIsNum.setSelected(false);
                    cbIsNum.setEnabled(true);
                }
            }
        });
    }
}
