package com.kh.demo.web.api;

import lombok.Data;

@Data
public class UpdateApi {
  private String pname;
  private Long quantity;
  private Long price;
}
