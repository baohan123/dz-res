package com.dz.dzim.pojo;/**
 * @description: some desc
 * @author: lenovo
 * @email: xxx@xx.com
 * @date: 2021/1/22 13:19
 */

import com.dz.dzim.common.MessageTypeEnum;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import javax.websocket.Session;

/**
 * @author baohan
 * @className OnlineUser  当前在线列表信息 list<OnlineUser>
 * @description TODO
 * @date 2021/1/22 13:19
 */
public class OnlineUser {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 用户会话 (使用该对象进行消息发送)
     */
    private Session session;
    /**
     * 连接时间
     */
    private String createTime;

    /**
     * 用户类型
     * @return
     */
    private MessageTypeEnum userType;

    /**
     * 用户状态
     */
    private MessageTypeEnum status;
    //客服客户端指定字段： 最大连接人数
    private Integer maxClents;
    //客服客户端指定字段： 客服已连接的用户
    private LinkedHashMap<String, User> users;
    //客服客户端指定字段： 当前可接听人数
    private Integer usableClents;

    /**
     * 表示 ==》用户消息
     * @param userId 用户id
     * @param username 用户名称
     * @param session  用户session 回话信息
     * @param createTime 上线时间
     * @param userType 用户类型 USER=用户 KFUSER=客服
     * @param status  用户状态
     */
    public OnlineUser(String userId, String username, Session session, String createTime, MessageTypeEnum userType, MessageTypeEnum status) {
        this.userId = userId;
        this.username = username;
        this.session = session;
        this.createTime = createTime;
        this.userType = userType;
        this.status = status;
    }

    /**
     * 表示 ==》客服消息
     * @param userId 用户id
     * @param username 用户名称
     * @param session  用户session 回话信息
     * @param createTime 上线时间
     * @param userType 用户类型 USER=用户 KFUSER=客服
     * @param status  用户状态
     */
    public OnlineUser(String userId, String username, Session session, String createTime,
                      MessageTypeEnum userType, MessageTypeEnum status,
                      Integer maxClents,LinkedHashMap<String, User> users,Integer usableClents) {
        this.userId = userId;
        this.username = username;
        this.session = session;
        this.createTime = createTime;
        this.userType = userType;
        this.status = status;
        this.maxClents = maxClents;
        this.users = users;
        this.usableClents = usableClents;
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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public MessageTypeEnum getUserType() {
        return userType;
    }

    public void setUserType(MessageTypeEnum userType) {
        this.userType = userType;
    }

    public MessageTypeEnum getStatus() {
        return status;
    }

    public void setStatus(MessageTypeEnum status) {
        this.status = status;
    }

    public Integer getMaxClents() {
        return maxClents;
    }

    public void setMaxClents(Integer maxClents) {
        this.maxClents = maxClents;
    }

    public LinkedHashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(LinkedHashMap<String, User> users) {
        this.users = users;
    }

    public Integer getUsableClents() {
        return usableClents;
    }

    public void setUsableClents(Integer usableClents) {
        this.usableClents = usableClents;
    }
}
