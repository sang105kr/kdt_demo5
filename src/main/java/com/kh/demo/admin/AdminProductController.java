package com.kh.demo.admin;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.web.page.form.product.DetailForm;
import com.kh.demo.web.page.form.product.SaveForm;
import com.kh.demo.web.page.form.product.UpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 관리자 상품관리 컨트롤러
 * SSR 방식으로 상품 CRUD 기능 제공 + 파일 업로드 처리
 */
@Slf4j
@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final MessageSource messageSource;

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
                     RedirectAttributes redirectAttributes) {
        log.info("관리자 상품 등록 요청: {}", saveForm);
        
        if (bindingResult.hasErrors()) {
            log.warn("상품 등록 폼 검증 실패: {}", bindingResult.getAllErrors());
            return "admin/product/addForm";
        }
        
        try {
            // SaveForm을 Products 엔티티로 변환
            Products products = new Products();
            products.setPname(saveForm.getPname());
            products.setDescription(saveForm.getDescription());
            products.setPrice(saveForm.getPrice());
            products.setRating(saveForm.getRating());
            products.setCategory(saveForm.getCategory());
            products.setStockQuantity(saveForm.getStockQuantity());
            
            // 파일 처리
            List<UploadFile> uploadImageFiles = processUploadFiles(imageFiles, "image");
            List<UploadFile> uploadManualFiles = processUploadFiles(manualFiles, "manual");
            
            // 상품 등록 (Oracle + Elasticsearch 동기화 + 파일 첨부)
            Long productId = productService.save(products, uploadImageFiles, uploadManualFiles);
            
            String successMessage = messageSource.getMessage("product.create.success", null, null);
            redirectAttributes.addFlashAttribute("message", successMessage + " (상품번호: " + productId + ")");
            return "redirect:/admin/product";
            
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("product.create.failed", null, null);
            bindingResult.reject("global", errorMessage + ": " + e.getMessage());
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
            String errorMessage = messageSource.getMessage("product.not.found", new Object[]{productId}, null);
            throw new IllegalArgumentException(errorMessage);
        }
        
        Products product = productOpt.get();
        DetailForm detailForm = new DetailForm();
        detailForm.setProductId(product.getProductId());
        detailForm.setPname(product.getPname());
        detailForm.setDescription(product.getDescription());
        detailForm.setPrice(product.getPrice());
        detailForm.setRating(product.getRating());
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
     * 상품 수정 폼 페이지 (기존 파일 정보 포함)
     * GET /admin/product/{productId}/edit
     */
    @GetMapping("/{productId}/edit")
    public String editForm(@PathVariable Long productId, Model model) {
        log.info("관리자 상품 수정 폼 페이지 요청 - productId: {}", productId);
        
        Optional<Products> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            String errorMessage = messageSource.getMessage("product.not.found", new Object[]{productId}, null);
            throw new IllegalArgumentException(errorMessage);
        }
        
        Products product = productOpt.get();
        UpdateForm updateForm = new UpdateForm();
        updateForm.setProductId(product.getProductId());
        updateForm.setPname(product.getPname());
        updateForm.setDescription(product.getDescription());
        updateForm.setPrice(product.getPrice());
        updateForm.setRating(product.getRating());
        updateForm.setCategory(product.getCategory());
        updateForm.setStockQuantity(product.getStockQuantity());
        
        // 기존 파일 정보 조회
        List<UploadFile> imageFiles = productService.findProductImages(productId);
        List<UploadFile> manualFiles = productService.findProductManuals(productId);
        
        model.addAttribute("updateForm", updateForm);
        model.addAttribute("imageFiles", imageFiles);
        model.addAttribute("manualFiles", manualFiles);
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
                      RedirectAttributes redirectAttributes) {
        log.info("관리자 상품 수정 요청 - productId: {}, updateForm: {}", productId, updateForm);
        
        if (bindingResult.hasErrors()) {
            log.warn("상품 수정 폼 검증 실패: {}", bindingResult.getAllErrors());
            return "admin/product/editForm";
        }
        
        try {
            // UpdateForm을 Products 엔티티로 변환
            Products products = new Products();
            products.setPname(updateForm.getPname());
            products.setDescription(updateForm.getDescription());
            products.setPrice(updateForm.getPrice());
            products.setRating(updateForm.getRating());
            products.setCategory(updateForm.getCategory());
            products.setStockQuantity(updateForm.getStockQuantity());
            
            // 파일 처리
            List<UploadFile> uploadImageFiles = processUploadFiles(imageFiles, "image");
            List<UploadFile> uploadManualFiles = processUploadFiles(manualFiles, "manual");
            
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

    /**
     * 파일 업로드 처리
     */
    private List<UploadFile> processUploadFiles(List<MultipartFile> files, String fileType) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return uploadFiles;
        }
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            try {
                // 파일 저장
                String originalFilename = file.getOriginalFilename();
                String storeFilename = generateStoreFilename(originalFilename);
                String uploadPath = "/uploads";
                
                // 업로드 디렉토리 생성
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // 파일 저장
                Path filePath = Paths.get(uploadPath, storeFilename);
                Files.copy(file.getInputStream(), filePath);
                
                // UploadFile 엔티티 생성
                UploadFile uploadFile = new UploadFile();
                uploadFile.setUploadFilename(originalFilename);
                uploadFile.setStoreFilename(storeFilename);
                uploadFile.setFsize(String.valueOf(file.getSize()));
                uploadFile.setFtype(file.getContentType());
                
                uploadFiles.add(uploadFile);
                log.info("파일 업로드 완료: {} -> {}", originalFilename, storeFilename);
                
            } catch (IOException e) {
                log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }
        
        return uploadFiles;
    }
    
    /**
     * 저장용 파일명 생성
     */
    private String generateStoreFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return timestamp + "_" + uuid + extension;
    }
}
