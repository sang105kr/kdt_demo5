/**
 * 댓글 SPA 시스템 - 순수 JavaScript로 구현
 * 모듈화된 댓글 시스템으로 게시판에서 독립적으로 사용 가능
 */

class ReplySPA {
    constructor(containerId) {
        this.containerId = containerId;
        this.currentPage = 1;
        this.isLoading = false;
        this.hasMore = true;
        this.pageSize = 10;
        this.isLoggedIn = false;
        this.userEmail = null;
        this.userNickname = null;
        
        this.init();
    }
    
    init() {
        this.checkLoginStatus();
        this.renderReplySection();
        this.bindEvents();
        this.loadReplies();
    }
    // 게시글 번호 가져오기
    getBoardId() {
        // data-board-id 속성에서 boardId 가져오기
        const container = document.getElementById(this.containerId);
        if (container) {
            const boardId = container.getAttribute('data-board-id');
            console.log('가져온 boardId:', boardId);
            return boardId;
        }
        return null;
    }
    // 로그인 상태 확인
    checkLoginStatus() {
        const userEmail = document.querySelector('[data-s-email]')?.dataset.sEmail;
        const userNickname = document.querySelector('[data-s-nickname]')?.dataset.sNickname;
        
        this.isLoggedIn = !!userEmail;
        this.userEmail = userEmail;
        this.userNickname = userNickname;
    }
    // 댓글기능 추가시 추가시 적용  
    renderReplySection() {
        const container = document.getElementById(this.containerId);
        if (!container) return;
        
        const boardId = this.getBoardId();
        container.innerHTML = `
            <div class="reply-section" data-board-id="${boardId}">
                <h4>댓글</h4>
                
                <!-- 댓글 작성 폼 -->
                ${this.isLoggedIn ? this.renderReplyForm() : this.renderLoginNotice()}
                
                <!-- 댓글 목록 -->
                <div class="reply-list" id="replyList">
                    <div class="no-replies" id="noReplies" style="display: none;">
                        <p>아직 댓글이 없습니다.</p>
                    </div>
                </div>
                
                <!-- 로딩 인디케이터 -->
                <div class="loading-indicator" id="loadingIndicator" style="display: none;">
                    <div class="loading-spinner"></div>
                    <p>댓글을 불러오는 중입니다...</p>
                </div>
                
                <!-- 더 보기 버튼 -->
                <div class="load-more" id="loadMore" style="display: none;">
                    <button type="button" class="btn btn--outline" id="loadMoreBtn">더 보기</button>
                </div>
            </div>
        `;
    }
    
    renderReplyForm() {
        return `
            <div class="reply-form">
                <form id="replyForm">
                    <div class="form-row">
                        <label for="rcontent">댓글 작성</label>
                        <textarea id="rcontent" placeholder="댓글을 입력하세요..." required></textarea>
                        <div class="field-error" id="rcontentError" style="display: none;"></div>
                    </div>
                    <div class="btn-area">
                        <button type="button" class="btn btn--primary" id="submitReplyBtn">댓글 등록</button>
                    </div>
                </form>
            </div>
        `;
    }
    
