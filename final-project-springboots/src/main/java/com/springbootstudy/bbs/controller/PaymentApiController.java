package com.springbootstudy.bbs.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbootstudy.bbs.domain.DeliveryVO;
import com.springbootstudy.bbs.domain.MemberAddrVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.PaymentVO;
import com.springbootstudy.bbs.service.DeliveryService;
import com.springbootstudy.bbs.service.MemberAddrService;
import com.springbootstudy.bbs.service.PaymentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final MemberAddrService memberAddrService;

    // 결제 승인
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> requestData,
            HttpSession session) {
        try {
        	MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
	        if (loginUser == null || loginUser.getMemIdx() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
	        Long memIdx = loginUser.getMemIdx();

            String paymentKey = (String) requestData.get("paymentKey");
            String orderId = (String) requestData.get("orderId");
            Long amount = Long.valueOf(String.valueOf(requestData.get("amount")));
            Long bidIdx = Long.valueOf(String.valueOf(requestData.get("bidIdx")));

            Map<String, Object> tossResponse = paymentService.confirmPayment(paymentKey, orderId, amount);

            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPaymentKey(paymentKey);
            paymentVO.setOrderId(orderId);
            paymentVO.setPayAmount(amount);
            paymentVO.setBidIdx(bidIdx);
            paymentVO.setMemIdx(memIdx);

            String method = (String) tossResponse.get("method");
            paymentVO.setPayMethod(method != null ? method : "UNKNOWN");

            paymentVO.setBuyerName((String) requestData.getOrDefault("buyerName", "구매자"));
            paymentVO.setBuyerTel((String) requestData.getOrDefault("buyerTel", "010-0000-0000"));
            paymentVO.setBuyerAddr((String) requestData.getOrDefault("buyerAddr", "정보 없음"));
            paymentVO.setBuyerZipcode((String) requestData.getOrDefault("buyerZipcode", "00000"));

            boolean isSuccess = paymentService.savePaymentAndTrackStatus(paymentVO, tossResponse);

            if (isSuccess) {
                return ResponseEntity.ok(Map.of("message", "success", "orderId", orderId));
            } else {
                return ResponseEntity.badRequest().body("결제 검증 및 DB 기록 실패");
            }

        } catch (Exception e) {
            log.error("결제 처리 중 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 판매자 운송장 입력
    @PostMapping("/ship")
    public ResponseEntity<?> shipOrder(@RequestBody Map<String, Object> requestData,
            HttpSession session) {
        try {
            MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            DeliveryVO deliveryVO = new DeliveryVO();
            deliveryVO.setBidIdx(Long.valueOf(String.valueOf(requestData.get("bidIdx"))));
            deliveryVO.setCourierCompany((String) requestData.get("courierCompany"));
            deliveryVO.setTrackingNumber((String) requestData.get("trackingNumber"));

            deliveryService.shipOrder(deliveryVO);
            return ResponseEntity.ok(Map.of("message", "success"));

        } catch (Exception e) {
            log.error("배송 처리 중 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 구매자 수령 확인
    @PostMapping("/confirm-receipt")
    public ResponseEntity<?> confirmReceipt(@RequestBody Map<String, Object> requestData,
            HttpSession session) {
        try {
            MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            Long bidIdx = Long.valueOf(String.valueOf(requestData.get("bidIdx")));
            deliveryService.confirmReceipt(bidIdx);
            return ResponseEntity.ok(Map.of("message", "success"));

        } catch (Exception e) {
            log.error("수령 확인 중 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 세션 정보 반환 API
    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        MemberAddrVO addr = memberAddrService.getPrimaryAddr(loginUser.getMemIdx());

        Map<String, Object> info = new HashMap<>();
        info.put("memName", loginUser.getMemName());
        info.put("memTel", loginUser.getMemTel() != null ? loginUser.getMemTel() : "010-0000-0000");
        info.put("buyerAddr", addr != null ? addr.getMemAddr() + " " + addr.getMemAddrDetail() : "주소없음");
        info.put("buyerZipcode", addr != null ? addr.getMemZipcode() : "00000");

        return ResponseEntity.ok(info);
    }

}