package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyPagePaymentVO {

  // 결제 정보
  private Long payIdx;
  private Long bidIdx;
  private Long memIdx;
  private String payMethod;
  private Long payAmount;
  private String payStatus;
  private LocalDateTime payRegdate;
  private LocalDateTime confirmedAt;

  // 구매자 배송지
  private String buyerName;
  private String buyerTel;
  private String buyerAddr;
  private String buyerZipcode;

  // 상품 정보 (JOIN)
  private String itemName;
  private String itemThumbnailImg;

  // 배송 정보 (delivery JOIN)
  private String courierCompany;
  private String trackingNumber;
  private String deliveryStatus;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;
}