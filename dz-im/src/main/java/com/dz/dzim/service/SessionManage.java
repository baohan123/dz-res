package com.dz.dzim.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dz.dzim.common.GeneralUtils;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.mapper.MeetingActorDao;
import com.dz.dzim.mapper.MeetingChattingDao;
import com.dz.dzim.mapper.MeetingDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.OnlineUser;
import com.dz.dzim.pojo.OnlineUserNew;
import com.dz.dzim.pojo.doman.MeetingActorEntity;
import com.dz.dzim.pojo.doman.MeetingChattingEntity;
import com.dz.dzim.pojo.doman.MeetingEntity;
import com.dz.dzim.pojo.doman.MeetingPlazaEntity;
import com.dz.dzim.pojo.vo.MessageVO;
import javafx.scene.control.TextArea;
import jdk.nashorn.internal.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketSession;

import java.beans.Transient;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baohan
 * @className SessionManage
 * @description TODO
 * @date 2021/1/28 9:54
 */
@Component
public class SessionManage {

    private void logerror(String msg) {
        LoggerFactory.getLogger(SessionManage.class).error(msg);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MeetingPlazaDao meetingPlazaDao;

    @Autowired
    private SendMsgServiceNew sendMsgServiceNew;

    //小会场
    @Autowired
    private MeetingDao meetingDao;
    //关联关系
    private MeetingActorDao meetingActorDao;

    /**
     * 大会场 所有在线用户(session + userId + username + createTime  -->
     */
    public Map<Long, OnlineUserNew> clients = new ConcurrentHashMap<>();

    //聊天记录
    @Autowired
    private MeetingChattingDao meetingChattingDao;

    /**
     * 小会场
     */
    //public Map<String, OnlineUserNew> smallclients = new HashMap<>();


    /**
     * 加入会场
     *
     * @param session
     * @param talkerType
     * @param userid
     * @param bigId
     */
    //@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void adds(WebSocketSession session, String talkerType, Long userid, String bigId) {
        clients.put(userid, new OnlineUserNew(bigId, userid, talkerType, session, null, new Date(),
                null, SysConstant.STATUS_ONE, null, null));
        String sysMsg = "用户:" + userid + "===类型：" + talkerType + "==加入大会场啦";
        TextMessage textMessage = new TextMessage(JSONObject.toJSONString(
                getObj(SysConstant.TWO, (JSONObject) new JSONObject()
                        .put("desc", sysMsg))));
        List<WebSocketExtension> extensions = session.getExtensions();
        sendMessage("all", textMessage, null);

        JSONObject jsonObject = new JSONObject();
        List<WebSocketSession> list = new ArrayList<>();
        jsonObject.put("desc", "用户当前在线列表===》");
        for (Map.Entry<Long, OnlineUserNew> entry : getAll().entrySet()) {
            OnlineUserNew user = entry.getValue();
            if ("member".equals(user.getTalkerType()) && SysConstant.ONE == user.getState()) {
                jsonObject.put("usetId", user.getTalker());
                jsonObject.put("bigId", user.getId());
                jsonObject.put("talkerType", user.getTalkerType());
            }
            if ("waiter".equals(user.getTalkerType())) {
                list.add(entry.getValue().getSession());
            }
        }
        String jsonStr = JSONObject.toJSONString(getObj(SysConstant.STATUS_THREE, jsonObject));
        TextMessage lb = new TextMessage(jsonStr);
        list.stream().forEach(s -> {
            try {
                s.sendMessage(lb);
            } catch (IOException e) {
                e.printStackTrace();
                logerror("在线列表发送失败");
            }
        });
    }

    /**
     * 获取大会场session
     *
     * @param key
     */
    public OnlineUserNew get(Long key) {
        Map<Long, OnlineUserNew> clients = this.clients;
        return this.clients.get(key);
    }

    /**
     * 获取大会场
     */
    public Map<Long, OnlineUserNew> getAll() {
        return this.clients;
    }

    /**
     * 添加session
     *
     * @param key 并返回更新后的clent
     */
    public OnlineUserNew remove(Long key, String bigId, String meetingId) {
        OnlineUserNew remove = clients.remove(key);
        meetingPlazaDao.updateById(new MeetingPlazaEntity(bigId, SysConstant.STATUS_FOUR, new Date()));
        meetingDao.updateById(new MeetingEntity(meetingId, new Date(), SysConstant.STATUS_THREE, SysConstant.STATUS_TOW));
        UpdateWrapper<MeetingActorEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("meetingId", meetingId).set("is_leaved", SysConstant.TWO);
        meetingActorDao.update(null, updateWrapper);
        //调mq 删除
        return remove;
    }

    /**
     * 删除并同步关闭连接
     *
     * @param key
     */
    public void removeAndClose(Long key) {
        OnlineUserNew onlineUser = clients.get(key);
        if (null != onlineUser) {
            try {
                onlineUser.getSession().close();
            } catch (IOException e) {
                e.printStackTrace();
                logerror("关闭连接异常");
            }
        }
        clients.remove(key);
        //调mq 删除

    }

    /**
     * 组装返回结果
     *
     * @param returnType
     * @param msg
     * @return
     */
    public JSONObject getObj(Integer returnType, JSONObject msg) {
        JSONObject object = new JSONObject();
        object.put("returnType", returnType);
        object.put("msg", msg);
        return object;
    }

    public void handleTextMeg(Long addr, String content, Long sendId, String addrType, Integer contentType) {
        OnlineUserNew onlineUserNew = get(addr);
        try {
            get(addr).getSession().sendMessage(new TextMessage(content));
            int insert = meetingChattingDao.insert(new MeetingChattingEntity(
                    null, sendId, addrType, null,
                    contentType, System.currentTimeMillis(),
                    null, content, null, null, null
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发消息
     *
     * @param sendType 收信人类型 all=群发
     * @param msg      消息体
     * @param addrId   收件人
     */
    public void sendMessage(String sendType, TextMessage msg, Long addrId) {
        try {
            //   群发
            if ("ALL".equals(sendType) || "all".equals(sendType)) {
                Set<Long> longs = getAll().keySet();
                longs.stream().forEach(l -> {
                    try {
                        clients.get(l).getSession().sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                clients.get(addrId).getSession().sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}