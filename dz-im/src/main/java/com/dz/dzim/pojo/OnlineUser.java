package com.dz.dzim.pojo;/**
 * @description: some desc
 * @author: lenovo
 * @email: xxx@xx.com
 * @date: 2021/1/22 13:19
 */

import java.time.LocalDateTime;
import java.util.Date;
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
     * @param userId   用户id
     * @param username 用户名称
     * @param session  用户session 回话信息
     */
    public OnlineUser(String userId, String username, Session session) {
        this.userId = userId;
        this.username = username;
        this.session = session;
        this.createTime = new Date().toString();
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
}
