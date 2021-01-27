package com.dz.dzim.config.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.thymeleaf.util.StringUtils;

import java.util.Map;

/**
 * Websocket—请求拦截
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    public WebSocketInterceptor() {
    }

    /**
     * 握手之前
     *
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @param webSocketHandler
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        /*处理逻辑！！！！*/
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            String url = serverHttpRequest.getURI().toString();
            String projectId = url.substring(url.lastIndexOf("/") + 1);
            if (!StringUtils.isEmpty(projectId)) {
//                LaborProjectConfigModel model = laborProjectConfigMapper.selectById(Integer.parseInt(projectId));
//                if (model != null) {
                return true;
            } else {
//                    StaticLog.error("非法请求 : projectId = {}", projectId);
            }
//            } else {
//                StaticLog.error("projectId不能为空");
//            }
        }
        return false;
        //  return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
