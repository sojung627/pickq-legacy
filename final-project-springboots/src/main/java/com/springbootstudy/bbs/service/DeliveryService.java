package com.springbootstudy.bbs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbootstudy.bbs.domain.DeliveryVO;
import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.domain.PaymentVO;
import com.springbootstudy.bbs.mapper.DeliveryMapper;
import com.springbootstudy.bbs.mapper.PaymentMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeliveryService {

  @Autowired
  private DeliveryMapper deliveryMapper;

  @Autowired
  private PaymentMapper paymentMapper;

  @Autowired
  private OrdersService ordersService;

  // 판매자 운송장 입력 + 배송 시작
  @Transactional(rollbackFor = Exception.class)
  public void shipOrder(DeliveryVO deliveryVO) throws Exception {

    // pay_idx 조회 (bid_idx로)
    PaymentVO payment = paymentMapper.findPaymentByBidIdx(deliveryVO.getBidIdx());
    if (payment == null) {
      throw new Exception("결제 정보를 찾을 수 없습니다.");
    }

    // 이미 배송 시작된 경우 체크
    DeliveryVO existing = deliveryMapper.findDeliveryByBidIdx(deliveryVO.getBidIdx());
    if (existing != null) {
      throw new Exception("이미 배송이 시작된 주문입니다.");
    }

    // pay_idx 세팅
    deliveryVO.setPayIdx(payment.getPayIdx());

    // 배송 정보 등록
    deliveryMapper.insertDelivery(deliveryVO);

    // 비즈니스 룰: 마감/낙찰 이후 auctionStatus/bidStatus는 더 이상 변경하지 않는다.
    log.debug("bidIdx={} 배송시작 후 auctionStatus 갱신은 비활성화됨", deliveryVO.getBidIdx());

    OrdersVO order = ordersService.findByBidIdx(deliveryVO.getBidIdx());
    if (order != null) {
      ordersService.markOrderShipped(order.getOrderIdx());
    }
  }

  // 구매자 수령 확인
  @Transactional(rollbackFor = Exception.class)
  public void confirmReceipt(Long bidIdx) throws Exception {

    // 배송 정보 확인
    DeliveryVO delivery = deliveryMapper.findDeliveryByBidIdx(bidIdx);
    if (delivery == null) {
      throw new Exception("배송 정보를 찾을 수 없습니다.");
    }
    if ("DELIVERED".equals(delivery.getDeliveryStatus())) {
      throw new Exception("이미 수령 확인된 주문입니다.");
    }

    // 배송 상태 DELIVERED로 변경
    deliveryMapper.updateDeliveryStatus(bidIdx, "DELIVERED");

    // 결제 confirmed_at 기록
    paymentMapper.updateConfirmedAt(bidIdx);

    // 비즈니스 룰: 마감/낙찰 이후 auctionStatus/bidStatus는 더 이상 변경하지 않는다.
    log.debug("bidIdx={} 수령확인 후 auctionStatus 갱신은 비활성화됨", bidIdx);

    OrdersVO order = ordersService.findByBidIdx(bidIdx);
    if (order != null) {
      ordersService.markOrderConfirmed(order.getOrderIdx());
    }
  }

  // 배송 정보 조회
  public DeliveryVO findDeliveryByBidIdx(Long bidIdx) {
    return deliveryMapper.findDeliveryByBidIdx(bidIdx);
  }

}