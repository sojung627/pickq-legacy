package com.springbootstudy.bbs.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.springbootstudy.bbs.domain.PaymentVO;
import com.springbootstudy.bbs.mapper.PaymentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowRefundService {

    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.secret-key:test_sk_LlDJaYngroay02a6RyKG3ezGdRpX}")
    private String tossSecretKey;

    /**
     * 판매자 결제완료 취소 시 에스크로 환불(결제취소) 처리.
     * 실제 운영에서는 PG 취소 성공 후 DB 상태를 CANCELED로 갱신한다.
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundPaidEscrowByBidIdx(Long bidIdx, String cancelReason) {
        PaymentVO payment = paymentMapper.findPaymentByBidIdx(bidIdx);
        if (payment == null) {
            throw new IllegalStateException("환불 대상 결제 정보를 찾지 못했습니다.");
        }

        if (!"DONE".equals(payment.getPayStatus())) {
            throw new IllegalStateException("결제완료 상태에서만 환불을 진행할 수 있습니다.");
        }

        callTossCancelApi(payment.getPaymentKey(), cancelReason);

        int updated = paymentMapper.cancelEscrowPaymentByBidIdx(bidIdx);
        if (updated == 0) {
            throw new IllegalStateException("결제 환불 상태 반영에 실패했습니다.");
        }
    }

    private void callTossCancelApi(String paymentKey, String cancelReason) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalStateException("환불 처리에 필요한 결제 키가 없습니다.");
        }

        try {
            String basicToken = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + basicToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "cancelReason", (cancelReason == null || cancelReason.isBlank())
                            ? "판매자 요청으로 거래가 취소되었습니다."
                            : cancelReason);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(
                    "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            log.error("토스 환불 API 호출 실패 - paymentKey={}", paymentKey, e);
            throw new IllegalStateException("결제 환불 처리 중 오류가 발생했습니다.");
        }
    }
}
