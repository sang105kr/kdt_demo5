package com.kh.demo.web.form.product;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateForm {
  private Long productId;

  @NotBlank(message = "상품명은 필수 입니다.")
  @Size(min=1,max=10,message = "상품은 10자를 초과할 수 없습니다.")
  private String pname;

  @NotNull(message="수량은 필수 입니다.")
  @Positive(message="수량은 양수여야 합니다.")
  @Max(value=9999999999L, message = "수량은 10자리 이하여야 합니다.")
  private Long quantity;

  @NotNull(message="가격은 필수 입니다.")
  @Positive(message="가격은 양수여야 합니다.")
  @Min(value=100, message = "가격은 100미만 불가합니다.")
  @Max(value=9999999999L, message = "가격은 10자리 이하여야 합니다.")
  private Long price;
}
