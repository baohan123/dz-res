//package com.dz.dzim.mq;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import javax.websocket.*;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.*;
//
//@ServerEndpoint(value = "/websocket")
//@Component
//public class MqSocket {
//    private static Logger logger = LoggerFactory.getLogger(MqSocket.class);
//    public static String msg;
//
//    //静态变量 用于记录当前在线连接数 应该把它设计成线程安全
//    private static int onlineNumber=0;
//
//    //定义交换机的名字
//    public static final String  EXCHANGE_NAME = "boot_topic_exchange";
//    //定义队列的名字
//    public static final String QUEUE_NAME = "boot_queue";
//
//
//
//
//    /**与某个客户端的链接会话,需要通过它来给客户端发送数据*/
//
//    private Session session;
//    private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
//    //线程相关
////    @PostConstruct       //启动后加载
////    public void init() {
////        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
////        executorService.scheduleAtFixedRate(new Runnable() {
////
////            int i = 0;
////
////            @Override
////            public void run() {
////                amqpTemplate.convertAndSend(RabbitFanout.EXCHANGENAME, "",("msg num : " + i).getBytes());
////                i++;
////            }
////        }, 50, 1, TimeUnit.SECONDS);
////    }
//
//   // @RabbitHandler//如果有消息过来 需要消费的时候才会调用该方法
////    @OnMessage
////    public void receiver(String body) throws IOException {
////        logger.info("<=============监听到task_REPqueued队列消息============>"+body);
////        msg=body;
////        if("成功".equals(msg)){
////            sendMessage(msg);
////        }else{
////            sendMessage(msg);
////        }
////    }
////    public void receiveUserMessage(@Payload MsgData msgData, @Headers Map<String,Object> headers, Channel channel) throws IOException {
////        //sendMessage(message.toString());
////        System.out.println("msgData对象"+msgData);
////        String s = msgData.toString();
////        onMessage("oo");//调用消息方法将数据船体给他
////
////        Long deliveryTag= (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
////        //手动接受并告诉rabbitmq消息已经接受了  deliverTag记录接受消息 false不批量接受
////        channel.basicAck(deliveryTag,false);
////        System.out.println();
////
////        /**
////         * basicReject()
////         * 参数1: 消息标签
////         * 参数2: true 将消息从新放入队列  false 接受到并将消息抛弃
////         *
////         *
////         try {
////         channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
////         System.out.println(message);
////         } catch (IOException e) {
////         e.printStackTrace();
////         }
////         */
////
////    }
//
//
//
//
//
//    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
//    private static CopyOnWriteArraySet<MqSocket> clentss = new CopyOnWriteArraySet<MqSocket>();
//
//    private static Map<String, MqSocket> clients = new ConcurrentHashMap<String, MqSocket>();
//
//
//    /**
//     * 连接建立成功调用的方法
//     *
//     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
//     */
//    @OnOpen
//    public void onOpen(Session session) {
//        // public void onOpen(Session session,@PathParam("userId") String userId) {
//        //   System.out.println("--uersid:"+userId);
//        this.session = session;
//        sessions.add(session);
//        clentss.add(this);     //加入set中
//        addOnlineCount();           //在线数加1
//        logger.info("有新连接加入！当前在线人数为" + getOnlineCount());
//
//
//    }
//
//    /**
//     * 连接关闭调用的方法
//     */
//    @OnClose
//    public void onClose() {
//        clentss.remove(this);  //从set中删除
//        sessions.remove(session);
//        subOnlineCount();           //在线数减1
//        logger.info("有一连接关闭！当前在线人数为" + getOnlineCount());
//    }
//
//    /**
//     * 收到客户端消息后调用的方法
//     *
//     * @param message 客户端发送过来的消息
//     * @param session 可选的参数
//     */
//    @OnMessage
//    public void onMessage(String message, Session session) {
//        logger.info("来自客户端的消息:" + message);
//        //群发消息
//        for (MqSocket item : clentss) {
//            try {
//                item.sendMessage(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//                continue;
//            }
//        }
//    }
//
//    /**
//     * 发生错误时调用
//     *
//     * @param session
//     * @param error
//     */
//    @OnError
//    public void onError(Session session, Throwable error) {
//        logger.error("发生错误");
//        error.printStackTrace();
//    }
//
//    /**
//     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
//     *
//     * @param message
//     * @throws IOException
//     */
//    public void sendMessage(String message) throws IOException {
//        //阻塞式的(同步的)
//        if (sessions.size() != 0) {
//            for (Session s : sessions) {
//                if (s != null) {
//                    s.getAsyncRemote().sendText(message);
//                }
//            }
//        }
//        //非阻塞式的（异步的）
//        logger.info("[x] 发送消息"+message);
//    }
//
//    public static synchronized int getOnlineCount() {
//        return onlineNumber;
//    }
//
//
//    /**
//     * 单独发送
//     * @param message
//     * @param TouserId
//     * @throws IOException
//     */
////    public void sendMessageTo(String message, String TouserId) throws IOException {
////        for (MqSocket item : clients.values()) {
////            //    System.out.println("在线人员名单  ："+item.userId.toString());
////            if (item.userId.equals(TouserId) ) {
////                item.session.getAsyncRemote().sendText(message);
////                System.out.println("ok");
////                break;
////            }
////        }
////
////        if (sessions.size() != 0) {
////            for (Session s : sessions) {
////              if()
////                if (s != null) {
////                    s.getAsyncRemote().sendText(message);
////                }
////            }
////        }
////    }
//
//
//    /**
//     * 在线人数统计+
//     */
//    public static synchronized void addOnlineCount() {
//        MqSocket.onlineNumber++;
//    }
//
//    public static synchronized void subOnlineCount() {
//        MqSocket.onlineNumber--;
//    }
//
//
//}
