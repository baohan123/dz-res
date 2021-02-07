package com.dz.dzim.common;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.socket.TextMessage;

/**
 * @author baohan
 * @className Result websocket
 * @description TODO
 * @date 2021/1/22 14:10
 */
public class ResultWebSocket<T>  {

    private Integer returnType;

    private T data;


    public static TextMessage txtMsg(Integer returnType, Object msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", msg);
        jsonObject.put("contentType", returnType);
        return new TextMessage(JSONObject.toJSONString(jsonObject));
    }

    public Integer getReturnType() {
        return returnType;
    }

    public void setReturnType(Integer returnType) {
        this.returnType = returnType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
