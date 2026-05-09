package com.springbootstudy.bbs.domain;

import lombok.Data;

//회원 프로필
@Data
public class MemberProfileVO {

 private Long   memIdx;       // PK & FK → member.mem_idx
 private String memNickname;  // 닉네임
 private String memIntro;     // 자기소개
 private String memImg;       // 프로필 이미지

}

