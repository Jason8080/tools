package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Int;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 网络寻址工具.
 *
 * @author Jas °
 */
public class AddressUtil {

    /**
     * 获取当前服务器地址对象.
     *
     * @return the address
     */
    public static InetAddress getAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 获取当前服务器ipv4地址.
     *
     * @return the ip
     */
    public static String getIp() {
        InetAddress loc = getAddress();
        return loc.getHostAddress();
    }

    /**
     * 获取当前服务器mac地址.
     *
     * @return the mac
     */
    public static String getMac() {
        try {
            InetAddress ia = getAddress();
            //获取网卡，获取地址
            NetworkInterface network = NetworkInterface.getByInetAddress(ia);
            byte[] mac = network.getHardwareAddress();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append(":");
                }
                // 转成十进制
                int temp = mac[i] & 0xff;
                String hex = Integer.toHexString(temp);
                sb.append(CharUtil.replenish(hex, Int.TWO));
            }
            return sb.toString().toUpperCase();
        } catch (SocketException e) {
            return ExceptionUtil.cast(e);
        }
    }
}
