package com.dz.dzim.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;


import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @Author：baohan
 * @Description：  {userId} 最好是IP
 * @Date： created in 09:31 2021/1/19
 */
@Component
@ServerEndpoint(value = "/connectWebSocket/{userId}")
public class WebSocket {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 在线人数  需保证线程安全的。
     */
    public static int onlineNumber = 0;
    /**
     * 以用户的姓名为key，l为对象保存起来
     */
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();
    /**
     * 会话
     */
    private Session session;
    /**
     * 用户名称
     */
    private String userId;

    private HttpSession httpSession;

    /**
     * 建立连接  连接成功调用的方法
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session,EndpointConfig config)
    {
        addOnlineCount();
        logger.info("建立连接：用户id："+session.getId()+"用户名："+userId);
        this.userId = userId;
        this.session = session;

        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        if(httpSession != null){
            ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(httpSession.getServletContext());
            String id = ctx.getId();
            Object bean = ctx.getBean("");
        }
        //  logger.info("有新连接加入！ 当前在线人数" + onlineNumber);
      //  try {
            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
            //!!!先给所有人发送通知，说我上线了
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",1);
            map1.put("userId",userId);
          //  sendMessageAll(JSON.toJSONString(map1),userId);

            //把自己的信息加入到map当中去
            clients.put(userId, this);
            logger.info("有连接完成！ 当前在线人数" + clients.size());
            //给自己发一条消息：告诉自己现在都有谁在线
            Map<String,Object> map2 = new HashMap<>();
            map2.put("messageType",3);
            //移除掉自己
            Set<String> set = clients.keySet();
            map2.put("onlineUsers",set);
            map2.put("online","都谁上线了");
            //单独发送   自己给自己发的 告诉自己谁都在线
            //sendMessageTo(JSON.toJSONString(map2),userId);
//        }
//        catch (IOException e){
//            logger.info(userId+"上线的时候通知所有人发生了错误");
//        }



    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("服务端发生了错误"+error.getMessage());
        //error.printStackTrace();
    }
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose()
    {
        subOnlineCount();
        //webSockets.remove(this);
        clients.remove(userId);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",2);
            map1.put("onlineUsers",clients.keySet());
            map1.put("userId",userId);
            sendMessageAll(JSON.toJSONString(map1),userId);
        }
        catch (IOException e){
            logger.info(userId+"下线的时候通知所有人发生了错误");
        }
        //logger.info("有连接关闭！ 当前在线人数" + onlineNumber);
        logger.info("有连接关闭！ 当前在线人数" + clients.size());
    }

    /**
     *服务端 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session sessionsession)
    {
        try {
            logger.info("服务端收到客户端的消息：" + message+"===》来自于id为："+session.getId()+"的客户");

            JSONObject jsonObject = JSON.parseObject(message);
            String textMessage = jsonObject.getString("message");
            String fromuserId = jsonObject.getString("userId"); //发信人
            String touserId = jsonObject.getString("to");  //收信人
            //如果不是发给所有，那么就发给某一个人
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",4);
            map1.put("message",textMessage);
            map1.put("fromuserId",fromuserId);
            if("all".equals(touserId)){
                map1.put("touserId","所有人");
                sendMessageAll(JSON.toJSONString(map1),fromuserId);
            }
            else{
                map1.put("touserId",touserId);
                System.out.println("开始推送消息给"+touserId);
                //单独发送
                sendMessageTo(JSON.toJSONString(map1),touserId);
            }
        }
        catch (Exception e){

            e.printStackTrace();
            logger.info("发生了错误了");
        }

    }

    /**
     * 单独发送
     * @param message
     * @param TouserId
     * @throws IOException
     */
    public void sendMessageTo(String message, String TouserId) throws IOException {
        System.out.println("=----/////");
        logger.info(message);
        for (WebSocket item : clients.values()) {
            //    System.out.println("在线人员名单  ："+item.userId.toString());
            if (item.userId.equals(TouserId) ) {
                logger.info("@@@"+message);
                item.session.getAsyncRemote().sendText(message);
                System.out.println("ok");
                break;
            }
        }
    }
    //群发 同步--》异步
    public void sendMessageAll(String message,String FromuserId) throws IOException {
        WebSocket webSocket =new WebSocket();
        for (WebSocket item : clients.values()) {
            //setSendTimeout(1000); //1 second  超时设置
            item.session.getAsyncRemote().sendText(message, new SendHandler() {
                @Override
                public void onResult(SendResult sendResult) {
                 logger.info("------------异步群发");
                 if(sendResult.isOK()){
                     logger.info("异步成功");
                     //发送成功 判断如果是聊天记录则 存入数据库
                     //   pushToDB(item.session.getId(), message, sendResult.isOK());
                 } else {
                     //失败重发 发过三次 策略
                     logger.info("异步失败");

                 }
                }
            });
        }
        System.out.println("---");
    }

    /**
     * 在线人数统计+
     */
    public static synchronized void addOnlineCount() {
        WebSocket.onlineNumber++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineNumber--;
    }




}
