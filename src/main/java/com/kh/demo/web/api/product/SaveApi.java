package com.kh.demo.web.api.product;

import lombok.Data;

@Data
public class SaveApi {
  private String pname;
  private Long quantity;
  private Long price;
}