    renderLoginNotice() {
        return `
            <div class="login-notice">
                <p>댓글을 작성하려면 <a href="/login">로그인</a>이 필요합니다.</p>
            </div>
        `;
    }
    // 이벤트 바인딩
    bindEvents() {
        // 댓글 등록 버튼
        const submitBtn = document.getElementById('submitReplyBtn');
        if (submitBtn) {
            submitBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.submitReply(e);
            });
        }
        
        // 댓글 폼 제출 이벤트
        const replyForm = document.getElementById('replyForm');
        if (replyForm) {
            replyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitReply(e);
            });
        }
        
        // 더 보기 버튼
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => this.loadReplies());
        }
        
        // 무한 스크롤
        window.addEventListener('scroll', () => this.handleScroll());
    }
    
    handleScroll() {
        if (this.isLoading || !this.hasMore) return;
        
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;
        
        if (scrollTop + windowHeight >= documentHeight - 100) {
            this.loadReplies();
        }
    }
    // 댓글 목록 로드
    async loadReplies() {
        const boardId = this.getBoardId();
        
        console.log(`댓글 로드 체크: isLoading=${this.isLoading}, hasMore=${this.hasMore}, boardId=${boardId}, page=${this.currentPage}`);
        
        // 유효성 검사
        if (this.isLoading) {
            console.log('이미 로딩 중이므로 중단');
            return;
        }
        
        if (!this.hasMore) {
            console.log('더 이상 로드할 데이터가 없으므로 중단');
            return;
        }
        
        if (!boardId) {
            console.log('boardId가 없으므로 중단');
            return;
        }
        
        console.log(`댓글 로드 시작: boardId=${boardId}, page=${this.currentPage}`);
        
        // 로딩 상태 설정
        this.isLoading = true;
        this.showLoading(true);
        
        const startTime = Date.now();
        const minLoadingTime = 500;
        
        try {
            const response = await ajax.get(`/api/replies?boardId=${boardId}&pageNo=${this.currentPage}&pageSize=${this.pageSize}`);
            
            console.log('댓글 로드 응답:', response);
            
            if (response && (response.code === 'SUCCESS' || response.code === '00') && Array.isArray(response.data)) {
                const replies = response.data;
                console.log(`받은 댓글 수: ${replies.length}`);
                
                if (replies.length === 0) {
                    if (this.currentPage === 1) {
                        this.showNoReplies();
                    }
                    this.hasMore = false;
                } else {
                    this.renderReplies(replies);
                    this.currentPage++;
                    
                    if (replies.length < this.pageSize) {
                        this.hasMore = false;
                    }
                }
            } else {
                console.error('댓글 로드 실패:', response?.message, response);
                this.showToastMessage('댓글 목록을 불러오는데 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 로드 중 오류:', error);
            this.showToastMessage('댓글 목록을 불러오는 중 오류가 발생했습니다.', 'error');
        } finally {
            // 최소 로딩 시간 보장
            const elapsedTime = Date.now() - startTime;
            if (elapsedTime < minLoadingTime) {
                await new Promise(resolve => setTimeout(resolve, minLoadingTime - elapsedTime));
            }
            
            // 로딩 상태 해제
            this.isLoading = false;
            this.showLoading(false);
            this.updateLoadMoreButton();
            
            console.log(`댓글 로드 완료: isLoading=${this.isLoading}, hasMore=${this.hasMore}, currentPage=${this.currentPage}`);
        }
    }
    // 댓글 목록 렌더링
    renderReplies(replies) {
        console.log(`댓글 렌더링 시작: currentPage=${this.currentPage}, replies.length=${replies.length}`);
        
        const replyList = document.getElementById('replyList');
        const noReplies = document.getElementById('noReplies');
        
        // DOM 요소 존재 확인
        if (!replyList) {
            console.error('replyList 요소를 찾을 수 없습니다');
            return;
        }
        
        if (!noReplies) {
            console.error('noReplies 요소를 찾을 수 없습니다');
            return;
        }
        
        // 첫 페이지인 경우 기존 목록 초기화
        if (this.currentPage === 1) {
            console.log('첫 페이지이므로 기존 목록 초기화');
            // replyList 내부의 댓글 요소들만 제거 (noReplies는 유지)
            const replyItems = replyList.querySelectorAll('.reply-item');
            replyItems.forEach(item => item.remove());
            noReplies.style.display = 'none';
        }
        
        // 댓글 렌더링
        let renderedCount = 0;
        replies.forEach((reply, index) => {
            try {
                console.log(`댓글 ${index + 1} 렌더링:`, reply);
                const replyElement = this.createReplyElement(reply);
                replyList.appendChild(replyElement);
                renderedCount++;
            } catch (error) {
                console.error(`댓글 ${index + 1} 렌더링 실패:`, error);
            }
        });
        
        console.log(`댓글 렌더링 완료: 총 ${renderedCount}개 댓글 표시됨`);
    }
    
    createReplyElement(reply) {
        const replyDiv = document.createElement('div');
        replyDiv.className = 'reply-item';
        replyDiv.style.paddingLeft = `${reply.rindent * 20}px`;
        replyDiv.dataset.replyId = reply.replyId;
        
        const formattedDate = this.formatDate(reply.cdate);
        const isAuthor = this.isLoggedIn && this.userEmail === reply.email;
        
        replyDiv.innerHTML = `
            <div class="reply-header">
                <span class="reply-author">${reply.nickname}</span>
                ${reply.parentNickname ? `<span class="reply-to">→ ${reply.parentNickname}님에게 답글</span>` : ''}
                <span class="reply-date">${formattedDate}</span>
            </div>
            <div class="reply-content">${this.escapeHtml(reply.rcontent)}</div>
            <div class="reply-actions">
                ${this.isLoggedIn ? `<button class="btn btn--small btn--outline reply-reply-btn" 
                    data-reply-id="${reply.replyId}">답글</button>` : ''}
                ${isAuthor ? `
                    <button type="button" class="btn btn--small btn--outline edit-reply-btn" 
                        data-reply-id="${reply.replyId}">수정</button>
                    <button type="button" class="btn btn--small btn--outline delete-reply-btn" 
                        data-reply-id="${reply.replyId}">삭제</button>
                ` : ''}
            </div>
            ${this.isLoggedIn ? `
            <div class="like-dislike-container">
                <button type="button" class="like-dislike-btn like-btn reply-like-btn" data-reply-id="${reply.replyId}">
                    <i class="fas fa-thumbs-up"></i>
                    <span class="like-dislike-count">${reply.likeCount || 0}</span>
                </button>
                <button type="button" class="like-dislike-btn dislike-btn reply-dislike-btn" data-reply-id="${reply.replyId}">
                    <i class="fas fa-thumbs-down"></i>
                    <span class="like-dislike-count">${reply.dislikeCount || 0}</span>
                </button>
            </div>
            <div class="like-dislike-status reply-status" style="display: none;"></div>
            ` : ''}
            <div class="reply-reply-form" style="display: none;">
                <form onsubmit="return false;">
                    <div class="form-row">
                        <textarea placeholder="답글을 입력하세요..." required></textarea>
                    </div>
                    <div class="btn-area">
                        <button type="button" class="btn btn--small btn--primary submit-reply-reply-btn">답글 등록</button>
                        <button type="button" class="btn btn--small btn--outline cancel-reply-btn">취소</button>
                    </div>
                </form>
            </div>
            <div class="reply-edit-form" style="display: none;">
                <form onsubmit="return false;">
                    <div class="form-row">
                        <textarea placeholder="댓글을 수정하세요..." required>${this.escapeHtml(reply.rcontent)}</textarea>
                    </div>
                    <div class="btn-area">
                        <button type="button" class="btn btn--small btn--primary submit-edit-reply-btn">수정 완료</button>
                        <button type="button" class="btn btn--small btn--outline cancel-edit-btn">취소</button>
                    </div>
                </form>
            </div>
        `;
        
        this.bindReplyEvents(replyDiv, reply);
        return replyDiv;
    }
    
    bindReplyEvents(replyDiv, reply) {
        // 답글 버튼
        const replyReplyBtn = replyDiv.querySelector('.reply-reply-btn');
        if (replyReplyBtn) {
            replyReplyBtn.addEventListener('click', () => {
                this.showReplyForm(reply.replyId);
            });
        }
        
        // 수정 버튼
        const editReplyBtn = replyDiv.querySelector('.edit-reply-btn');
        if (editReplyBtn) {
            editReplyBtn.addEventListener('click', () => {
                this.showEditForm(reply.replyId, reply.rcontent);
            });
        }
        
        // 삭제 버튼
        const deleteReplyBtn = replyDiv.querySelector('.delete-reply-btn');
        if (deleteReplyBtn) {
            deleteReplyBtn.addEventListener('click', () => {
                this.showDeleteReplyModal(reply.replyId, this.getBoardId());
            });
        }
        
        // 답글 등록 버튼
        const submitReplyReplyBtn = replyDiv.querySelector('.submit-reply-reply-btn');
        if (submitReplyReplyBtn) {
            submitReplyReplyBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.submitReplyReply(reply.replyId);
            });
        }
        
        // 답글 취소 버튼
        const cancelReplyBtn = replyDiv.querySelector('.cancel-reply-btn');
        if (cancelReplyBtn) {
            cancelReplyBtn.addEventListener('click', () => {
                this.hideReplyForm(cancelReplyBtn);
            });
        }
        
        // 수정 완료 버튼
        const submitEditReplyBtn = replyDiv.querySelector('.submit-edit-reply-btn');
        if (submitEditReplyBtn) {
            submitEditReplyBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.submitEditReply(reply.replyId);
            });
        }
        
        // 수정 취소 버튼
        const cancelEditBtn = replyDiv.querySelector('.cancel-edit-btn');
        if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', () => {
                this.hideEditForm(cancelEditBtn);
            });
        }
        
        // 좋아요/싫어요 버튼
        const replyLikeBtn = replyDiv.querySelector('.reply-like-btn');
        if (replyLikeBtn) {
            replyLikeBtn.addEventListener('click', () => {
                this.likeReply(reply.replyId, replyDiv);
            });
        }
        
        const replyDislikeBtn = replyDiv.querySelector('.reply-dislike-btn');
        if (replyDislikeBtn) {
            replyDislikeBtn.addEventListener('click', () => {
                this.dislikeReply(reply.replyId, replyDiv);
            });
        }
    }
    
    async submitReply(event) {
        // 폼 제출 방지
        if (event) {
            event.preventDefault();
        }
        
        const rcontent = document.getElementById('rcontent')?.value?.trim();
        const rcontentError = document.getElementById('rcontentError');
        
        if (!rcontent) {
            this.showFieldError('rcontentError', '댓글 내용을 입력해주세요.');
            return;
        }
        
        const boardId = this.getBoardId();
        if (!boardId) {
            this.showFieldError('rcontentError', '게시글 정보를 찾을 수 없습니다.');
            return;
        }
        
        console.log('댓글 등록 시작:', { boardId: boardId, rcontent });
        
        // 버튼 비활성화
        const submitBtn = document.getElementById('submitReplyBtn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '등록 중...';
        }
        
        try {
            const requestData = {
                boardId: parseInt(boardId),
                rcontent: rcontent
            };
            
            console.log('전송할 데이터:', requestData);
            
            const response = await ajax.post('/api/replies', requestData);
            
            console.log('댓글 등록 응답:', response);
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                // 폼 초기화
                document.getElementById('rcontent').value = '';
                this.hideFieldError('rcontentError');
                
                console.log('댓글 등록 성공, 목록 새로고침 시작');
                
                // 성공 메시지 표시
                this.showToastMessage('댓글이 등록되었습니다.', 'success');
                
                // 목록 새로고침 (약간의 지연 후)
                setTimeout(() => {
                    this.refreshReplies();
                }, 100);
            } else {
                this.showFieldError('rcontentError', response?.message || '댓글 등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('댓글 등록 중 오류:', error);
            this.showFieldError('rcontentError', '댓글 등록 중 오류가 발생했습니다.');
        } finally {
            // 버튼 다시 활성화
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '댓글 등록';
            }
        }
    }
    
    async submitReplyReply(parentId) {
        // 폼 제출 방지
        event?.preventDefault();
        
        const replyForm = document.querySelector(`[data-reply-id="${parentId}"] .reply-reply-form`);
        const textarea = replyForm?.querySelector('textarea');
        const rcontent = textarea?.value?.trim();
        
        if (!rcontent) {
            this.showToastMessage('답글 내용을 입력해주세요.', 'error');
            return;
        }
        
        const boardId = this.getBoardId();
        if (!boardId) {
            this.showToastMessage('게시글 정보를 찾을 수 없습니다.', 'error');
            return;
        }
        
        // 버튼 비활성화
        const submitBtn = replyForm?.querySelector('.submit-reply-reply-btn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '등록 중...';
        }
        
        try {
            const response = await ajax.post('/api/replies', {
                boardId: parseInt(boardId),
                rcontent: rcontent,
                parentId: parentId
                // rgroup, rstep, rindent는 백엔드에서 자동 계산
            });
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                textarea.value = '';
                this.hideReplyForm(replyForm);
                this.refreshReplies();
                this.showToastMessage('답글이 등록되었습니다.', 'success');
            } else {
                this.showToastMessage(response?.message || '답글 등록에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('답글 등록 중 오류:', error);
            this.showToastMessage('답글 등록 중 오류가 발생했습니다.', 'error');
        } finally {
            // 버튼 다시 활성화
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '답글 등록';
            }
        }
    }
    
    showReplyForm(replyId) {
        const replyElement = document.querySelector(`[data-reply-id="${replyId}"]`);
        const replyForm = replyElement.querySelector('.reply-reply-form');
        const textarea = replyForm.querySelector('textarea');
        
        // 다른 모든 답글 폼 숨기기
        document.querySelectorAll('.reply-reply-form').forEach(form => {
            form.style.display = 'none';
        });
        
        replyForm.style.display = 'block';
        textarea.focus();
    }
    
    hideReplyForm(button) {
        const replyForm = button.closest('.reply-reply-form');
        replyForm.style.display = 'none';
    }
    
    showEditForm(replyId, currentContent) {
        const replyElement = document.querySelector(`[data-reply-id="${replyId}"]`);
        const editForm = replyElement.querySelector('.reply-edit-form');
        const textarea = editForm.querySelector('textarea');
        
        // 다른 모든 수정 폼 숨기기
        document.querySelectorAll('.reply-edit-form').forEach(form => {
            form.style.display = 'none';
        });
        
        // 다른 모든 답글 폼 숨기기
        document.querySelectorAll('.reply-reply-form').forEach(form => {
            form.style.display = 'none';
        });
        
        editForm.style.display = 'block';
        textarea.value = currentContent;
        textarea.focus();
    }
    
    hideEditForm(button) {
        const editForm = button.closest('.reply-edit-form');
        editForm.style.display = 'none';
    }
    
    showDeleteReplyModal(replyId, boardId) {
        if (typeof showModal === 'function') {
            showModal({
                title: '댓글 삭제',
                message: '정말로 이 댓글을 삭제하시겠습니까?\n삭제된 댓글은 복구할 수 없습니다.',
                onConfirm: () => this.deleteReply(replyId, boardId),
                onCancel: () => {}
            });
        } else {
            if (confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
                this.deleteReply(replyId, boardId);
            }
        }
    }
    
    async deleteReply(replyId, boardId) {
        try {
            const response = await ajax.delete(`/api/replies/${replyId}`);
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                this.refreshReplies();
                this.showToastMessage('댓글이 삭제되었습니다.', 'success');
            } else {
                this.showToastMessage(response?.message || '댓글 삭제에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 삭제 중 오류:', error);
            this.showToastMessage('댓글 삭제 중 오류가 발생했습니다.', 'error');
        }
    }
    
    async submitEditReply(replyId) {
        const replyElement = document.querySelector(`[data-reply-id="${replyId}"]`);
        const editForm = replyElement.querySelector('.reply-edit-form');
        const textarea = editForm.querySelector('textarea');
        const rcontent = textarea?.value?.trim();
        
        if (!rcontent) {
            this.showToastMessage('댓글 내용을 입력해주세요.', 'error');
            return;
        }
        
        // 버튼 비활성화
        const submitBtn = editForm?.querySelector('.submit-edit-reply-btn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '수정 중...';
        }
        
        try {
            console.log('댓글 수정 요청 URL:', '/api/replies');
            console.log('댓글 수정 요청 데이터:', { replyId, rcontent });
            const response = await ajax.patch('/api/replies', {
                replyId: replyId,
                rcontent: rcontent
            });
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                // 해당 댓글의 내용만 업데이트
                this.updateReplyContent(replyElement, rcontent);
                this.hideEditForm(editForm);
                this.showToastMessage('댓글이 수정되었습니다.', 'success');
            } else {
                this.showToastMessage(response?.message || '댓글 수정에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 수정 중 오류:', error);
            this.showToastMessage('댓글 수정 중 오류가 발생했습니다.', 'error');
        } finally {
            // 버튼 다시 활성화
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '수정 완료';
            }
        }
    }
    
    async likeReply(replyId, replyElement) {
        try {
            const response = await ajax.post(`/api/replies/${replyId}/like`);
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                // 해당 댓글의 좋아요 카운트만 업데이트
                this.updateReplyLikeCount(replyElement, response.data);
                this.showReplyStatus(replyElement, '좋아요가 등록되었습니다.');
            } else {
                this.showReplyStatus(replyElement, response?.message || '좋아요 처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 좋아요 처리 중 오류:', error);
            this.showReplyStatus(replyElement, '오류가 발생했습니다.', 'error');
        }
    }
    
    async dislikeReply(replyId, replyElement) {
        try {
            const response = await ajax.post(`/api/replies/${replyId}/dislike`);
            
            if (response && (response.code === 'SUCCESS' || response.code === '00')) {
                // 해당 댓글의 싫어요 카운트만 업데이트
                this.updateReplyDislikeCount(replyElement, response.data);
                this.showReplyStatus(replyElement, '싫어요가 등록되었습니다.');
            } else {
                this.showReplyStatus(replyElement, response?.message || '싫어요 처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 싫어요 처리 중 오류:', error);
            this.showReplyStatus(replyElement, '오류가 발생했습니다.', 'error');
        }
    }
    
    updateReplyLikeCount(replyElement, likeData) {
        const likeBtn = replyElement.querySelector('.reply-like-btn');
        if (likeBtn) {
            const countSpan = likeBtn.querySelector('.like-dislike-count');
            if (countSpan && likeData && typeof likeData.likeCount !== 'undefined') {
                countSpan.textContent = likeData.likeCount;
                console.log(`댓글 좋아요 카운트 업데이트: ${likeData.likeCount}`);
            }
        }
    }
    
    updateReplyDislikeCount(replyElement, dislikeData) {
        const dislikeBtn = replyElement.querySelector('.reply-dislike-btn');
        if (dislikeBtn) {
            const countSpan = dislikeBtn.querySelector('.like-dislike-count');
            if (countSpan && dislikeData && typeof dislikeData.dislikeCount !== 'undefined') {
                countSpan.textContent = dislikeData.dislikeCount;
                console.log(`댓글 싫어요 카운트 업데이트: ${dislikeData.dislikeCount}`);
            }
        }
    }
    
    updateReplyContent(replyElement, newContent) {
        const contentElement = replyElement.querySelector('.reply-content');
        if (contentElement) {
            contentElement.innerHTML = this.escapeHtml(newContent);
            console.log(`댓글 내용 업데이트 완료: ${newContent.substring(0, 20)}...`);
        } else {
            console.error('댓글 내용 요소를 찾을 수 없습니다');
        }
    }
    
    refreshReplies() {
        console.log('댓글 목록 새로고침 시작');
        
        // 상태 완전 초기화 (로딩 시작 전에)
        this.currentPage = 1;
        this.hasMore = true;
        this.isLoading = false;
        
        // 기존 댓글 목록 초기화
        this.clearReplyList();
        
        // 새로 로드
        this.loadReplies();
    }
    
    clearReplyList() {
        const replyList = document.getElementById('replyList');
        const noReplies = document.getElementById('noReplies');
        
        if (replyList) {
            // replyList 내부의 댓글 요소들만 제거 (noReplies는 유지)
            const replyItems = replyList.querySelectorAll('.reply-item');
            replyItems.forEach(item => item.remove());
            console.log('기존 댓글 목록 초기화 완료');
        } else {
            console.error('replyList 요소를 찾을 수 없습니다');
        }
        
        if (noReplies) {
            noReplies.style.display = 'none';
        } else {
            console.error('noReplies 요소를 찾을 수 없습니다');
        }
    }
    
    showLoading(show) {
        const loading = document.getElementById('loadingIndicator');
        if (loading) {
            loading.style.display = show ? 'block' : 'none';
            console.log(`로딩 인디케이터 ${show ? '표시' : '숨김'}`);
        } else {
            console.error('로딩 인디케이터 요소를 찾을 수 없습니다');
        }
    }
    
    showNoReplies() {
        const noReplies = document.getElementById('noReplies');
        if (noReplies) noReplies.style.display = 'block';
    }
    
    updateLoadMoreButton() {
        const loadMore = document.getElementById('loadMore');
        if (loadMore) loadMore.style.display = this.hasMore ? 'block' : 'none';
    }
    
    showFieldError(fieldId, message) {
        const errorElement = document.getElementById(fieldId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }
    
    hideFieldError(fieldId) {
        const errorElement = document.getElementById(fieldId);
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
    }
    
    showToastMessage(message, type = 'info') {
        if (typeof showToastMessage === 'function') {
            showToastMessage(message, type);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }
    
    showReplyStatus(replyElement, message, type = 'info') {
        const statusElement = replyElement.querySelector('.reply-status');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = `like-dislike-status reply-status ${type}`;
            statusElement.style.display = 'block';
            
            setTimeout(() => {
                statusElement.textContent = '';
                statusElement.style.display = 'none';
            }, 3000);
        }
    }
    
    formatDate(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear().toString().slice(-2);
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}년 ${month}월 ${day}일 ${hours}시 ${minutes}분`;
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// 전역 함수로 댓글 시스템 초기화
function initReplySPA(containerId) {
    return new ReplySPA(containerId);
} 