package com.springbootstudy.bbs.domain;

import lombok.Data;

//입찰 상태 코드 테이블
@Data
public class BidStatusVO {

 private Integer bidStatusIdx;   // PK
 private String  bidStatusCode;  // 상태 코드 (normal/won/lost/canceled)
 private String  bidStatusName;  // 한글 상태명 (일반/낙찰 등)

}
