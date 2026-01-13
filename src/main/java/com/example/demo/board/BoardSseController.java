package com.example.demo.board;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/sse")
@PreAuthorize("isAuthenticated()")
public class BoardSseController {

    private final BoardEventService boardEventService;

    public BoardSseController(BoardEventService boardEventService) {
        this.boardEventService = boardEventService;
    }

    @GetMapping("/board")
    public SseEmitter boardUpdates() {
        return boardEventService.subscribe();
    }
}