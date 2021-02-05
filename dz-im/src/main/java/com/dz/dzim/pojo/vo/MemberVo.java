package com.dz.dzim.pojo.vo;

import org.springframework.web.socket.WebSocketSession;

/**
 * @author baohan
 * @className 用户信息Vo
 * @description TODO
 * @date 2021/2/5 13:14
 */
public class MemberVo {
    /**
     * 大会场id
     */
    private String bigId;

    /**
     * 用户id
     */
    private Long memberId;

    /**
     * 用户类型
     */
    private String memberType;

    public String getBigId() {
        return bigId;
    }

    public void setBigId(String bigId) {
        this.bigId = bigId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }
}
