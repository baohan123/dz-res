package com.dz.dzim.config.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dz.dzim.common.SysConstant;
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

import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.*;

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
    private MeetingPlazaDao meetingPlazaDao;

    @Autowired
    RabbitTemplate rabbitTemplate;


    //小会场关联表
    @Autowired
    private MeetingActorDao meetingActorDao;
    @Autowired
    private SessionManage sessionManage;

//    private SessionManage sessionManage = (SessionManage) SpringContextUtil.getApplicationContext().getBean("sessionManage");


    // private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();

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

        Principal principal = session.getPrincipal();
        System.out.println("进入了工具类");
        String payload = message.getPayload();
        JSONObject jsonObject = JSONObject.parseObject(payload);
        String meetingId = jsonObject.getString("meetingId");//小会场id
        Integer contentType = jsonObject.getInteger("contentType");//谁发给谁
        String content = jsonObject.getString("content");//发送内容
        Long memberId = jsonObject.getJSONObject("user").getLong("memberId");
        String memberType = jsonObject.getJSONObject("user").getString("memberType");
        Long waiterId = jsonObject.getJSONObject("kf").getLong("waiterId");
        String waiterType = jsonObject.getJSONObject("kf").getString("waiterType");
        //查询子表是否有记录
        List<MeetingActorEntity> byIdAndActor = meetingActorDao.selectList(new QueryWrapper<>(new MeetingActorEntity(meetingId, SysConstant.ZERO)));
        //未建立会场
        if (byIdAndActor.size() == 0) {
            MeetingActorEntity meetingActorEntity = new MeetingActorEntity(null, memberId, memberType,
                    null, meetingId, null,
                    new Date(), null,
                    SysConstant.ZERO, null);

            rabbitTemplate.convertAndSend("imageExchange","img.#", JSON.toJSONString(meetingActorEntity));

            MeetingActorEntity meetingActorEntity1 = new MeetingActorEntity(null, waiterId, waiterType,
                    null, meetingId, null,
                    new Date(), null,
                    SysConstant.ZERO, null);

            rabbitTemplate.convertAndSend("imageExchange","img.#", JSON.toJSONString(meetingActorEntity1));

        }
        //收信人
        Long addr = new Long(0);
        //发信人
        Long sendId = new Long(0);
        //发件人身份类型、
        String addrType = null;

        //内容
        String sysContent = null;
        switch (contentType) {
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
                sendId = new Long(000);
                addrType = "system";
                break;
            default:
                break;
        }
        sessionManage.handleTextMeg(addr, content, sendId, addrType, contentType);
        System.out.println("接收数据：" + message.getPayload().toString());
        // 处理消息 msgContent消息内容
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
        OnlineUserNew onlineUserNew = sessionManage.get(userid);
        WebSocketSession session1 = onlineUserNew.getSession();
        sessionManage.remove(userid,bigId,meetingId);
        if (session1.isOpen()) {
           //  meetingPlazaDao.update()
            session1.close();
        }
        //
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
