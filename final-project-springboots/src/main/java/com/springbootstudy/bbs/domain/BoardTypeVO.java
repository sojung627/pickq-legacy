package com.springbootstudy.bbs.domain;

import lombok.Data;

//게시판 타입 코드 테이블
@Data
public class BoardTypeVO {

 private Integer boardTypeIdx;     // PK
 private String  boardTypeCode;    // 게시판 코드 (GOLF_BOARD 등)
 private String  boardTypeName;    // 게시판 이름
 private String  boardCanComment;  // 'Y' / 'N'
 private Integer boardMinRole;     // 최소 권한 (role_idx)

}

