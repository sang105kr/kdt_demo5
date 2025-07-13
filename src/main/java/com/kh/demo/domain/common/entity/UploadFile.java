package com.kh.demo.domain.common.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UploadFile extends BaseEntity {
    private Long uploadfileId;      // 파일아이디
    private Long code;              // 분류코드 (code_id 참조)
    private String rid;             // 참조번호
    private String storeFilename;   // 서버보관파일명
    private String uploadFilename;  // 업로드파일명
    private String fsize;           // 업로드파일크기
    private String ftype;           // 파일유형
} 