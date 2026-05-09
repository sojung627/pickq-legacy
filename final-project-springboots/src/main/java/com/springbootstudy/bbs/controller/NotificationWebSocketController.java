package com.springbootstudy.bbs.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.NotificationVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notifications/ping")
    public void ping(SimpMessageHeaderAccessor headerAccessor) {
        MemberVO loginUser = null;

        if (headerAccessor.getSessionAttributes() != null
                && headerAccessor.getSessionAttributes().get("loginUser") instanceof MemberVO memberVO) {
            loginUser = memberVO;
        }

        if (loginUser == null || loginUser.getMemIdx() == null) {
            return;
        }

        NotificationVO ack = new NotificationVO();
        ack.setReceiverIdx(loginUser.getMemIdx());
        ack.setNotificationType("SYSTEM_WS_CONNECTED");
        ack.setNotificationTitle("알림 채널 연결 완료");
        ack.setNotificationMessage("실시간 알림 수신이 활성화되었습니다.");
        ack.setCreatedAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/notifications/" + loginUser.getMemIdx(), ack);
    }
}
