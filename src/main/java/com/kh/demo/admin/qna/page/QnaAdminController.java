package com.kh.demo.admin.qna.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.util.MemberAuthUtil;
import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.qna.svc.QnaCommentService;
import com.kh.demo.domain.qna.svc.QnaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/qna")
@RequiredArgsConstructor
public class QnaAdminController {

    private final QnaService qnaService;
    private final QnaCommentService qnaCommentService;
    private final CodeSVC codeSVC;
    private final MemberAuthUtil memberAuthUtil;

    /**
     * Q&A 관리 목록 페이지
     */
    @GetMapping
    public String qnaList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "redirect:/login";
        }

        try {
            // Q&A 목록 조회
            List<Qna> qnaList = qnaService.findAll(category, keyword, status, page, size);
            int totalCount = qnaService.countAll(category, keyword, status);
            
            // 카테고리 코드 조회
            List<Code> categories = codeSVC.getCodeList("QNA_CATEGORY");
            List<Code> statuses = codeSVC.getCodeList("QNA_STATUS");
            
            // 페이징 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            // 통계 정보 조회
            Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            Long answeredStatusId = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
            Long hiddenStatusId = codeSVC.getCodeId("QNA_STATUS", "HIDDEN");
            
            int waitingCount = qnaService.countByStatusId(pendingStatusId);
            int completedCount = qnaService.countByStatusId(answeredStatusId);
            int hiddenCount = qnaService.countByStatusId(hiddenStatusId);
            
            model.addAttribute("qnaList", qnaList);
            model.addAttribute("categories", categories);
            model.addAttribute("statuses", statuses);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("category", category);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("waitingCount", waitingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("hiddenCount", hiddenCount);
            
            log.info("관리자 Q&A 목록 조회: page={}, size={}, totalCount={}", page, size, totalCount);
            
        } catch (Exception e) {
            log.error("관리자 Q&A 목록 조회 실패", e);
            model.addAttribute("error", "Q&A 목록을 불러오는 중 오류가 발생했습니다.");
        }
        
        return "admin/qna/list";
    }

    /**
     * Q&A 상세 보기 (관리자용)
     */
    @GetMapping("/{qnaId}")
    public String qnaDetail(
            @PathVariable Long qnaId,
            Model model,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "redirect:/login";
        }

        try {
            // Q&A 상세 정보 조회
            Optional<Qna> qnaOpt = qnaService.findById(qnaId);
            if (qnaOpt.isEmpty()) {
                model.addAttribute("error", "존재하지 않는 Q&A입니다.");
                return "admin/qna/list";
            }
            Qna qna = qnaOpt.get();
            
            // Q&A 댓글 목록 조회
            List<QnaComment> comments = qnaCommentService.findByQnaId(qnaId);
            
            // 카테고리 코드 조회
            List<Code> categories = codeSVC.getCodeList("QNA_CATEGORY");
            List<Code> statuses = codeSVC.getCodeList("QNA_STATUS");
            
            model.addAttribute("qna", qna);
            model.addAttribute("comments", comments);
            model.addAttribute("categories", categories);
            model.addAttribute("statuses", statuses);
            
            log.info("관리자 Q&A 상세 조회: qnaId={}", qnaId);
            
        } catch (Exception e) {
            log.error("관리자 Q&A 상세 조회 실패: qnaId={}", qnaId, e);
            model.addAttribute("error", "Q&A 정보를 불러오는 중 오류가 발생했습니다.");
        }
        
        return "admin/qna/detail";
    }

    /**
     * Q&A 답변 작성/수정 폼
     */
    @GetMapping("/{qnaId}/answer")
    public String answerForm(
            @PathVariable Long qnaId,
            Model model,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "redirect:/login";
        }

        try {
            // Q&A 정보 조회
            Optional<Qna> qnaOpt = qnaService.findById(qnaId);
            if (qnaOpt.isEmpty()) {
                model.addAttribute("error", "존재하지 않는 Q&A입니다.");
                return "admin/qna/list";
            }
            Qna qna = qnaOpt.get();
            
            model.addAttribute("qna", qna);
            
            log.info("관리자 Q&A 답변 폼: qnaId={}", qnaId);
            
        } catch (Exception e) {
            log.error("관리자 Q&A 답변 폼 조회 실패: qnaId={}", qnaId, e);
            model.addAttribute("error", "Q&A 정보를 불러오는 중 오류가 발생했습니다.");
        }
        
        return "admin/qna/answer";
    }

    /**
     * Q&A 답변 작성/수정 처리
     */
    @PostMapping("/{qnaId}/answer")
    public String answerSubmit(
            @PathVariable Long qnaId,
            @RequestParam String answer,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "redirect:/login";
        }

        try {
            // 답변 업데이트
            int result = qnaService.updateAnswer(qnaId, answer, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("관리자 Q&A 답변 작성 성공: qnaId={}, adminId={}", qnaId, loginMember.getMemberId());
                return "redirect:/admin/qna/" + qnaId;
            } else {
                model.addAttribute("error", "답변 작성에 실패했습니다.");
                return "admin/qna/answer";
            }
            
        } catch (Exception e) {
            log.error("관리자 Q&A 답변 작성 실패: qnaId={}", qnaId, e);
            model.addAttribute("error", "답변 작성 중 오류가 발생했습니다.");
            return "admin/qna/answer";
        }
    }

    /**
     * Q&A 답변 수정 (AJAX)
     */
    @PutMapping("/{qnaId}/answer")
    @ResponseBody
    public ApiResponse<String> updateAnswer(
            @PathVariable Long qnaId,
            @RequestBody AnswerUpdateRequest request,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, "권한이 없습니다.");
        }

        try {
            // 답변 업데이트
            int result = qnaService.updateAnswer(qnaId, request.getAnswer(), loginMember.getMemberId());
            
            if (result > 0) {
                log.info("관리자 Q&A 답변 수정 성공: qnaId={}, adminId={}", qnaId, loginMember.getMemberId());
                return ApiResponse.of(ApiResponseCode.SUCCESS, "답변이 성공적으로 수정되었습니다.");
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "답변 수정에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("관리자 Q&A 답변 수정 실패: qnaId={}", qnaId, e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "답변 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 답변 수정 요청 DTO
     */
    public static class AnswerUpdateRequest {
        private String answer;

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    /**
     * Q&A 상태 변경
     */
    @PostMapping("/{qnaId}/status")
    @ResponseBody
    public String updateStatus(
            @PathVariable Long qnaId,
            @RequestParam String status,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "unauthorized";
        }

        try {
            Long statusId = codeSVC.getCodeId("QNA_STATUS", status);
            int result = qnaService.updateStatus(qnaId, statusId);
            
            if (result > 0) {
                log.info("관리자 Q&A 상태 변경 성공: qnaId={}, status={}, adminId={}", 
                        qnaId, status, loginMember.getMemberId());
                return "success";
            } else {
                return "failed";
            }
            
        } catch (Exception e) {
            log.error("관리자 Q&A 상태 변경 실패: qnaId={}, status={}", qnaId, status, e);
            return "error";
        }
    }

    /**
     * Q&A 삭제
     */
    @PostMapping("/{qnaId}/delete")
    @ResponseBody
    public String deleteQna(
            @PathVariable Long qnaId,
            HttpSession session) {
        
        // 관리자 권한 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return "unauthorized";
        }

        try {
            int result = qnaService.deleteById(qnaId);
            
            if (result > 0) {
                log.info("관리자 Q&A 삭제 성공: qnaId={}, adminId={}", qnaId, loginMember.getMemberId());
                return "success";
            } else {
                return "failed";
            }
            
        } catch (Exception e) {
            log.error("관리자 Q&A 삭제 실패: qnaId={}", qnaId, e);
            return "error";
        }
    }
}
