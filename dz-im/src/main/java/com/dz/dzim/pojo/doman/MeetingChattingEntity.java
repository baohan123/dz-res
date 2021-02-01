package com.dz.dzim.pojo.doman;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 聊天记录表
 *
 * @author baohan
 * @email
 * @date 2021-01-31 17:24:42
 */
@TableName("meeting_chatting")
public class MeetingChattingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 说话人在其所属类型下的唯一编号
     */
    private Long talker;
    /**
     * 客服(Waiter)，客户(Member)，系统（System），智能应答（Robot）
     */
    private String talkerType;
    /**
     * 内容长度，允许值：0x0001~0x7fff
     */
    private Integer contentLength;
    /**
     * 0x00~0x1F:保留；
     * 0x20：主会场欢迎辞；0x21:主会场告别辞；0x23:主会场闭幕辞；
     * 0x24~0x2F：保留；
     * 0x30：欢迎辞；0x31：告别辞；0x32~0x3f：保留；
     * 0x40：普通正文；0x41：文件发送；0x42：文件接收；0x43：截图发送；0x44:截图接收；00x45~0x5f保留；
     * 0x60~0xff：未定义
     * 8 用户发消息给客服
     * 9 客服发消息给用户
     * 10 系统发消息给用户
     * 11 系统发消息给客服
     */
    private Integer contentType;
    /**
     * 时间戳，消息到达服务器端（会场）的时间
     */
    private Long serverTime;
    /**
     * 内容标签，用于对聊天内容的总体评定
     */
    private String contentRemark;
    /**
     * 聊天的内容文本
     */
    private String content;
    /**
     * 保留
     */
    private String reserved0;
    /**
     * 保留
     */
    private String reserved1;
    /**
     * 保留
     */
    private String reserved2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTalker() {
        return talker;
    }

    public void setTalker(Long talker) {
        this.talker = talker;
    }

    public String getTalkerType() {
        return talkerType;
    }

    public void setTalkerType(String talkerType) {
        this.talkerType = talkerType;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public String getContentRemark() {
        return contentRemark;
    }

    public void setContentRemark(String contentRemark) {
        this.contentRemark = contentRemark;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReserved0() {
        return reserved0;
    }

    public void setReserved0(String reserved0) {
        this.reserved0 = reserved0;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public MeetingChattingEntity(Long id, Long talker, String talkerType, Integer contentLength, Integer contentType, Long serverTime, String contentRemark, String content, String reserved0, String reserved1, String reserved2) {
        this.id = id;
        this.talker = talker;
        this.talkerType = talkerType;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.serverTime = serverTime;
        this.contentRemark = contentRemark;
        this.content = content;
        this.reserved0 = reserved0;
        this.reserved1 = reserved1;
        this.reserved2 = reserved2;
    }

    @Override
    public String toString() {
        return "MeetingChattingEntity{" +
                "talker=" + talker +
                ", talkerType='" + talkerType + '\'' +
                ", contentLength=" + contentLength +
                ", contentType=" + contentType +
                ", serverTime=" + serverTime +
                ", contentRemark='" + contentRemark + '\'' +
                ", content='" + content + '\'' +
                ", reserved0='" + reserved0 + '\'' +
                ", reserved1='" + reserved1 + '\'' +
                ", reserved2='" + reserved2 + '\'' +
                '}';
    }
}
