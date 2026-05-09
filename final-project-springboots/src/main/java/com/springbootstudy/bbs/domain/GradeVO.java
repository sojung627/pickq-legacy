package com.springbootstudy.bbs.domain;

import lombok.Data;

// 회원 등급 코드 테이블
@Data
public class GradeVO {

    private Integer gradeIdx;     // PK
    private String  gradeName;    // 등급명 (basic / silver / gold / vip)
    private Integer gradeCredit;  // 등급 기준 크레딧 (예: normal 0, bronze 500, silver 2000, gold 5000, vip 10000)

}
