package com.dz.dzim.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketSession;

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
    private MeetingActorDao meetingActorDao;

    //小会场
    @Autowired
    private MeetingDao meetingDao;


    @Autowired
    RabbitTemplate rabbitTemplate;


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
        String sysMsg = "用户:" + userid + "  类型：" + talkerType + "==加入大会场上线了啦";
        JSONObject desc = new JSONObject();
        desc.put("desc", sysMsg);
        TextMessage textMessage = new TextMessage(
                getObj(SysConstant.TWO, desc));
        List<WebSocketExtension> extensions = session.getExtensions();
        sendMessage("all", textMessage, null);

        JSONArray array = new JSONArray();
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
        TextMessage lb = new TextMessage(getObj(SysConstant.STATUS_THREE, array));
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
        if (null != meetingId) {
            meetingDao.updateById(new MeetingEntity(meetingId, new Date(), SysConstant.STATUS_THREE, SysConstant.STATUS_TOW));
            List<MeetingActorEntity> byIdAndActor = meetingActorDao.selectList(new QueryWrapper<>(new MeetingActorEntity(meetingId, SysConstant.ZERO)));
            if (SysConstant.ZERO != byIdAndActor.size()) {
                UpdateWrapper<MeetingActorEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meetingId", meetingId).set("is_leaved", SysConstant.TWO);
                meetingActorDao.update(null, updateWrapper);
            }
        }
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
    public String getObj(Integer returnType, Object msg) {
        JSONObject object = new JSONObject();
        object.put("returnType", returnType);
        object.put("msg", msg);
        return JSONObject.toJSONString(object);
    }

    public void handleTextMeg(Long addr, String content, Long sendId, String addrType, Integer contentType) {
        try {
            get(addr).getSession().sendMessage(new TextMessage(content));

            MeetingChattingEntity meetingChattingEntity = new MeetingChattingEntity(
                    null, sendId, addrType, null,
                    contentType, System.currentTimeMillis(),
                    null, content, null, null, null
            );

            rabbitTemplate.convertAndSend("imageExchange","img.#", JSON.toJSONString(meetingChattingEntity));

//            int insert = meetingChattingDao.insert(new MeetingChattingEntity(
//                    null, sendId, addrType, null,
//                    contentType, System.currentTimeMillis(),
//                    null, content, null, null, null
//            ));

            get(addr).getSession().sendMessage(new TextMessage(getObj(SysConstant.STATUS_FOUR, content)));
            meetingChattingDao.insert(new MeetingChattingEntity(
                    null, sendId, addrType, null,
                    contentType, System.currentTimeMillis(),
                    null, content, addr, addrType, null
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
