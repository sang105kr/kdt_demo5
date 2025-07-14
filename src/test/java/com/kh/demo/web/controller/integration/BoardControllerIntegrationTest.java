package com.kh.demo.web.controller.integration;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.board.svc.RboardSVC;
import com.kh.demo.web.controller.form.board.ReplyForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Clob;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BoardController 통합 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("BoardController 통합 테스트")
class BoardControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BoardSVC boardSVC;

    @Autowired
    private RboardSVC rboardSVC;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private Long testBoardId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        session = new MockHttpSession();
        
        // 테스트용 게시글 생성
        Boards testBoard = new Boards();
        testBoard.setBcategory(1L);
        testBoard.setTitle("테스트 게시글");
        testBoard.setNickname("테스트작성자");
        testBoard.setEmail("test@example.com");
        testBoard.setBcontent(new javax.sql.rowset.serial.SerialClob("테스트 게시글 내용".toCharArray()));
        testBoard.setStatus("A");
        testBoardId = boardSVC.save(testBoard);
        
        // 로그인 세션 설정
        session.setAttribute("loginMember", new com.kh.demo.web.controller.form.login.LoginMember(
            1L, "test@example.com", "테스트사용자", "USER"
        ));
    }

    @Test
    @DisplayName("게시글 목록 페이지 조회 테스트")
    void testBoardList() throws Exception {
        mockMvc.perform(get("/board"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/list"))
                .andExpect(model().attributeExists("list"))
                .andExpect(model().attributeExists("pagination"));
    }

    @Test
    @DisplayName("카테고리별 게시글 목록 페이지 조회 테스트")
    void testBoardListByCategory() throws Exception {
        mockMvc.perform(get("/board/category/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/list"))
                .andExpect(model().attributeExists("list"))
                .andExpect(model().attributeExists("pagination"))
                .andExpect(model().attribute("selectedCategory", 1L));
    }

    @Test
    @DisplayName("게시글 상세 페이지 조회 테스트")
    void testBoardDetail() throws Exception {
        mockMvc.perform(get("/board/" + testBoardId))
                .andExpect(status().isOk())
                .andExpect(view().name("board/detail"))
                .andExpect(model().attributeExists("detailForm"))
                .andExpect(model().attributeExists("replies"))
                .andExpect(model().attributeExists("replyForm"));
    }

    @Test
    @DisplayName("게시글 등록 폼 페이지 조회 테스트")
    void testBoardAddForm() throws Exception {
        mockMvc.perform(get("/board/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/add"))
                .andExpect(model().attributeExists("saveForm"));
    }

    @Test
    @DisplayName("게시글 등록 처리 테스트")
    void testBoardAdd() throws Exception {
        mockMvc.perform(post("/board/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("bcategory", "1")
                .param("title", "새 게시글 제목")
                .param("nickname", "새작성자")
                .param("email", "new@example.com")
                .param("bcontent", "새 게시글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("게시글 수정 폼 페이지 조회 테스트")
    void testBoardEditForm() throws Exception {
        mockMvc.perform(get("/board/" + testBoardId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/edit"))
                .andExpect(model().attributeExists("updateForm"));
    }

    @Test
    @DisplayName("게시글 수정 처리 테스트")
    void testBoardEdit() throws Exception {
        mockMvc.perform(post("/board/" + testBoardId + "/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("bcategory", "1")
                .param("title", "수정된 게시글 제목")
                .param("nickname", "수정된작성자")
                .param("email", "updated@example.com")
                .param("bcontent", "수정된 게시글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("게시글 삭제 처리 테스트")
    void testBoardDelete() throws Exception {
        mockMvc.perform(post("/board/" + testBoardId + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("댓글 등록 테스트 (로그인 상태)")
    void testAddReplyWithLogin() throws Exception {
        mockMvc.perform(post("/board/" + testBoardId + "/reply")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rcontent", "테스트 댓글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("댓글 등록 테스트 (비로그인 상태)")
    void testAddReplyWithoutLogin() throws Exception {
        mockMvc.perform(post("/board/" + testBoardId + "/reply")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rcontent", "테스트 댓글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("대댓글 등록 테스트")
    void testAddReplyToReply() throws Exception {
        // 먼저 부모 댓글 생성
        Replies parentReply = new Replies();
        parentReply.setBoardId(testBoardId);
        parentReply.setEmail("test@example.com");
        parentReply.setNickname("테스트사용자");
        parentReply.setRcontent("부모 댓글");
        parentReply.setParentId(null);
        parentReply.setRgroup(null);
        parentReply.setRstep(0);
        parentReply.setRindent(0);
        parentReply.setStatus("A");
        Long parentReplyId = rboardSVC.save(parentReply);

        mockMvc.perform(post("/board/" + testBoardId + "/reply")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("parentId", parentReplyId.toString())
                .param("rgroup", parentReplyId.toString())
                .param("rstep", "0")
                .param("rindent", "0")
                .param("rcontent", "대댓글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("댓글 삭제 테스트 (작성자)")
    void testDeleteReplyByAuthor() throws Exception {
        // 댓글 생성
        Replies reply = new Replies();
        reply.setBoardId(testBoardId);
        reply.setEmail("test@example.com");
        reply.setNickname("테스트사용자");
        reply.setRcontent("삭제할 댓글");
        reply.setParentId(null);
        reply.setRgroup(null);
        reply.setRstep(0);
        reply.setRindent(0);
        reply.setStatus("A");
        Long replyId = rboardSVC.save(reply);

        mockMvc.perform(post("/board/" + testBoardId + "/reply/" + replyId + "/delete")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("댓글 삭제 테스트 (비로그인 상태)")
    void testDeleteReplyWithoutLogin() throws Exception {
        // 댓글 생성
        Replies reply = new Replies();
        reply.setBoardId(testBoardId);
        reply.setEmail("test@example.com");
        reply.setNickname("테스트사용자");
        reply.setRcontent("삭제할 댓글");
        reply.setParentId(null);
        reply.setRgroup(null);
        reply.setRstep(0);
        reply.setRindent(0);
        reply.setStatus("A");
        Long replyId = rboardSVC.save(reply);

        mockMvc.perform(post("/board/" + testBoardId + "/reply/" + replyId + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId))
                .andExpect(flash().attributeExists("msg"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 테스트")
    void testNonExistentBoard() throws Exception {
        mockMvc.perform(get("/board/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"));
    }

    @Test
    @DisplayName("게시글 등록 유효성 검사 실패 테스트")
    void testBoardAddValidationFailure() throws Exception {
        mockMvc.perform(post("/board/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("bcategory", "1")
                .param("title", "") // 빈 제목
                .param("nickname", "새작성자")
                .param("email", "new@example.com")
                .param("bcontent", "새 게시글 내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/add"));
    }

    @Test
    @DisplayName("댓글 등록 유효성 검사 실패 테스트")
    void testReplyAddValidationFailure() throws Exception {
        mockMvc.perform(post("/board/" + testBoardId + "/reply")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rcontent", "")) // 빈 내용
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/" + testBoardId));
    }
} 