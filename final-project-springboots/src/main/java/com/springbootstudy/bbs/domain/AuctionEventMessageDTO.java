package com.springbootstudy.bbs.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuctionEventMessageDTO {
    private String type;      // "BID_CREATED", "AUCTION_WON" 등
    private Long auctionId;
    private Long bidId;
    private Long memberId;    // 알림의 주 대상 (선택)
    private Long price;       // 입찰가 (선택)
}
