package com.springbootstudy.bbs.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.springbootstudy.bbs.domain.ChatMessageVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.dto.ChatMessageRequest;
import com.springbootstudy.bbs.dto.ChatMessageResponse;
import com.springbootstudy.bbs.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        MemberVO loginUser = null;

        if (sessionAttributes != null && sessionAttributes.get("loginUser") instanceof MemberVO memberVO) {
            loginUser = memberVO;
        }

        Long senderIdx = request.getSenderIdx();
        String senderName = "";

        if (loginUser != null) {
            senderIdx = loginUser.getMemIdx();
            senderName = loginUser.getMemName();
        }

        ChatMessageVO message = new ChatMessageVO();
        message.setChatroomIdx(request.getChatroomIdx());
        message.setSenderIdx(senderIdx);
        message.setMessageContent(request.getMessageContent());
        message.setSentAt(LocalDateTime.now());

        ChatMessageVO savedMessage = chatMessageService.saveWebSocketMessage(message);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .chatroomIdx(savedMessage.getChatroomIdx())
                .senderIdx(savedMessage.getSenderIdx())
                .messageContent(savedMessage.getMessageContent())
                .senderName(senderName)
                .sentAt(savedMessage.getSentAt())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + savedMessage.getChatroomIdx(),
                response
        );
    }
}
