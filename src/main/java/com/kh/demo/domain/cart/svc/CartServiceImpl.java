package com.kh.demo.domain.cart.svc;

import com.kh.demo.domain.cart.dao.CartDAO;
import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }
        
        Products product = productOpt.get();
        
        // 재고 확인
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        
        // 기존 장바구니 아이템 확인
        Optional<CartItem> existingItem = cartDAO.findByMemberIdAndProductId(memberId, productId);
        
        if (existingItem.isPresent()) {
            // 기존 아이템이 있으면 수량 추가
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (product.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            
            cartDAO.updateQuantity(item.getCartItemId(), newQuantity);
            return item.getCartItemId();
        } else {
            // 새 아이템 추가
            CartItem cartItem = new CartItem();
            cartItem.setMemberId(memberId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
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
        // 장바구니 아이템 확인
        Optional<CartItem> cartItemOpt = cartDAO.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        
        // 본인의 장바구니인지 확인
        if (!cartItem.getMemberId().equals(memberId)) {
            return false;
        }
        
        // 상품 재고 확인
        Optional<Products> productOpt = productDAO.findById(cartItem.getProductId());
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Products product = productOpt.get();
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        
        // 수량 업데이트
        int updatedRows = cartDAO.updateQuantity(cartItemId, quantity);
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
        if (!cartItem.getMemberId().equals(memberId)) {
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
} 