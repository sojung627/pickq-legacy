package com.springbootstudy.bbs.domain;

import lombok.Data;

//경매 상태 코드 테이블
@Data
public class AuctionStatusVO {

 private Integer auctionStatusIdx;   // PK
 private String  auctionStatusCode;  // 상태 코드 (open/closed/failed/canceled)
 private String  auctionStatusName;  // 한글 상태명 (진행중/마감/유찰/취소 등)

}