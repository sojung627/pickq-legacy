package com.springbootstudy.bbs.domain;

import lombok.Data;
import java.time.LocalDateTime;

// 메세지 테이블
@Data
public class ChatMessageVO {
    private Long messageIdx;		// 메시지 pk
    private Long chatroomIdx;		// 대화방 fk
    private Long senderIdx;			// 작성자 fk
    private String messageContent;	// 메시지 내용
    private LocalDateTime sentAt;	// 메시지 작성시간
    private String isRead;			// 읽음 여부 (Y/N)
    private LocalDateTime readAt;	// 읽음 시각
}
