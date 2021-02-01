//package com.dz.dzim.common;/**
// * @description: some desc
// * @author: lenovo
// * @email: xxx@xx.com
// * @date: 2021/1/25 19:16
// */
//
//import com.dz.dzim.controller.WebSocketImpl2;
//import com.dz.dzim.pojo.OnlineUser;
//import com.dz.dzim.pojo.User;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author map 工具类
// * @className MapUtils
// * @description TODO
// * @date 2021/1/25 19:16
// */
//public class MapUtils {
//
//
//    /**
//     * ConcurrentHashMap 根据条件排序
//     *
//     * @param maps
//     * @param sortType /true 升序序  false  降序
//     * @return
//     */
//    public static List<Map.Entry<String, Integer>> maoTolistSort(Map<String, Integer> maps, boolean sortType){
//        //排序后的输出
//        final boolean flag = sortType;
//        List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(maps.entrySet());
//        Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
//            @Override
//            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
//                if (obj2.getValue() - obj1.getValue() != 0) {
//                    return obj2.getValue() - obj1.getValue();
//                } else {
//                    //true 升序序 即 abcd 后为 acdd
//                    if (flag) {
//                        return obj1.getKey().compareTo(obj2.getKey());
//                    } else {
//                        //false  降序即为 acdd 后为 abcd
//                        return -(obj1.getKey().compareTo(obj2.getKey()));
//                    }
//                }
//            }
//        });
//        return info;
//    }
//
//    /**
//     * 可用客服排序
//     * @param cmpMap
//     * @param sortType
//     * @return
//     */
//    public static List<Map.Entry<String, Integer>> mapTosortClent(Map<String, OnlineUser> cmpMap, boolean sortType) {
//        //初始化 当前可用客服
//        WebSocketImpl2.usableClentsAll = new ConcurrentHashMap<>();
//        //初始化 在线客服已产生的用户会话
//        WebSocketImpl2.cmpMaps = new ConcurrentHashMap<>();
//        //Map<String, List<String>> cmpMaps = new ConcurrentHashMap<>();
//        Map<String, Integer> maps = new ConcurrentHashMap<>();
//        for (Map.Entry<String, OnlineUser> entry : cmpMap.entrySet()) {
//            Set<String> strings = entry.getValue().getUsers().keySet();
//            if(null != strings && strings.size() >0){
//                //将客服对应用户关系存入缓存
//                WebSocketImpl2.cmpMaps.put(entry.getKey(),strings);
//            }
//            if (entry.getValue().getUsableClents() >0) {
//                maps.put(entry.getKey(), entry.getValue().getUsers().size());
//            }
//        }
//        List<Map.Entry<String, Integer>> list = maoTolistSort(maps, sortType);
//        for (Map.Entry<String, Integer> entry : list) {
//            String key = entry.getKey();
//            OnlineUser onLineKfUser = cmpMap.get(key);
//            Integer maxClents = onLineKfUser.getMaxClents();
//            Integer userNum = onLineKfUser.getUsers().size();
//            WebSocketImpl2.usableClentsAll.put(key, maxClents - userNum);
//        }
//        return maoTolistSort(WebSocketImpl2.usableClentsAll, sortType);
//    }
//
//
//    /**
//     * 移除当前客服中所包含的用户id
//     * @param userId
//     */
//    public static void delKfClientUserId(String userId) {
//        for (Map.Entry<String, OnlineUser> entry : WebSocketImpl2.kfclients.entrySet()) {
//            OnlineUser value = entry.getValue();
//            LinkedHashMap<String, User> users = value.getUsers();
//            if(null != users.get(userId)){
//                users.remove(userId);
//            }
//        }
//    }
//
//}
