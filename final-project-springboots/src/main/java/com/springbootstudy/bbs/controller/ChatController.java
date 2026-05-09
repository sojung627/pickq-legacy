package com.springbootstudy.bbs.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springbootstudy.bbs.domain.ChatMessageVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.service.ChatMessageService;
import com.springbootstudy.bbs.service.ChatRoomService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private HttpSession session;

    // 채팅방 열기
    @GetMapping("/chats/{chatroomIdx}")
    public String chatRoom(@PathVariable("chatroomIdx") Long chatroomIdx,
            Model model) {

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser != null) {
            chatMessageService.markRoomMessagesRead(chatroomIdx, loginUser.getMemIdx());
        }

        List<ChatMessageVO> messageList = chatMessageService.getMessagesByRoom(chatroomIdx);

        model.addAttribute("chatroomIdx", chatroomIdx);
        model.addAttribute("messageList", messageList);

        // templates/views/chat/chatRoom.html 반환
        return "views/chat/chatRoom";
    }

    // 메시지 전송하기
    @PostMapping("/chats/{chatroomIdx}/messages")
    public String sendMessage(@PathVariable("chatroomIdx") Long chatroomIdx,
            @RequestParam("messageContent") String messageContent) {

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인 안 되어 있으면 리다이렉트
            return "redirect:/members/login";
        }

        Long senderIdx = loginUser.getMemIdx();

        ChatMessageVO message = new ChatMessageVO();
        message.setChatroomIdx(chatroomIdx);
        message.setSenderIdx(senderIdx);
        message.setMessageContent(messageContent);

        chatMessageService.saveMessage(message);

        // 전송 후 다시 해당 채팅방으로
        return "redirect:/chats/" + chatroomIdx;
    }

    @GetMapping("/api/chats/unread-count")
    @ResponseBody
    public ResponseEntity<?> unreadCount() {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "NOT_LOGIN"));
        }

        int unreadCount = chatMessageService.countUnreadForMember(loginUser.getMemIdx());
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @GetMapping("/api/chats/rooms")
    @ResponseBody
    public ResponseEntity<?> chatRooms() {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "NOT_LOGIN"));
        }

        List<Map<String, Object>> rooms = chatRoomService.getRoomSummariesByMember(loginUser.getMemIdx());
        return ResponseEntity.ok(Map.of("rooms", rooms));
    }

    @GetMapping("/api/chats/rooms/{chatroomIdx}/messages")
    @ResponseBody
    public ResponseEntity<?> roomMessages(@PathVariable("chatroomIdx") Long chatroomIdx) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "NOT_LOGIN"));
        }

        chatMessageService.markRoomMessagesRead(chatroomIdx, loginUser.getMemIdx());
        List<ChatMessageVO> messages = chatMessageService.getMessagesByRoom(chatroomIdx);
        return ResponseEntity.ok(Map.of("messages", messages));
    }

    @PostMapping("/api/chats/rooms/{chatroomIdx}/read")
    @ResponseBody
    public ResponseEntity<?> markRoomRead(@PathVariable("chatroomIdx") Long chatroomIdx) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "NOT_LOGIN"));
        }

        chatMessageService.markRoomMessagesRead(chatroomIdx, loginUser.getMemIdx());
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PostMapping("/api/chats/rooms/{chatroomIdx}/messages")
    @ResponseBody
    public ResponseEntity<?> sendMessageApi(@PathVariable("chatroomIdx") Long chatroomIdx,
            @RequestBody Map<String, String> requestBody) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "NOT_LOGIN"));
        }

        String content = requestBody.get("messageContent");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "EMPTY_MESSAGE"));
        }

        ChatMessageVO message = new ChatMessageVO();
        message.setChatroomIdx(chatroomIdx);
        message.setSenderIdx(loginUser.getMemIdx());
        message.setMessageContent(content.trim());

        ChatMessageVO saved = chatMessageService.saveWebSocketMessage(message);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageIdx", saved.getMessageIdx());
        payload.put("chatroomIdx", saved.getChatroomIdx());
        payload.put("senderIdx", saved.getSenderIdx());
        payload.put("messageContent", saved.getMessageContent());
        payload.put("sentAt", saved.getSentAt());

        return ResponseEntity.ok(payload);
    }
}