package com.kh.demo.web.page;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.page.form.member.JoinForm;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.web.page.form.member.MypageForm;
import com.kh.demo.web.page.form.member.PasswordChangeForm;
import com.kh.demo.web.page.form.member.PasswordResetForm;
import com.kh.demo.web.page.form.member.EmailVerificationForm;
import com.kh.demo.web.page.form.member.FindIdForm;
import com.kh.demo.web.page.form.member.ProfileImageForm;
import com.kh.demo.web.page.form.member.MemberDTO;
import com.kh.demo.web.page.form.login.LoginMember;
import com.kh.demo.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import java.sql.Blob;
import java.sql.SQLException;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.web.page.form.review.ReviewForm;
import com.kh.demo.web.page.form.review.ReviewCommentForm;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.order.dto.OrderDTO;
import java.util.Arrays;

@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberController extends BaseController {

  private final MemberSVC memberSVC;
  private final CodeSVC codeSVC;
  private final MessageSource messageSource;
  private final OrderService orderService;
  private final ReviewService reviewService;
  private final ProductService productService;
  private final ReviewCommentService reviewCommentService;

  @ModelAttribute("regionCodes")
  public List<Code> regionCodes() {
    return codeSVC.findByGcode("REGION").stream()
        .filter(c -> c.getPcode() == 0L)
        .findFirst()
        .map(parent -> codeSVC.findByPcode(parent.getCodeId()))
        .orElse(List.of());
  }

  @ModelAttribute("genderCodes")
  public List<Code> genderCodes() {
    return codeSVC.findByGcode("GENDER");
  }

  @ModelAttribute("hobbyCodes")
  public List<Code> hobbyCodes() {
    return codeSVC.findByGcode("HOBBY").stream()
        .filter(c -> c.getPcode() == 0L)
        .findFirst()
        .map(parent -> codeSVC.findByPcode(parent.getCodeId()))
        .orElse(List.of());
  }

  /**
   * Member 엔티티를 MemberDTO로 변환
   */
  private MemberDTO convertToMemberDTO(Member member) {
    MemberDTO dto = new MemberDTO();
    dto.setMemberId(member.getMemberId());
    dto.setEmail(member.getEmail());
    dto.setTel(member.getTel());
    dto.setNickname(member.getNickname());
    dto.setGender(member.getGender());
    dto.setBirthDate(member.getBirthDate());
    dto.setHobby(member.getHobby());
    dto.setRegion(member.getRegion());
    dto.setGubun(member.getGubun());
    dto.setPic(member.getPic());
    dto.setHasProfileImage(member.getPic() != null && member.getPic().length > 0);
    
    // 성별 디코드
    if (member.getGender() != null) {
      // gender는 "M", "F" 문자열이므로 직접 매핑
      if ("M".equals(member.getGender())) {
        dto.setGenderName("남성");
      } else if ("F".equals(member.getGender())) {
        dto.setGenderName("여성");
      } else {
        dto.setGenderName(member.getGender());
      }
    }
    
    // 지역 디코드
    if (member.getRegion() != null) {
      Optional<Code> regionCode = codeSVC.findById(member.getRegion());
      if (regionCode.isPresent()) {
        dto.setRegionName(regionCode.get().getDecode());
      }
    }
    
    // 회원구분 디코드
    if (member.getGubun() != null) {
      Optional<Code> gubunCode = codeSVC.findById(member.getGubun());
      if (gubunCode.isPresent()) {
        dto.setGubunName(gubunCode.get().getDecode());
      }
    }
    
    // 취미 디코드
    if (member.getHobby() != null && !member.getHobby().isEmpty()) {
      List<String> hobbyNames = new ArrayList<>();
      String[] hobbyCodes = member.getHobby().split(",");
      for (String hobbyCode : hobbyCodes) {
        try {
          Long codeId = Long.valueOf(hobbyCode.trim());
          Optional<Code> hobbyCodeInfo = codeSVC.findById(codeId);
          if (hobbyCodeInfo.isPresent()) {
            hobbyNames.add(hobbyCodeInfo.get().getDecode());
          }
        } catch (NumberFormatException e) {
          log.warn("취미 코드 변환 실패: {}", hobbyCode);
        }
      }
      dto.setHobbyNames(hobbyNames);
    }
    
    return dto;
  }

  //회원가입 화면
  @GetMapping("/join")
  public String joinForm(Model model) {
    model.addAttribute("joinForm", new JoinForm());
    return "member/joinForm";
  }

  //회원가입 처리
  @PostMapping("/join")
  public String join(@Valid @ModelAttribute JoinForm joinForm,
                     BindingResult bindingResult,
                     Model model,
                     RedirectAttributes redirectAttributes) {
    log.info("joinForm={}", joinForm);

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = getMessage("member.validation.error");
      model.addAttribute("errorMessage", errorMessage);
      return "member/joinForm";
    }

    //2) 회원가입 처리
    try {
      Member member = new Member();
      BeanUtils.copyProperties(joinForm, member);

      // hobby: List<String> → String(콤마구분)
      if (joinForm.getHobby() != null) {
        member.setHobby(String.join(",", joinForm.getHobby()));
      }
      // gender는 그대로 복사됨
      Member savedMember = memberSVC.join(member);
      log.info("회원가입 성공: memberId={}", savedMember.getMemberId());
      String successMessage = getMessage("member.join.success");
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/login";

    } catch (Exception e) {
      log.error("회원가입 실패", e);
      String errorMessage = getMessage("member.join.failed");
      model.addAttribute("errorMessage", errorMessage);
      return "member/joinForm";
    }
  }

  // 마이페이지 메인
  @GetMapping("/mypage")
  public String mypage(HttpSession session, Model model, HttpServletRequest request) {
    log.info("마이페이지 요청");
    log.info("현재 URI: {}", request.getRequestURI());
    log.info("'/member/mypage' 포함 여부: {}", request.getRequestURI().contains("/member/mypage"));
    
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
    if (memberOpt.isEmpty()) {
      return "redirect:/login";
    }


    Member member = memberOpt.get();
    
    // Member 엔티티를 MemberDTO로 변환
    MemberDTO memberDTO = convertToMemberDTO(member);
    model.addAttribute("member", memberDTO);
    return "member/mypage";
  }

  // 회원정보 수정 화면
  @GetMapping("/mypage/edit")
  public String editForm(HttpSession session, Model model) {
    log.info("회원정보 수정 화면 요청");
    
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      log.warn("로그인되지 않은 사용자 접근");
      return "redirect:/login";
    }

    log.info("로그인된 회원: memberId={}, email={}", loginMember.getMemberId(), loginMember.getEmail());

    Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
    if (memberOpt.isEmpty()) {
      log.warn("회원 정보를 찾을 수 없음: memberId={}", loginMember.getMemberId());
      return "redirect:/login";
    }

    Member member = memberOpt.get();
    log.info("회원 정보 조회 성공: member={}", member);
    log.info("회원 생년월일: {}", member.getBirthDate());
    log.info("회원 지역: {}", member.getRegion());
    log.info("회원 취미: {}", member.getHobby());
    
    MypageForm mypageForm = new MypageForm();
    BeanUtils.copyProperties(member, mypageForm);
    log.info("MypageForm 생년월일: {}", mypageForm.getBirthDate());
    log.info("MypageForm 지역: {}", mypageForm.getRegion());
    log.info("MypageForm 취미: {}", mypageForm.getHobby());
    
    // hobby: String → List<Long>
    if (member.getHobby() != null && !member.getHobby().isEmpty()) {
      List<Long> hobbyList = List.of(member.getHobby().split(","))
          .stream()
          .map(Long::valueOf)
          .toList();
      mypageForm.setHobby(hobbyList);
    }

    log.info("MypageForm 생성 완료: mypageForm={}", mypageForm);
    model.addAttribute("mypageForm", mypageForm);
    
    log.info("회원정보 수정 화면 렌더링 시작");
    return "member/editForm";
  }

  // 회원정보 수정 처리
  @PostMapping("/mypage/edit")
  public String edit(@Valid @ModelAttribute MypageForm mypageForm,
                     BindingResult bindingResult,
                     HttpSession session,
                     Model model,
                     RedirectAttributes redirectAttributes) {
    log.info("mypageForm={}", mypageForm);
    log.info("수정 요청 생년월일: {}", mypageForm.getBirthDate());
    log.info("수정 요청 지역: {}", mypageForm.getRegion());
    log.info("수정 요청 취미: {}", mypageForm.getHobby());

    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = getMessage("member.validation.error");
      model.addAttribute("errorMessage", errorMessage);
      return "member/editForm";
    }

    //2) 회원정보 수정 처리
    try {
      // 기존 회원 정보 조회
      Optional<Member> existingMemberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
      if (existingMemberOpt.isEmpty()) {
        throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
      }
      
      Member existingMember = existingMemberOpt.get();
      
      // 수정할 필드만 업데이트 (비밀번호, 이메일, gubun, pic는 유지)
      existingMember.setTel(mypageForm.getTel());
      existingMember.setNickname(mypageForm.getNickname());
      existingMember.setGender(mypageForm.getGender());
      existingMember.setBirthDate(mypageForm.getBirthDate());
      existingMember.setRegion(mypageForm.getRegion());
      
      log.info("수정된 생년월일: {}", existingMember.getBirthDate());
      log.info("수정된 지역: {}", existingMember.getRegion());

      // hobby: List<Long> → String(콤마구분)
      if (mypageForm.getHobby() != null && !mypageForm.getHobby().isEmpty()) {
        String hobbyString = mypageForm.getHobby().stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b)
            .orElse("");
        existingMember.setHobby(hobbyString);
        log.info("수정된 취미: {}", existingMember.getHobby());
      } else {
        existingMember.setHobby(null);
        log.info("수정된 취미: null");
      }

      memberSVC.updateMember(existingMember.getMemberId(), existingMember);
      log.info("회원정보 수정 성공: memberId={}", existingMember.getMemberId());
      
      // 세션 정보 업데이트
      LoginMember updatedLoginMember = new LoginMember(
          existingMember.getMemberId(),
          existingMember.getEmail(),
          existingMember.getNickname(),
          existingMember.getGubun(),
          existingMember.getPic() != null && existingMember.getPic().length > 0
      );
      session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
      
      String successMessage = getMessage("member.edit.success");
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/member/mypage";

    } catch (Exception e) {
      log.error("회원정보 수정 실패", e);
      String errorMessage = getMessage("member.edit.failed");
      model.addAttribute("errorMessage", errorMessage);
      return "member/editForm";
    }
  }

  // 비밀번호 변경 화면
  @GetMapping("/mypage/password")
  public String passwordForm(Model model) {
    model.addAttribute("passwordChangeForm", new PasswordChangeForm());
    return "member/passwordForm";
  }

  // 비밀번호 변경 처리
  @PostMapping("/mypage/password")
  public String changePassword(@Valid @ModelAttribute PasswordChangeForm passwordChangeForm,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
    log.info("passwordChangeForm={}", passwordChangeForm);

    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = getMessage("member.validation.error");
      model.addAttribute("errorMessage", errorMessage);
      return "member/passwordForm";
    }

    //2) 비밀번호 변경 처리
    try {
      memberSVC.changePasswd(loginMember.getMemberId(), 
                              passwordChangeForm.getCurrentPassword(), 
                              passwordChangeForm.getNewPassword());
      
      log.info("비밀번호 변경 성공: memberId={}", loginMember.getMemberId());
      String successMessage = getMessage("member.password.change.success");
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/member/mypage";

    } catch (Exception e) {
      log.error("비밀번호 변경 실패", e);
      String errorMessage = getMessage("member.password.change.failed");
      model.addAttribute("errorMessage", errorMessage);
      return "member/passwordForm";
    }
  }

  // 회원 탈퇴 화면
  @GetMapping("/mypage/withdraw")
  public String withdrawForm() {
    return "member/withdrawForm";
  }

  // 회원 탈퇴 처리
  @PostMapping("/mypage/withdraw")
  public String withdraw(HttpSession session, RedirectAttributes redirectAttributes) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      memberSVC.deleteMember(loginMember.getMemberId());
      session.invalidate();
      
      log.info("회원 탈퇴 성공: memberId={}", loginMember.getMemberId());
      String successMessage = getMessage("member.withdraw.success");
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/";

    } catch (Exception e) {
      log.error("회원 탈퇴 실패", e);
      String errorMessage = getMessage("member.withdraw.failed");
      redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage";
    }
  }

  // 주문 내역 조회
  @GetMapping("/mypage/orders")
  public String orderHistory(@RequestParam(required = false) String orderStatus,
                           HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      // OrderDTO 사용으로 변경 - 주문과 상품 정보를 함께 조회
      List<OrderDTO> orders;
      
      if (orderStatus != null && !orderStatus.isEmpty()) {
        // 주문 상태별 필터링
        orders = orderService.findDTOByMemberId(loginMember.getMemberId())
            .stream()
            .filter(order -> orderStatus.equals(order.getOrderStatus()))
            .toList();
        model.addAttribute("selectedStatus", orderStatus);
      } else {
        orders = orderService.findDTOByMemberId(loginMember.getMemberId());
      }
      
      log.info("마이페이지 주문 목록 조회 - memberId: {}, 조회된 주문 개수: {}, 필터: {}", 
              loginMember.getMemberId(), orders.size(), orderStatus);
      
      model.addAttribute("orders", orders);
      
      // 필터링 옵션 추가
      model.addAttribute("statusFilters", Arrays.asList("전체", "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"));
      
      return "member/orderHistory";
    } catch (Exception e) {
      log.error("주문 내역 조회 실패", e);
      String errorMessage = getMessage("member.order.history.failed");
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage";
    }
  }

  // 주문 상세 조회 - OrderDTO 사용으로 변경
  @GetMapping("/mypage/orders/{orderId}")
  public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      // OrderDTO 사용으로 변경
      Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
      if (orderOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.order.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders";
      }

      OrderDTO order = orderOpt.get();
      
      // 본인의 주문인지 확인
      if (!order.getMemberId().equals(loginMember.getMemberId())) {
        String errorMessage = messageSource.getMessage("member.order.access.denied", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders";
      }

      log.info("마이페이지 주문 상세 조회 - orderId: {}, orderNumber: {}", 
              orderId, order.getOrderNumber());
      log.info("주문 상품 개수: {}", order.getOrderItems() != null ? order.getOrderItems().size() : 0);

      model.addAttribute("order", order);
      return "member/orderDetail";
    } catch (Exception e) {
      log.error("주문 상세 조회 실패", e);
      String errorMessage = messageSource.getMessage("member.order.detail.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/orders";
    }
  }

  // 주문 취소 기능 추가
  @PostMapping("/mypage/orders/{orderId}/cancel")
  public String cancelOrder(@PathVariable Long orderId, HttpSession session, RedirectAttributes redirectAttributes) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      // 주문 조회하여 본인 주문인지 확인
      Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
      if (orderOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("errorMessage", "주문을 찾을 수 없습니다.");
        return "redirect:/member/mypage/orders";
      }

      OrderDTO order = orderOpt.get();
      if (!order.getMemberId().equals(loginMember.getMemberId())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인의 주문만 취소할 수 있습니다.");
        return "redirect:/member/mypage/orders";
      }

      // 주문 취소 실행
      orderService.cancelOrder(orderId);
      
      log.info("주문 취소 완료 - orderId: {}, memberId: {}", orderId, loginMember.getMemberId());
      redirectAttributes.addFlashAttribute("successMessage", "주문이 취소되었습니다.");
      
    } catch (Exception e) {
      log.error("주문 취소 실패 - orderId: {}", orderId, e);
      redirectAttributes.addFlashAttribute("errorMessage", "주문 취소에 실패했습니다.");
    }

    return "redirect:/member/mypage/orders";
  }

  // 리뷰 내역 조회
  @GetMapping("/mypage/reviews")
  public String reviewHistory(HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      List<Review> reviews = reviewService.findByMemberId(loginMember.getMemberId());
      model.addAttribute("reviews", reviews);
      return "member/reviewHistory";
    } catch (Exception e) {
      log.error("리뷰 내역 조회 실패", e);
      String errorMessage = messageSource.getMessage("member.review.history.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage";
    }
  }

  // 리뷰 상세 조회
  @GetMapping("/mypage/reviews/{reviewId}")
  public String reviewDetail(@PathVariable Long reviewId, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      Optional<Review> reviewOpt = reviewService.findById(reviewId);
      if (reviewOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.review.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      Review review = reviewOpt.get();
      // 본인의 리뷰인지 확인
      if (!review.getMemberId().equals(loginMember.getMemberId())) {
        String errorMessage = messageSource.getMessage("member.review.access.denied", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      // 리뷰 댓글 목록 조회
      List<ReviewComment> comments = reviewService.findCommentsByReviewId(reviewId);
      model.addAttribute("review", review);
      model.addAttribute("comments", comments);
      return "member/reviewDetail";
    } catch (Exception e) {
      log.error("리뷰 상세 조회 실패", e);
      String errorMessage = messageSource.getMessage("member.review.detail.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews";
    }
  }

  // 리뷰 작성 폼
  @GetMapping("/mypage/reviews/write")
  public String reviewWriteForm(@RequestParam Long productId, @RequestParam Long orderId, 
                               HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      // 주문 정보 조회하여 본인 주문인지 확인
      Optional<Order> orderOpt = orderService.findByOrderId(orderId);
      if (orderOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.order.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders";
      }

      Order order = orderOpt.get();
      if (!order.getMemberId().equals(loginMember.getMemberId())) {
        String errorMessage = messageSource.getMessage("member.order.access.denied", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders";
      }

      // 이미 리뷰가 작성되었는지 확인
      Optional<Review> existingReview = reviewService.findByOrderId(orderId);
      if (existingReview.isPresent()) {
        String errorMessage = messageSource.getMessage("member.review.already.exists", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders/" + orderId;
      }

      // 상품 정보 조회
      Optional<Products> productOpt = productService.findById(productId);
      if (productOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.product.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/orders";
      }

      model.addAttribute("product", productOpt.get());
      model.addAttribute("order", order);
      model.addAttribute("reviewForm", new ReviewForm());
      return "member/reviewWriteForm";
    } catch (Exception e) {
      log.error("리뷰 작성 폼 로드 실패", e);
      String errorMessage = messageSource.getMessage("member.review.write.form.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/orders";
    }
  }

  // 리뷰 작성 처리
  @PostMapping("/mypage/reviews/write")
  public String reviewWrite(@Valid @ModelAttribute ReviewForm reviewForm, 
                           BindingResult bindingResult, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
      // 상품과 주문 정보를 다시 조회하여 폼에 표시
      try {
        Optional<Products> productOpt = productService.findById(reviewForm.getProductId());
        Optional<Order> orderOpt = orderService.findByOrderId(reviewForm.getOrderId());
        
        if (productOpt.isPresent()) model.addAttribute("product", productOpt.get());
        if (orderOpt.isPresent()) model.addAttribute("order", orderOpt.get());
        
        return "member/reviewWriteForm";
      } catch (Exception e) {
        return "redirect:/member/mypage/orders";
      }
    }

    try {
      Review review = new Review();
      review.setProductId(reviewForm.getProductId());
      review.setOrderId(reviewForm.getOrderId());
      review.setMemberId(loginMember.getMemberId());
      review.setTitle(reviewForm.getTitle());
      review.setContent(reviewForm.getContent());
      review.setRating(reviewForm.getRating());
      review.setStatus("ACTIVE");

      Review savedReview = reviewService.createReview(review);
      
      String successMessage = messageSource.getMessage("member.review.write.success", null, null);
      model.addAttribute("successMessage", successMessage);
      
      return "redirect:/member/mypage/reviews/" + savedReview.getReviewId();
    } catch (Exception e) {
      log.error("리뷰 작성 실패", e);
      String errorMessage = messageSource.getMessage("member.review.write.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      
      // 폼 데이터 복원
      try {
        Optional<Products> productOpt = productService.findById(reviewForm.getProductId());
        Optional<Order> orderOpt = orderService.findByOrderId(reviewForm.getOrderId());
        
        if (productOpt.isPresent()) model.addAttribute("product", productOpt.get());
        if (orderOpt.isPresent()) model.addAttribute("order", orderOpt.get());
      } catch (Exception ex) {
        return "redirect:/member/mypage/orders";
      }
      
      return "member/reviewWriteForm";
    }
  }

  // 리뷰 수정 폼
  @GetMapping("/mypage/reviews/{reviewId}/edit")
  public String reviewEditForm(@PathVariable Long reviewId, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      Optional<Review> reviewOpt = reviewService.findById(reviewId);
      if (reviewOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.review.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      Review review = reviewOpt.get();
      // 본인의 리뷰인지 확인
      if (!review.getMemberId().equals(loginMember.getMemberId())) {
        String errorMessage = messageSource.getMessage("member.review.access.denied", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      // 수정 불가능한 상태인지 확인
      if (!"ACTIVE".equals(review.getStatus())) {
        String errorMessage = messageSource.getMessage("member.review.edit.not.allowed", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews/" + reviewId;
      }

      ReviewForm reviewForm = new ReviewForm();
      reviewForm.setProductId(review.getProductId());
      reviewForm.setOrderId(review.getOrderId());
      reviewForm.setTitle(review.getTitle());
      reviewForm.setContent(review.getContent());
      reviewForm.setRating(review.getRating());

      model.addAttribute("review", review);
      model.addAttribute("reviewForm", reviewForm);
      return "member/reviewEditForm";
    } catch (Exception e) {
      log.error("리뷰 수정 폼 로드 실패", e);
      String errorMessage = messageSource.getMessage("member.review.edit.form.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews";
    }
  }

  // 리뷰 수정 처리
  @PostMapping("/mypage/reviews/{reviewId}/edit")
  public String reviewEdit(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewForm reviewForm,
                          BindingResult bindingResult, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
      try {
        Optional<Review> reviewOpt = reviewService.findById(reviewId);
        if (reviewOpt.isPresent()) {
          model.addAttribute("review", reviewOpt.get());
        }
        return "member/reviewEditForm";
      } catch (Exception e) {
        return "redirect:/member/mypage/reviews";
      }
    }

    try {
      Review review = new Review();
      review.setTitle(reviewForm.getTitle());
      review.setContent(reviewForm.getContent());
      review.setRating(reviewForm.getRating());

      int result = reviewService.updateReview(reviewId, review, loginMember.getMemberId());
      
      if (result > 0) {
        String successMessage = messageSource.getMessage("member.review.edit.success", null, null);
        model.addAttribute("successMessage", successMessage);
        return "redirect:/member/mypage/reviews/" + reviewId;
      } else {
        String errorMessage = messageSource.getMessage("member.review.edit.failed", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews/" + reviewId;
      }
    } catch (Exception e) {
      log.error("리뷰 수정 실패", e);
      String errorMessage = messageSource.getMessage("member.review.edit.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews/" + reviewId;
    }
  }

  // 리뷰 삭제 처리
  @PostMapping("/mypage/reviews/{reviewId}/delete")
  public String reviewDelete(@PathVariable Long reviewId, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      int result = reviewService.deleteReview(reviewId, loginMember.getMemberId(), false);
      
      if (result > 0) {
        String successMessage = messageSource.getMessage("member.review.delete.success", null, null);
        model.addAttribute("successMessage", successMessage);
        return "redirect:/member/mypage/reviews";
      } else {
        String errorMessage = messageSource.getMessage("member.review.delete.failed", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews/" + reviewId;
      }
    } catch (Exception e) {
      log.error("리뷰 삭제 실패", e);
      String errorMessage = messageSource.getMessage("member.review.delete.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews/" + reviewId;
    }
  }

  // 리뷰 댓글 작성 처리
  @PostMapping("/mypage/reviews/{reviewId}/comments")
  public String reviewCommentWrite(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewCommentForm commentForm,
                                  BindingResult bindingResult, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
      String errorMessage = messageSource.getMessage("member.review.comment.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews/" + reviewId;
    }

    try {
      // 리뷰 존재 여부 확인
      Optional<Review> reviewOpt = reviewService.findById(reviewId);
      if (reviewOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.review.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      ReviewComment comment = new ReviewComment();
      comment.setReviewId(reviewId);
      comment.setContent(commentForm.getContent());
      comment.setStatus("ACTIVE");

      ReviewComment savedComment = reviewCommentService.createComment(comment, loginMember.getMemberId());
      
      String successMessage = messageSource.getMessage("member.review.comment.write.success", null, null);
      model.addAttribute("successMessage", successMessage);
      
      return "redirect:/member/mypage/reviews/" + reviewId;
    } catch (Exception e) {
      log.error("리뷰 댓글 작성 실패", e);
      String errorMessage = messageSource.getMessage("member.review.comment.write.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews/" + reviewId;
    }
  }

  // 리뷰 댓글 수정 처리
  @PostMapping("/mypage/reviews/comments/{commentId}/edit")
  public String reviewCommentEdit(@PathVariable Long commentId, @Valid @ModelAttribute ReviewCommentForm commentForm,
                                 BindingResult bindingResult, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
      String errorMessage = messageSource.getMessage("member.review.comment.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews";
    }

    try {
      // 댓글 존재 여부 및 권한 확인
      Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
      if (commentOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.review.comment.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      ReviewComment existingComment = commentOpt.get();
      if (!existingComment.getMemberId().equals(loginMember.getMemberId())) {
        String errorMessage = messageSource.getMessage("member.review.comment.access.denied", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "redirect:/member/mypage/reviews";
      }

      ReviewComment comment = new ReviewComment();
      comment.setContent(commentForm.getContent());

      int result = reviewCommentService.updateComment(commentId, comment, loginMember.getMemberId());
      
      if (result > 0) {
        String successMessage = messageSource.getMessage("member.review.comment.edit.success", null, null);
        model.addAttribute("successMessage", successMessage);
      } else {
        String errorMessage = messageSource.getMessage("member.review.comment.edit.failed", null, null);
        model.addAttribute("errorMessage", errorMessage);
      }
      
      return "redirect:/member/mypage/reviews/" + existingComment.getReviewId();
    } catch (Exception e) {
      log.error("리뷰 댓글 수정 실패", e);
      String errorMessage = messageSource.getMessage("member.review.comment.edit.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "redirect:/member/mypage/reviews";
    }
  }

  // 리뷰 댓글 삭제 처리
  @PostMapping("/mypage/reviews/comments/{commentId}/delete")
  public String reviewCommentDelete(@PathVariable Long commentId, HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      reviewCommentService.deleteById(commentId);
      String successMessage = messageSource.getMessage("review.comment.delete.success", null, null);
      model.addAttribute("message", successMessage);
    } catch (Exception e) {
      log.error("리뷰 댓글 삭제 실패", e);
      String errorMessage = messageSource.getMessage("review.comment.delete.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
    }

    return "redirect:/member/mypage/reviews";
  }
  
  // 이메일 인증 화면
  @GetMapping("/email/verify")
  public String emailVerificationForm(Model model) {
    model.addAttribute("emailVerificationForm", new EmailVerificationForm());
    return "member/emailVerification";
  }
  
  // 이메일 인증 코드 발송
  @PostMapping("/email/send-code")
  public String sendVerificationCode(@RequestParam String email, 
                                   RedirectAttributes redirectAttributes) {
    try {
      memberSVC.sendVerificationCode(email);
      String successMessage = messageSource.getMessage("email.verification.sent", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
    } catch (Exception e) {
      log.error("이메일 인증 코드 발송 실패", e);
      String errorMessage = messageSource.getMessage("email.verification.send.failed", null, null);
      redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
    }
    
    return "redirect:/member/email/verify";
  }
  
  // 이메일 인증 코드 확인
  @PostMapping("/email/verify")
  public String verifyEmailCode(@Valid @ModelAttribute EmailVerificationForm emailVerificationForm,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
    log.info("emailVerificationForm={}", emailVerificationForm);
    
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = messageSource.getMessage("member.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/emailVerification";
    }
    
    try {
      boolean isVerified = memberSVC.verifyEmailCode(
        emailVerificationForm.getEmail(), 
        emailVerificationForm.getVerificationCode()
      );
      
      if (isVerified) {
        String successMessage = messageSource.getMessage("email.verification.success", null, null);
        redirectAttributes.addFlashAttribute("message", successMessage);
        return "redirect:/login";
      } else {
        String errorMessage = messageSource.getMessage("email.verification.failed", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "member/emailVerification";
      }
    } catch (Exception e) {
      log.error("이메일 인증 실패", e);
      String errorMessage = messageSource.getMessage("email.verification.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/emailVerification";
    }
  }
  
  // 비밀번호 찾기 화면
  @GetMapping("/password/find")
  public String passwordFindForm(Model model) {
    model.addAttribute("passwordResetForm", new PasswordResetForm());
    return "member/passwordFind";
  }
  
  // 비밀번호 재설정 토큰 발송
  @PostMapping("/password/send-token")
  public String sendPasswordResetToken(@RequestParam String email, 
                                     RedirectAttributes redirectAttributes) {
    try {
      memberSVC.sendPasswordResetToken(email);
      String successMessage = messageSource.getMessage("password.reset.sent", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
    } catch (Exception e) {
      log.error("비밀번호 재설정 토큰 발송 실패", e);
      String errorMessage = messageSource.getMessage("password.reset.send.failed", null, null);
      redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
    }
    
    return "redirect:/member/password/find";
  }
  
  // 비밀번호 재설정 화면
  @GetMapping("/password/reset")
  public String passwordResetForm(@RequestParam String token, Model model) {
    PasswordResetForm passwordResetForm = new PasswordResetForm();
    passwordResetForm.setToken(token);
    model.addAttribute("passwordResetForm", passwordResetForm);
    return "member/passwordReset";
  }
  
  // 비밀번호 재설정 처리
  @PostMapping("/password/reset")
  public String passwordReset(@Valid @ModelAttribute PasswordResetForm passwordResetForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
    log.info("passwordResetForm={}", passwordResetForm);
    
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = messageSource.getMessage("member.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/passwordReset";
    }
    
    // 비밀번호 확인 검증
    if (!passwordResetForm.isPasswordMatch()) {
      String errorMessage = messageSource.getMessage("password.confirm.mismatch", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/passwordReset";
    }
    
    try {
      memberSVC.resetPassword(passwordResetForm.getToken(), passwordResetForm.getNewPassword());
      String successMessage = messageSource.getMessage("password.reset.success", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/login";
    } catch (Exception e) {
      log.error("비밀번호 재설정 실패", e);
      String errorMessage = messageSource.getMessage("password.reset.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/passwordReset";
    }
  }
  
  // 아이디 찾기 화면
  @GetMapping("/id/find")
  public String findIdForm(Model model) {
    model.addAttribute("findIdForm", new FindIdForm());
    return "member/findId";
  }
  
  // 아이디 찾기 처리
  @PostMapping("/id/find")
  public String findId(@Valid @ModelAttribute FindIdForm findIdForm,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirectAttributes) {
    log.info("findIdForm={}", findIdForm);
    
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = messageSource.getMessage("member.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/findId";
    }
    
    try {
      // 생년월일을 YYYYMMDD 형식으로 변환
      String birthDateStr = findIdForm.getBirthDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
      
      // 전화번호와 생년월일로 이메일 조회
      Optional<String> emailOpt = memberSVC.findEmailByPhoneAndBirth(findIdForm.getTel(), birthDateStr);
      
      if (emailOpt.isEmpty()) {
        String errorMessage = messageSource.getMessage("member.id.find.not.found", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "member/findId";
      }
      
      String foundEmail = emailOpt.get();
      
      // 회원 정보 조회 (닉네임, 가입일 등 추가 정보용)
      Optional<Member> memberOpt = memberSVC.findByEmail(foundEmail);
      if (memberOpt.isPresent()) {
      Member member = memberOpt.get();
      model.addAttribute("foundNickname", member.getNickname());
      model.addAttribute("joinDate", member.getCdate());
      }
      
      model.addAttribute("foundEmail", foundEmail);
      
      String successMessage = messageSource.getMessage("member.id.find.success", null, null);
      model.addAttribute("message", successMessage);
      
      return "member/findIdResult";
      
    } catch (Exception e) {
      log.error("아이디 찾기 실패", e);
      String errorMessage = messageSource.getMessage("member.id.find.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/findId";
    }
  }
  
  // 프로필 사진 관리 화면
  @GetMapping("/mypage/profile-image")
  public String profileImageForm(HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
    if (memberOpt.isEmpty()) {
      return "redirect:/login";
    }

    Member member = memberOpt.get();
    model.addAttribute("member", member);
    model.addAttribute("profileImageForm", new ProfileImageForm());
    return "member/profileImageForm";
  }
  
  // 프로필 사진 업로드 처리
  @PostMapping("/mypage/profile-image")
  public String uploadProfileImage(@Valid @ModelAttribute ProfileImageForm profileImageForm,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
    log.info("profileImageForm={}", profileImageForm);

    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      String errorMessage = messageSource.getMessage("member.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/profileImageForm";
    }

    //2) 파일 유효성 검증
    MultipartFile file = profileImageForm.getProfileImage();
    if (file.isEmpty()) {
      String errorMessage = messageSource.getMessage("member.profile.image.empty", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/profileImageForm";
    }

    // 파일 크기 검증 (5MB 이하)
    if (file.getSize() > 5 * 1024 * 1024) {
      String errorMessage = messageSource.getMessage("member.profile.image.size", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/profileImageForm";
    }

    // 파일 타입 검증 (이미지 파일만)
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      String errorMessage = messageSource.getMessage("member.profile.image.type", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/profileImageForm";
    }

    //3) 프로필 사진 업로드 처리
    try {
      // 회원 정보 조회
      Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
      if (memberOpt.isEmpty()) {
        return "redirect:/login";
      }

      Member member = memberOpt.get();

      member.setPic(file.getBytes());
      
      // 회원 정보 업데이트
      memberSVC.updateMember(member.getMemberId(), member);
      
      // 세션 업데이트 (프로필 사진 존재 여부)
      LoginMember updatedLoginMember = new LoginMember(
          loginMember.getMemberId(),
          loginMember.getEmail(),
          loginMember.getNickname(),
          loginMember.getGubun(),
          true // 프로필 사진이 업로드되었으므로 true
      );
      session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
      
      String successMessage = messageSource.getMessage("member.profile.image.success", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/member/mypage/profile-image";
      
    } catch (IOException e) {
      log.error("프로필 사진 업로드 실패", e);
      String errorMessage = messageSource.getMessage("member.profile.image.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/profileImageForm";
    }
  }
  
  // 프로필 사진 삭제
  @PostMapping("/mypage/profile-image/delete")
  public String deleteProfileImage(HttpSession session, RedirectAttributes redirectAttributes) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return "redirect:/login";
    }

    try {
      Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
      if (memberOpt.isEmpty()) {
        return "redirect:/login";
      }

      Member member = memberOpt.get();
      member.setPic(null);
      memberSVC.updateMember(member.getMemberId(), member);
      
      // 세션 업데이트 (프로필 사진 존재 여부)
      LoginMember updatedLoginMember = new LoginMember(
          loginMember.getMemberId(),
          loginMember.getEmail(),
          loginMember.getNickname(),
          loginMember.getGubun(),
          false // 프로필 사진이 삭제되었으므로 false
      );
      session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
      
      String successMessage = messageSource.getMessage("member.profile.image.delete.success", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
      
    } catch (Exception e) {
      log.error("프로필 사진 삭제 실패", e);
      String errorMessage = messageSource.getMessage("member.profile.image.delete.failed", null, null);
      redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
    }
    
    return "redirect:/member/mypage/profile-image";
  }
  
  // 프로필 사진 조회 (이미지 표시용)
  @GetMapping("/mypage/profile-image/view")
  public void viewProfileImage(HttpSession session, 
                              jakarta.servlet.http.HttpServletResponse response) {
    LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    if (loginMember == null) {
      return;
    }

    try {
      Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
      if (memberOpt.isEmpty() || memberOpt.get().getPic() == null) {
        return;
      }

      Member member = memberOpt.get();
      byte[] imageBytes = member.getPic();

      response.setContentType("image/jpeg");
      response.setContentLength(imageBytes.length);
      
      try (java.io.OutputStream outputStream = response.getOutputStream()) {
        outputStream.write(imageBytes);
      }
      
    } catch (Exception e) {
      log.error("프로필 사진 조회 실패", e);
    }
  }
} 