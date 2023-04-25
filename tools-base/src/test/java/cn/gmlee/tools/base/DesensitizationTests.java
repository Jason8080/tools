package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.DesensitizationUtil;

/**
 * @author Jas°
 * @date 2021/4/28 (周三)
 */
public class DesensitizationTests {
    public static void main(String[] args) {
        System.out.println(DesensitizationUtil.hide("13767663237"));
        System.out.println(DesensitizationUtil.hide("352031199205283113"));
        System.out.println(DesensitizationUtil.hide(""));
        System.out.println(DesensitizationUtil.hide("1"));
        System.out.println(DesensitizationUtil.hide("12"));
        System.out.println(DesensitizationUtil.hide("123"));
        System.out.println(DesensitizationUtil.hide("1234"));
        System.out.println(DesensitizationUtil.hide("12345"));
        System.out.println(DesensitizationUtil.hide("123456"));
        System.out.println(DesensitizationUtil.hide("1234567"));
        System.out.println(DesensitizationUtil.hide("12345678"));
        System.out.println(DesensitizationUtil.hide("123456789"));
        System.out.println(DesensitizationUtil.hide("1234567891"));
        System.out.println(DesensitizationUtil.hide("12345678910"));
        System.out.println(DesensitizationUtil.hide("123456789101"));
        System.out.println(DesensitizationUtil.hide("1234567891012"));
        System.out.println(DesensitizationUtil.hide("12345678910123"));
        System.out.println(DesensitizationUtil.hide("123456789101234"));
        System.out.println(DesensitizationUtil.hide("1234567891012345"));
        System.out.println(DesensitizationUtil.hide("12345678910123456"));
        System.out.println(DesensitizationUtil.hide("123456789101234567"));
        System.out.println(DesensitizationUtil.hide("1234567891012345678"));
        System.out.println(DesensitizationUtil.hide("12345678910123456789"));
    }
}
