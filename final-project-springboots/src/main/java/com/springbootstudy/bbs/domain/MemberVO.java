package com.springbootstudy.bbs.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

//회원 기본 정보
@Data
public class MemberVO {

 private Long    		memIdx;        	// PK
 private String  		memId;         	// 로그인 ID
 private String  		memPwd;        	// 비밀번호 해시
 private String  		memName;      	// 성명
 private String  		memTel;       	// 전화번호
 private String  		memEmail;      	// 이메일
 private String  		memIp;         	// IP 주소
 private Integer 		memRoleIdx;    	// FK → role.role_idx (권한 등급) 
 private Integer 		memGradeIdx;   	// FK → grade.grade_idx (신용도 등급)
 private Integer 		memCredit;      // 신용 크레딧 점수 (기본 50, 0~10000 등)
 private Integer 		memPenalty;		// 패널티 점수 (일단 0으로 시작)
 private LocalDate     	memBday;     	// 생일
 private LocalDateTime 	memRegdate;  	// 가입일
 private String  		memIsDeleted;   // 'Y' / 'N' (삭제 여부)
 private LocalDateTime 	memDeldate;  	// 탈퇴일 
 private String 		memLoginType;   // 로그인 타입(로그인 API용)

}


