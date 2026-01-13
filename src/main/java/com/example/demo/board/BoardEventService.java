package com.example.demo.board;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BoardEventService {

    private static final long SSE_TIMEOUT_MS = 0L;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        return emitter;
    }

    public void broadcastBoardUpdate() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("board").data("updated"));
            } catch (IOException ex) {
                emitters.remove(emitter);
            }
        }
    }
}