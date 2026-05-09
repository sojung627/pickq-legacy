package com.springbootstudy.bbs.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.springbootstudy.bbs.domain.PaymentVO;
import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.mapper.PaymentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentMapper paymentMapper;
  private final OrdersService ordersService;
  private final RestTemplate restTemplate = new RestTemplate();

  // 토스페이먼츠 테스트 시크릿 키 (내 정보에 맞게 수정 가능)
  private final String SECRET_KEY = "test_sk_LlDJaYngroay02a6RyKG3ezGdRpX" + ":";

  @Override
  public Map<String, Object> confirmPayment(String paymentKey, String orderId, Long amount) throws Exception {
    // 1. 토스 API 호출을 위한 헤더 설정 (Basic Auth)
    String encodedKey = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + encodedKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 2. 요청 바디 구성
    Map<String, Object> params = Map.of(
        "paymentKey", paymentKey,
        "orderId", orderId,
        "amount", amount);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

    // 3. 토스 승인 API 호출 (POST)
    try {
      // ParameterizedTypeReference를 사용하여 응답 타입을 명확히 규정합니다.
      return restTemplate.exchange(
          "https://api.tosspayments.com/v1/payments/confirm",
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<Map<String, Object>>() {
          }).getBody();

    } catch (Exception e) {
      log.error("토스 승인 API 호출 중 에러 발생", e);
      throw new Exception("결제 승인에 실패했습니다.");
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class) // 에러 발생 시 전체 롤백
  public boolean savePaymentAndTrackStatus(PaymentVO paymentVO, Map<String, Object> tossResponse) throws Exception {
    // 1. 금액 검증 (토스 응답 데이터와 VO 데이터 비교)
    // 토스는 숫자를 가끔 Integer로 줄 때가 있으므로 숫자로 안전하게 변환
    Number totalAmountNum = (Number) tossResponse.get("totalAmount");
    Long totalAmount = totalAmountNum.longValue();

    if (!paymentVO.getPayAmount().equals(totalAmount)) {
      log.error("금액 불일치: 요청({}) vs 실제({})", paymentVO.getPayAmount(), totalAmount);
      throw new Exception("결제 금액이 일치하지 않습니다."); // return false보다 예외를 던져야 롤백이 확실함
    }

    // 2. 결제 내역 저장 (READY 상태로 INSERT)
    int insertResult = paymentMapper.insertPayment(paymentVO);

    if (insertResult > 0) {
      // 3. 결제 상태 업데이트 (READY -> DONE) ★필수 추가★
      paymentMapper.updatePaymentStatus(paymentVO.getOrderId(), "DONE");

      // 비즈니스 룰: 마감/낙찰 이후 auctionStatus/bidStatus는 더 이상 변경하지 않는다.
      log.debug("bidIdx={} 결제완료 후 auctionStatus/bidStatus 갱신은 비활성화됨", paymentVO.getBidIdx());

      OrdersVO order = ordersService.findByBidIdx(paymentVO.getBidIdx());
      if (order != null) {
        ordersService.markOrderPaid(order.getOrderIdx());
      }

      return true;
    }
    return false;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void shipOrder(PaymentVO paymentVO) throws Exception {
    // 운송장 정보 저장
    int result = paymentMapper.updateShippingInfo(paymentVO);
    if (result == 0) {
      throw new Exception("배송 처리에 실패했습니다.");
    }

    // 비즈니스 룰: 마감/낙찰 이후 auctionStatus/bidStatus는 더 이상 변경하지 않는다.
    log.debug("bidIdx={} 배송시작 후 auctionStatus 갱신은 비활성화됨", paymentVO.getBidIdx());

    OrdersVO order = ordersService.findByBidIdx(paymentVO.getBidIdx());
    if (order != null) {
      ordersService.markOrderShipped(order.getOrderIdx());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirmReceipt(Long bidIdx) throws Exception {
    // escrow_status DELIVERED, confirmed_at 기록
    paymentMapper.updateEscrowStatus(bidIdx, "DELIVERED");
    paymentMapper.updateConfirmedAt(bidIdx);

    // 비즈니스 룰: 마감/낙찰 이후 auctionStatus/bidStatus는 더 이상 변경하지 않는다.
    log.debug("bidIdx={} 수령확인 후 auctionStatus 갱신은 비활성화됨", bidIdx);

    OrdersVO order = ordersService.findByBidIdx(bidIdx);
    if (order != null) {
      ordersService.markOrderConfirmed(order.getOrderIdx());
    }
  }
}
