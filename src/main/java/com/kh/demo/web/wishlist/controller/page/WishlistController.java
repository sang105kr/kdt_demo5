package com.kh.demo.web.wishlist.controller.page;

import com.kh.demo.domain.wishlist.entity.Wishlist;
import com.kh.demo.domain.wishlist.dto.WishlistItemDto;
import com.kh.demo.domain.wishlist.svc.WishlistSVC;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 위시리스트 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/member/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistSVC wishlistSVC;

    /**
     * 위시리스트 페이지
     */
    @GetMapping
    public String wishlistPage(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("위시리스트 페이지 요청 - page: {}, size: {}", page, size);

        // 로그인 체크
        if (loginMember == null) {
            log.warn("위시리스트 접근 실패 - 로그인 필요");
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        try {
            Long memberId = loginMember.getMemberId();

            // 위시리스트 조회 (페이징)
            List<WishlistItemDto> wishlistItems = wishlistSVC.getWishlistByMemberId(memberId, page, size);
            
            // 전체 개수 조회
            int totalCount = wishlistSVC.getWishlistCountByMemberId(memberId);
            
            // 페이징 정보 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasPrevious = page > 1;
            boolean hasNext = page < totalPages;

            // 페이지네이션 객체 생성
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPages", totalPages);
            pagination.put("pageSize", size);
            pagination.put("totalElements", totalCount);
            pagination.put("hasPrevious", hasPrevious);
            pagination.put("hasNext", hasNext);
            pagination.put("first", page == 1);
            pagination.put("last", page == totalPages);

            // 모델에 데이터 추가
            model.addAttribute("wishlistItems", wishlistItems);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasPrevious", hasPrevious);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("pagination", pagination);
            model.addAttribute("loginMember", loginMember);

            log.info("위시리스트 조회 완료 - memberId: {}, totalCount: {}, currentPage: {}", 
                    memberId, totalCount, page);

            return "member/wishlist/list";

        } catch (Exception e) {
            log.error("위시리스트 페이지 로드 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            model.addAttribute("errorMessage", "위시리스트를 불러오는 중 오류가 발생했습니다.");
            return "member/wishlist/list";
        }
    }
} 