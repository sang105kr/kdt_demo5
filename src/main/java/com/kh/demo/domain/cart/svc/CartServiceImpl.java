package com.kh.demo.domain.cart.svc;

import com.kh.demo.domain.cart.dao.CartDAO;
import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.web.exception.BusinessException;
import com.kh.demo.web.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    
    @Override
    public Long addToCart(Long memberId, Long productId, Integer quantity) {
        // 상품 존재 여부 확인
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            throw ErrorCode.PRODUCT_NOT_FOUND.toException(details);
        }
        
        Products product = productOpt.get();
        
        // 재고 확인 (null이면 재고 정보가 없는 것으로 간주)
        Integer stockQuantity = product.getStockQuantity();
        if (stockQuantity == null) {
            log.warn("상품 재고 정보가 null입니다: productId={}", product.getProductId());
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            details.put("productName", product.getPname());
            throw ErrorCode.INVALID_STOCK_INFO.toException(details);
        }
        
        if (stockQuantity < quantity) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            details.put("productName", product.getPname());
            details.put("requestedQuantity", quantity);
            details.put("availableStock", stockQuantity);
            throw ErrorCode.INSUFFICIENT_STOCK.toException(details);
        }
        
        // 기존 장바구니 아이템 확인
        Optional<CartItem> existingItem = cartDAO.findByMemberIdAndProductId(memberId, productId);
        
        if (existingItem.isPresent()) {
            // 기존 아이템이 있으면 수량 추가
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            // 재고 확인 (null이면 재고 정보가 없는 것으로 간주)
            Integer existingStockQuantity = product.getStockQuantity();
            if (existingStockQuantity == null) {
                log.warn("상품 재고 정보가 null입니다: productId={}", product.getProductId());
                Map<String, Object> details = new HashMap<>();
                details.put("productId", productId);
                details.put("productName", product.getPname());
                throw ErrorCode.INVALID_STOCK_INFO.toException(details);
            }
            
            if (existingStockQuantity < newQuantity) {
                Map<String, Object> details = new HashMap<>();
                details.put("productId", productId);
                details.put("productName", product.getPname());
                details.put("requestedQuantity", newQuantity);
                details.put("availableStock", existingStockQuantity);
                details.put("existingCartQuantity", item.getQuantity());
                throw ErrorCode.INSUFFICIENT_STOCK.toException(details);
            }
            
            cartDAO.updateQuantity(item.getCartItemId(), newQuantity);
            return item.getCartItemId();
        } else {
            // 새 아이템 추가
            // 1. 회원의 장바구니 ID 조회 (없으면 생성)
            Optional<Long> cartIdOpt = cartDAO.findCartIdByMemberId(memberId);
            Long cartId;
            
            if (cartIdOpt.isPresent()) {
                cartId = cartIdOpt.get();
            } else {
                cartId = cartDAO.createCart(memberId);
            }
            
            // 2. 새 장바구니 아이템 생성
            CartItem cartItem = new CartItem();
            cartItem.setCartId(cartId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            
            // 가격 정보 설정 (시나리오1: 장바구니 추가 시점의 가격 정보 저장)
            BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());
            cartItem.setOriginalPrice(originalPrice);
            cartItem.setSalePrice(originalPrice); // 기본적으로 원가와 동일
            cartItem.setDiscountRate(BigDecimal.ZERO); // 할인율 0%
            cartItem.calculateTotalPrice();
            
            return cartDAO.save(cartItem);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long memberId) {
        return cartDAO.findByMemberId(memberId);
    }
    
    @Override
    public boolean updateQuantity(Long memberId, Long cartItemId, Integer quantity) {
        log.info("CartService.updateQuantity 호출: memberId={}, cartItemId={}, quantity={}", memberId, cartItemId, quantity);
        
        // 장바구니 아이템 확인
        Optional<CartItem> cartItemOpt = cartDAO.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            log.warn("장바구니 아이템을 찾을 수 없음: cartItemId={}", cartItemId);
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        log.info("찾은 장바구니 아이템: {}", cartItem);
        
        // 본인의 장바구니인지 확인
        Optional<Long> cartMemberIdOpt = cartDAO.findMemberIdByCartId(cartItem.getCartId());
        if (cartMemberIdOpt.isEmpty() || !cartMemberIdOpt.get().equals(memberId)) {
            log.warn("본인의 장바구니가 아님: cartMemberId={}, requestMemberId={}, cartMemberIdOpt.orElse(null)={}, memberId={}", memberId, cartMemberIdOpt.orElse(null), memberId);
            return false;
        }
        
        // 상품 재고 확인
        Optional<Products> productOpt = productDAO.findById(cartItem.getProductId());
        if (productOpt.isEmpty()) {
            log.warn("상품을 찾을 수 없음: productId={}", cartItem.getProductId());
            return false;
        }
        
        Products product = productOpt.get();
        log.info("상품 재고 확인: productId={}, stockQuantity={}, requestQuantity={}", product.getProductId(), product.getStockQuantity(), quantity);
        
        // 재고가 null인 경우 처리
        Integer stockQuantity = product.getStockQuantity();
        if (stockQuantity == null) {
            log.warn("상품 재고 정보가 null입니다: productId={}", product.getProductId());
            Map<String, Object> details = new HashMap<>();
            details.put("productId", cartItem.getProductId());
            details.put("productName", product.getPname());
            throw ErrorCode.INVALID_STOCK_INFO.toException(details);
        }
        
        if (stockQuantity < quantity) {
            log.warn("재고 부족: stockQuantity={}, requestQuantity={}", stockQuantity, quantity);
            Map<String, Object> details = new HashMap<>();
            details.put("productId", cartItem.getProductId());
            details.put("productName", product.getPname());
            details.put("requestedQuantity", quantity);
            details.put("availableStock", stockQuantity);
            throw ErrorCode.INSUFFICIENT_STOCK.toException(details);
        }
        
        // 수량 업데이트
        int updatedRows = cartDAO.updateQuantity(cartItemId, quantity);
        log.info("수량 업데이트 결과: updatedRows={}", updatedRows);
        return updatedRows > 0;
    }
    
    @Override
    public boolean removeFromCart(Long memberId, Long cartItemId) {
        // 장바구니 아이템 확인
        Optional<CartItem> cartItemOpt = cartDAO.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        
        // 본인의 장바구니인지 확인
        Optional<Long> cartMemberIdOpt = cartDAO.findMemberIdByCartId(cartItem.getCartId());
        if (cartMemberIdOpt.isEmpty() || !cartMemberIdOpt.get().equals(memberId)) {
            return false;
        }
        
        // 삭제
        int deletedRows = cartDAO.deleteById(cartItemId);
        return deletedRows > 0;
    }
    
    @Override
    public boolean clearCart(Long memberId) {
        int deletedRows = cartDAO.deleteByMemberId(memberId);
        return deletedRows > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long memberId) {
        return cartDAO.countByMemberId(memberId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getCartTotalAmount(Long memberId) {
        return cartDAO.getTotalAmountByMemberId(memberId);
    }
    
    @Override
    public boolean applyDiscount(Long memberId, Long cartItemId, Double discountRate) {
        // 할인율 유효성 검사
        if (discountRate < 0.0 || discountRate > 1.0) {
            Map<String, Object> details = new HashMap<>();
            details.put("cartItemId", cartItemId);
            details.put("discountRate", discountRate);
            details.put("validRange", "0.0 ~ 1.0");
            throw ErrorCode.INVALID_INPUT.toException(details);
        }
        
        // 장바구니 아이템 확인
        Optional<CartItem> cartItemOpt = cartDAO.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        
        // 본인의 장바구니인지 확인
        Optional<Long> cartMemberIdOpt = cartDAO.findMemberIdByCartId(cartItem.getCartId());
        if (cartMemberIdOpt.isEmpty() || !cartMemberIdOpt.get().equals(memberId)) {
            return false;
        }
        
        // 할인 적용
        BigDecimal originalPrice = cartItem.getOriginalPrice();
        BigDecimal newSalePrice = originalPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(discountRate)));
        
        int updatedRows = cartDAO.updatePriceInfo(cartItemId, 
                newSalePrice.longValue(), 
                originalPrice.longValue(), 
                discountRate);
        
        return updatedRows > 0;
    }
    
    @Override
    public Optional<CartItem> getCartItemById(Long cartItemId) {
        return cartDAO.findById(cartItemId);
    }
} 