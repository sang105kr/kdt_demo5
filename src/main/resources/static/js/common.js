/**************************************/
/* 문자열의 바이트 길이 반환
/**************************************/
function getBytesSize(str){
  const encoder = new TextEncoder();
  const byteArray = encoder.encode(str);
  return byteArray.length;
}

//console.log(getBytesSize('KH정보교육원'));

export { getBytesSize }