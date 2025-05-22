import { ajax,  PaginationUI} from '/js/common.js';

let currentPage = 1; // 현재 페이지를 위한 전역 변수
let initialPage = 1; // 상품 추가 후 이동할 페이지 (1페이지)

const recordsPerPage = 10;        // 페이지당 레코드수
const pagesPerPage = 10;          // 한페이지당 페이지수

//상품등록
const addProduct = async product => {
  try {
    const url = '/api/products';
    const result = await ajax.post(url, product);
    if (result.header.rtcd === 'S00') {
      console.log(result.body);
      frm.reset();
      initialPage = 1; // 생성 후 1페이지로 이동
      getProducts(initialPage, recordsPerPage); // 첫 페이지의 기본 레코드로 호출
    } else if(result.header.rtcd.substr(0,1) == 'E'){
        for(let key in result.header.details){
            console.log(`필드명:${key}, 오류:${result.header.details[key]}`);
        }
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err.message);
  }
};

//상품조회
const getProduct = async pid => {
  try {
    const url = `/api/products/${pid}`;
    const result = await ajax.get(url);
    console.log(result);
    if (result.header.rtcd === 'S00') {
      console.log(result.body);
      // productId2.value = result.body.productId;
      productId2.setAttribute('value', result.body.productId);
      pname2.setAttribute('value', result.body.pname);
      quantity2.setAttribute('value', result.body.quantity);
      price2.setAttribute('value', result.body.price);

      productId2.value = result.body.productId;
      pname2.value = result.body.pname;
      quantity2.value =  result.body.quantity;
      price2.value = result.body.price;

    } else if(result.header.rtcd.substr(0,1) == 'E'){
        for(let key in result.header.details){
            console.log(`필드명:${key}, 오류:${result.header.details[key]}`);
        }
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
  }
};

//상품삭제
const delProduct = async (pid, frm) => {
  try {
    const url = `/api/products/${pid}`;
    const result = await ajax.delete(url);
    console.log(result);
    if (result.header.rtcd === 'S00') {
      console.log(result.body);
      const $inputs = frm.querySelectorAll('input');
      [...$inputs].forEach(ele => (ele.value = '')); //폼필드 초기화
      getProducts(currentPage, recordsPerPage); // 현재 페이지의 기본 레코드로 호출
    } else if(result.header.rtcd.substr(0,1) == 'E'){
        for(let key in result.header.details){
            console.log(`필드명:${key}, 오류:${result.header.details[key]}`);
        }
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
  }
};

//상품수정
const modifyProduct = async (pid, product) => {
  try {
    const url = `/api/products/${pid}`;
    const result = await ajax.patch(url, product);
    if (result.header.rtcd === 'S00') {
      console.log(result.body);
      getProducts(currentPage, recordsPerPage); // 현재 페이지의 기본 레코드로 호출
    } else if(result.header.rtcd.substr(0,1) == 'E'){
        for(let key in result.header.details){
            console.log(`필드명:${key}, 오류:${result.header.details[key]}`);
        }
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err.message);
  }
};

//상품목록
const getProducts = async (reqPage, reqRec) => {

  try {
    const url = `/api/products/paging?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);

    if (result.header.rtcd === 'S00') {
      currentPage = reqPage; // 현재 페이지 업데이트
      displayProductList(result.body);

    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
  }
};

//상품등록 화면
function displayForm() {
  //상품등록
  const $addFormWrap = document.createElement('div');
  $addFormWrap.innerHTML = `
    <form id="frm">
      <div>
          <label for="pname">상품명</label>
          <input type="text" id="pname" name="pname"/>
          <span class="field-error client" id="errPname"></span>
      </div>
      <div>
          <label for="quantity">수량</label>
          <input type="text" id="quantity" name="quantity"/>
          <span class="field-error client" id="errQuantity"></span>
      </div>
      <div>
          <label for="price">가격</label>
          <input type="text" id="price" name="price"/>
          <span class="field-error client" id="errPrice"></span>
      </div>
      <div>
          <button id="btnAdd" type="submit">등록</button>
      </div>
    </form>
  `;
  document.body.insertAdjacentElement('afterbegin', $addFormWrap);
  const $frm = $addFormWrap.querySelector('#frm');
  $frm.addEventListener('submit', e => {
    e.preventDefault(); // 기본동작 중지

    //유효성 체크
    if($frm.pname.value.trim().length === 0) {
      errPname.textContent = '상품명은 필수 입니다';
      $frm.pname.focus();
      return;
    }
    if($frm.quantity.value.trim().length === 0) {
      errQuantity.textContent = '수량 필수 입니다';
      $frm.quantity.focus();
      return;
    }
    if($frm.price.value.trim().length === 0) {
      errPrice.textContent = '가격은 필수 입니다';
      $frm.price.focus();
      return;
    }

    const formData = new FormData(e.target); //폼데이터가져오기
    const product = {};
    [...formData.keys()].forEach(
      ele => (product[ele] = formData.get(ele)),
    ); // {pname:'책상', quantitiy:10, price:100 }

    addProduct(product);

  });
}

//상품조회 화면
function displayReadForm() {
  //상태 : 조회 mode-read, 편집 mode-edit
  const changeEditMode = frm => {
    frm.classList.toggle('mode-edit', true);
    [...frm.querySelectorAll('input')]
      .filter(input => input.name !== 'productId')
      .forEach(input => input.removeAttribute('readonly'));

    const $btns = frm.querySelector('.btns');
    $btns.innerHTML = `
      <button id="btnSave" type="button">저장</button>
      <button id="btnCancel" type="button">취소</button>
    `;

    const $btnSave = $btns.querySelector('#btnSave');
    const $btnCancel = $btns.querySelector('#btnCancel');

    //저장
    $btnSave.addEventListener('click', e => {
      const formData = new FormData(frm); //폼데이터가져오기
      const product = {};

      [...formData.keys()].forEach(
        ele => (product[ele] = formData.get(ele)),
      ); // {pname:'책상', quantitiy:10, price:100 }

      modifyProduct(product.productId, product); //수정
      getProduct(product.productId); //조회
      changeReadMode(frm); //읽기모드
    });

    //취소
    $btnCancel.addEventListener('click', e => {
      frm.reset(); //초기화
      changeReadMode(frm);
    });
  };

  const changeReadMode = frm => {
    frm.classList.toggle('mode-read', true);
    [...frm.querySelectorAll('input')]
      .filter(input => input.name !== 'productId')
      .forEach(input => input.setAttribute('readonly', ''));

    const $btns = frm.querySelector('.btns');
    $btns.innerHTML = `
      <button id="btnEdit" type="button">수정</button>
      <button id="btnDelete" type="button">삭제</button>
    `;

    const $btnDelete = $btns.querySelector('#btnDelete');
    const $btnEdit = $btns.querySelector('#btnEdit');

    //수정
    $btnEdit.addEventListener('click', e => {
      changeEditMode(frm);
    });

    //삭제
    $btnDelete.addEventListener('click', e => {
      const pid = frm.productId.value;
      if (!pid) {
        alert('상품조회 후 삭제바랍니다.');
        return;
      }

      if (!confirm('삭제하시겠습니까?')) return;
      delProduct(pid, frm);
    });
  };

  const $readFormWrap = document.createElement('div');
  $readFormWrap.innerHTML = `
    <form id="frm2">

      <div>
          <label for="productId2">상품아이디</label>
          <input type="text" id="productId2" name="productId" readonly/>
      </div>
      <div>
          <label for="pname">상품명</label>
          <input type="text" id="pname2" name="pname"/>
      </div>
      <div>
          <label for="quantity">수량</label>
          <input type="text" id="quantity2" name="quantity"/>
      </div>
      </div>
      <div>
          <label for="price">가격</label>
          <input type="text" id="price2" name="price"/>
      </div>
      </div>
      <div class='btns'></div>

    </form>
  `;
  document.body.insertAdjacentElement('afterbegin', $readFormWrap);
  const $frm2 = $readFormWrap.querySelector('#frm2');
  changeReadMode($frm2);
}

//상품목록 화면
function displayProductList(products) {

  const makeTr = products => {
    const $tr = products
      .map(
        product =>
          `<tr data-pid=${product.productId}>
            <td>${product.productId}</td>
            <td>${product.pname}</td></tr>`,
      )
      .join('');
    return $tr;
  };

  $list.innerHTML = `
    <table>
      <caption> 상 품 목 록 </caption>
      <thead>
        <tr>
          <th>상품번호</th>
          <th>상품명</th>
        </tr>
      </thead>
      <tbody>
        ${makeTr(products)}
      </tbody>
    </table>`;

  const $products = $list.querySelectorAll('table tbody tr');

  // Array.from($products)
  [...$products].forEach(product =>
    product.addEventListener('click', e => {
      const pid = e.currentTarget.dataset.pid;
      getProduct(pid);
    }),
  );
}

displayReadForm(); //조회
displayForm();//등록
//getProducts();//목록

const $list = document.createElement('div');
$list.setAttribute('id','list')
document.body.appendChild($list);

const divEle = document.createElement('div');
divEle.setAttribute('id','reply_pagenation');
document.body.appendChild(divEle);

(async ()=>{
  const url = '/api/products/totCnt';
  try {
    const result = await ajax.get(url);

    const totalRecords = result.body; // 전체 레코드수

    const handlePageChange = (reqPage)=>{
      return getProducts(reqPage,recordsPerPage);
    };

    // Pagination UI 초기화
    var pagination = new PaginationUI('reply_pagenation', handlePageChange);

    pagination.setTotalRecords(totalRecords);       //총건수
    pagination.setRecordsPerPage(recordsPerPage);   //한페이지당 레코드수
    pagination.setPagesPerPage(pagesPerPage);       //한페이지당 페이지수

    // 첫페이지 가져오기
    pagination.handleFirstClick();

  }catch(err){
    console.error(err);
  }
})();