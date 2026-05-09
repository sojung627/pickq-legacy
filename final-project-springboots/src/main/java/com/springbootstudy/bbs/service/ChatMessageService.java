package com.springbootstudy.bbs.service;

import com.springbootstudy.bbs.domain.ChatMessageVO;
import com.springbootstudy.bbs.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
	
	@Autowired
    private ChatMessageMapper chatMessageMapper;

    public List<ChatMessageVO> getMessagesByRoom(Long chatroomIdx) {
        return chatMessageMapper.findByChatroom(chatroomIdx);
    }

    public void saveMessage(ChatMessageVO message) {
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }
        message.setIsRead("N");
        chatMessageMapper.insertMessage(message);
    }

    public ChatMessageVO saveWebSocketMessage(ChatMessageVO message) {
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }

        message.setIsRead("N");

        chatMessageMapper.insertMessage(message);
        return message;
    }

    public int markRoomMessagesRead(Long chatroomIdx, Long viewerIdx) {
        return chatMessageMapper.markRoomMessagesRead(chatroomIdx, viewerIdx);
    }

    public int countUnreadForMember(Long memIdx) {
        return chatMessageMapper.countUnreadForMember(memIdx);
    }
}