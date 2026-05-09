package com.springbootstudy.bbs.domain;

import lombok.Data;

//권한 코드 테이블
@Data
public class RoleVO {

 private Long   roleIdx;    // PK
 private String roleName;   // 권한 등급 명칭
 
}
