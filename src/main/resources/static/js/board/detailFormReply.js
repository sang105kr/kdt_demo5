//수정
btnUpdate.addEventListener('click', e=>{
  const id = bbsId.value;
  location.href = `/bbs/${id}/edit`;     // GET http://localhost:9080/bbs/{id}/edit
});

//삭제
btnDelete.addEventListener('click', e=>{

  //모달창 띄우기
  modalDel.showModal();// modal

  //모달창이 닫힐때
  modalDel.addEventListener('close',e=>{

    const id = bbsId.value;
    $btnYes = document.querySelector('#modalDel .btnYes');
    if(modalDel.returnValue == $btnYes.value){
      location.href = `/bbs/${id}/del`;     // GET http://localhost:9080/bbs/{id}/delete
    }else{
      return;
    }
  });
});

//목록
btnList.addEventListener('click', e=>{
  location.href = '/bbs';               // GET http://localhost:9080/bbs
});

// 댓글/대댓글 목록 조회 및 렌더링
const commentList = document.getElementById('commentList');
const btnAddComment = document.getElementById('btnAddComment');
const commentContent = document.getElementById('commentContent');

// boardId 추출 (예: hidden input[name='bbsId'] 또는 URL 등에서)
const boardId = document.querySelector("input[name='bbsId']")?.value || getBoardIdFromUrl();

function getBoardIdFromUrl() {
  // /bbs/{id} 형태에서 id 추출
  const match = window.location.pathname.match(/\/(\d+)/);
  return match ? match[1] : null;
}

// 댓글 목록 불러오기
async function fetchComments() {
  if (!boardId) return;
  const res = await fetch(`/api/replies?boardId=${boardId}&pageNo=1&pageSize=10`);
  const data = await res.json();
  if (data && data.data) {
    renderComments(data.data);
  }
}

// 본인 이메일(임시: 서버에서 window.loginMemberEmail로 렌더링 필요)
const loginMemberEmail = window.loginMemberEmail || null;

// 댓글 목록 렌더링 (수정/삭제/답글 버튼, 계층형 UI)
function renderComments(comments) {
  commentList.innerHTML = '';
  comments.forEach(c => {
    const div = document.createElement('div');
    div.className = 'comment-item';
    div.style.marginLeft = (c.rindent || 0) * 24 + 'px'; // 계층형 들여쓰기
    let btns = '';
    if (loginMemberEmail && c.email === loginMemberEmail) {
      btns += `<button class="btn-edit" data-id="${c.replyId}">수정</button>`;
      btns += `<button class="btn-del" data-id="${c.replyId}">삭제</button>`;
    }
    btns += `<button class="btn-reply" data-id="${c.replyId}" data-nick="${c.nickname}">답글</button>`;
    div.innerHTML = `
      <div>
        <span><b>${c.nickname}</b></span>
        <span style="color:#888;font-size:0.9em;">${c.cdate ? c.cdate.replace('T',' ').substring(0,16) : ''}</span>
        ${btns}
      </div>
      <div class="comment-content" data-id="${c.replyId}">${c.rcontent}</div>
      <div class="reply-form-area" id="replyFormArea-${c.replyId}"></div>
    `;
    commentList.appendChild(div);
  });
  // 버튼 이벤트 바인딩
  bindCommentEvents();
}

function bindCommentEvents() {
  // 수정
  document.querySelectorAll('.btn-edit').forEach(btn => {
    btn.onclick = function() {
      const id = this.dataset.id;
      const contentDiv = document.querySelector(`.comment-content[data-id='${id}']`);
      const oldContent = contentDiv.textContent;
      const input = document.createElement('input');
      input.type = 'text';
      input.value = oldContent;
      contentDiv.innerHTML = '';
      contentDiv.appendChild(input);
      input.focus();
      input.onblur = async function() {
        const newContent = input.value.trim();
        if (newContent && newContent !== oldContent) {
          await fetch('/api/replies', {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ replyId: id, rcontent: newContent })
          });
        }
        fetchComments();
      };
    };
  });
  // 삭제
  document.querySelectorAll('.btn-del').forEach(btn => {
    btn.onclick = async function() {
      const id = this.dataset.id;
      if (!confirm('정말 삭제하시겠습니까?')) return;
      await fetch(`/api/replies/${id}`, { method: 'DELETE' });
      fetchComments();
    };
  });
  // 답글(대댓글)
  document.querySelectorAll('.btn-reply').forEach(btn => {
    btn.onclick = function() {
      const id = this.dataset.id;
      const nick = this.dataset.nick;
      const area = document.getElementById(`replyFormArea-${id}`);
      if (area.querySelector('textarea')) return; // 이미 열려있으면 무시
      const textarea = document.createElement('textarea');
      textarea.placeholder = `@${nick} 답글 입력`;
      const submitBtn = document.createElement('button');
      submitBtn.textContent = '등록';
      submitBtn.onclick = async function() {
        const content = textarea.value.trim();
        if (!content) return;
        await fetch('/api/replies', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ boardId, parentId: id, rcontent: content })
        });
        fetchComments();
      };
      area.appendChild(textarea);
      area.appendChild(submitBtn);
      textarea.focus();
    };
  });
}

// 댓글 작성
btnAddComment?.addEventListener('click', async () => {
  const content = commentContent.value.trim();
  if (!content) {
    alert('댓글을 입력하세요');
    return;
  }
  const res = await fetch('/api/replies', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ boardId, rcontent: content })
  });
  const data = await res.json();
  if (data && data.code === '00') {
    commentContent.value = '';
    fetchComments();
  } else {
    alert(data.message || '댓글 등록 실패');
  }
});

// 페이지 진입 시 댓글 목록 조회
window.addEventListener('DOMContentLoaded', fetchComments);
