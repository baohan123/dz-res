package com.dz.dzim.pojo;

import com.dz.dzim.common.MessageTypeEnum;

import java.io.Serializable;
import java.security.Principal;

/**
 * 用户信息
 * @author baohan
 * @date 2021/1/22
 */

public class User implements Principal, Serializable {

    private static final long serialVersionUID = 5114506546129512029L;

    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户昵称
     */
    private String username;
    /**
     * 地址
     */
    private String address;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 用户状态
     */
    private MessageTypeEnum status;

    @Override
    public String getName() {
        return userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", address='" + address + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                '}';
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public MessageTypeEnum getStatus() {
        return status;
    }

    public void setStatus(MessageTypeEnum status) {
        this.status = status;
    }


}
