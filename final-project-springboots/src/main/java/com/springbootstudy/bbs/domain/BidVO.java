package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BidVO {
  private Long bidIdx;
  private Long auctionIdx;
  private Long bidderIdx;
  private Long itemIdx;
  private Long bidPrice;
  private Integer bidQuantity;
  private String bidMessage;
  private Integer bidStatusIdx;
  private LocalDateTime bidRegdate;
  private LocalDateTime bidModdate;
  
}