package com.kh.demo.web.qna.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.qna.svc.QnaService;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Q&A 페이지 컨트롤러
 * - 웹 페이지 렌더링 (Thymeleaf)
 * - Q&A 목록, 상세보기, 작성 페이지
 * - 세션 기반 사용자 정보 처리
 * - 다국어 지원
 */
@Slf4j
@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController extends BaseController {

    private final QnaService qnaService;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;

    /**
     * Q&A 목록 페이지
     */
    @GetMapping({"", "/list"})
    public String qnaList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {
        
        log.info("Q&A 목록 페이지 요청 - keyword: {}, category: {}, page: {}, size: {}", 
                keyword, category, page, size);
        
        try {
            // Q&A 카테고리 목록 조회
            List<Code> categories = codeSVC.getCodeList("QNA_CATEGORY");
            List<Code> statuses = codeSVC.getCodeList("QNA_STATUS");
            model.addAttribute("categories", categories);
            model.addAttribute("statuses", statuses);
            
            // 검색 조건에 따른 Q&A 목록 조회
            List<Qna> qnaList;
            int totalCount;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 키워드 검색
                int offset = (page - 1) * size;
                qnaList = qnaService.findByKeyword(keyword, offset, size);
                totalCount = qnaService.countByKeyword(keyword);
            } else if (category != null) {
                // 카테고리별 조회
                int offset = (page - 1) * size;
                qnaList = qnaService.findByCategoryId(category, offset, size);
                totalCount = qnaService.countByCategoryId(category);
            } else if (status != null) {
                // 상태별 조회
                int offset = (page - 1) * size;
                qnaList = qnaService.findByStatusId(status, offset, size);
                totalCount = qnaService.countByStatusId(status);
            } else {
                // 전체 조회 (최신순) - PENDING, ANSWERED 상태의 Q&A 조회
                int offset = (page - 1) * size;
                qnaList = qnaService.findAllWithPaging(offset, size);
                totalCount = qnaService.countAll();
            }
            
            // 페이지네이션 정보 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            // 통계 정보 조회
            Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            Long answeredStatusId = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
            Long hiddenStatusId = codeSVC.getCodeId("QNA_STATUS", "HIDDEN");
            
            int waitingCount = qnaService.countByStatusId(pendingStatusId);
            int completedCount = qnaService.countByStatusId(answeredStatusId);
            int hiddenCount = qnaService.countByStatusId(hiddenStatusId);
            
            // 모델에 데이터 추가
            model.addAttribute("qnaList", qnaList);
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("qnaStatus", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("waitingCount", waitingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("hiddenCount", hiddenCount);
            
            // 로그인 정보 추가
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            model.addAttribute("loginMember", loginMember);
            
            return "qna/list";
            
        } catch (Exception e) {
            log.error("Q&A 목록 페이지 로드 실패", e);
            model.addAttribute("error", "Q&A 목록을 불러오는데 실패했습니다.");
            return "qna/list";
        }
    }

    /**
     * Q&A 상세 페이지
     */
    @GetMapping("/{qnaId}")
    public String qnaDetail(@PathVariable Long qnaId, Model model, HttpSession session) {
        log.info("Q&A 상세 페이지 요청 - qnaId: {}", qnaId);
        
        try {
            // Q&A 상세 조회 (조회수 증가 포함)
            Optional<Qna> qnaOpt = qnaService.findByIdWithViewCount(qnaId);
            
            if (qnaOpt.isEmpty()) {
                // Q&A를 찾지 못한 경우에도 detail 페이지를 렌더링하되, qna는 null로 설정
                model.addAttribute("qna", null);
                model.addAttribute("comments", null);
                LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
                model.addAttribute("loginMember", loginMember);
                return "qna/detail";
            }
            
            Qna qna = qnaOpt.get();
            
            // Q&A 댓글 목록 조회
            List<QnaComment> comments = qnaService.findCommentsByQnaId(qnaId);
            
            // 모델에 데이터 추가
            model.addAttribute("qna", qna);
            model.addAttribute("comments", comments);
            
            // 로그인 정보 추가
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            model.addAttribute("loginMember", loginMember);
            
            return "qna/detail";
            
        } catch (Exception e) {
            log.error("Q&A 상세 페이지 로드 실패 - qnaId: {}", qnaId, e);
            // 예외 발생 시에도 detail 페이지를 렌더링하되, qna는 null로 설정
            model.addAttribute("qna", null);
            model.addAttribute("comments", null);
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            model.addAttribute("loginMember", loginMember);
            return "qna/detail";
        }
    }

    /**
     * Q&A 작성 페이지
     */
    @GetMapping("/write")
    public String qnaWriteForm(
            @RequestParam(required = false) Long productId,
            Model model, 
            HttpSession session) {
        
        log.info("Q&A 작성 페이지 요청 - productId: {}", productId);
        
        // 로그인 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirect=/qna/write";
        }
        
        try {
            // Q&A 카테고리 목록 조회
            List<Code> qnaCategories = codeSVC.getCodeList("QNA_CATEGORY");
            model.addAttribute("qnaCategories", qnaCategories);
            model.addAttribute("productId", productId);
            model.addAttribute("loginMember", loginMember);
            
            return "qna/write";
            
        } catch (Exception e) {
            log.error("Q&A 작성 페이지 로드 실패", e);
            model.addAttribute("error", "Q&A 작성 페이지를 불러오는데 실패했습니다.");
            return "qna/list";
        }
    }

    /**
     * Q&A 수정 페이지
     */
    @GetMapping("/{qnaId}/edit")
    public String qnaEditForm(@PathVariable Long qnaId, Model model, HttpSession session) {
        log.info("Q&A 수정 페이지 요청 - qnaId: {}", qnaId);
        
        // 로그인 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirect=/qna/" + qnaId + "/edit";
        }
        
        try {
            // Q&A 조회
            Optional<Qna> qnaOpt = qnaService.findById(qnaId);
            
            if (qnaOpt.isEmpty()) {
                model.addAttribute("error", "존재하지 않는 Q&A입니다.");
                return "qna/list";
            }
            
            Qna qna = qnaOpt.get();
            
            // 본인 작성 Q&A인지 확인
            if (!qna.getMemberId().equals(loginMember.getMemberId())) {
                model.addAttribute("error", "본인이 작성한 Q&A만 수정할 수 있습니다.");
                return "qna/list";
            }
            
            // Q&A 카테고리 목록 조회
            List<Code> qnaCategories = codeSVC.getCodeList("QNA_CATEGORY");
            
            // 모델에 데이터 추가
            model.addAttribute("qna", qna);
            model.addAttribute("qnaCategories", qnaCategories);
            model.addAttribute("loginMember", loginMember);
            
            return "qna/edit";
            
        } catch (Exception e) {
            log.error("Q&A 수정 페이지 로드 실패 - qnaId: {}", qnaId, e);
            model.addAttribute("error", "Q&A 수정 페이지를 불러오는데 실패했습니다.");
            return "qna/list";
        }
    }

    /**
     * 내 Q&A 목록 페이지
     */
    @GetMapping("/my")
    public String myQnaList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, 
            HttpSession session) {
        
        log.info("내 Q&A 목록 페이지 요청 - page: {}, size: {}", page, size);
        
        // 로그인 체크
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirect=/qna/my";
        }
        
        try {
            // Q&A 카테고리 및 상태 목록 조회
            List<Code> categories = codeSVC.getCodeList("QNA_CATEGORY");
            List<Code> statuses = codeSVC.getCodeList("QNA_STATUS");
            
            // 내 Q&A 목록 조회 (모든 상태)
            List<Qna> qnaList = qnaService.findByMemberIdAllStatus(loginMember.getMemberId());
            
            // 페이지네이션 처리
            int totalCount = qnaList.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalCount);
            
            List<Qna> pagedQnaList = qnaList.subList(startIndex, endIndex);
            
            // 통계 정보 조회 (내 Q&A 기준)
            Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            Long answeredStatusId = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
            Long hiddenStatusId = codeSVC.getCodeId("QNA_STATUS", "HIDDEN");
            
            int waitingCount = (int) qnaList.stream()
                    .filter(qna -> pendingStatusId.equals(qna.getStatusId()))
                    .count();
            int completedCount = (int) qnaList.stream()
                    .filter(qna -> answeredStatusId.equals(qna.getStatusId()))
                    .count();
            int hiddenCount = (int) qnaList.stream()
                    .filter(qna -> hiddenStatusId.equals(qna.getStatusId()))
                    .count();
            
            // 모델에 데이터 추가
            model.addAttribute("qnaList", pagedQnaList);
            model.addAttribute("categories", categories);
            model.addAttribute("statuses", statuses);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("waitingCount", waitingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("hiddenCount", hiddenCount);
            model.addAttribute("loginMember", loginMember);
            
            return "qna/my";
            
        } catch (Exception e) {
            log.error("내 Q&A 목록 페이지 로드 실패", e);
            model.addAttribute("error", "내 Q&A 목록을 불러오는데 실패했습니다.");
            return "qna/my";
        }
    }
}
