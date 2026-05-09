package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentVO {
  private Long payIdx; // PK
  private Long bidIdx; // FK (낙찰 정보)
  private Long memIdx; // FK (구매자 정보)

  // 토스페이먼츠 식별 정보
  private String paymentKey; // 토스 결제 고유 키 (기존 impUid 대체)
  private String orderId; // 우리 시스템 주문 번호 (기존 merchantUid 대체)

  private String payMethod; // 결제 수단 (카드, 가상계좌 등)
  private Long payAmount; // 결제 금액
  private String payStatus; // 결제 상태 (DONE, READY, CANCELED 등)

  // 기록용 배송지 스냅샷 (결제 시점 정보 고정)
  private String buyerName;
  private String buyerTel;
  private String buyerAddr;
  private String buyerZipcode;

  private LocalDateTime payRegdate; // 결제 기록 일시
  private LocalDateTime confirmedAt; // 구매 확정 일시
  private LocalDateTime canceledAt; // 결제 취소 일시

}