package com.dz.dzim.pojo;/**
 * @description: some desc
 * @author: lenovo
 * @email: xxx@xx.com
 * @date: 2021/1/22 13:19
 */

import com.dz.dzim.common.enums.MessageTypeEnum;
import org.springframework.web.socket.WebSocketSession;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author baohan
 * @className OnlineUser  当前在线列表信息 list<OnlineUser>
 * @description TODO
 * @date 2021/1/22 13:19
 */
public class OnlineUserNew {
    /**
     * 主会场 id
     */
    private String id;

    /**
     * 说话人在其所属类型下的唯一编号
     */
    private Long talker;
    /**
     * 说话人类型：
     * 客服(Waiter)，客户(Member)，系统（System），智能应答（Robot）
     */
    private String talkerType;
    /**
     * 用户会话 (使用该对象进行消息发送)
     */
    private WebSocketSession session;

    /**
     * 终端设备标识
     */
    private Long meid;

    /**
     * 用户加入主会场的时间
     */
    private Date enterTime;
    /**
     * 用户离开主会场的时间
     */
    private Date leavingTime;
    /**
     * 当前状态：0：未定义；1、等待中；2：交谈中；3：休息中；4：已离开
     */
    private Integer state;
    /**
     * 前一次登录主会场时所分配的ID，0表示这是第一次登录主会场，- 表示无记录
     */
    private String prevId;
    /**
     * 后一次登录主会场时所分配的ID，0表示这是最后一次登录主会场, - 表示无记录
     */
    private String nextId;


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

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public Long getMeid() {
        return meid;
    }

    public void setMeid(Long meid) {
        this.meid = meid;
    }

    public Date getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(Date enterTime) {
        this.enterTime = enterTime;
    }

    public Date getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(Date leavingTime) {
        this.leavingTime = leavingTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getPrevId() {
        return prevId;
    }

    public void setPrevId(String prevId) {
        this.prevId = prevId;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OnlineUserNew(String id,Long talker, String talkerType, WebSocketSession session, Long meid, Date enterTime, Date leavingTime, Integer state, String prevId, String nextId) {
        this.id = id;
        this.talker = talker;
        this.talkerType = talkerType;
        this.session = session;
        this.meid = meid;
        this.enterTime = enterTime;
        this.leavingTime = leavingTime;
        this.state = state;
        this.prevId = prevId;
        this.nextId = nextId;
    }
    public OnlineUserNew(){

    }
}
