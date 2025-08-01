package com.kh.demo.admin.controller.api;

import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.report.svc.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 관리자 대시보드 API 컨트롤러
 * 
 * 관리자 top 메뉴에서 사용하는 카운트 정보를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final OrderService orderService;
    private final ReportService reportService;
    private final CodeSVC codeSVC;

    /**
     * 관리자 대시보드 카운트 정보 조회
     * GET /api/admin/dashboard/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardCounts() {
        log.info("관리자 대시보드 카운트 정보 조회 요청");

        try {

            // 시스템 알림 개수 (reportService에서 가져오기)
            int systemAlertCount = reportService.getSystemAlertCount();
            // 처리 대기 신고 개수 (PENDING + PROCESSING 상태)
            int pendingReportCount = reportService.countByStatus("PENDING") + 
                                   reportService.countByStatus("PROCESSING");

            // 처리 대기 주문 개수 (PENDING + PROCESSING 상태)
            Long pendingStatusId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
            Long processingStatusId = codeSVC.getCodeId("ORDER_STATUS", "PROCESSING");
            int pendingOrderCount = orderService.countOrdersByStatus(pendingStatusId) +
                orderService.countOrdersByStatus(processingStatusId);

            Map<String, Object> counts = Map.of(
                "pendingOrderCount", pendingOrderCount,
                "pendingReportCount", pendingReportCount,
                "systemAlertCount", systemAlertCount
            );

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, counts));

        } catch (Exception e) {
            log.error("관리자 대시보드 카운트 정보 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 
                            Map.of("error", "카운트 정보 조회 중 오류가 발생했습니다.")));
        }
    }

    /**
     * 처리 대기 주문 개수 조회
     * GET /api/admin/dashboard/pending-orders/count
     */
    @GetMapping("/pending-orders/count")
    public ResponseEntity<ApiResponse<Integer>> getPendingOrderCount() {
        log.info("처리 대기 주문 개수 조회 요청");

        try {
            Long pendingStatusId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
            int count = orderService.countOrdersByStatus(pendingStatusId);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));

        } catch (Exception e) {
            log.error("처리 대기 주문 개수 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }

    /**
     * 신고 처리 개수 조회
     * GET /api/admin/dashboard/reports/count
     */
    @GetMapping("/reports/count")
    public ResponseEntity<ApiResponse<Integer>> getPendingReportCount() {
        log.info("신고 처리 개수 조회 요청");

        try {
            int count = reportService.countByStatus("PENDING");
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));

        } catch (Exception e) {
            log.error("신고 처리 개수 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }

    /**
     * 시스템 알림 개수 조회
     * GET /api/admin/dashboard/system-alerts/count
     */
    @GetMapping("/system-alerts/count")
    public ResponseEntity<ApiResponse<Integer>> getSystemAlertCount() {
        log.info("시스템 알림 개수 조회 요청");

        try {
            // 임시로 0 반환 (실제 시스템 알림 기능 구현 시 수정)
            int count = 0;
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));

        } catch (Exception e) {
            log.error("시스템 알림 개수 조회 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }


} 