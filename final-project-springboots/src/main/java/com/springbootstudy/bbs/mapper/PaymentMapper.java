package com.springbootstudy.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.PaymentVO;

@Mapper
public interface PaymentMapper {

  // 1. 결제 정보 최초 저장 (READY 상태로 저장)
  int insertPayment(PaymentVO payment);

  // 2. 결제 최종 승인 시 상태 변경 (READY -> DONE) - 추가됨
  // orderId를 기준으로 해당 결제 건을 찾아 상태를 변경합니다.
  int updatePaymentStatus(@Param("orderId") String orderId, @Param("status") String status);

  int updateShippingInfo(PaymentVO paymentVO);

  int updateConfirmedAt(@Param("bidIdx") Long bidIdx);

  int updateEscrowStatus(@Param("bidIdx") Long bidIdx, @Param("escrowStatus") String escrowStatus);

  int cancelEscrowPaymentByBidIdx(@Param("bidIdx") Long bidIdx);

  // 중복 결제 체크 및 결제 정보 조회
  PaymentVO findPaymentByBidIdx(@Param("bidIdx") Long bidIdx);
}