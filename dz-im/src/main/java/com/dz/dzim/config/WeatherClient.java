package com.dz.dzim.config;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

/**
 * 继承 继承java.websocket.Endpoint类
 */
public class WeatherClient extends Endpoint {
    private Session session;
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        try {
            //sends back the session ID to the peer
            this.session.getBasicRemote().sendText("Session ID: " + this.session.getId());
      } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    }

