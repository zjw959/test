/**
 * @date 2014/7/8
 * @author ChenLong
 */
package util;

/**
 *
 * @author ChenLong
 */
public class MiscUtils
{
    /**
     * netbeans/eclipse等IDE下 运行/调试 关闭时IDE内部调用Process.destroy() <br />
     * 无法触发到JVM shutdown hook, 导致关闭时无法回存 <br />
     * 故在IDE环境下添加了system property: ideDebug
     *
     * @return
     */
    public static boolean isIDEEnvironment()
    {
        String val = System.getProperty("ideDebug");
        return val != null && val.equals("true");
    }
    
    /**
     * 获取指定毫秒数时间对应的秒数时间(时间戳)
     * 
     * @param timeMillis 毫秒数时间
     * @return 对应的秒数(时间戳)
     */
    public static int getTimestamp(long timeMillis)
    {
        return (int)(timeMillis / 1000L);
    }
    
    /**
     * 获取当前时间戳
     * 
     * @return 当前时间戳
     */
    public static int getTimestamp()
    {
        return getTimestamp(System.currentTimeMillis());
    }
    
}
