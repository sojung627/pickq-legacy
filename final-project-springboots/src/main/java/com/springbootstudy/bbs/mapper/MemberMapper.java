package com.springbootstudy.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.MemberVO;

@Mapper
public interface MemberMapper {
   
   // 회원가입 - 회원가입 처리용
   int insertMember(
            @Param("memId") String memId,
            @Param("memPwd") String memPwd,
            @Param("memName") String memName,
            @Param("memTel") String memTel,
            @Param("memEmail") String memEmail,
           @Param("memIp") String memIp, 
           @Param("memRoleIdx") Long memRoleIdx,
           @Param("memGradeIdx") int memGradeIdx
    );
   
   Long findDefaultRoleIdx(); 
   
   // 회원 조회 
   int countByMemId(@Param("memId") String memId);


	// 로그인 =============================================
	
	
    // 아이디 존재 여부 체크
    int checkId(String memId);

    // 비밀번호 조회
    String getPassword(String memId);

    // 회원 정보 조회
    MemberVO getMemberVO(String memId);
    
    // 탈퇴
    int deleteMember(String memId);
    
    
    // 회원정보 수정 =============================================

    
    // 회원정보 수정
 	void update(MemberVO vo);
 	MemberVO selectOneFromId(String memId); 
 	String selectGradeNameByMemId(String memId); // grade만 따로 조회
 	
 	// 비밀번호
 	public String selectPwdById(String memId);

 	
 	// 비밀번호 재발급 =============================================
 	
 	// 비밀번호 재발급 시 아이디 + 전화번호 맞는 지 확인
	MemberVO findByIdAndTel(@Param("memId") String memId, @Param("memTel") String memTel);

	// 새비밀번호로 변경
	int updatePassword(@Param("memId") String memId, @Param("newPassword") String newPassword);

}
