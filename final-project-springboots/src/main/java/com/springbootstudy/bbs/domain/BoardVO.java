package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

//게시글
@Data
public class BoardVO {

 private Long    		boardIdx;       // PK
 private Long    		memIdx;         // FK → member.mem_idx
 private String  		boardTitle;     // 제목
 private String  		boardContent;   // 내용
 private String  		boardIp;        // IP
 private String  		boardThumbnail; // 썸네일
 private Long    		boardViewCount; // 조회수
 private int            boardLike; 		// 좋아요 수
 private Integer 		boardTypeIdx;   // FK → board_type.board_type_idx
 private LocalDateTime 	boardRegdate;	// 등록일
 private LocalDateTime 	boardModdate;	// 수정일
 private String  		boardIsDeleted; // 'Y' / 'N'
 private LocalDateTime 	boardDeldate; 	// 삭제일

 // 조회용 추가 필드
 private String         boardTypeName;  // 게시판 타입명 (JOIN)
 private String         boardTypeCode;  // 게시판 타입 코드 (JOIN)
 private String			memNickname;	// 작성자 닉네임 (JOIN)
 private String			memId;			// 작성자 아이디 (JOIN)
 private int			replyCount;		// 댓글 수 (서브쿼리)
 private boolean        hasImage;       // 이미지 포함 여부
}
