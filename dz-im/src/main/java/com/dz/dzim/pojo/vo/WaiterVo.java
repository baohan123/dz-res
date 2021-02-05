package com.dz.dzim.pojo.vo;

import org.springframework.web.socket.WebSocketSession;

/**
 * @author baohan
 * @className 客服信息vo
 * @description TODO
 * @date 2021/2/5 13:14
 */
public class WaiterVo {
    /**
     * 大会场id
     */
    private String bigId;

    /**
     * 小会场id
     */
    private String meetingId;

    /**
     * 客服id
     */
    private Long waiterId;

    /**
     * 客服类型
     */
    private String waiterType;

    /**
     * 会场
     */
    private WebSocketSession session;

    public String getBigId() {
        return bigId;
    }

    public void setBigId(String bigId) {
        this.bigId = bigId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public Long getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(Long waiterId) {
        this.waiterId = waiterId;
    }

    public String getWaiterType() {
        return waiterType;
    }

    public void setWaiterType(String waiterType) {
        this.waiterType = waiterType;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
