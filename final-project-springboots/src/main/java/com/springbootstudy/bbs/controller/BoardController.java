package com.springbootstudy.bbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springbootstudy.bbs.domain.BoardTypeVO;
import com.springbootstudy.bbs.domain.BoardVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.ReplyVO;
import com.springbootstudy.bbs.service.BoardService;
import com.springbootstudy.bbs.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;
    
    @Autowired
    private NotificationService notificationService;

    // ── 게시판 목록 (전체 or 카테고리별) ─────────────────────
    // /boards → 전체 목록 (커뮤니티 홈)
    @GetMapping
    public String home(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        int totalCount = boardService.countBoards(null, keyword, searchType);
        addPagingAttributes(model, null, keyword, searchType, page, totalCount);
        model.addAttribute("boards", boardService.getBoardsPaged(null, keyword, searchType, page));
        model.addAttribute("typeCode", null);
        model.addAttribute("currentType", null);
        return "views/board/boardList";
    }

    // /boards/{typeCode} → 특정 게시판 목록
    @GetMapping("/{typeCode}")
    public String list(
            @PathVariable("typeCode") String typeCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        BoardTypeVO currentType = boardService.getBoardTypeByCode(typeCode);
        if (currentType == null)
            return "redirect:/boards";

        int totalCount = boardService.countBoards(typeCode, keyword, searchType);
        addPagingAttributes(model, typeCode, keyword, searchType, page, totalCount);
        model.addAttribute("boards", boardService.getBoardsPaged(typeCode, keyword, searchType, page));
        model.addAttribute("typeCode", typeCode);
        model.addAttribute("currentType", currentType);
        return "views/board/boardList";
    }

    // ── 페이징 공통 처리 ──────────────────────────────────────
    private void addPagingAttributes(Model model, String typeCode, String keyword,
            String searchType, int page, int totalCount) {
        int pageSize = 10; // 페이지당 게시글 수
        int blockSize = 10; // 페이지 블록 크기
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;

        int blockStart = ((page - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

        model.addAttribute("boardTypes", boardService.getBoardTypes());
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("totalCount", totalCount);
    }

    // ── 게시글 상세 ──────────────────────────────────────────
    @GetMapping("/{typeCode}/{boardIdx}")
    public String detail(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "replyPage", defaultValue = "1") int replyPage,
            @RequestParam(value = "sortType", defaultValue = "oldest") String sortType,
            Model model,
            HttpSession session) {
        BoardVO board = boardService.getBoardDetail(boardIdx, session);

        int replyPageSize = 20;
        int replyBlockSize = 10;
        int totalReplies = boardService.getReplyCount(boardIdx); // 전체 댓글 수 기준
        int totalReplyPages = (int) Math.ceil((double) totalReplies / replyPageSize);
        if (totalReplyPages == 0)
            totalReplyPages = 1;
        if (replyPage < 1)
            replyPage = 1;
        if (replyPage > totalReplyPages)
            replyPage = totalReplyPages;

        int replyBlockStart = ((replyPage - 1) / replyBlockSize) * replyBlockSize + 1;
        int replyBlockEnd = Math.min(replyBlockStart + replyBlockSize - 1, totalReplyPages);

        List<ReplyVO> replies = boardService.getRepliesPaged(boardIdx, replyPage, sortType);

        String backUrl = "all".equals(from)
                ? "/boards?page=" + page
                : "/boards/" + typeCode + "?page=" + page;

        model.addAttribute("board", board);
        model.addAttribute("replies", replies);
        model.addAttribute("typeCode", typeCode);
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("boardTypes", boardService.getBoardTypes());
        model.addAttribute("replyPage", replyPage);
        model.addAttribute("totalReplyPages", totalReplyPages);
        model.addAttribute("replyBlockStart", replyBlockStart);
        model.addAttribute("replyBlockEnd", replyBlockEnd);
        model.addAttribute("totalReplies", totalReplies);
        model.addAttribute("sortType", sortType);
        return "views/board/boardDetail";
    }

    // ── 게시글 작성 폼 ───────────────────────────────────────
    @GetMapping("/{typeCode}/new")
    public String newForm(
            @PathVariable("typeCode") String typeCode,
            @RequestParam(value = "from", required = false) String from,
            Model model) {
        BoardTypeVO currentType = boardService.getBoardTypeByCode(typeCode);
        if (currentType == null)
            return "redirect:/boards";

        model.addAttribute("boardTypes", boardService.getBoardTypes());
        model.addAttribute("currentType", currentType);
        model.addAttribute("typeCode", typeCode);
        model.addAttribute("from", from);
        return "views/board/boardNew";
    }

    // ── 게시글 등록 처리 ─────────────────────────────────────
    @PostMapping("/{typeCode}")
    public String create(
            @PathVariable("typeCode") String typeCode,
            @RequestParam("boardTitle") String boardTitle,
            @RequestParam("boardContent") String boardContent,
            @RequestParam(value = "from", required = false) String from,
            HttpServletRequest request,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            String redirectUrl = "/boards/" + typeCode + "/new" + ("all".equals(from) ? "?from=all" : "");
            return "redirect:/members/login?redirect=" + redirectUrl;
        }

        BoardTypeVO boardType = boardService.getBoardTypeByCode(typeCode);
        if (boardType == null)
            return "redirect:/boards";

        BoardVO board = new BoardVO();
        board.setMemIdx(loginUser.getMemIdx());
        board.setBoardTypeIdx(boardType.getBoardTypeIdx());
        board.setBoardTitle(boardTitle);
        board.setBoardContent(boardContent);
        board.setBoardIp(getClientIp(request));

        boardService.writeBoard(board);
        // from=all 이면 전체 목록, 아니면 해당 카테고리 목록
        return "all".equals(from) ? "redirect:/boards" : "redirect:/boards/" + typeCode;
    }

    // ── 게시글 수정 폼 ───────────────────────────────────────
    @GetMapping("/{typeCode}/{boardIdx}/edit")
    public String editForm(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            Model model, HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        BoardVO board = boardService.getBoardDetail(boardIdx, session);
        if (!board.getMemIdx().equals(loginUser.getMemIdx())) {
            return "redirect:/boards/" + typeCode + "/" + boardIdx;
        }
        model.addAttribute("board", board);
        model.addAttribute("typeCode", typeCode);
        return "views/board/boardEdit";
    }

    // ── 게시글 수정 처리 ─────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/edit")
    public String edit(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @RequestParam("boardTitle") String boardTitle,
            @RequestParam("boardContent") String boardContent,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        BoardVO board = new BoardVO();
        board.setBoardIdx(boardIdx);
        board.setBoardTitle(boardTitle);
        board.setBoardContent(boardContent);
        boardService.editBoard(board);
        return "redirect:/boards/" + typeCode + "/" + boardIdx;
    }

    // ── 게시글 삭제 ──────────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/delete")
    public String delete(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        boardService.removeBoard(boardIdx);
        return "all".equals(from)
                ? "redirect:/boards?page=" + page
                : "redirect:/boards/" + typeCode + "?page=" + page;
    }

    // ── 댓글 등록 ────────────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/replies")
    public String createReply(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @RequestParam("replyContent") String replyContent,
            @RequestParam(value = "parentReplyIdx", required = false) Long parentReplyIdx,
            @RequestParam(value = "replyPage", defaultValue = "1") int replyPage,
            @RequestParam(value = "sortType", defaultValue = "oldest") String sortType,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            String redirectUrl = "/boards/" + typeCode + "/" + boardIdx
                    + "?replyPage=" + replyPage + "&sortType=" + sortType
                    + (from != null ? "&from=" + from : "") + "&page=" + page;
            try {
                return "redirect:/members/login?redirect=" + java.net.URLEncoder.encode(redirectUrl, "UTF-8");
            } catch (Exception e) {
                return "redirect:/members/login";
            }
        }

        ReplyVO reply = new ReplyVO();
        reply.setBoardIdx(boardIdx);
        reply.setMemIdx(loginUser.getMemIdx());
        reply.setReplyContent(replyContent);
        reply.setReplyIp(getClientIp(request));

        String fromParam = (from != null ? "&from=" + from : "") + "&page=" + page;

        int result = boardService.writeReply(reply, parentReplyIdx);
        if (result == -1) {
            return "redirect:/boards/" + typeCode + "/" + boardIdx
                    + "?replyPage=" + replyPage + "&sortType=" + sortType + fromParam + "&replyLimitExceeded=true";
        }

        // 등록 후 자신의 댓글이 있는 페이지 계산
        int totalRootReplies = boardService.getReplyCount(boardIdx); // 전체 댓글 수 기준
        int totalReplyPages = (int) Math.ceil((double) totalRootReplies / 20);
        if (totalReplyPages == 0)
            totalReplyPages = 1;

        int targetPage;
        if (parentReplyIdx == null) {
            // 원댓글: 등록순이면 마지막 페이지, 최신순이면 1페이지
            targetPage = "oldest".equals(sortType) ? totalReplyPages : 1;
        } else {
            // 대댓글: 현재 보던 페이지 유지
            targetPage = replyPage;
        }
        
        BoardVO boardVO = boardService.getBoardDetail(boardIdx, session);
        if (boardVO != null) {
            notificationService.notifyNewReplyToBoardWriter(boardVO, reply);
        }
        
        return "redirect:/boards/" + typeCode + "/" + boardIdx
                + "?replyPage=" + targetPage + "&sortType=" + sortType + fromParam;
    }

    // ── 게시글 좋아요 ─────────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/like")
    public String likeBoard(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @RequestParam(value = "replyPage", defaultValue = "1") int replyPage,
            @RequestParam(value = "sortType", defaultValue = "oldest") String sortType,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session) {
        boardService.likeBoardIfNotYet(boardIdx, session);
        String fromParam = (from != null ? "&from=" + from : "") + "&page=" + page;
        return "redirect:/boards/" + typeCode + "/" + boardIdx + "?replyPage=" + replyPage + "&sortType=" + sortType + fromParam;
    }

    // ── 댓글 좋아요 ───────────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/replies/{replyIdx}/like")
    public String likeReply(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @PathVariable("replyIdx") Long replyIdx,
            @RequestParam(value = "replyPage", defaultValue = "1") int replyPage,
            @RequestParam(value = "sortType", defaultValue = "oldest") String sortType,
            HttpSession session) {
        boardService.likeReplyIfNotYet(replyIdx, session);
        return "redirect:/boards/" + typeCode + "/" + boardIdx + "?replyPage=" + replyPage + "&sortType=" + sortType;
    }

    // ── 댓글 수정 ────────────────────────────────────────────
    @PostMapping("/{typeCode}/{boardIdx}/replies/{replyIdx}/edit")
    public String editReply(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("boardIdx") Long boardIdx,
            @PathVariable("replyIdx") Long replyIdx,
            @RequestParam("replyContent") String replyContent,
            @RequestParam(value = "replyPage", defaultValue = "1") int replyPage,
            @RequestParam(value = "sortType", defaultValue = "oldest") String sortType,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        ReplyVO existing = boardService.getReplies(boardIdx).stream()
                .filter(r -> r.getReplyIdx().equals(replyIdx))
                .findFirst().orElse(null);
        if (existing == null || !existing.getMemIdx().equals(loginUser.getMemIdx())) {
            return "redirect:/boards/" + typeCode + "/" + boardIdx + "?replyPage=" + replyPage + "&sortType="
                    + sortType;
        }

        if (replyContent != null && replyContent.trim().length() >= 3) {
            boardService.editReply(replyIdx, replyContent.trim());
        }
        return "redirect:/boards/" + typeCode + "/" + boardIdx + "?replyPage=" + replyPage + "&sortType=" + sortType;
    }

    // ── 댓글 삭제 ────────────────────────────────────────────
    @PostMapping("/{typeCode}/replies/{replyIdx}/delete")
    public String deleteReply(
            @PathVariable("typeCode") String typeCode,
            @PathVariable("replyIdx") Long replyIdx,
            @RequestParam("boardIdx") Long boardIdx,
            HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        boardService.removeReply(replyIdx);
        return "redirect:/boards/" + typeCode + "/" + boardIdx;
    }

    // ── IP 유틸 ──────────────────────────────────────────────
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
