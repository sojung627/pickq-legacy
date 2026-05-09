// 게시글 작성 임시저장 (draft) 관련 기능

var DRAFT_TITLE_KEY = 'board_draft_title';
var DRAFT_CONTENT_KEY = 'board_draft_content';

// 제목 저장
function saveDraftTitle(value) {
  localStorage.setItem(DRAFT_TITLE_KEY, value);
}

// 내용 저장 (summernote onChange 콜백에서 호출)
function saveDraftContent(value) {
  localStorage.setItem(DRAFT_CONTENT_KEY, value);
}

// draft 삭제
function clearDraft() {
  localStorage.removeItem(DRAFT_TITLE_KEY);
  localStorage.removeItem(DRAFT_CONTENT_KEY);
}

// 글쓰기 폼 초기화 (boardNew.html에서 호출)
function initBoardForm(formId) {

  // 제목 복원 및 자동 저장
  var titleInput = document.querySelector('[name=boardTitle]');
  if (titleInput) {
    var savedTitle = localStorage.getItem(DRAFT_TITLE_KEY);
    if (savedTitle) titleInput.value = savedTitle;

    titleInput.addEventListener('input', function () {
      saveDraftTitle(this.value);
    });
  }

  // summernote 내용 복원 (초기화 완료 후 실행)
  setTimeout(function () {
    var savedContent = localStorage.getItem(DRAFT_CONTENT_KEY);
    if (savedContent) {
      var editor = $('#boardContent');
      if (editor.length) {
        editor.summernote('code', savedContent);
      }
    }
  }, 500);

  // 폼 제출 시 summernote 내용 textarea에 반영만 함
  // clearDraft()는 여기서 호출하지 않음
  // → 세션 만료 시 draft 유지, 등록 성공 시 boardList.html에서 clearDraft() 호출
  var form = document.getElementById(formId);
  if (form) {
    form.addEventListener('submit', function () {
      document.getElementById('boardContent').value = $('#boardContent').summernote('code');
    });
  }
}

// 게시글 목록 페이지에서 draft 삭제 (등록 성공 후 호출)
function clearDraftOnSuccess() {
  if (localStorage.getItem(DRAFT_TITLE_KEY) || localStorage.getItem(DRAFT_CONTENT_KEY)) {
    clearDraft();
  }
}
