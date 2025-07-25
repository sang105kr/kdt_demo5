package com.kh.demo.admin.controller.page;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.svc.FileUploadService;
import com.kh.demo.admin.form.product.DetailForm;
import com.kh.demo.admin.form.product.SaveForm;
import com.kh.demo.admin.form.product.UpdateForm;
import com.kh.demo.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kh.demo.domain.common.entity.Code;

/**
 * 관리자 상품관리 컨트롤러
 * - 관리자 웹 인터페이스 (SSR)
 * - 파일 업로드 UI 포함
 * - 폼 검증 및 에러 처리
 * - Thymeleaf 템플릿 렌더링
 * 
 * vs AdminApiProductController: 웹 UI vs REST API
 */
@Slf4j
@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;
    private final FileUploadService fileUploadService;
    
    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 카테고리 목록을 모든 요청에 자동으로 추가 (하위 카테고리만)
     */
    @ModelAttribute("categories")
    public List<Code> categories() {
        return codeSVC.findActiveSubCodesByGcode("PRODUCT_CATEGORY");
    }

    /**
     * 카테고리명 매핑을 모든 요청에 자동으로 추가
     */
    @ModelAttribute("categoryNames")
    public Map<String, String> categoryNames() {
        Map<String, String> categoryNames = new HashMap<>();
        List<Code> subCategories = codeSVC.findActiveSubCodesByGcode("PRODUCT_CATEGORY");
        for (var cat : subCategories) {
            categoryNames.put(cat.getCode(), cat.getDecode());
        }
        return categoryNames;
    }

    /**
     * 상품 목록 페이지
     * GET /admin/product
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int pageNo,
                      @RequestParam(defaultValue = "10") int numOfRows,
                      Model model) {
        log.info("관리자 상품 목록 페이지 요청 - pageNo: {}, numOfRows: {}", pageNo, numOfRows);
        
        // 상품 목록 조회 (Oracle)
        List<Products> products = productService.findAll(pageNo, numOfRows);
        int totalCount = productService.getTotalCount();
        
        model.addAttribute("products", products);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("pageSize", numOfRows);
        
        return "admin/product/list";
    }

    /**
     * 상품 등록 폼 페이지
     * GET /admin/product/add
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        log.info("관리자 상품 등록 폼 페이지 요청");
        model.addAttribute("saveForm", new SaveForm());
        return "admin/product/addForm";
    }

    /**
     * 상품 등록 처리 (파일 업로드 포함)
     * POST /admin/product/add
     */
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute SaveForm saveForm,
                     BindingResult bindingResult,
                     @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                     @RequestParam(value = "manualFiles", required = false) List<MultipartFile> manualFiles,
                     RedirectAttributes redirectAttributes,
                     Model model) {
        log.info("관리자 상품 등록 요청: {}", saveForm);
        
        if (bindingResult.hasErrors()) {
            log.warn("상품 등록 폼 검증 실패: {}", bindingResult.getAllErrors());
            
            // 검증 실패 시 파일 정보를 모델에 추가하여 폼에 유지
            if (imageFiles != null && !imageFiles.isEmpty()) {
                model.addAttribute("tempImageFiles", imageFiles);
                model.addAttribute("hasImageFiles", true);
            }
            if (manualFiles != null && !manualFiles.isEmpty()) {
                model.addAttribute("tempManualFiles", manualFiles);
                model.addAttribute("hasManualFiles", true);
            }
            
            return "admin/product/addForm";
        }
        
        try {
            // SaveForm을 Products 엔티티로 변환
            Products products = new Products();
            products.setPname(saveForm.getPname());
            products.setDescription(saveForm.getDescription());
            products.setPrice(saveForm.getPrice());
            products.setCategory(saveForm.getCategory());
            products.setStockQuantity(saveForm.getStockQuantity());
            
            // 파일 처리
            List<UploadFile> uploadImageFiles = fileUploadService.uploadMultipleFiles(imageFiles, "image", uploadPath);
            List<UploadFile> uploadManualFiles = fileUploadService.uploadMultipleFiles(manualFiles, "manual", uploadPath);
            
            // 상품 등록 (Oracle + Elasticsearch 동기화 + 파일 첨부)
            Long productId = productService.save(products, uploadImageFiles, uploadManualFiles);
            
            String successMessage = messageSource.getMessage("product.create.success", null, null);
            redirectAttributes.addFlashAttribute("message", successMessage + " (상품번호: " + productId + ")");
            return "redirect:/admin/product";
            
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("product.create.failed", null, null);
            bindingResult.reject("global", errorMessage + ": " + e.getMessage());
            
            // 예외 발생 시에도 파일 정보를 모델에 추가하여 폼에 유지
            if (imageFiles != null && !imageFiles.isEmpty()) {
                model.addAttribute("tempImageFiles", imageFiles);
                model.addAttribute("hasImageFiles", true);
            }
            if (manualFiles != null && !manualFiles.isEmpty()) {
                model.addAttribute("tempManualFiles", manualFiles);
                model.addAttribute("hasManualFiles", true);
            }
            
            return "admin/product/addForm";
        }
    }

    /**
     * 상품 상세 페이지 (파일 정보 포함)
     * GET /admin/product/{productId}
     */
    @GetMapping("/{productId}")
    public String detail(@PathVariable Long productId, Model model) {
        log.info("관리자 상품 상세 페이지 요청 - productId: {}", productId);
        
        Optional<Products> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            throw ErrorCode.PRODUCT_NOT_FOUND.toException(details);
        }
        
        Products product = productOpt.get();
        DetailForm detailForm = new DetailForm();
        detailForm.setProductId(product.getProductId());
        detailForm.setPname(product.getPname());
        detailForm.setDescription(product.getDescription());
        detailForm.setPrice(product.getPrice());
        detailForm.setCategory(product.getCategory());
        detailForm.setStockQuantity(product.getStockQuantity());
        detailForm.setCdate(product.getCdate());
        detailForm.setUdate(product.getUdate());
        
        // 파일 정보 조회
        List<UploadFile> imageFiles = productService.findProductImages(productId);
        List<UploadFile> manualFiles = productService.findProductManuals(productId);
        
        model.addAttribute("detailForm", detailForm);
        model.addAttribute("imageFiles", imageFiles);
        model.addAttribute("manualFiles", manualFiles);
        return "admin/product/detailForm";
    }

    /**
     * 상품 수정 폼 페이지
     * GET /admin/product/{productId}/edit
     */
    @GetMapping("/{productId}/edit")
    public String editForm(@PathVariable Long productId, Model model) {
        log.info("관리자 상품 수정 폼 페이지 요청 - productId: {}", productId);
        
        Optional<Products> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("상품을 찾을 수 없습니다: " + productId);
        }
        
        Products product = productOpt.get();
        UpdateForm updateForm = new UpdateForm();
        updateForm.setProductId(product.getProductId());
        updateForm.setPname(product.getPname());
        updateForm.setDescription(product.getDescription());
        updateForm.setPrice(product.getPrice());
        updateForm.setCategory(product.getCategory());
        updateForm.setStockQuantity(product.getStockQuantity());
        
        model.addAttribute("updateForm", updateForm);
        model.addAttribute("product", product);
        
        // 기존 파일 정보 추가
        List<UploadFile> existingImageFiles = productService.findProductImages(productId);
        List<UploadFile> existingManualFiles = productService.findProductManuals(productId);
        model.addAttribute("existingImageFiles", existingImageFiles);
        model.addAttribute("existingManualFiles", existingManualFiles);
        
        return "admin/product/editForm";
    }

    /**
     * 상품 수정 처리 (파일 업로드/삭제 포함)
     * POST /admin/product/{productId}/edit
     */
    @PostMapping("/{productId}/edit")
    public String edit(@PathVariable Long productId,
                      @Valid @ModelAttribute UpdateForm updateForm,
                      BindingResult bindingResult,
                      @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                      @RequestParam(value = "manualFiles", required = false) List<MultipartFile> manualFiles,
                      @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                      @RequestParam(value = "deleteManualIds", required = false) List<Long> deleteManualIds,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        log.info("관리자 상품 수정 요청 - productId: {}, updateForm: {}", productId, updateForm);
        
        if (bindingResult.hasErrors()) {
            log.warn("상품 수정 폼 검증 실패: {}", bindingResult.getAllErrors());
            
            // 상품 정보 조회하여 폼에 유지
            Optional<Products> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                Products product = productOpt.get();
                UpdateForm form = new UpdateForm();
                form.setPname(product.getPname());
                form.setDescription(product.getDescription());
                form.setPrice(product.getPrice());
                form.setCategory(product.getCategory());
                form.setStockQuantity(product.getStockQuantity());
                model.addAttribute("updateForm", form);
                
                // 파일 정보도 추가
                List<UploadFile> existingImageFiles = productService.findProductImages(productId);
                List<UploadFile> existingManualFiles = productService.findProductManuals(productId);
                model.addAttribute("existingImageFiles", existingImageFiles);
                model.addAttribute("existingManualFiles", existingManualFiles);
            }
            
            return "admin/product/editForm";
        }
        
        try {
            // UpdateForm을 Products 엔티티로 변환
            Products products = new Products();
            products.setPname(updateForm.getPname());
            products.setDescription(updateForm.getDescription());
            products.setPrice(updateForm.getPrice());
            products.setCategory(updateForm.getCategory());
            products.setStockQuantity(updateForm.getStockQuantity());
            
            // 파일 처리
            List<UploadFile> uploadImageFiles = fileUploadService.uploadMultipleFiles(imageFiles, "image", uploadPath);
            List<UploadFile> uploadManualFiles = fileUploadService.uploadMultipleFiles(manualFiles, "manual", uploadPath);
            
            // 상품 수정 (Oracle + Elasticsearch 동기화 + 파일 첨부/삭제)
            int updatedRows = productService.updateById(productId, products, uploadImageFiles, uploadManualFiles, 
                                                      deleteImageIds, deleteManualIds);
            
            if (updatedRows > 0) {
                String successMessage = messageSource.getMessage("product.update.success", null, null);
                redirectAttributes.addFlashAttribute("message", successMessage);
            } else {
                String errorMessage = messageSource.getMessage("product.update.failed", null, null);
                redirectAttributes.addFlashAttribute("error", errorMessage);
            }
            
            return "redirect:/admin/product";
            
        } catch (Exception e) {
            log.error("상품 수정 실패: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("product.update.failed", null, null);
            bindingResult.reject("global", errorMessage + ": " + e.getMessage());
            
            // 상품 정보 조회하여 폼에 유지
            Optional<Products> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                Products product = productOpt.get();
                UpdateForm form = new UpdateForm();
                form.setPname(product.getPname());
                form.setDescription(product.getDescription());
                form.setPrice(product.getPrice());
                form.setCategory(product.getCategory());
                form.setStockQuantity(product.getStockQuantity());
                model.addAttribute("updateForm", form);
                
                // 파일 정보도 추가
                List<UploadFile> existingImageFiles = productService.findProductImages(productId);
                List<UploadFile> existingManualFiles = productService.findProductManuals(productId);
                model.addAttribute("existingImageFiles", existingImageFiles);
                model.addAttribute("existingManualFiles", existingManualFiles);
            }
            
            return "admin/product/editForm";
        }
    }

    /**
     * 상품 삭제 처리
     * POST /admin/product/{productId}/delete
     */
    @PostMapping("/{productId}/delete")
    public String delete(@PathVariable Long productId,
                        RedirectAttributes redirectAttributes) {
        log.info("관리자 상품 삭제 요청 - productId: {}", productId);
        
        try {
            // 상품 삭제 (Oracle + Elasticsearch 동기화 + 파일 삭제)
            int deletedRows = productService.deleteById(productId);
            
            if (deletedRows > 0) {
                String successMessage = messageSource.getMessage("product.delete.success", null, null);
                redirectAttributes.addFlashAttribute("message", successMessage);
            } else {
                String errorMessage = messageSource.getMessage("product.delete.failed", null, null);
                redirectAttributes.addFlashAttribute("error", errorMessage);
            }
            
        } catch (Exception e) {
            log.error("상품 삭제 실패: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("product.delete.failed", null, null);
            redirectAttributes.addFlashAttribute("error", errorMessage + ": " + e.getMessage());
        }
        
        return "redirect:/admin/product";
    }

    /**
     * 상품 다중 삭제 처리
     * POST /admin/product/delete-multiple
     */
    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam("productIds") List<Long> productIds,
                                RedirectAttributes redirectAttributes) {
        log.info("관리자 상품 다중 삭제 요청 - productIds: {}", productIds);
        
        if (productIds == null || productIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "삭제할 상품을 선택해주세요.");
            return "redirect:/admin/product";
        }
        
        try {
            // 상품 다중 삭제 (Oracle + Elasticsearch 동기화 + 파일 삭제)
            int deletedRows = productService.deleteByIds(productIds);
            
            if (deletedRows > 0) {
                String successMessage = messageSource.getMessage("product.delete.multiple.success", null, null);
                redirectAttributes.addFlashAttribute("message", successMessage + " (삭제된 상품: " + deletedRows + "건)");
            } else {
                String errorMessage = messageSource.getMessage("product.delete.multiple.failed", null, null);
                redirectAttributes.addFlashAttribute("error", errorMessage);
            }
            
        } catch (Exception e) {
            log.error("상품 다중 삭제 실패: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("product.delete.multiple.failed", null, null);
            redirectAttributes.addFlashAttribute("error", errorMessage + ": " + e.getMessage());
        }
        
        return "redirect:/admin/product";
    }
}
