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