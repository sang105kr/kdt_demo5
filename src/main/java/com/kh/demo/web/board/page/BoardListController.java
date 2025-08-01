package com.kh.demo.web.board.page;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 목록 및 검색 컨트롤러
 * - 게시글 목록 조회
 * - 카테고리별 조회
 * - 검색 기능
 * - 페이징 처리
 */
@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardListController extends BaseController {
    
    private final BoardSVC boardSVC;
    private final CodeSVC codeSVC;
    
    private static final int PAGE_SIZE = 10; // 페이지당 게시글 수

    /**
     * 게시판 카테고리를 모든 요청에 자동으로 추가 (하위 카테고리만)
     */
    @ModelAttribute("boardCategories")
    public List<Code> boardCategories() {
        return codeSVC.getCodeList("BOARD").stream()
            .filter(code -> "Y".equals(code.getUseYn()) && code.getPcode() != null)
            .collect(Collectors.toList());
    }

    /**
     * 게시글 목록 페이지
     * GET /board
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long category,
                      @RequestParam(required = false) String search,
                      @RequestParam(defaultValue = "1", name = "pageNo") int pageNo, 
                      Model model,
                      HttpSession session) {
        
        log.info("게시판 목록 조회 요청 - category: {}, search: {}, pageNo: {}", category, search, pageNo);
        
        // 권한 정보를 모델에 추가
        addAuthInfoToModel(model, session);
        
        List<Boards> boards;
        int totalCount;
        Pagination pagination;
        
        // 카테고리별 또는 전체 게시글 조회 (검색 포함)
        if (search != null && !search.trim().isEmpty()) {
            // 검색이 있는 경우
            if (category != null) {
                // 카테고리 + 검색
                totalCount = boardSVC.countByBcategoryAndTitleContaining(category, search.trim());
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByBcategoryAndTitleContainingWithPaging(category, search.trim(), pageNo, PAGE_SIZE);
            } else {
                // 전체 + 검색
                totalCount = boardSVC.countByTitleContaining(search.trim());
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByTitleContainingWithPaging(search.trim(), pageNo, PAGE_SIZE);
            }
        } else {
            // 검색이 없는 경우
            if (category != null) {
                // 카테고리별 조회
                totalCount = boardSVC.countByBcategory(category);
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByBcategoryWithPaging(category, pageNo, PAGE_SIZE);
                log.info("카테고리별 조회 - category: {}, totalCount: {}, boards.size: {}", category, totalCount, boards.size());
            } else {
                // 전체 조회
                totalCount = boardSVC.countAll();
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findAllWithPaging(pageNo, PAGE_SIZE);
                log.info("전체 조회 - totalCount: {}, boards.size: {}, boards: {}", totalCount, boards.size(), boards);
            }
        }
        
        // 각 게시글의 좋아요/싫어요 수와 카테고리 정보를 Map으로 관리
        Map<Long, Map<String, Integer>> likeDislikeMap = new HashMap<>();
        Map<Long, String> categoryNameMap = new HashMap<>();
        
        for (Boards board : boards) {
            // 좋아요/싫어요 정보
            Map<String, Integer> likeDislike = new HashMap<>();
            likeDislike.put("like", board.getLikeCount());
            likeDislike.put("dislike", board.getDislikeCount());
            likeDislikeMap.put(board.getBoardId(), likeDislike);
            
            // 카테고리 이름 정보 (전체 목록일 때만 필요)
            if (category == null) {
                String boardCategoryName = codeSVC.getCodeDecode("BOARD", board.getBcategory());
                if (boardCategoryName == null) {
                    boardCategoryName = "알 수 없음";
                }
                categoryNameMap.put(board.getBoardId(), boardCategoryName);
            }
        }
        
        // 카테고리 정보 추가
        String categoryName = null;
        if (category != null) {
            // 선택된 카테고리 이름 조회
            categoryName = codeSVC.getCodeDecode("BOARD", category);
            if (categoryName == null) {
                categoryName = "알 수 없음";
            }
        }
        
        model.addAttribute("list", boards);
        model.addAttribute("pagination", pagination);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("likeDislikeMap", likeDislikeMap);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categoryNameMap", categoryNameMap);
        
        log.info("모델에 추가된 데이터 - list.size: {}, pagination: {}, selectedCategory: {}, searchKeyword: {}, categoryName: {}", 
                boards.size(), pagination, category, search, categoryName);
        
        return "board/list";
    }
} 