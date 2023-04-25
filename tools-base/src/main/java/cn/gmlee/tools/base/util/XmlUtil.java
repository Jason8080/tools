package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Kv;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Xml util.
 *
 * @author Jas °
 */
public class XmlUtil {
    /**
     * XML格式字符串转换为Map.
     * <p>微信支付专用: 完整版</p>
     *
     * @param xml XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String xml) throws Exception {
        Map<String, String> data = new HashMap(0);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        org.w3c.dom.Document doc = documentBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        // 腾讯用于支持动态标签的骚操作
        for (int idx=0; idx<nodeList.getLength(); ++idx) {
            Node node = nodeList.item(idx);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                data.put(element.getNodeName(), element.getTextContent());
            }
        }
        try {
            stream.close();
        }
        catch (Exception ex) {

        }
        return data;
    }

    /**
     * 将Map转换为XML格式的字符串.
     * <p>微信支付专用: 完整版</p>
     *
     * @param map Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> map) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document document = documentBuilder.newDocument();
        org.w3c.dom.Element root = document.createElement("xml");
        document.appendChild(root);
        for (String key: map.keySet()) {
            String value = map.get(key);
            if (value == null) {
                value = "";
            }
            value = value.trim();
            org.w3c.dom.Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        //.replaceAll("\n|\r", "")
        String output = writer.getBuffer().toString();
        try {
            writer.close();
        }
        catch (Exception ex) {
        }
        return output;
    }

    /**
     * 对象转Xml
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param ignoreHead the ignore head
     * @return the string
     */
    public static <T> String toXml(T t, boolean ignoreHead) {
        if (t != null) {
            try {
                JAXBContext context = JAXBContext.newInstance(t.getClass());
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                // 不生成报头
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, ignoreHead);
                StringWriter writer = new StringWriter();
                marshaller.marshal(t, writer);
                return writer.toString();
            } catch (Exception e) {
                return ExceptionUtil.cast(e);
            }
        }
        return "";
    }


    /**
     * xml转成对象
     *
     * @param <T>   the type parameter
     * @param xml   the xml
     * @param clazz the clazz
     * @return t t
     */
    public static <T> T toBean(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 输入流转对象
     *
     * @param <T>    the type parameter
     * @param reader the reader
     * @param clazz  the clazz
     * @return t
     */
    public static <T> T toBean(Reader reader, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 对象输出成xml.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param writer     the writer
     * @param ignoreHead the ignore head
     */
    public static <T> void toXml(T t, Writer writer, boolean ignoreHead) {
        if (t != null) {
            try {
                JAXBContext context = JAXBContext.newInstance(t.getClass());
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                // 不生成报头
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, ignoreHead);
                marshaller.marshal(t, writer);
            } catch (Exception e) {
                ExceptionUtil.cast(e);
            }
        }
    }

    /**
     * xml文件转换为对象
     *
     * @param <T>     the type parameter
     * @param xmlPath the xml path
     * @param clazz   the clazz
     * @return the t
     */
    public static <T> T file2bean(String xmlPath, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new FileReader(xmlPath));
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 对象转xml文件.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param xmlPath    the xml path
     * @param ignoreHead the ignore head
     */
    public static <T> void bean2file(T t, String xmlPath, boolean ignoreHead) {
        if (t != null) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(t.getClass());
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                // 不生成报头
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, ignoreHead);
                marshaller.marshal(t, new FileWriter(xmlPath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class KvAdapter<K, V> extends XmlAdapter<HashMap<K, V>, Kv<K, V>[]> {
        @Override
        public HashMap<K, V> marshal(Kv<K, V>[] kvs) throws Exception {
            HashMap<K, V> map = new HashMap(kvs.length);
            for(int i=0;i<kvs.length;i++) {
                Kv<K, V> pairs = kvs[i];
                map.put(pairs.getKey(), pairs.getVal());
            }
            return map;
        }

        @Override
        public Kv[] unmarshal(HashMap<K, V> map) throws Exception {
            Kv<K, V>[] kvs = new Kv[map.size()];
            int index = 0;
            for(Map.Entry<K, V> entry: map.entrySet()) {
                Kv<K, V> kv = new Kv();
                kv.setKey(entry.getKey());
                kv.setVal(entry.getValue());
                kvs[index++] = kv;
            }
            return kvs;
        }
    }
}
