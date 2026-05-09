package com.springbootstudy.bbs.service;

import java.util.Map;

import com.springbootstudy.bbs.domain.PaymentVO;

public interface PaymentService {

  /**
   * 1. 토스페이먼츠 결제 최종 승인 (Confirm API 호출)
   * 클라이언트에서 받은 paymentKey, orderId, amount를 토스 서버에 전송하여 결제를 확정합니다.
   */
  Map<String, Object> confirmPayment(String paymentKey, String orderId, Long amount) throws Exception;

  /**
   * 2. 결제 완료 데이터 검증 및 DB 저장 + 상태 업데이트 (트랜잭션 처리)
   * 승인된 응답 객체와 우리쪽 VO를 조합하여 결제 정보를 기록하고 상태를 변경합니다.
   */
  boolean savePaymentAndTrackStatus(PaymentVO paymentVO, Map<String, Object> tossResponse) throws Exception;

  void shipOrder(PaymentVO paymentVO) throws Exception;

  void confirmReceipt(Long bidIdx) throws Exception;
}