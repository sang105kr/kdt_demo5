package com.kh.demo.web.review.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductSearchService;
import com.kh.demo.domain.product.svc.ProductService;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.review.vo.ReviewDetailVO;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.product.controller.page.dto.ProductDetailDTO;
import com.kh.demo.web.product.controller.page.dto.ProductListDTO;
import com.kh.demo.web.product.controller.page.dto.SearchCriteria;
import com.kh.demo.web.product.controller.page.dto.SearchResult;
import com.kh.demo.web.review.controller.page.form.ReviewCommentForm;
import com.kh.demo.common.session.SessionConst;
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
import java.util.stream.Collectors;

/**
 * 고객용 상품 페이지 컨트롤러
 * - 웹 페이지 렌더링 (Thymeleaf)
 * - 고객 중심 기능 (검색, 상세보기, 자동완성)
 * - 세션 기반 사용자 정보 처리
 * - 다국어 지원
 */
@Slf4j
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController extends BaseController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;




} 