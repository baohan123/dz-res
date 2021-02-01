package com.dz.dzim.pojo.vo;

import com.dz.dzim.common.enums.MessageTypeEnum;
import com.dz.dzim.pojo.User;
import java.io.Serializable;
import java.util.Arrays;

/**
 * 消息视图
 *
 * @author baohan
 * @date 2021/1/22
 */
public class MessageVONew implements Serializable {

    private static final long serialVersionUID = -1455469852669257711L;

    private Long timestamp = System.currentTimeMillis();
    /**
     * 小会场id
     */
    private String meetingId;
    /**
     * 用户
     */
    private User user;
    /**
     * 消息信息
     */
    private String message;
    /**
     * 图片
     */
    private String image;
    /**
     * 谁发给谁
     */
    private MessageTypeEnum sendType;
    /**
     * 说话人类型：
     * 客服(Waiter)，客户(Member)，系统（System），智能应答（Robot）
     */
    private String talkerType;
    /**
     * 消息id
     */
    private String messageId;
    /**
     * 发送时间
     */
    private String sendTime;

    /**
     * 接收者
     */
    private String[] receiver;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public MessageTypeEnum getSendType() {
        return sendType;
    }

    public void setSendType(MessageTypeEnum sendType) {
        this.sendType = sendType;
    }

    public String getTalkerType() {
        return talkerType;
    }

    public void setTalkerType(String talkerType) {
        this.talkerType = talkerType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String[] getReceiver() {
        return receiver;
    }

    public void setReceiver(String[] receiver) {
        this.receiver = receiver;
    }
    public MessageVONew(){

    }
    public MessageVONew(Long timestamp, User user, String message, String image, MessageTypeEnum sendType, String talkerType, String messageId, String sendTime, String[] receiver) {
        this.timestamp = timestamp;
        this.user = user;
        this.message = message;
        this.image = image;
        this.sendType = sendType;
        this.talkerType = talkerType;
        this.messageId = messageId;
        this.sendTime = sendTime;
        this.receiver = receiver;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }
}
