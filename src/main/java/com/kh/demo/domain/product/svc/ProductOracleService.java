package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.dao.UploadFileDAO;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Oracle 데이터베이스 관련 상품 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOracleService {

    private final ProductDAO productDAO;
    private final UploadFileDAO uploadFileDAO;
    private final CodeSVC codeSVC;

    // 파일 타입 그룹 코드
    private static final String FILE_TYPE_GCODE = "FILE_TYPE";

    /**
     * 상품 저장 (Oracle)
     */
    @Transactional
    public Long save(Products products) {
        validateProduct(products);
        
        // Set default values
        if (products.getCdate() == null) {
            products.setCdate(LocalDateTime.now());
        }
        if (products.getUdate() == null) {
            products.setUdate(LocalDateTime.now());
        }

        Long productId = productDAO.save(products);
        log.info("상품 등록 완료 - Oracle: {}", productId);
        return productId;
    }

    /**
     * 상품 수정 (Oracle)
     */
    @Transactional
    public int updateById(Long productId, Products products) {
        validateProduct(products);
        products.setUdate(LocalDateTime.now());
        
        int result = productDAO.updateById(productId, products);
        log.info("상품 수정 완료 - Oracle: {}", productId);
        return result;
    }

    /**
     * 상품 삭제 (Oracle)
     */
    @Transactional
    public int deleteById(Long productId) {
        // 파일 삭제
        deleteProductFiles(productId);
        
        int result = productDAO.deleteById(productId);
        log.info("상품 삭제 완료 - Oracle: {}", productId);
        return result;
    }

    /**
     * 상품 조회
     */
    public Optional<Products> findById(Long productId) {
        return productDAO.findById(productId);
    }

    /**
     * 전체 상품 조회
     */
    public List<Products> findAll() {
        return productDAO.findAll();
    }

    /**
     * 페이징 상품 조회
     */
    public List<Products> findAll(int pageNo, int numOfRows) {
        return productDAO.findAllWithPaging(pageNo, numOfRows);
    }

    /**
     * 전체 상품 수 조회
     */
    public int getTotalCount() {
        return productDAO.getTotalCount();
    }

    /**
     * 상품 이미지 조회
     */
    public List<UploadFile> findProductImages(Long productId) {
        Long imageCodeId = getFileTypeCodeId("PRODUCT_IMAGE");
        return uploadFileDAO.findByCodeAndRid(imageCodeId, productId.toString());
    }

    /**
     * 상품 매뉴얼 조회
     */
    public List<UploadFile> findProductManuals(Long productId) {
        Long manualCodeId = getFileTypeCodeId("PRODUCT_MANUAL");
        return uploadFileDAO.findByCodeAndRid(manualCodeId, productId.toString());
    }

    /**
     * 상품 파일 저장
     */
    @Transactional
    public void saveProductFiles(Long productId, List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        if (imageFiles != null) {
            Long imageCodeId = getFileTypeCodeId("PRODUCT_IMAGE");
            for (UploadFile file : imageFiles) {
                file.setCode(imageCodeId);
                file.setRid(productId.toString());
                uploadFileDAO.save(file);
            }
        }
        
        if (manualFiles != null) {
            Long manualCodeId = getFileTypeCodeId("PRODUCT_MANUAL");
            for (UploadFile file : manualFiles) {
                file.setCode(manualCodeId);
                file.setRid(productId.toString());
                uploadFileDAO.save(file);
            }
        }
    }

    /**
     * 상품 파일 삭제
     */
    @Transactional
    public void deleteProductFiles(Long productId) {
        List<UploadFile> imageFiles = findProductImages(productId);
        List<UploadFile> manualFiles = findProductManuals(productId);
        
        for (UploadFile file : imageFiles) {
            uploadFileDAO.deleteById(file.getUploadfileId());
        }
        
        for (UploadFile file : manualFiles) {
            uploadFileDAO.deleteById(file.getUploadfileId());
        }
    }

    /**
     * 상품 검증
     */
    private void validateProduct(Products products) {
        ValidationUtils.notEmpty(products.getPname(), "상품명");
        ValidationUtils.notEmpty(products.getDescription(), "상품설명");
        ValidationUtils.notEmpty(products.getCategory(), "카테고리");
        
        if (products.getPrice() == null || products.getPrice() < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        
        if (products.getStockQuantity() == null || products.getStockQuantity() < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
    }

    /**
     * 재고 감소
     */
    @Transactional
    public int decreaseStock(Long productId, Integer quantity) {
        return productDAO.decreaseStock(productId, quantity);
    }

    /**
     * 재고 증가
     */
    @Transactional
    public int increaseStock(Long productId, Integer quantity) {
        return productDAO.increaseStock(productId, quantity);
    }

    /**
     * 카테고리별 상품 조회 (페이징)
     */
    public List<Products> findByCategory(String category, int page, int size) {
        log.info("Oracle 카테고리별 상품 조회 - category: {}, page: {}, size: {}", category, page, size);
        return productDAO.findByCategory(category, page, size);
    }

    /**
     * 카테고리별 상품 개수 조회
     */
    public int countByCategory(String category) {
        log.info("Oracle 카테고리별 상품 개수 조회 - category: {}", category);
        return productDAO.countByCategory(category);
    }

    /**
     * 파일 타입 코드 ID 조회
     */
    private Long getFileTypeCodeId(String code) {
        Long codeId = codeSVC.getCodeId(FILE_TYPE_GCODE, code);
        if (codeId != null) {
            return codeId;
        } else {
            log.error("파일 타입 코드를 찾을 수 없습니다: {}", code);
            throw new IllegalArgumentException("파일 타입 코드를 찾을 수 없습니다: " + code);
        }
    }
} 