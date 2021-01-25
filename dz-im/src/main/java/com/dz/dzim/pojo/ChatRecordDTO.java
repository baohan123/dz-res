package com.dz.dzim.pojo;

import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 聊天记录
 */
public class ChatRecordDTO implements Serializable {
    /**
     * 用户信息
     */
    private User user;
    /**
     * 消息
     */
    private String message;
    /**
     * 图片
     */
    private String image;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 发送时间
     */
    private String sendTime;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "ChatRecordDTO{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", image='" + image + '\'' +
                ", type='" + type + '\'' +
                ", sendTime='" + sendTime + '\'' +
                '}';
    }
}
