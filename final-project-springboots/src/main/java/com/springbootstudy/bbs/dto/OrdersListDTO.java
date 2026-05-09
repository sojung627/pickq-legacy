package com.springbootstudy.bbs.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrdersListDTO {

    private Long orderIdx;
    private Long auctionIdx;
    private Long bidIdx;
    private Long buyerIdx;
    private Long sellerIdx;
    private Long orderAmount;

    private String orderStatusCode;
    private String orderStatusName;
    private String isSettled;

    private LocalDateTime orderRegdate;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime refundAt;

    private String auctionTitle;
    private String itemName;
    private String buyerMemId;
    private String buyerMemIdMasked;
    private String sellerMemId;
    private String sellerMemIdMasked;
    private String buyerNickname;
    private String sellerNickname;
    private String courierCompany;
    private String trackingNumber;
    private Long reviewIdx;

    private String paymentStatusCode;
    private String paymentStatusName;

    private String shippingStatusCode;
    private String shippingStatusName;

    // BUYER / SELLER / BOTH
    private String userRole;
}
