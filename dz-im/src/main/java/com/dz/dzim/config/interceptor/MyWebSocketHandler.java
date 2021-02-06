package com.dz.dzim.config.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dz.dzim.common.GeneralUtils;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.config.rabbitmq.RabbitMqConfig;
import com.dz.dzim.mapper.MeetingActorDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.OnlineUserNew;
import com.dz.dzim.pojo.doman.MeetingActorEntity;
import com.dz.dzim.service.SessionManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baohan
 * @className websocket 处理器
 * @description TODO
 * @date 2021/1/28 14:13
 */
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RabbitTemplate rabbitTemplate;


    //小会场关联表
    @Autowired
    private MeetingActorDao meetingActorDao;
    @Autowired
    private SessionManage sessionManage;
    //未读列表
    public Map<String, List<JSONObject>> unreadList = new ConcurrentHashMap<>();


    /**
     * 建立成功事件
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        String talkerType = (String) attributes.get("talkerType");
        Long userid = new Long((String) attributes.get("userid"));
        String bigId = (String) attributes.get("bigId");
        sessionManage.adds(session, talkerType, userid, bigId);
        log.info("connect websocket success.......");
    }

    /**
     * 接受消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(message.getPayload());
        String meetingId = jsonObject.getString("meetingId");//小会场id
        Integer contentType = jsonObject.getInteger("contentType");//谁发给谁
        if (SysConstant.ONE == contentType) {
            session.sendMessage(new TextMessage(
                    sessionManage.getObj(SysConstant.ONE, "心跳ok")));
            return;
        }
        String content = jsonObject.getString("content");//发送内容
        Long memberId = jsonObject.getJSONObject("user").getLong("memberId");
        String memberType = jsonObject.getJSONObject("user").getString("memberType");
        Long waiterId = jsonObject.getJSONObject("kf").getLong("waiterId");
        //未读列表  未分配小会场
        if (unList(meetingId, memberId, waiterId, jsonObject, content, session)) {
            return;
        }
        String waiterType = jsonObject.getJSONObject("kf").getString("waiterType");

        //收信人
        Long addr = new Long(SysConstant.ZERO);
        //发信人
        Long sendId = new Long(SysConstant.ZERO);
        //发件人身份类型、
        String addrType = null;
        boolean isture = false;
        //内容
        String sysContent = null;
        switch (contentType) {
            //2 客服领取用户（初次建立小会场）
            case 2:
                sessionManage.sendMessageSys(SysConstant.STATUS_TOW, jsonObject, memberId);
                isture = true;
                break;
            //用户发送至客服
            case 8:
                addr = waiterId;
                sendId = memberId;
                addrType = memberType;
                break;
            //客服发送至用户
            case 9:
                //判断是否已有小会场
                addr = memberId;
                sendId = waiterId;
                addrType = waiterType;

                break;
            //系统发送至用户
            case 10:
                addr = memberId;
                sendId = new Long(000);
                addrType = "system";
                break;

            //系统发送至客服
            case 11:
                addr = waiterId;
                sendId = new Long(0);
                addrType = "system";
                break;
            default:
                break;
            //用户发送图片-》客服
            case 12:
                addr = waiterId;
                sendId = memberId;
                addrType = memberType;
                break;
            //客服发送图片-》用户
            case 13:
                addr = memberId;
                sendId = waiterId;
                addrType = waiterType;
        }
        if (true == isture) {
            //初次建立会场后 更新用户状态 ，更新代抢列表
            OnlineUserNew onlineUserNew = sessionManage.clients.get(memberId);
            if (null == onlineUserNew) {
                onlineUserNew.setState(SysConstant.STATUS_TOW);
            }
            sessionManage.rodList();
            return;
        }
        //判断是否是初次进入 初次进入小会场
        creatMeetActor(meetingId, memberId, memberType, waiterId, waiterType);
        if (StringUtils.isEmpty(content)) {
            sessionManage.sendMessageSys(SysConstant.STATUS_TOW, "发送消息不能为空", addr);
        }
        sessionManage.handleTextMeg(addr, content, sendId, addrType, contentType, jsonObject);
        System.out.println("接收数据：" + message.getPayload().toString());
        // 处理消息 msgContent消息内容
    }

    /**
     * 未读消息列表
     *
     * @param meetingId
     * @param memberId
     * @param waiterId
     * @param jsonObject
     * @param content
     * @param session
     * @throws IOException
     */
    public boolean unList(String meetingId, Long memberId, Long waiterId, JSONObject jsonObject, String content, WebSocketSession session) throws IOException {
        if (null == waiterId && null == meetingId) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("content", content);
            jsonObject1.put("addrTime", new Date()); //未读 接受时间
            if (null != unreadList.get(memberId)) {
                unreadList.get(String.valueOf(memberId)).add(jsonObject);
            } else {
                List<JSONObject> jsonObjects = new ArrayList<>();
                jsonObjects.add(jsonObject);
                unreadList.put(String.valueOf(memberId), jsonObjects);
            }
            session.sendMessage(new TextMessage(sessionManage.getObj(SysConstant.FIVE, this.unreadList)));
            // return;
            return true;
        }
        return false;
    }

    /**
     * 是否初次进入小会场
     * 2 @param meetingId  下会场id
     *
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
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY2, GeneralUtils.objectToString("insert", meetingActorEntity));
            MeetingActorEntity meetingActorEntity1 = new MeetingActorEntity(null, waiterId, waiterType,
                    null, meetingId, null,
                    new Date(), null,
                    SysConstant.ZERO, null);
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY2, GeneralUtils.objectToString("insert", meetingActorEntity1));
            return true;
        }
        return false;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("连接出错");
        //sessionManage.remove(null);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // TODO Auto-generated method stub
        log.info("connect websocket closed.......");
        Map<String, Object> attributes = session.getAttributes();
        String talkerType = (String) attributes.get("talkerType");
        Long userid = new Long((String) attributes.get("userid"));
        String bigId = (String) attributes.get("bigId");
        String meetingId = (String) attributes.get("meetingId");
        sessionManage.remove(userid, bigId, meetingId, talkerType);
        if (session.isOpen()) {
            session.close();
        }
    }

    // 处理二进制消息
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        super.handleBinaryMessage(session, message);
    }

    protected void handlePingMessage(WebSocketSession session) throws Exception {
        byte[] array = new byte[1];
        array[0] = 1;
        ByteBuffer buffer = ByteBuffer.wrap(array);
        PingMessage pingMessage = new PingMessage(buffer);
        session.sendMessage(pingMessage);
    }

}
