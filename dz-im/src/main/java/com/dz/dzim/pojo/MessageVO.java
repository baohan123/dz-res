package com.dz.dzim.pojo;


import com.dz.dzim.common.MessageTypeEnum;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 消息视图
 *
 * @author baohan
 * @date 2021/1/22
 */
public class MessageVO implements Serializable {

    private static final long serialVersionUID = -1455469852669257711L;

    private Long timestamp = System.currentTimeMillis();

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
     * 消息类型
     * SYSTEM,  //系统消息
     * USER,   //用户消息
     * REVOKE, //撤回消息
     * ROBOT,  //机器人及其他
     */
    private MessageTypeEnum type;
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

    public MessageTypeEnum getType() {
        return type;
    }

    public void setType(MessageTypeEnum type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "MessageVO{" +
                ", user=" + user +
                ", message='" + message + '\'' +
                ", image='" + image + '\'' +
                ", type=" + type +
                ", messageId='" + messageId + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", receiver=" + Arrays.toString(receiver) +
                '}';
    }


    public MessageVO(User user, String message, MessageTypeEnum type, String[] receiver, String image) {
        this.user = user;
        this.message = message;
        this.image = image;
        this.type = type;
        this.receiver = receiver;
    }

    public MessageVO(User user, String message, MessageTypeEnum type, String[] receiver) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.receiver = receiver;
    }



    public MessageVO(User user, String message, MessageTypeEnum type) {
        this.user = user;
        this.message = message;
        this.type = type;
    }



}
