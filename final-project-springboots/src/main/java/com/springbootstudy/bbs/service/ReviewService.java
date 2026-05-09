package com.springbootstudy.bbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.ReviewVO;
import com.springbootstudy.bbs.dto.ProfileReviewDTO;
import com.springbootstudy.bbs.dto.ProfileReviewDetailDTO;
import com.springbootstudy.bbs.mapper.ReviewMapper;

@Service
public class ReviewService {

	
	@Autowired
    private ReviewMapper reviewMapper;
	
	// 내가 쓴 리뷰 조회
	public List<ReviewVO> getMyReviewList(Long buyerIdx) {
	    return reviewMapper.getMyReviewList(buyerIdx); 
	}
	
	// 내가 받은 리뷰 조회
	public List<ReviewVO> getReceivedReviews(Long memIdx) {
		return reviewMapper.getReceivedReviews(memIdx);   
	}

	// 프로필 화면용 받은 리뷰 목록
	public List<ProfileReviewDTO> getReceivedReviewsForProfile(Long memIdx) {
		return reviewMapper.getReceivedReviewsForProfile(memIdx);
	}
	
	// 내가 받은 리뷰 별점 평균
	public Double getAvgRating(Long memIdx) {
	    return reviewMapper.getAvgRating(memIdx);
	}
	
	// 검색 전 기본 목록
	public List<ReviewVO> getWritableReviewList(Long buyerIdx) {
		return reviewMapper.getWritableReviewList(buyerIdx);
	}

	// 검색 기능
	public List<ReviewVO> search(String searchType, String keyword, Long buyerIdx) {
	    return reviewMapper.search(searchType, keyword, buyerIdx);
	}

	// 리뷰 작성하기
	public void insertReview(ReviewVO vo) {
	    reviewMapper.insertReview(vo);
	}
	
	// 🔥 알림 수신자 검증용 DB 조회 메서드
	public Long findBidderIdxByBidIdx(Long bidIdx) {
	    return reviewMapper.findBidderIdxByBidIdx(bidIdx);
	}
	
	// 리뷰 상세보기
	public ReviewVO getReviewDetail(Long reviewIdx) {
	    return reviewMapper.getReviewDetail(reviewIdx);
	}

	// 프로필 화면에서 진입하는 리뷰 상세보기
	public ProfileReviewDetailDTO getProfileReviewDetail(Long reviewIdx) {
		return reviewMapper.getProfileReviewDetail(reviewIdx);
	}
	
	// 리뷰 임시삭제하기
    public void deleteReview(Long reviewIdx) {
        reviewMapper.deleteReview(reviewIdx);
    }
    // 리뷰 삭제하기
    public List<ReviewVO> getActiveReviewList() {
        return reviewMapper.getActiveReviewList();
    }
    // 리뷰 임시삭제 조회하기
    public List<ReviewVO> getDeletedReviewList() {
        return reviewMapper.getDeletedReviewList();
    }
    
    // 리뷰 영구삭제하기
    public void hardDeleteReview(Long reviewIdx) {
        reviewMapper.hardDeleteReview(reviewIdx);
    }

    // 리뷰 삭제 취소
	public void cancelDelete(Long reviewIdx) {
		reviewMapper.cancelDelete(reviewIdx);
	}
	
}
