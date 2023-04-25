package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.XmlUtil;

/**
 * @author Jas°
 * @date 2021/2/23 (周二)
 */
public class XmlTests {
    public static void main(String[] args) {
        xml2bean();
    }

    public static void toBean() {
        String xml = XmlUtil.toXml(new Student(), true);
        Student student = XmlUtil.toBean(xml, Student.class);
        System.out.println(student);
    }

    public static void toXml() {
        String xml = XmlUtil.toXml(new Student(), true);
        System.out.println(xml);
    }

    private static String xmlPath = "C:\\Users\\DELL\\Desktop\\" + Student.class.getSimpleName() + ".xml";
    public static void xml2bean() {
        XmlUtil.bean2file(new Student(), xmlPath, true);
        Student student = XmlUtil.file2bean(xmlPath, Student.class);
        System.out.println(student);
    }

    public static void bean2xml() {
        XmlUtil.bean2file(new Student(), xmlPath, true);
    }
}
