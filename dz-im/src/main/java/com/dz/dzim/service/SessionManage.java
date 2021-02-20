package com.dz.dzim.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dz.dzim.common.ResultWebSocket;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.mapper.MeetingActorDao;
import com.dz.dzim.mapper.MeetingChattingDao;
import com.dz.dzim.mapper.MeetingDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.OnlineUserNew;
import com.dz.dzim.pojo.doman.MeetingActorEntity;
import com.dz.dzim.pojo.doman.MeetingChattingEntity;
import com.dz.dzim.pojo.doman.MeetingEntity;
import com.dz.dzim.pojo.doman.MeetingPlazaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketSession;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

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
    private MeetingActorDao meetingActorDao;

    //小会场
    @Autowired
    private MeetingDao meetingDao;

    @Autowired
    private MeetingChattingDao meetingChattingDao;
//    @Autowired
//    RabbitTemplate rabbitTemplate;

    /**
     * 大会场 所有在线用户(session + userId + username + createTime  -->
     */
    public Map<Long, OnlineUserNew> clients = new ConcurrentHashMap<>();

    //小会场
    public Map<String, CopyOnWriteArraySet<WebSocketSession>> smallClent = new ConcurrentHashMap<>();


    /**
     * 加入会场
     *
     * @param session
     * @param talkerType
     * @param userid
     * @param bigId
     */
    public void adds(WebSocketSession session, String talkerType, Long userid, String bigId) {
        clients.put(userid, new OnlineUserNew(bigId, userid, talkerType, session, null, new Date(),
                null, SysConstant.STATUS_ONE, null, null));
        String sysMsg = "用户:" + userid + "  类型：" + talkerType + "==加入大会场上线了啦";
        sendMessage("all", ResultWebSocket.txtMsg(SysConstant.FOUR, sysMsg), null);
        //更新代抢列表
        rodList();
    }

    /**
     * 代抢列表
     */
    public void rodList() {
        //代抢用户
        JSONArray array = new JSONArray();
        //在线客服
        List<WebSocketSession> list = new ArrayList<>();
        for (Map.Entry<Long, OnlineUserNew> entry : getAll().entrySet()) {
            JSONObject jsonObject = new JSONObject();
            OnlineUserNew user = entry.getValue();
            if ("member".equals(user.getTalkerType()) && SysConstant.ONE == user.getState()) {
                jsonObject.put("usetId", user.getTalker());
                jsonObject.put("bigId", user.getId());
                jsonObject.put("talkerType", user.getTalkerType());
                array.add(jsonObject);
            }
            if ("waiter".equals(user.getTalkerType())) {
                list.add(entry.getValue().getSession());
            }
        }
        TextMessage txtMsg = ResultWebSocket.txtMsg(SysConstant.STATUS_THREE, array);
        list.stream().forEach(s -> {
            try {

                s.sendMessage(txtMsg);
            } catch (IOException e) {
                e.printStackTrace();
                logerror("在线列表发送失败");
            }
        });
        logger.info("更新代抢列表，当前用户共:==>" + array.size());

    }

    /**
     * 获取大会场session
     *
     * @param key
     */
    public OnlineUserNew get(Long key) {
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
    public OnlineUserNew remove(Long key, String bigId, String meetingId, String talkerType) {
        smallClent.remove(meetingId);
        OnlineUserNew remove = clients.remove(key);
        //如果用户下线 更新代抢列表
        if ("member".equals(talkerType)) {
            rodList();
        }
        meetingPlazaDao.updateById(new MeetingPlazaEntity(bigId, SysConstant.STATUS_FOUR, new Date()));
        if (null != meetingId) {
            meetingDao.updateById(new MeetingEntity(meetingId, new Date(), SysConstant.STATUS_THREE, SysConstant.STATUS_TOW));
            List<MeetingActorEntity> byIdAndActor = meetingActorDao.selectList(new QueryWrapper<>(new MeetingActorEntity(meetingId, SysConstant.ZERO)));
            if (SysConstant.ZERO != byIdAndActor.size()) {
                UpdateWrapper<MeetingActorEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meetingId", meetingId).set("is_leaved", SysConstant.TWO);
                meetingActorDao.update(null, updateWrapper);
            }
        }
        logger.info("下线end==>:" + key);
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

    public void handleTextMeg(Long addr, String content, Long sendId, String addrType,
                              Integer contentType, JSONObject jsonObject, String meetingId) {
        try {
            CopyOnWriteArraySet<WebSocketSession> webSocketSessions = smallClent.get(meetingId);
            for (WebSocketSession websocketServerEndpoint : webSocketSessions) {
                websocketServerEndpoint.sendMessage(ResultWebSocket.txtMsg(contentType, jsonObject));
            }
            // get(addr).getSession().sendMessage(ResultWebSocket.txtMsg(contentType, jsonObject));

            MeetingChattingEntity meetingChattingEntity = new MeetingChattingEntity(
                    null, sendId, addrType, null,
                    contentType, System.currentTimeMillis(),
                    null, content, addr, addrType, null
            );
            meetingChattingDao.insert(meetingChattingEntity);
            // rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY1, GeneralUtils.objectToString("insert", meetingChattingEntity));

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("sessionmanger.handleTextMeg()==>" + "addrId:" + addr);
        }

    }

    //系统消息
    public void sendMessageSys(Integer sendType, Object msg, Long addrId) {
        try {
            clients.get(addrId).getSession().sendMessage(ResultWebSocket.txtMsg(sendType, msg));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("==>sendMessageSys()" + "addrId:" + addrId);
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
            logger.error("==>sendMessage()" + "addrId:" + addrId);
        }
    }

    /**
     * 是否初次进入小会场
     *
     * @param meetingId  下会场id
     * @param memberId
     * @param memberType
     * @param waiterId
     * @param waiterType
     * @return
     */
    public boolean creatMeetActor(String meetingId, Long memberId, String memberType, Long waiterId, String waiterType) {
        //查询子表是否有记录
        List<MeetingActorEntity> byIdAndActor = meetingActorDao.selectList(new QueryWrapper<>(new MeetingActorEntity(meetingId, SysConstant.ZERO)));
        //未建立会场
        if (byIdAndActor.size() == 0) {
            MeetingActorEntity meetingActorEntity = new MeetingActorEntity(null, memberId, memberType,
                    null, meetingId, null,
                    new Date(), null,
                    SysConstant.ZERO, null);
            // rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY2, GeneralUtils.objectToString("insert", meetingActorEntity));
            meetingActorDao.insert(meetingActorEntity);
            MeetingActorEntity meetingActorEntity1 = new MeetingActorEntity(null, waiterId, waiterType,
                    null, meetingId, null,
                    new Date(), null,
                    SysConstant.ZERO, null);
            //rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY2, GeneralUtils.objectToString("insert", meetingActorEntity1));
            meetingActorDao.insert(meetingActorEntity1);

            return true;
        }
        return false;
    }

    public void smallMeeing(String meetingId, WebSocketSession session, Long userid) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = smallClent.get(meetingId);
        if (null == webSocketSessions) {
            webSocketSessions = new CopyOnWriteArraySet<>();
        }
        webSocketSessions.add(session);
        smallClent.put(meetingId, webSocketSessions);
        try {
            session.sendMessage(ResultWebSocket.txtMsg(111, "小会场创建成功" + userid + "加入小会场"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
