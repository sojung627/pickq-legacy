package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DeliveryVO {
  private Long deliveryIdx;
  private Long payIdx;
  private Long bidIdx;
  private String courierCompany;
  private String trackingNumber;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;
  private String deliveryStatus;
}