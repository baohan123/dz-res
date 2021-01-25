package com.dz.dzim.common;

/**
 * 消息类型枚举
 *
 * @author yanpanyi
 * @date 2019/3/22
 */
public enum MessageTypeEnum {
    SYSTEM,  //系统消息
    USER,   //用户消息
    REVOKE, //撤回消息
    ROBOT,  //机器人及其他
    ONLINE, //上线 提醒
    OFFLINE// 下线
}
