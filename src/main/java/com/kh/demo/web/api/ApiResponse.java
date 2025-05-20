package com.kh.demo.web.api;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Getter
@ToString
public class ApiResponse<T> {
  private final Header header;    //응답헤더
  private final T body;           //응답바디
  private final Paging paging;    //페이지정보

  private ApiResponse(Header header, T body) {
    this.header = header;
    this.body = body;
    this.paging = null;
  }

  private ApiResponse(Header header, T body, Paging paging) {
    this.header = header;
    this.body = body;
    this.paging = paging;
  }

  // 1. 기본 헤더 (details가 없는 경우)
  @Getter
  @ToString
  private static class Header {
    private final String rtcd;      //응답코드
    private final String rtmsg;     //응답메시지
    private final Map<String, String> details;

    Header(String rtmsg, String rtcd, Map<String, String> details) {
      this.rtmsg = rtmsg;
      this.rtcd = rtcd;
      this.details = details;
    }
  }

  @Getter
  @ToString
  private static class Paging {
    private int numOfRows;    //레코드건수
    private int pageNo;       //요청페이지
    private int totalCount;   //총건수

    Paging(int numOfRows, int pageNo, int totalCount) {
      this.numOfRows = numOfRows;
      this.pageNo = pageNo;
      this.totalCount = totalCount;
    }
  }

  // API 응답 생성 메소드-상세 오류 미포함
  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null),
        body, null);
  }

  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null),
        body, paging);
  }

  // API 응답 생성 메소드-상세 오류 포함
  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details),
        body, null);
  }

  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details),
        body, paging);
  }
}
