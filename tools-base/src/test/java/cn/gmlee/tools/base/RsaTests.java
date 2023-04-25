package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.RsaUtil;

/**
 * @author Jas°
 * @date 2021/4/26 (周一)
 */
public class RsaTests {

//    public static void main(String[] args) throws Exception {
//        Kv<String, String> kv = RsaUtil.generateSecretKey();
//        String publicKey = kv.getKey();
//        String privateKey = kv.getVal();
//        String str = "站在大明门前守卫的禁卫军，事先没有接到\n" +
//                "有关的命令，但看到大批盛装的官员来临，也就\n" +
//                "以为确系举行大典，因而未加询问。进大明门即\n" +
//                "为皇城。文武百官看到端门午门之前气氛平静，\n" +
//                "城楼上下也无朝会的迹象，既无几案，站队点名\n" +
//                "的御史和御前侍卫“大汉将军”也不见踪影，不免\n" +
//                "心中揣测，互相询问：所谓午朝是否讹传？";
//        System.out.println("公钥长度" + publicKey.length());
//        System.out.println("私钥长度" + privateKey.length());
//        System.out.println(str.getBytes().length);
//        String encodedData = RsaUtil.publicEncoder(str, publicKey);
//        System.out.println("密文长度" + encodedData.length());
//        String decodedData = RsaUtil.privateDecoder(encodedData, privateKey);
//
//
//        String encodedData2 = RsaUtil.privateEncoder(str, privateKey);
//        System.out.println("密文长度" + encodedData2.length());
//        String decodedData2 = RsaUtil.publicDecoder(encodedData2, publicKey);
//    }

    public static void main(String[] args) {
        String content = "12345678";
        Kv<String, String> kv = RsaUtil.generateSecretKey();
        System.out.println("公钥: "+kv.getKey());
        System.out.println("私钥: "+kv.getVal());
        String encoder = RsaUtil.publicEncoder(content, kv.getKey());
        System.out.println("公钥加密: "+ encoder);
        String decoder = RsaUtil.privateDecoder(encoder, kv.getVal());
        System.out.println("私钥解密: "+ decoder);
        encoder = RsaUtil.privateEncoder(content, kv.getVal());
        System.out.println("私钥加密: "+ encoder);
        decoder = RsaUtil.publicDecoder(encoder, kv.getKey());
        System.out.println("公钥解密: "+ decoder);
    }

}
