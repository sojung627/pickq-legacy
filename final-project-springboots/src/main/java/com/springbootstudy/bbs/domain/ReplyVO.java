package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

//댓글
@Data
public class ReplyVO {

 private Long    		replyIdx;        // PK
 private Long    		boardIdx;        // FK → board.board_idx
 private Long    		memIdx;          // FK → member.mem_idx
 private String  		replyContent;    // 댓글 내용
 private String  		replyIp;         // IP
 private int            replyLike; 		 // 좋아요 수
 private LocalDateTime 	replyRegdate;  	 // 등록일
 private LocalDateTime 	replyModdate;  	 // 수정일
 private String  		replyIsDeleted;  // 'Y' / 'N'
 private LocalDateTime 	replyDeldate;  	 // 삭제일
 private Integer 		replyRef;        // 원댓
 private Integer 		replyStep;       // 댓글 순서
 private Integer 		replyDepth;      // 댓글 깊이

 // 조회용 추가 필드
 private String         memNickname;     // 작성자 닉네임 (JOIN)
 private String         memId;           // 작성자 아이디 (JOIN)

}


