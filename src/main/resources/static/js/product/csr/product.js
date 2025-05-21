const $button = document.createElement('button');
$button.textContent = '목록 가져오기';

$button.addEventListener('click',e=>{
  productList()
});

const $body = document.querySelector('body');
$body.append($button);

async function productList () {

  const pageNo = 1;
  const numOfRows = 10;  

  const option = {
    method: 'GET',
    headers: {
      Accept: 'application/json'
    }
  };
  const url = `/api/products/paging?pageNo=${pageNo}&numOfRows=${numOfRows}`;

  try {
    const res = await fetch(url, option);
    if(!res.ok){
      throw new Error(`응답오류! : ${res.status}`);
    }
    const json = await res.json();  //응답메세지 body내 json포맷문자열은 js객체로 변환
    console.log(json);
    console.log(json.header);
    console.log(json.body);
    console.log(json.paging);
  }catch (err){
    console.error(err.message);
  }  

}