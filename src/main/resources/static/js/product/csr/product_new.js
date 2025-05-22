import { ajax, PaginationUI } from '/js/common.js';

const doProduct = ()=>{
  // 상수 및 설정
  const CONFIG = {
    recordsPerPage: 10,     //한 페이지에 보여줄 행 수
    pagesPerPage: 10        //한 페이지에 보여줄 페이지 수
  };

  // 상태 관리
  const state = {
    currentPage: 1
  };

  // DOM 요소 참조
  const elements = {
    addProductForm: null,           // 상품추가
    readProductForm: null,          // 상품조회

    productId2: null,               // 상품아이디
    pname2: null,                   // 상품명
    quantity2: null,                // 상품수량
    price2: null,                   // 상품가격

    list: document.createElement('div'),            // 상품목록
    pagination: document.createElement('div')       // 상품페이지
  };

  // 필드 구성
  const formFieldsConfig = {
    add: [
      { label: '상품명', name: 'pname' },
      { label: '수량', name: 'quantity' },
      { label: '가격', name: 'price' }
    ]
  };

  // API 호출 핸들러
  const api = {
    async call(method, url, data) {
      try {
        const result = await ajax[method](url, data);
        return result;
      } catch (err) {
        console.error(`API 호출 오류 (${method.toUpperCase()} ${url}):`, err);
        alert(`데이터 처리 중 오류가 발생했습니다: ${err.message}`);
        return null;
      }
    },

    async addProduct(product) {
      const url = '/api/products';
      const result = await this.call('post', url, product);

      if (result && result.header.rtcd === 'S00') {
        console.log('상품 등록 성공:', result.body);
        if (elements.addProductForm) {
          elements.addProductForm.reset();
          elements.addProductForm.querySelectorAll('.field-error.client').forEach(span => span.textContent = '');
        }
        this.getProducts(1);
      } else {
        console.log('상품 등록 실패:', result?.header.rtmsg);
        alert(`상품 등록 실패: ${result?.header.rtmsg || '알 수 없는 오류'}`);
      }
    },

    async getProduct(pid) {
      const url = `/api/products/${pid}`;
      const result = await this.call('get', url);

      if (result && result.header.rtcd === 'S00') {
        console.log('상품 조회 성공:', result.body);
        ui.updateFormFields(result.body);
      } else {
        console.log('상품 조회 실패:', result?.header.rtmsg);
        alert(`상품 조회 실패: ${result?.header.rtmsg || '알 수 없는 오류'}`);
      }
    },

    async deleteProduct(pid, formToReset) {
      const url = `/api/products/${pid}`;
      const result = await this.call('delete', url);

      if (result && result.header.rtcd === 'S00') {
        console.log('상품 삭제 성공:', result.body);
        if (formToReset) {
          formToReset.reset();
        }
        this.getProducts(state.currentPage);
      } else {
        console.log('상품 삭제 실패:', result?.header.rtmsg);
        alert(`상품 삭제 실패: ${result?.header.rtmsg || '알 수 없는 오류'}`);
      }
    },

    async modifyProduct(pid, product) {
      const url = `/api/products/${pid}`;
      const result = await this.call('patch', url, product);

      if (result && result.header.rtcd === 'S00') {
        console.log('상품 수정 성공:', result.body);
        this.getProducts(state.currentPage);
      } else {
        console.log('상품 수정 실패:', result?.header.rtmsg);
        alert(`상품 수정 실패: ${result?.header.rtmsg || '알 수 없는 오류'}`);
      }
    },

    async getProducts(pageNo) {
      const url = `/api/products/paging?pageNo=${pageNo}&numOfRows=${CONFIG.recordsPerPage}`;
      const result = await this.call('get', url);

      if (result && result.header.rtcd === 'S00') {
        console.log('상품 목록 조회 성공 (페이지:', pageNo, ')');
        state.currentPage = pageNo;
        ui.displayProductList(result.body || []);
      } else {
        console.log('상품 목록 조회 실패:', result?.header.rtmsg);
        ui.displayProductList([]);
      }
    },

    async getTotalCount() {
      const url = '/api/products/totCnt';
      const result = await this.call('get', url);

      if (result && result.body != null) {
        return Number(result.body);
      }
      return 0;
    }
  };

  // UI 핸들러
  const ui = {
    updateFormFields(product) {
      if (elements.productId2 && elements.pname2 && elements.quantity2 && elements.price2) {
        elements.productId2.value = product.productId || '';
        elements.pname2.value = product.pname || '';
        elements.quantity2.value = product.quantity || '';
        elements.price2.value = product.price || '';
      } else {
        console.error('상품 조회/수정 폼 필드가 초기화되지 않았습니다.');
      }
    },

    validateForm(formElement, fieldConfigs) {
      let isValid = true;
      // 기존 에러 메시지 초기화
      fieldConfigs.forEach(field => {
        const errSpan = formElement.querySelector(`#err${field.name.charAt(0).toUpperCase() + field.name.slice(1)}`);
        if (errSpan) errSpan.textContent = '';
      });

      for (const field of fieldConfigs) {
        const inputElement = formElement[field.name];
        if (inputElement.value.trim().length === 0) {
          const errSpan = formElement.querySelector(`#err${field.name.charAt(0).toUpperCase() + field.name.slice(1)}`);
          if (errSpan) errSpan.textContent = `${field.label}은(는) 필수 항목입니다.`;
          if (isValid) {
            inputElement.focus();
          }
          isValid = false;
        }
      }
      return isValid;
    },

    //상품 등록 양식
    createAddForm() {
      const $formContainer = document.createElement('div');
      $formContainer.innerHTML = `
        <form id="frmAddProduct">
          ${formFieldsConfig.add.map(field => `
            <div>
              <label for="${field.name}">${field.label}</label>
              <input type="text" id="${field.name}" name="${field.name}"/>
              <span class="field-error client" id="err${field.name.charAt(0).toUpperCase() + field.name.slice(1)}"></span>
            </div>
          `).join('')}
          <div>
              <button type="submit">등록</button>
          </div>
        </form>
      `;

      elements.addProductForm = $formContainer.querySelector('#frmAddProduct');

      elements.addProductForm.addEventListener('submit', e => {
        e.preventDefault();
        if (!this.validateForm(elements.addProductForm, formFieldsConfig.add)) return;

        const formData = new FormData(e.target);
        const product = Object.fromEntries(formData.entries());
        api.addProduct(product);
      });

      document.body.insertAdjacentElement('afterbegin', $formContainer);
    },

    //상품 조회 양식
    createReadForm() {
      const $readFormContainer = document.createElement('div');
      $readFormContainer.innerHTML = `
        <form id="frm2">
          <div>
              <label for="productId2">상품아이디</label>
              <input type="text" id="productId2" name="productId" readonly />
          </div>
          <div>
              <label for="pname2">상품명</label>
              <input type="text" id="pname2" name="pname" />
          </div>
          <div>
              <label for="quantity2">수량</label>
              <input type="text" id="quantity2" name="quantity" />
          </div>
          <div>
              <label for="price2">가격</label>
              <input type="text" id="price2" name="price" />
          </div>
          <div class='btns'></div>
        </form>
      `;

      elements.readProductForm = $readFormContainer.querySelector('#frm2');

      // 폼 필드 요소들 참조 할당
      if (elements.readProductForm) {
        elements.productId2 = elements.readProductForm.querySelector('#productId2');
        elements.pname2 = elements.readProductForm.querySelector('#pname2');
        elements.quantity2 = elements.readProductForm.querySelector('#quantity2');
        elements.price2 = elements.readProductForm.querySelector('#price2');
      }

      this.changeReadMode(elements.readProductForm);
      document.body.insertAdjacentElement('afterbegin', $readFormContainer);
    },

    //상품 조회 모드
    changeReadMode(frm) {
      frm.classList.remove('mode-edit');
      frm.classList.add('mode-read');
      this.toggleInputReadOnly(frm, true);

      const $btns = frm.querySelector('.btns');
      $btns.innerHTML = `
        <button id="btnEdit" type="button">수정</button>
        <button id="btnDelete" type="button">삭제</button>
      `;

      const $btnEdit = $btns.querySelector('#btnEdit');
      const $btnDelete = $btns.querySelector('#btnDelete');

      $btnEdit.replaceWith($btnEdit.cloneNode(true));
      $btns.querySelector('#btnEdit').addEventListener('click', () => this.changeEditMode(frm));

      $btnDelete.replaceWith($btnDelete.cloneNode(true));
      $btns.querySelector('#btnDelete').addEventListener('click', () => {
        const pid = frm.productId.value;
        if (!pid) {
          alert('상품조회 후 삭제바랍니다.');
          return;
        }
        if (confirm('삭제하시겠습니까?')) api.deleteProduct(pid, frm);
      });
    },
    //상품 수정 모드
    changeEditMode(frm) {
      frm.classList.remove('mode-read');
      frm.classList.add('mode-edit');
      this.toggleInputReadOnly(frm, false);

      const $btns = frm.querySelector('.btns');
      $btns.innerHTML = `
        <button id="btnSave" type="button">저장</button>
        <button id="btnCancel" type="button">취소</button>
      `;

      const $btnSave = $btns.querySelector('#btnSave');
      const $btnCancel = $btns.querySelector('#btnCancel');

      $btnSave.replaceWith($btnSave.cloneNode(true));
      $btns.querySelector('#btnSave').addEventListener('click', () => {
        const formData = new FormData(frm);
        const product = Object.fromEntries(formData.entries());
        if (!product.productId) {
          alert('상품 ID가 없습니다. 수정할 수 없습니다.');
          return;
        }
        api.modifyProduct(product.productId, product);
        api.getProduct(product.productId);
        this.changeReadMode(frm);
      });

      $btnCancel.replaceWith($btnCancel.cloneNode(true));
      $btns.querySelector('#btnCancel').addEventListener('click', () => {
        if (frm.productId.value) {
          api.getProduct(frm.productId.value);
        }
        this.changeReadMode(frm);
      });
    },

    toggleInputReadOnly(frm, isReadOnly) {
      [...frm.querySelectorAll('input')].forEach(input => {
        if (input.name !== 'productId') {
          input.readOnly = isReadOnly;
        }
      });
    },

    displayProductList(products) {
      elements.list.setAttribute('id', 'list');

      if (!Array.isArray(products)) {
        console.error('displayProductList: products 인자는 배열이어야 합니다.', products);
        elements.list.innerHTML = '<tr><td colspan="2">상품 데이터를 불러올 수 없습니다.</td></tr>';
        return;
      }

      const makeTr = products.map(product => `
        <tr data-pid="${product.productId}">
          <td>${product.productId}</td>
          <td>${product.pname}</td>
        </tr>
      `).join('');

      elements.list.innerHTML = `
        <table>
          <caption>상품 목록</caption>
          <thead>
            <tr>
              <th>상품번호</th>
              <th>상품명</th>
            </tr>
          </thead>
          <tbody>
            ${makeTr || '<tr><td colspan="2">상품이 없습니다.</td></tr>'}
          </tbody>
        </table>`;

      const $productRows = elements.list.querySelectorAll('tbody tr[data-pid]');
      $productRows.forEach(productRow =>
        productRow.addEventListener('click', e => {
          const pid = e.currentTarget.dataset.pid;
          api.getProduct(pid);
        })
      );
    },

    // 페이지네이션
    async initPagination() {
      elements.pagination.setAttribute('id', 'reply_pagenation');

      const totalRecords = await api.getTotalCount();

      if (totalRecords > 0) {
        const paginationCallback = (page) => api.getProducts(page);

        const pagination = new PaginationUI('reply_pagenation', paginationCallback);
        pagination.setTotalRecords(totalRecords);
        pagination.setRecordsPerPage(CONFIG.recordsPerPage);
        pagination.setPagesPerPage(CONFIG.pagesPerPage);

        pagination.handleFirstClick();
      } else {
        this.displayProductList([]);
        console.log('등록된 상품이 없습니다.');
      }
    }
  };

  // 초기화 및 실행
  const init = async () => {
    // DOM 요소 준비
    document.body.appendChild(elements.list);
    document.body.appendChild(elements.pagination);

    // 폼 생성
    ui.createReadForm();
    ui.createAddForm();

    // 페이지네이션 및 데이터 로드
    await ui.initPagination();
  };

  // 공개 API
  return {
    init
  };
}
// 상품 관리 모듈
const ProductManager = doProduct();

// 애플리케이션 시작
document.addEventListener('DOMContentLoaded', ProductManager.init);