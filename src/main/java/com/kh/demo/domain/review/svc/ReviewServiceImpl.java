package com.kh.demo.domain.review.svc;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.dao.MemberDAO;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.order.dao.OrderDAO;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.review.dao.ReviewDAO;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.vo.ReviewDetailVO;
import com.kh.demo.common.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.kh.demo.domain.review.entity.ReviewComment;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewDAO reviewDAO;
    private final MemberDAO memberDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final CodeSVC codeSVC;
    private Long deliveredCodeId;

    @PostConstruct
    public void init() {
        this.deliveredCodeId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
        if (this.deliveredCodeId == null) {
            throw new IllegalStateException("DELIVERED 코드가 존재하지 않습니다.");
        }
    }
    
    @Override
    @Transactional
    public Long save(Review review) {
        return reviewDAO.save(review);
    }
    
    @Override
    public Optional<Review> findById(Long id) {
        return reviewDAO.findById(id);
    }
    
    @Override
    public List<Review> findAll() {
        return reviewDAO.findAll();
    }
    
    @Override
    public List<Review> findAll(int pageNo, int numOfRows) {
        int offset = (pageNo - 1) * numOfRows;
        return reviewDAO.findByProductId(null, offset, numOfRows); // 임시로 null 처리
    }
    
    @Override
    @Transactional
    public int updateById(Long id, Review review) {
        return reviewDAO.updateById(id, review);
    }
    
    @Override
    @Transactional
    public int deleteById(Long id) {
        return reviewDAO.deleteById(id);
    }
    
    @Override
    public int getTotalCount() {
        return reviewDAO.getTotalCount();
    }
    
    @Override
    public Optional<ReviewDetailVO> findReviewDetailById(Long reviewId) {
        Optional<Review> reviewOpt = reviewDAO.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Review review = reviewOpt.get();
        
        // 관련 엔티티들 조회
        Optional<Member> memberOpt = memberDAO.findById(review.getMemberId());
        Optional<Products> productOpt = productDAO.findById(review.getProductId());
        Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
        
        ReviewDetailVO detailVO = ReviewDetailVO.builder()
            .review(review)
            .member(memberOpt.orElse(null))
            .product(productOpt.orElse(null))
            .order(orderOpt.orElse(null))
            .build();
        
        return Optional.of(detailVO);
    }
    
    @Override
    public List<ReviewDetailVO> findReviewDetailsByProductId(Long productId, int offset, int limit) {
        List<Review> reviews = reviewDAO.findByProductId(productId, offset, limit);
        
        return reviews.stream()
            .map(review -> {
                Optional<Member> memberOpt = memberDAO.findById(review.getMemberId());
                Optional<Products> productOpt = productDAO.findById(review.getProductId());
                Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewDetailVO> findReviewDetailsByMemberId(Long memberId) {
        List<Review> reviews = reviewDAO.findByMemberId(memberId);
        
        return reviews.stream()
            .map(review -> {
                Optional<Member> memberOpt = memberDAO.findById(review.getMemberId());
                Optional<Products> productOpt = productDAO.findById(review.getProductId());
                Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Review> findByMemberId(Long memberId) {
        return reviewDAO.findByMemberId(memberId);
    }

    @Override
    public List<ReviewComment> findCommentsByReviewId(Long reviewId) {
        return reviewDAO.findCommentsByReviewId(reviewId);
    }

    @Override
    public Optional<Review> findByOrderId(Long orderId) {
        return reviewDAO.findByOrderId(orderId);
    }
    
    @Override
    public Optional<ReviewDetailVO> findReviewDetailByOrderId(Long orderId) {
        Optional<Review> reviewOpt = reviewDAO.findByOrderId(orderId);
        if (reviewOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Review review = reviewOpt.get();
        
        Optional<Member> memberOpt = memberDAO.findById(review.getMemberId());
        Optional<Products> productOpt = productDAO.findById(review.getProductId());
        Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
        
        ReviewDetailVO detailVO = ReviewDetailVO.builder()
            .review(review)
            .member(memberOpt.orElse(null))
            .product(productOpt.orElse(null))
            .order(orderOpt.orElse(null))
            .build();
        
        return Optional.of(detailVO);
    }
    
    @Override
    public List<ReviewDetailVO> findReviewDetailsByProductIdAndRating(Long productId, Double rating, int offset, int limit) {
        List<Review> reviews = reviewDAO.findByProductIdAndRating(productId, rating, offset, limit);
        
        return reviews.stream()
            .map(review -> {
                Optional<Member> memberOpt = memberDAO.findById(review.getMemberId());
                Optional<Products> productOpt = productDAO.findById(review.getProductId());
                Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public int incrementHelpfulCount(Long reviewId) {
        return reviewDAO.incrementHelpfulCount(reviewId);
    }
    
    @Override
    @Transactional
    public int incrementReportCount(Long reviewId) {
        return reviewDAO.incrementReportCount(reviewId);
    }
    
    @Override
    @Transactional
    public int updateStatus(Long reviewId, Long statusCodeId) {
        return reviewDAO.updateStatus(reviewId, statusCodeId);
    }
    
    @Override
    @Transactional
    public int updateProductRating(Long productId) {
        return reviewDAO.updateProductRating(productId);
    }
    
    @Override
    @Transactional
    public Review createReview(Review review) {
        // 비즈니스 로직: 주문 존재 여부 확인 (구매 인증)
        Optional<Order> orderOpt = orderDAO.findById(review.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 주문입니다.");
        }
        
        // 비즈니스 로직: 이미 리뷰가 작성되었는지 확인 (주문 ID + 상품 ID로 체크)
        Optional<Review> existingReview = reviewDAO.findByOrderIdAndProductId(review.getOrderId(), review.getProductId());
        if (existingReview.isPresent()) {
            throw new BusinessValidationException("이미 해당 상품에 대한 리뷰가 작성된 주문입니다.");
        }
        
        // 비즈니스 로직: 배송완료된 주문만 리뷰 작성 가능
        Order order = orderOpt.get();
        Long deliveredCodeId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
        if (deliveredCodeId == null) {
            throw new IllegalStateException("DELIVERED 코드가 존재하지 않습니다.");
        }
        if (!order.getOrderStatusId().equals(deliveredCodeId)) {
            throw new BusinessValidationException("배송완료된 주문만 리뷰를 작성할 수 있습니다.");
        }
        
        // status가 설정되지 않았다면 ACTIVE로 설정
        if (review.getStatus() == null) {
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            review.setStatus(activeStatusId);
        }
        
        // 초기값 설정
        if (review.getHelpfulCount() == null) {
            review.setHelpfulCount(0);
        }
        if (review.getReportCount() == null) {
            review.setReportCount(0);
        }
        
        Long reviewId = reviewDAO.save(review);
        
        // 상품 평균 평점 업데이트
        updateProductRating(review.getProductId());
        
        return reviewDAO.findById(reviewId)
            .orElseThrow(() -> new BusinessValidationException("리뷰 저장 중 오류가 발생했습니다."));
    }
    
    @Override
    @Transactional
    public int updateReview(Long reviewId, Review review, Long memberId) {
        // 비즈니스 로직: 리뷰 존재 여부 확인
        Optional<Review> existingReview = reviewDAO.findById(reviewId);
        if (existingReview.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 리뷰입니다.");
        }
        
        // 비즈니스 로직: 작성자 본인인지 확인
        Review originalReview = existingReview.get();
        if (!originalReview.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("리뷰 작성자만 수정할 수 있습니다.");
        }
        
        // 비즈니스 로직: 활성 상태인지 확인
        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        if (!originalReview.getStatus().equals(activeStatusId)) {
            throw new BusinessValidationException("삭제되거나 숨김 리뷰는 수정할 수 없습니다.");
        }
        
        int result = reviewDAO.updateById(reviewId, review);
        
        // 상품 평균 평점 업데이트
        if (result > 0) {
            updateProductRating(review.getProductId());
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public int deleteReview(Long reviewId, Long memberId, boolean isAdmin) {
        // 비즈니스 로직: 리뷰 존재 여부 확인
        Optional<Review> existingReview = reviewDAO.findById(reviewId);
        if (existingReview.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 리뷰입니다.");
        }
        
        // 비즈니스 로직: 권한 확인 (작성자 본인 또는 관리자)
        Review review = existingReview.get();
        if (!isAdmin && !review.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("리뷰 작성자 또는 관리자만 삭제할 수 있습니다.");
        }
        
        // 논리 삭제 (상태를 DELETED로 변경)
        Long deletedStatusId = codeSVC.getCodeId("REVIEW_STATUS", "DELETED");
        int result = reviewDAO.updateStatus(reviewId, deletedStatusId);
        
        // 상품 평균 평점 업데이트
        if (result > 0) {
            updateProductRating(review.getProductId());
        }
        
        return result;
    }

    @Override
    public Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId) {
        return reviewDAO.findByOrderIdAndProductId(orderId, productId);
    }
    
    @Override
    public List<Review> findByProductIdAndStatus(Long productId, Long statusCodeId) {
        return reviewDAO.findByProductIdAndStatus(productId, statusCodeId);
    }
    
    @Override
    public Optional<Review> findByIdAndStatus(Long reviewId, Long statusCodeId) {
        return reviewDAO.findByIdAndStatus(reviewId, statusCodeId);
    }
} 