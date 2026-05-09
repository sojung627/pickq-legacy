package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrdersVO {

    // PK
    private Long orderIdx;

    // FK
    private Long auctionIdx;   // auction.auction_idx
    private Long bidIdx;       // bid.bid_idx
    private Long buyerIdx;     // member.mem_idx (구매자)
    private Long sellerIdx;    // member.mem_idx (판매자)

    // 거래 금액 (낙찰가 스냅샷)
    private Long orderAmount;

    // 거래 전체 상태 (CREATED, PAID, SHIPPED, CONFIRMED, CANCELED)
    private String orderStatus;

    // 정산 여부 (Y / N)
    private String isSettled;

    // 타임라인
    private LocalDateTime orderRegdate;  // 주문 생성일
    private LocalDateTime paidAt;        // 결제 완료 시각
    private LocalDateTime shippedAt;     // 배송 시작 시각
    private LocalDateTime confirmedAt;   // 구매 확정 시각
    private LocalDateTime refundAt;      // 환불 시각

}
