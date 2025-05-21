// const $button = document.createElement('button');
// $button.textContent = '목록 가져오기';

// $button.addEventListener('click',e=>{
//   productList()
// });

// const $body = document.querySelector('body');
// $body.append($button);


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

    //상품목록화면
    displayProductList(json.body);

  }catch (err){
    console.error(err.message);
  }  

}

function displayProductList(list) {

  const $div = document.createElement('div');
  const makeTr = list => list.map(product =>
      `<tr>
          <td>${product.productId}</td>
          <td>${product.pname}</td>      
      </tr>`
    ).join('');

  $div.innerHTML = `
    <table>
      <caption>상품목록</caption>
      <thead>
        <tr>
          <th>상품번호</th>
          <th>상품명</th>
        </tr>
      </thead>
      <tbody>
        ${makeTr(list)}
      </tbody>
    </table>
  `;

  document.body.append($div);
}


productList();