package com.kh.demo.domain.wishlist.svc;

import com.kh.demo.domain.wishlist.dao.WishlistDAO;
import com.kh.demo.domain.wishlist.entity.Wishlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 위시리스트 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WishlistSVCImpl implements WishlistSVC {

    private final WishlistDAO wishlistDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByMemberId(Long memberId) {
        log.info("회원별 위시리스트 조회 - memberId: {}", memberId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        return wishlistDAO.findByMemberId(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByMemberId(Long memberId, int pageNo, int pageSize) {
        log.info("회원별 위시리스트 조회 (페이징) - memberId: {}, pageNo: {}, pageSize: {}", 
                memberId, pageNo, pageSize);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        if (pageNo < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        }
        
        if (pageSize < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        
        return wishlistDAO.findByMemberId(memberId, pageNo, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public int getWishlistCountByMemberId(Long memberId) {
        log.info("회원별 위시리스트 개수 조회 - memberId: {}", memberId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        return wishlistDAO.countByMemberId(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long memberId, Long productId) {
        log.debug("위시리스트 존재 여부 확인 - memberId: {}, productId: {}", memberId, productId);
        
        if (memberId == null || productId == null) {
            return false;
        }
        
        return wishlistDAO.existsByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public boolean addToWishlist(Long memberId, Long productId) {
        log.info("위시리스트 추가 - memberId: {}, productId: {}", memberId, productId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        
        try {
            // 이미 존재하는지 확인
            if (wishlistDAO.existsByMemberIdAndProductId(memberId, productId)) {
                log.warn("이미 위시리스트에 존재하는 상품 - memberId: {}, productId: {}", memberId, productId);
                return false;
            }
            
            Long wishlistId = wishlistDAO.addWishlist(memberId, productId);
            log.info("위시리스트 추가 성공 - wishlistId: {}", wishlistId);
            return true;
            
        } catch (Exception e) {
            log.error("위시리스트 추가 실패 - memberId: {}, productId: {}, error: {}", 
                    memberId, productId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeFromWishlist(Long memberId, Long productId) {
        log.info("위시리스트 제거 - memberId: {}, productId: {}", memberId, productId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        
        try {
            int removedCount = wishlistDAO.removeWishlist(memberId, productId);
            boolean success = removedCount > 0;
            
            if (success) {
                log.info("위시리스트 제거 성공 - memberId: {}, productId: {}", memberId, productId);
            } else {
                log.warn("위시리스트에 존재하지 않는 상품 - memberId: {}, productId: {}", memberId, productId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("위시리스트 제거 실패 - memberId: {}, productId: {}, error: {}", 
                    memberId, productId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean toggleWishlist(Long memberId, Long productId) {
        log.info("위시리스트 토글 - memberId: {}, productId: {}", memberId, productId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        
        boolean exists = wishlistDAO.existsByMemberIdAndProductId(memberId, productId);
        
        if (exists) {
            // 존재하면 제거
            boolean removed = removeFromWishlist(memberId, productId);
            return !removed; // 제거 성공하면 false 반환 (위시리스트에 없음)
        } else {
            // 존재하지 않으면 추가
            return addToWishlist(memberId, productId);
        }
    }

    @Override
    public int clearWishlist(Long memberId) {
        log.info("위시리스트 전체 제거 - memberId: {}", memberId);
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        
        try {
            int removedCount = wishlistDAO.removeAllByMemberId(memberId);
            log.info("위시리스트 전체 제거 완료 - memberId: {}, 제거된 항목 수: {}", memberId, removedCount);
            return removedCount;
            
        } catch (Exception e) {
            log.error("위시리스트 전체 제거 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            throw new RuntimeException("위시리스트 제거 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int removeWishlistByProductId(Long productId) {
        log.info("상품 관련 위시리스트 전체 제거 - productId: {}", productId);
        
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        
        try {
            int removedCount = wishlistDAO.removeAllByProductId(productId);
            log.info("상품 관련 위시리스트 전체 제거 완료 - productId: {}, 제거된 항목 수: {}", 
                    productId, removedCount);
            return removedCount;
            
        } catch (Exception e) {
            log.error("상품 관련 위시리스트 제거 실패 - productId: {}, error: {}", productId, e.getMessage());
            throw new RuntimeException("상품 관련 위시리스트 제거 중 오류가 발생했습니다.", e);
        }
    }
} 