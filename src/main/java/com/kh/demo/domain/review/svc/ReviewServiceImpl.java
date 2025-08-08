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
import com.kh.demo.domain.notification.svc.NotificationSVC;
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
    private final ReviewCommentService reviewCommentService;
    private final MemberDAO memberDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final CodeSVC codeSVC;  // CodeSVC 대신 CodeCache 사용
    private final NotificationSVC notificationSVC;
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
        
        // 댓글 개수 조회
        int commentCount = reviewCommentService.countByReviewId(reviewId);
        
        ReviewDetailVO detailVO = ReviewDetailVO.builder()
            .review(review)
            .member(memberOpt.orElse(null))
            .product(productOpt.orElse(null))
            .order(orderOpt.orElse(null))
            .commentCount(commentCount)
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
                
                // 댓글 개수 조회
                int commentCount = reviewCommentService.countByReviewId(review.getReviewId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .commentCount(commentCount)
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
                
                // 댓글 개수 조회
                int commentCount = reviewCommentService.countByReviewId(review.getReviewId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .commentCount(commentCount)
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
    public Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId) {
        return reviewDAO.findByOrderIdAndProductId(orderId, productId);
    }
    
    @Override
    public Optional<Review> findActiveByOrderIdAndProductId(Long orderId, Long productId) {
        return reviewDAO.findActiveByOrderIdAndProductId(orderId, productId);
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
        
        // 댓글 개수 조회
        int commentCount = reviewCommentService.countByReviewId(review.getReviewId());
        
        ReviewDetailVO detailVO = ReviewDetailVO.builder()
            .review(review)
            .member(memberOpt.orElse(null))
            .product(productOpt.orElse(null))
            .order(orderOpt.orElse(null))
            .commentCount(commentCount)
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
                
                // 댓글 개수 조회
                int commentCount = reviewCommentService.countByReviewId(review.getReviewId());
                
                return ReviewDetailVO.builder()
                    .review(review)
                    .member(memberOpt.orElse(null))
                    .product(productOpt.orElse(null))
                    .order(orderOpt.orElse(null))
                    .commentCount(commentCount)
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
        
        // 비즈니스 로직: 이미 리뷰가 작성되었는지 확인 (주문 ID + 상품 ID로 체크) - ACTIVE 상태만 확인
        Optional<Review> existingReview = reviewDAO.findActiveByOrderIdAndProductId(review.getOrderId(), review.getProductId());
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
        if (review.getStatusId() == null) {
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            review.setStatusId(activeStatusId);
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
        
        // 상품 리뷰 개수 업데이트
        productDAO.updateReviewCount(review.getProductId());
        
        // 리뷰 작성 알림 생성
        try {
            // 상품 정보 조회
            Optional<Products> productOpt = productDAO.findById(review.getProductId());
            if (productOpt.isPresent()) {
                Products product = productOpt.get();
                notificationSVC.createReviewNotification(
                    review.getMemberId(),
                    "리뷰가 작성되었습니다",
                    String.format("'%s' 상품에 대한 리뷰가 성공적으로 작성되었습니다.", product.getPname()),
                    review.getProductId()
                );
                log.info("리뷰 작성 알림 생성 - reviewId: {}, memberId: {}, productId: {}", 
                        reviewId, review.getMemberId(), review.getProductId());
            }
        } catch (Exception e) {
            log.error("리뷰 작성 알림 생성 실패 - reviewId: {}, memberId: {}, error: {}", 
                    reviewId, review.getMemberId(), e.getMessage());
        }
        
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
        if (!originalReview.getStatusId().equals(activeStatusId)) {
            throw new BusinessValidationException("삭제되거나 숨김 리뷰는 수정할 수 없습니다.");
        }
        
        int result = reviewDAO.updateById(reviewId, review);
        
        // 상품 평균 평점 업데이트
        if (result > 0) {
            updateProductRating(review.getProductId());
            // 상품 리뷰 개수 업데이트
            productDAO.updateReviewCount(review.getProductId());
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
            // 상품 리뷰 개수 업데이트
            productDAO.updateReviewCount(review.getProductId());
        }
        
        return result;
    }

    @Override
    public List<Review> findByProductIdAndStatus(Long productId, Long statusCodeId) {
        return reviewDAO.findByProductIdAndStatus(productId, statusCodeId);
    }
    
    @Override
    public Optional<Review> findByIdAndStatus(Long reviewId, Long statusCodeId) {
        return reviewDAO.findByIdAndStatus(reviewId, statusCodeId);
    }
    
    @Override
    public boolean hasUserReviewedProduct(Long memberId, Long productId) {
        log.info("사용자 리뷰 작성 여부 확인 - memberId: {}, productId: {}", memberId, productId);
        
        // ACTIVE 상태의 리뷰만 확인
        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        if (activeStatusId == null) {
            log.error("REVIEW_STATUS ACTIVE 코드를 찾을 수 없습니다.");
            return false;
        }
        
        // 해당 사용자가 해당 상품에 대해 작성한 ACTIVE 상태의 리뷰가 있는지 확인
        List<Review> userReviews = reviewDAO.findByMemberIdAndProductIdAndStatus(memberId, productId, activeStatusId);
        boolean hasReviewed = !userReviews.isEmpty();
        
        log.info("사용자 리뷰 작성 여부 확인 결과 - memberId: {}, productId: {}, hasReviewed: {}", 
                memberId, productId, hasReviewed);
        
        return hasReviewed;
    }
    
    // === 관리자용 페이징 및 검색 메서드들 ===
    
    @Override
    public List<Review> findAllWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return reviewDAO.findAllWithPaging(offset, pageSize);
    }
    
    @Override
    public List<Review> findByStatusWithPaging(Long statusId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return reviewDAO.findByStatusWithPaging(statusId, offset, pageSize);
    }
    
    @Override
    public List<Review> findByKeywordWithPaging(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return reviewDAO.findByKeywordWithPaging(keyword, offset, pageSize);
    }
    
    @Override
    public List<Review> findByDateRangeWithPaging(String startDate, String endDate, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return reviewDAO.findByDateRangeWithPaging(startDate, endDate, offset, pageSize);
    }
    
    // === 카운트 메서드들 ===
    
    @Override
    public int countAll() {
        return reviewDAO.countAll();
    }
    
    @Override
    public int countByStatus(Long statusId) {
        return reviewDAO.countByStatus(statusId);
    }
    
    @Override
    public int countByKeyword(String keyword) {
        return reviewDAO.countByKeyword(keyword);
    }
    
    @Override
    public int countByDateRange(String startDate, String endDate) {
        return reviewDAO.countByDateRange(startDate, endDate);
    }
    
    @Override
    @Transactional
    public int reportReview(Long reviewId, Long memberId, String reason) {
        log.info("리뷰 신고 처리 - reviewId: {}, memberId: {}, reason: {}", reviewId, memberId, reason);
        
        try {
            // 리뷰 존재 여부 확인
            Optional<Review> reviewOpt = reviewDAO.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                log.warn("존재하지 않는 리뷰 신고 시도 - reviewId: {}", reviewId);
                return 0;
            }
            
            Review review = reviewOpt.get();
            
            // 자신의 리뷰는 신고할 수 없음
            if (review.getMemberId().equals(memberId)) {
                log.warn("자신의 리뷰 신고 시도 - reviewId: {}, memberId: {}", reviewId, memberId);
                throw new IllegalArgumentException("자신의 리뷰는 신고할 수 없습니다.");
            }
            
            // 신고 수 증가
            int result = reviewDAO.incrementReportCount(reviewId);
            
            if (result > 0) {
                log.info("리뷰 신고 성공 - reviewId: {}, memberId: {}, reason: {}", reviewId, memberId, reason);
                
                // TODO: 신고 내역을 reports 테이블에 저장하는 로직 추가
                // reportService.createReport(memberId, "REVIEW", reviewId, reason);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("리뷰 신고 처리 실패 - reviewId: {}, memberId: {}", reviewId, memberId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public int reportComment(Long commentId, Long memberId, String reason) {
        log.info("댓글 신고 처리 - commentId: {}, memberId: {}, reason: {}", commentId, memberId, reason);
        
        try {
            // 댓글 존재 여부 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                log.warn("존재하지 않는 댓글 신고 시도 - commentId: {}", commentId);
                return 0;
            }
            
            ReviewComment comment = commentOpt.get();
            
            // 자신의 댓글은 신고할 수 없음
            if (comment.getMemberId().equals(memberId)) {
                log.warn("자신의 댓글 신고 시도 - commentId: {}, memberId: {}", commentId, memberId);
                throw new IllegalArgumentException("자신의 댓글은 신고할 수 없습니다.");
            }
            
            // 신고 수 증가
            int result = reviewCommentService.incrementReportCount(commentId);
            
            if (result > 0) {
                log.info("댓글 신고 성공 - commentId: {}, memberId: {}, reason: {}", commentId, memberId, reason);
                
                // TODO: 신고 내역을 reports 테이블에 저장하는 로직 추가
                // reportService.createReport(memberId, "REVIEW_COMMENT", commentId, reason);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("댓글 신고 처리 실패 - commentId: {}, memberId: {}", commentId, memberId, e);
            throw e;
        }
    }
} 
