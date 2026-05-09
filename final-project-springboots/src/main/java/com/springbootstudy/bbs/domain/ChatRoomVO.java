package com.springbootstudy.bbs.domain;

import lombok.Data;
import java.time.LocalDateTime;

// 대화방 테이블
@Data
public class ChatRoomVO {
    private Long chatroomIdx;			// 대화방 pk
    private Long auctionIdx;			// 관련 옥션 fk 
    private Long buyerIdx;				// 구매자 fk
    private Long bidderIdx;				// 판매자 fk
    private LocalDateTime createdAt;	// 대화방 생성시간
}