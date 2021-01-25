package com.dz.dzim.config;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * 通过Configurator用于获取 httpsession
 * ，通过httpsession可获取service
 * @author baoahn
 */
public class GetHttpSessionConfigurator extends Configurator{
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if(httpSession != null){
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        }
    }

}
