package com.springbootstudy.bbs.domain;

import lombok.Data;

//아이템 카테고리 코드
@Data
public class ItemCategoryVO {

 private Integer itemCategoryIdx;   // PK
 private String  itemCategoryCode;  // 카테고리 코드 (BALL, RACKET 등)
 private String  itemCategoryName;  // 카테고리 이름 (구기 종목 등)

}

