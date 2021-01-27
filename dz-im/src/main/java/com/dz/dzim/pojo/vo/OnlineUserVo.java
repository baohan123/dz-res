package com.dz.dzim.pojo.vo;

/**
 * @author baohan
 * @className OnlineUser  当前在线列表信息返回
 * @description TODO
 * @date 2021/1/22 13:19
 */
public class OnlineUserVo {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String username;

    /**
     * 连接时间
     */
    private String createTime;


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


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "OnlineUserVo{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
