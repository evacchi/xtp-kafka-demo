package com.dylibso.examples.viz;

import com.dylibso.examples.kafka.Record;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/viz/{subscriber}")
@ApplicationScoped
public class VizSocket {

    @Inject
    ObjectMapper mapper;
    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("subscriber") String id) {
        sessions.put(id, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("subscriber") String id) {
        sessions.remove(id);
    }

    @OnError
    public void onError(Session session, @PathParam("subscriber") String id, Throwable throwable) {
        sessions.remove(id);
    }

    public void onRecord(Record r) {
        try {
            broadcast(mapper.writeValueAsString(r));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcast(String jsonString) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(jsonString, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

}
