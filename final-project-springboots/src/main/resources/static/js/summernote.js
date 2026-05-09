// Summernote 공통 초기화
function initSummernote(targetId, onChangeCallback) {
  $(document).ready(function () {
    $('#' + targetId).summernote({
      lang: 'ko-KR',
      height: 400,
      callbacks: {
        onImageUpload: function (files) {
          for (var i = 0; i < files.length; i++) {
            uploadSummernoteImage(files[i], targetId);
          }
        },
        onChange: function (contents) {
          if (typeof onChangeCallback === 'function') {
            onChangeCallback(contents);
          }
        }
      }
    });
  });
}

// 이미지 업로드
function uploadSummernoteImage(file, targetId) {
  var formData = new FormData();
  formData.append('file', file);
  $.ajax({
    url: '/summernoteImageUpload',
    type: 'POST',
    data: formData,
    processData: false,
    contentType: false,
    success: function (res) {
      $('#' + targetId).summernote('insertImage', res.url);
    }
  });
}

// 폼 제출 시 summernote 내용을 textarea에 반영
function bindSummernoteSubmit(formId, targetId) {
  document.getElementById(formId).addEventListener('submit', function () {
    document.getElementById(targetId).value = $('#' + targetId).summernote('code');
  });
}
