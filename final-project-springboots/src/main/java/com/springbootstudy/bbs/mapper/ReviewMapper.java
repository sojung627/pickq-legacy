package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; 

import com.springbootstudy.bbs.domain.ReviewVO;
import com.springbootstudy.bbs.dto.ProfileReviewDTO;
import com.springbootstudy.bbs.dto.ProfileReviewDetailDTO;

@Mapper
public interface ReviewMapper {
	
	// 내가 쓴 리뷰 조회
	List<ReviewVO> getMyReviewList(@Param("buyerIdx") Long buyerIdx);
	
	// 내가 받은 리뷰 조회
	List<ReviewVO> getReceivedReviews(@Param("memIdx") Long memIdx);  

	// 프로필 화면용 받은 리뷰 목록
	List<ProfileReviewDTO> getReceivedReviewsForProfile(@Param("memIdx") Long memIdx);
	
	// 내가 받은 리뷰 별점 평균
	Double getAvgRating(@Param("memIdx") Long memIdx);
	
	// 검색 전 기본 목록
	List<ReviewVO> getWritableReviewList(@Param("buyerIdx") Long buyerIdx); 
	
	// 검색 기능
	List<ReviewVO> search(@Param("searchType") String searchType,
            @Param("keyword") String keyword, @Param("buyerIdx") Long buyerIdx);
	
	// 리뷰 작성하기
	void insertReview(ReviewVO vo);
	Long findBidderIdxByBidIdx(Long bidIdx); 
	
	// 리뷰 상세보기
	ReviewVO getReviewDetail(Long reviewIdx);

	// 프로필 화면에서 진입하는 리뷰 상세 조회
	ProfileReviewDetailDTO getProfileReviewDetail(@Param("reviewIdx") Long reviewIdx);
	
	// 리뷰 삭제하기(관리자만)
	//List<ReviewVO> getAllReviewList();    
    // 임시삭제 처리
    void deleteReview(@Param("reviewIdx") Long reviewIdx);
    // 영구삭제 처리
    void hardDeleteReview(@Param("reviewIdx") Long reviewIdx);
    
    // 삭제 관련 (띄우기)
    List<ReviewVO> getActiveReviewList();  
    List<ReviewVO> getDeletedReviewList();

    // 리뷰 삭제 취소
	void cancelDelete(Long reviewIdx);
	
}