package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 회원 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 회원의 CRUD 및 검색 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements MemberDAO {

    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;  // CodeCache 대신 CodeSVC 사용

    // RowMapper 정의 (스키마 변경사항 반영)
    private final RowMapper<Member> memberRowMapper = (ResultSet rs, int rowNum) -> {
        Member member = new Member();
        
        // member_id 조회 및 설정
        Long memberId = rs.getLong("member_id");
        member.setMemberId(memberId);
        log.debug("RowMapper - member_id 조회: {}", memberId);
        
        member.setEmail(rs.getString("email"));
        member.setPasswd(rs.getString("passwd"));
        member.setTel(rs.getString("tel"));
        member.setNickname(rs.getString("nickname"));
        member.setGender(rs.getLong("gender"));                // String -> Long으로 변경
        member.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
        // hobby 필드 제거됨 (member_hobbies 테이블로 분리)
        member.setRegion(rs.getLong("region"));
        member.setGubun(rs.getLong("gubun"));
        member.setStatus(rs.getLong("status"));
        member.setStatusReason(rs.getString("status_reason"));
        member.setStatusChangedAt(rs.getObject("status_changed_at", LocalDateTime.class));
        member.setPic(rs.getBytes("pic"));
        member.setAddress(rs.getString("address"));
        member.setAddressDetail(rs.getString("address_detail"));
        member.setZipcode(rs.getString("zipcode"));
        member.setCdate(rs.getObject("cdate", LocalDateTime.class));
        member.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        log.debug("RowMapper 완료 - Member 객체: memberId={}, email={}, nickname={}", 
            member.getMemberId(), member.getEmail(), member.getNickname());
        return member;
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Member member) {
        String sql = """
            INSERT INTO member (member_id, email, passwd, tel, nickname, gender, birth_date, region, gubun, status, status_reason, status_changed_at, pic, address, address_detail, zipcode, cdate, udate)
            VALUES (seq_member_id.nextval, :email, :passwd, :tel, :nickname, :gender, :birthDate, :region, :gubun, :status, :statusReason, :statusChangedAt, :pic, :address, :addressDetail, :zipcode, :cdate, :udate)
            """;
        
        // 기본값 설정
        if (member.getGubun() == null) {
            Long defaultGubun = codeSVC.getCodeId("MEMBER_GUBUN", "NORMAL");
            member.setGubun(defaultGubun);
        }
        if (member.getStatus() == null) {
            Long defaultStatus = codeSVC.getCodeId("MEMBER_STATUS", "ACTIVE");
            member.setStatus(defaultStatus);
        }
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", member.getEmail())
                .addValue("passwd", member.getPasswd())
                .addValue("tel", member.getTel())
                .addValue("nickname", member.getNickname())
                .addValue("gender", member.getGender())
                .addValue("birthDate", member.getBirthDate())
                .addValue("region", member.getRegion())
                .addValue("gubun", member.getGubun())
                .addValue("status", member.getStatus())
                .addValue("statusReason", member.getStatusReason())
                .addValue("statusChangedAt", member.getStatusChangedAt() != null ? member.getStatusChangedAt() : LocalDateTime.now())
                .addValue("address", member.getAddress())
                .addValue("addressDetail", member.getAddressDetail())
                .addValue("zipcode", member.getZipcode())
                .addValue("cdate", member.getCdate() != null ? member.getCdate() : LocalDateTime.now())
                .addValue("udate", member.getUdate() != null ? member.getUdate() : LocalDateTime.now());
        
        // BLOB 처리: SerialBlob을 바이트 배열로 변환
        if (member.getPic() != null) {
            try {
                param.addValue("pic", member.getPic());
            } catch (Exception e) {
                log.error("BLOB을 바이트 배열로 변환 실패", e);
                param.addValue("pic", null);
            }
        } else {
            param.addValue("pic", null);
        }
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"member_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("operation", "member_save");
            throw ErrorCode.INTERNAL_SERVER_ERROR.toException(details);
        }
        
        log.info("회원 저장 완료: memberId={}", key.longValue());
        return key.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateById(Long id, Member member) {
        String sql = """
            UPDATE member 
            SET email = :email, passwd = :passwd, tel = :tel, nickname = :nickname, 
                gender = :gender, birth_date = :birthDate, region = :region, gubun = :gubun, 
                status = :status, status_reason = :statusReason, status_changed_at = :statusChangedAt,
                pic = :pic, address = :address, address_detail = :addressDetail, zipcode = :zipcode, udate = SYSTIMESTAMP
            WHERE member_id = :memberId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", id)
                .addValue("email", member.getEmail())
                .addValue("passwd", member.getPasswd())
                .addValue("tel", member.getTel())
                .addValue("nickname", member.getNickname())
                .addValue("gender", member.getGender())
                .addValue("birthDate", member.getBirthDate())
                .addValue("region", member.getRegion())
                .addValue("gubun", member.getGubun())
                .addValue("status", member.getStatus())
                .addValue("statusReason", member.getStatusReason())
                .addValue("statusChangedAt", member.getStatusChangedAt() != null ? member.getStatusChangedAt() : LocalDateTime.now())
                .addValue("address", member.getAddress())
                .addValue("addressDetail", member.getAddressDetail())
                .addValue("zipcode", member.getZipcode());
        
        // BLOB 처리: SerialBlob을 바이트 배열로 변환
        if (member.getPic() != null) {
            try {
                param.addValue("pic", member.getPic());
            } catch (Exception e) {
                log.error("BLOB을 바이트 배열로 변환 실패", e);
                param.addValue("pic", null);
            }
        } else {
            param.addValue("pic", null);
        }
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long memberId) {
        String sql = "DELETE FROM member WHERE member_id = :memberId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findById(Long memberId) {
        String sql = "SELECT * FROM member WHERE member_id = :memberId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member ORDER BY cdate DESC";
        return template.query(sql, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM member";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM member 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findByEmailAndPasswd(String email, String passwd) {
        String sql = "SELECT * FROM member WHERE email = :email AND passwd = :passwd";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("passwd", passwd);
        
        log.info("로그인 시도 - email: {}", email);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            log.info("로그인 성공 - 조회된 Member: memberId={}, email={}, nickname={}", 
                member.getMemberId(), member.getEmail(), member.getNickname());
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            log.warn("로그인 실패 - 사용자를 찾을 수 없음: email={}", email);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAllWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findByRegion(Long region) {
        String sql = """
            SELECT * FROM member 
            WHERE region = :region 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("region", region);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findByGubun(Long gubun) {
        String sql = """
            SELECT * FROM member 
            WHERE gubun = :gubun 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gubun", gubun);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM member WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        int count = template.queryForObject(sql, param, Integer.class);
        return count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByRegion(Long region) {
        String sql = "SELECT COUNT(*) FROM member WHERE region = :region";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("region", region);
        
        return template.queryForObject(sql, param, Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByGubun(Long gubun) {
        String sql = "SELECT COUNT(*) FROM member WHERE gubun = :gubun";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gubun", gubun);
        
        return template.queryForObject(sql, param, Integer.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findEmailByPhoneAndBirth(String phone, String birth) {
        String sql = """
            SELECT email FROM member 
            WHERE tel IS NOT NULL AND tel = :phone AND 
                  birth_date IS NOT NULL AND
                  TO_CHAR(birth_date, 'YYYY-MM-DD') = :birth
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("phone", phone)
                .addValue("birth", birth);
        
        log.info("아이디 찾기 쿼리 실행: phone={}, birth={}", phone, birth);
        
        try {
            String email = template.queryForObject(sql, param, String.class);
            log.info("아이디 찾기 쿼리 성공: email={}", email);
            return Optional.ofNullable(email);
        } catch (EmptyResultDataAccessException e) {
            log.warn("아이디 찾기 쿼리 결과 없음: phone={}, birth={}", phone, birth);
            return Optional.empty();
        }
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM member WHERE email LIKE :kw OR nickname LIKE :kw";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("kw", "%" + keyword + "%");
        return template.queryForObject(sql, param, Integer.class);
    }

    @Override
    public List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE email LIKE :kw OR nickname LIKE :kw
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("kw", "%" + keyword + "%")
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }

    @Override
    public int countByStatus(String status) {
        if ("ALL".equals(status)) {
            return getTotalCount();
        }
        // String status code를 code_id로 변환
        Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
        String sql = "SELECT COUNT(*) FROM member WHERE status = :status";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("status", statusId);
        return template.queryForObject(sql, param, Integer.class);
    }
    @Override
    public List<Member> findByStatusWithPaging(String status, int pageNo, int pageSize) {
        if ("ALL".equals(status)) {
            return findAllWithPaging(pageNo, pageSize);
        }
        // String status code를 code_id로 변환
        Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE status = :status
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", statusId)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }
    @Override
    public int countByStatusAndKeyword(String status, String keyword) {
        if ("ALL".equals(status)) {
            return countByKeyword(keyword);
        }
        // String status code를 code_id로 변환
        Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
        String sql = "SELECT COUNT(*) FROM member WHERE status = :status AND (email LIKE :kw OR nickname LIKE :kw)";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", statusId)
                .addValue("kw", "%" + keyword + "%");
        return template.queryForObject(sql, param, Integer.class);
    }
    @Override
    public List<Member> findByStatusAndKeywordWithPaging(String status, String keyword, int pageNo, int pageSize) {
        if ("ALL".equals(status)) {
            return findByKeywordWithPaging(keyword, pageNo, pageSize);
        }
        // String status code를 code_id로 변환
        Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE status = :status AND (email LIKE :kw OR nickname LIKE :kw)
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", statusId)
                .addValue("kw", "%" + keyword + "%")
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }

    // === statusId 기반 검색 메서드들 (새로 추가) ===
    
    @Override
    public int countByStatusId(Long statusId) {
        String sql = "SELECT COUNT(*) FROM member WHERE status = :statusId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("statusId", statusId);
        return template.queryForObject(sql, param, Integer.class);
    }
    
    @Override
    public List<Member> findByStatusIdWithPaging(Long statusId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE status = :statusId
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("statusId", statusId)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }
    
    @Override
    public int countByStatusIdAndKeyword(Long statusId, String keyword) {
        String sql = "SELECT COUNT(*) FROM member WHERE status = :statusId AND (email LIKE :kw OR nickname LIKE :kw)";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("statusId", statusId)
                .addValue("kw", "%" + keyword + "%");
        return template.queryForObject(sql, param, Integer.class);
    }
    
    @Override
    public List<Member> findByStatusIdAndKeywordWithPaging(Long statusId, String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE status = :statusId AND (email LIKE :kw OR nickname LIKE :kw)
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("statusId", statusId)
                .addValue("kw", "%" + keyword + "%")
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }

    @Override
    public Optional<Member> findByNickname(String nickname) {
        String sql = "SELECT * FROM member WHERE nickname = :nickname";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("nickname", nickname);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    // === 새로 추가된 메서드들 구현 ===
    
    /**
     * 회원 상세 정보 조회 (코드 decode 값 포함)
     */
    @Override
    public Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findDetailById(Long memberId) {
        String sql = """
            SELECT m.*, 
                   g.code as gender_code, g.decode as gender_name,
                   r.code as region_code, r.decode as region_name,
                   gb.code as gubun_code, gb.decode as gubun_name,
                   s.code as status_code, s.decode as status_name,
                   m.address, m.address_detail, m.zipcode
            FROM member m
            LEFT JOIN code g ON m.gender = g.code_id AND g.gcode = 'GENDER'
            LEFT JOIN code r ON m.region = r.code_id AND r.gcode = 'REGION'
            LEFT JOIN code gb ON m.gubun = gb.code_id AND gb.gcode = 'MEMBER_TYPE'
            LEFT JOIN code s ON m.status = s.code_id AND s.gcode = 'MEMBER_STATUS'
            WHERE m.member_id = :memberId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        try {
            com.kh.demo.domain.member.dto.MemberDetailDTO memberDetail = template.queryForObject(sql, param, memberDetailRowMapper);
            if (memberDetail != null) {
                // 취미 정보 추가
                List<com.kh.demo.domain.member.dto.MemberHobbyDTO> hobbies = findHobbiesByMemberId(memberId);
                memberDetail.setHobbies(hobbies);
            }
            return Optional.ofNullable(memberDetail);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    /**
     * 회원 상세 정보 조회 (이메일로, 코드 decode 값 포함)
     */
    @Override
    public Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findDetailByEmail(String email) {
        String sql = """
            SELECT m.*, 
                   g.code as gender_code, g.decode as gender_name,
                   r.code as region_code, r.decode as region_name,
                   gb.code as gubun_code, gb.decode as gubun_name,
                   s.code as status_code, s.decode as status_name,
                   m.address, m.address_detail, m.zipcode
            FROM member m
            LEFT JOIN code g ON m.gender = g.code_id AND g.gcode = 'GENDER'
            LEFT JOIN code r ON m.region = r.code_id AND r.gcode = 'REGION'
            LEFT JOIN code gb ON m.gubun = gb.code_id AND gb.gcode = 'MEMBER_TYPE'
            LEFT JOIN code s ON m.status = s.code_id AND s.gcode = 'MEMBER_STATUS'
            WHERE m.email = :email
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        try {
            com.kh.demo.domain.member.dto.MemberDetailDTO memberDetail = template.queryForObject(sql, param, memberDetailRowMapper);
            if (memberDetail != null) {
                // 취미 정보 추가
                List<com.kh.demo.domain.member.dto.MemberHobbyDTO> hobbies = findHobbiesByMemberId(memberDetail.getMemberId());
                memberDetail.setHobbies(hobbies);
            }
            return Optional.ofNullable(memberDetail);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    /**
     * 회원 취미 목록 조회
     */
    @Override
    public List<com.kh.demo.domain.member.dto.MemberHobbyDTO> findHobbiesByMemberId(Long memberId) {
        String sql = """
            SELECT mh.hobby_id, mh.member_id, mh.hobby_code_id, mh.cdate,
                   c.code as hobby_code, c.decode as hobby_name
            FROM member_hobbies mh
            JOIN code c ON mh.hobby_code_id = c.code_id AND c.gcode = 'HOBBY'
            WHERE mh.member_id = :memberId
            ORDER BY c.sort_order, mh.cdate
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, param, memberHobbyRowMapper);
    }
    
    /**
     * 회원 취미 추가
     */
    @Override
    public Long addMemberHobby(Long memberId, Long hobbyCodeId) {
        String sql = """
            INSERT INTO member_hobbies (hobby_id, member_id, hobby_code_id, cdate)
            VALUES (seq_member_hobby_id.nextval, :memberId, :hobbyCodeId, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("hobbyCodeId", hobbyCodeId);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"hobby_id"});
        
        Number hobbyIdNumber = keyHolder.getKey();
        return hobbyIdNumber != null ? hobbyIdNumber.longValue() : null;
    }
    
    /**
     * 회원 취미 삭제
     */
    @Override
    public int removeMemberHobby(Long memberId, Long hobbyCodeId) {
        String sql = """
            DELETE FROM member_hobbies 
            WHERE member_id = :memberId AND hobby_code_id = :hobbyCodeId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("hobbyCodeId", hobbyCodeId);
        
        return template.update(sql, param);
    }
    
    /**
     * 회원의 모든 취미 삭제
     */
    @Override
    public int removeAllMemberHobbies(Long memberId) {
        String sql = "DELETE FROM member_hobbies WHERE member_id = :memberId";
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.update(sql, param);
    }
    
    // === RowMapper들 ===
    
    /**
     * MemberDetailDTO RowMapper
     */
    private final RowMapper<com.kh.demo.domain.member.dto.MemberDetailDTO> memberDetailRowMapper = (ResultSet rs, int rowNum) -> {
        com.kh.demo.domain.member.dto.MemberDetailDTO dto = new com.kh.demo.domain.member.dto.MemberDetailDTO();
        
        // Member 기본 정보
        dto.setMemberId(rs.getLong("member_id"));
        dto.setEmail(rs.getString("email"));
        dto.setTel(rs.getString("tel"));
        dto.setNickname(rs.getString("nickname"));
        dto.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
        dto.setPic(rs.getBytes("pic"));
        dto.setStatusReason(rs.getString("status_reason"));
        dto.setStatusChangedAt(rs.getObject("status_changed_at", LocalDateTime.class));
        dto.setCdate(rs.getObject("cdate", LocalDateTime.class));
        dto.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        // 코드 참조 필드들
        dto.setGender(rs.getLong("gender"));
        dto.setRegion(rs.getLong("region"));
        dto.setGubun(rs.getLong("gubun"));
        dto.setStatus(rs.getLong("status"));
        
        // 코드 decode 값들
        dto.setGenderName(rs.getString("gender_name"));
        dto.setRegionName(rs.getString("region_name"));
        dto.setGubunName(rs.getString("gubun_name"));
        dto.setStatusName(rs.getString("status_name"));
        
        // 주소 정보
        dto.setAddress(rs.getString("address"));
        dto.setAddressDetail(rs.getString("address_detail"));
        dto.setZipcode(rs.getString("zipcode"));
        
        return dto;
    };
    
    /**
     * MemberHobbyDTO RowMapper
     */
    private final RowMapper<com.kh.demo.domain.member.dto.MemberHobbyDTO> memberHobbyRowMapper = (ResultSet rs, int rowNum) -> {
        com.kh.demo.domain.member.dto.MemberHobbyDTO dto = new com.kh.demo.domain.member.dto.MemberHobbyDTO();
        dto.setHobbyId(rs.getLong("hobby_id"));
        dto.setMemberId(rs.getLong("member_id"));
        dto.setHobbyCodeId(rs.getLong("hobby_code_id"));
        dto.setHobbyCode(rs.getString("hobby_code"));
        dto.setHobbyName(rs.getString("hobby_name"));
        dto.setCdate(rs.getObject("cdate", LocalDateTime.class));
        return dto;
    };

    // === 신규 회원, VIP 회원, 휴면 회원 관련 메서드들 ===

    @Override
    public List<Member> findNewMembersWithPaging(int pageNo, int pageSize) {
        String sql = """
            SELECT member_id, email, passwd, tel, nickname, gender, birth_date, region, gubun, status, status_reason, status_changed_at, pic, address, address_detail, zipcode, cdate, udate
            FROM member
            WHERE cdate >= SYSTIMESTAMP - INTERVAL '30' DAY
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        int offset = (pageNo - 1) * pageSize;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", pageSize);
        
        return template.query(sql, param, memberRowMapper);
    }

    @Override
    public int countNewMembers() {
        String sql = """
            SELECT COUNT(*)
            FROM member
            WHERE cdate >= SYSTIMESTAMP - INTERVAL '30' DAY
            """;
        
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<Member> findVipMembersWithPaging(int pageNo, int pageSize) {
        String sql = """
            SELECT m.member_id, m.email, m.passwd, m.tel, m.nickname, m.gender, m.birth_date, m.region, m.gubun, m.status, m.status_reason, m.status_changed_at, m.pic, m.address, m.address_detail, m.zipcode, m.cdate, m.udate
            FROM member m
            JOIN code c ON m.gubun = c.code_id
            WHERE c.gcode = 'MEMBER_GUBUN' AND c.code = 'VIP'
            ORDER BY m.cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        int offset = (pageNo - 1) * pageSize;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", pageSize);
        
        return template.query(sql, param, memberRowMapper);
    }

    @Override
    public int countVipMembers() {
        String sql = """
            SELECT COUNT(*)
            FROM member m
            JOIN code c ON m.gubun = c.code_id
            WHERE c.gcode = 'MEMBER_GUBUN' AND c.code = 'VIP'
            """;
        
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<Member> findInactiveMembersWithPaging(int pageNo, int pageSize) {
        String sql = """
            SELECT m.member_id, m.email, m.passwd, m.tel, m.nickname, m.gender, m.birth_date, m.region, m.gubun, m.status, m.status_reason, m.status_changed_at, m.pic, m.address, m.address_detail, m.zipcode, m.cdate, m.udate
            FROM member m
            JOIN code c ON m.status = c.code_id
            WHERE c.gcode = 'MEMBER_STATUS' AND c.code = 'SUSPENDED'
            ORDER BY m.status_changed_at DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        int offset = (pageNo - 1) * pageSize;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", pageSize);
        
        return template.query(sql, param, memberRowMapper);
    }

    @Override
    public int countInactiveMembers() {
        String sql = """
            SELECT COUNT(*)
            FROM member m
            JOIN code c ON m.status = c.code_id
            WHERE c.gcode = 'MEMBER_STATUS' AND c.code = 'SUSPENDED'
            """;
        
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
}
