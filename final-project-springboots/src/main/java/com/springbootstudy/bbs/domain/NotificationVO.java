package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationVO {

    private Long notificationIdx;       // PK

    private Long receiverIdx;           // 수신자 memIdx
    private Long senderIdx;             // 발신자 memIdx (null 가능)

    private Long auctionIdx;            // 관련 경매
    private Long bidIdx;                // 관련 입찰
    private Long boardIdx;              // 관련 게시글
    private Long replyIdx;              // 관련 댓글

    private String notificationType;    // AUCTION_..., TRADE_..., BOARD_...
    private String notificationTitle;   // 알림 제목
    private String notificationMessage; // 알림 내용
    private String targetUrl;           // 이동 URL (현재 전부 /mypage/~ 로 보낼것임)

    private String isRead;              // 'Y' / 'N'
    private LocalDateTime createdAt;    // 생성일
    private LocalDateTime readAt;       // 읽은 시각
}