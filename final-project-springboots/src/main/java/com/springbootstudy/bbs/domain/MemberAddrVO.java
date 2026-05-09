package com.springbootstudy.bbs.domain;

import lombok.Data;

//회원 배송지
@Data
public class MemberAddrVO {

 private Long   addrIdx;        // PK
 private Long   memIdx;         // FK → member.mem_idx
 private String memZipcode;     // 우편번호
 private String memAddr;        // 주소
 private String memAddrDetail;  // 상세 주소
 private String isPrimary;      // 'Y' / 'N' 대표 주소 여부

}

