package com.springbootstudy.bbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.BoardTypeVO;
import com.springbootstudy.bbs.domain.BoardVO;
import com.springbootstudy.bbs.domain.ReplyVO;
import com.springbootstudy.bbs.mapper.BoardMapper;

import jakarta.servlet.http.HttpSession;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private NotificationService notificationService;

    // ── 게시판 타입 ──────────────────────────────────────────
    public List<BoardTypeVO> getBoardTypes() {
        return boardMapper.findAllBoardTypes();
    }

    public BoardTypeVO getBoardTypeByCode(String typeCode) {
        return boardMapper.findBoardTypeByCode(typeCode);
    }

    // ── 게시글 목록 ──────────────────────────────────────────
    public List<BoardVO> getBoards(String typeCode, String keyword) {
        return boardMapper.findBoards(typeCode, keyword, null);
    }

    public List<BoardVO> getBoards(String typeCode, String keyword, String searchType) {
        return boardMapper.findBoards(typeCode, keyword, searchType);
    }

    // ── 게시글 목록 (페이징) ──────────────────────────────────
    public List<BoardVO> getBoardsPaged(String typeCode, String keyword, String searchType, int page) {
        int limit  = 10;
        int offset = (page - 1) * limit;
        return boardMapper.findBoardsPaged(typeCode, keyword, searchType, offset, limit);
    }

    // ── 게시글 전체 수 ────────────────────────────────────────
    public int countBoards(String typeCode, String keyword, String searchType) {
        return boardMapper.countBoards(typeCode, keyword, searchType);
    }

    // ── 게시글 좋아요 ─────────────────────────────────────────
    public void likeBoardIfNotYet(Long boardIdx, HttpSession session) {
        String key = "boardLike_" + boardIdx;
        if (session.getAttribute(key) == null) {
            boardMapper.increaseBoardLike(boardIdx);
            session.setAttribute(key, true);
        }
    }

    // ── 댓글 좋아요 ───────────────────────────────────────────
    public void likeReplyIfNotYet(Long replyIdx, HttpSession session) {
        String key = "replyLike_" + replyIdx;
        if (session.getAttribute(key) == null) {
            boardMapper.increaseReplyLike(replyIdx);
            session.setAttribute(key, true);
        }
    }

    // ── 게시글 상세 (조회수 포함) ─────────────────────────────
    public BoardVO getBoardDetail(Long boardIdx, HttpSession session) {
        String key = "boardView_" + boardIdx;
        if (session.getAttribute(key) == null) {
            boardMapper.increaseViewCount(boardIdx);
            session.setAttribute(key, true);
        }
        return boardMapper.findBoardById(boardIdx);
    }

    // ── 게시글 등록 ──────────────────────────────────────────
    public int writeBoard(BoardVO board) {
        return boardMapper.insertBoard(board);
    }

    // ── 게시글 수정 ──────────────────────────────────────────
    public int editBoard(BoardVO board) {
        return boardMapper.updateBoard(board);
    }

    // ── 게시글 삭제 ──────────────────────────────────────────
    public int removeBoard(Long boardIdx) {
        return boardMapper.deleteBoard(boardIdx);
    }

    // ── 댓글 목록 ────────────────────────────────────────────
    public List<ReplyVO> getReplies(Long boardIdx) {
        return boardMapper.findRepliesByBoard(boardIdx);
    }

    // ── 댓글 목록 (페이징) ────────────────────────────────────
    public List<ReplyVO> getRepliesPaged(Long boardIdx, int page, String sortType) {
        int limit  = 20;
        int offset = (page - 1) * limit;
        return boardMapper.findRepliesByBoardPaged(boardIdx, offset, limit, sortType);
    }

    // ── 원댓글 그룹 수 (페이징 기준) ──────────────────────────
    public int countRootReplies(Long boardIdx) {
        return boardMapper.countRootRepliesByBoard(boardIdx);
    }

    // ── 댓글 등록 ────────────────────────────────────────────
    // parentReplyIdx == null 이면 원댓, 있으면 대댓
    public int writeReply(ReplyVO reply, Long parentReplyIdx) {
        if (parentReplyIdx == null) {
            reply.setReplyRef(0);
            reply.setReplyStep(0);
            reply.setReplyDepth(0);
            boardMapper.insertReply(reply);
            ReplyVO update = new ReplyVO();
            update.setReplyIdx(reply.getReplyIdx());
            update.setReplyRef(reply.getReplyIdx().intValue());
            update.setReplyStep(0);
            update.setReplyDepth(0);
            update.setBoardIdx(reply.getBoardIdx());
            update.setMemIdx(reply.getMemIdx());
            update.setReplyContent(reply.getReplyContent());
            update.setReplyIp(reply.getReplyIp());
            boardMapper.updateReplyRef(update);
			notifyBoardWriterOnNewReply(reply);
            return 1;
        } else {
            ReplyVO parent = boardMapper.findReplyById(parentReplyIdx);
            if (parent == null) return -2; // 부모 댓글 없음

            Integer replyRef = parent.getReplyRef();
            Integer replyStep = parent.getReplyStep();
            Integer replyDepth = parent.getReplyDepth();

            // replyRef가 null이면 자기 자신의 idx를 ref로 사용
            int ref   = (replyRef != null) ? replyRef : parent.getReplyIdx().intValue();
            int step  = (replyStep != null) ? replyStep : 0;
            int depth = (replyDepth != null) ? replyDepth : 0;

            // 대댓글 depth 3 제한 (대대대댓글까지만 허용)
            if (depth >= 3) {
                return -1; // 제한 초과
            }

            boardMapper.shiftReplyStep(reply.getBoardIdx(), ref, step);
            reply.setReplyRef(ref);
            reply.setReplyStep(step + 1);
            reply.setReplyDepth(depth + 1);
            int inserted = boardMapper.insertReply(reply);
            if (inserted > 0) {
                notifyBoardWriterOnNewReply(reply);
            }
            return inserted;
        }
    }

    private void notifyBoardWriterOnNewReply(ReplyVO reply) {
        BoardVO board = boardMapper.findBoardById(reply.getBoardIdx());
        if (board == null) {
            return;
        }
        notificationService.notifyNewReplyToBoardWriter(board, reply);
    }

    // ── 댓글 수정 ────────────────────────────────────────────
    public int editReply(Long replyIdx, String replyContent) {
        ReplyVO reply = new ReplyVO();
        reply.setReplyIdx(replyIdx);
        reply.setReplyContent(replyContent);
        return boardMapper.updateReply(reply);
    }

    // ── 댓글 삭제 ────────────────────────────────────────────
    public int removeReply(Long replyIdx) {
        return boardMapper.deleteReply(replyIdx);
    }

    // ── 댓글 수 ──────────────────────────────────────────────
    public int getReplyCount(Long boardIdx) {
        return boardMapper.countRepliesByBoard(boardIdx);
    }
}
