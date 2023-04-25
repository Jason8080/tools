package cn.gmlee.tools.third.party.tencent.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送客服消息(CM)的请求参数实体
 *
 * @author Jas°
 * @date 2020/12/8 (周二)
 */
@Data
public class SendAppletCmRequest implements Serializable {
    /**
     * 接收人OpenId
     */
    private String touser;
    /**
     * 消息类型: text, image, link, miniprogrampage
     */
    private String msgtype;
    private Text text;
    private Image image;
    private Link link;
    private Miniprogrampage miniprogrampage;


    public void setText(Text text) {
        this.msgtype = MessageType.TEXT;
        this.text = text;
    }

    public void setImage(Image image) {
        this.msgtype = MessageType.IMAGE;
        this.image = image;
    }

    public void setLink(Link link) {
        this.msgtype = MessageType.LINK;
        this.link = link;
    }

    public void setMiniprogrampage(Miniprogrampage miniprogrampage) {
        this.msgtype = MessageType.MINI_PROGRAM_PAGE;
        this.miniprogrampage = miniprogrampage;
    }

    @Data
    public static class Text implements Serializable {
        private String content;

        public Text(String content) {
            this.content = content;
        }
    }

    @Data
    public static class Image implements Serializable {
        // 发送的图片的媒体ID，通过 新增素材接口 上传图片文件获得。
        // https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/customer-message/customerServiceMessage.uploadTempMedia.html
        private String media_id;
    }

    @Data
    public static class Link implements Serializable {
        /**
         * 标题
         */
        private String title;
        /**
         * 图文链接消息
         */
        private String description;
        /**
         * 图文链接消息被点击后跳转的链接
         */
        private String url;
        /**
         * 图文链接消息的图片链接，支持 JPG、PNG 格式，较好的效果为大图 640 X 320，小图 80 X 80
         */
        private String thumb_url;
    }

    @Data
    public static class Miniprogrampage implements Serializable {
        /**
         * 标题
         */
        private String title;
        /**
         * 小程序的页面路径，跟app.json对齐，支持参数，比如pages/index/index?foo=bar
         */
        private String pagepath;
        /**
         * 小程序消息卡片的封面，
         * image 类型的 media_id，通过 新增素材接口 上传图片文件获得，
         * 建议大小为 520*416
         */
        private String thumb_media_id;
    }
}
