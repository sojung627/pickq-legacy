function toggleReplyForm(replyIdx) {
  const el = document.getElementById('replyForm-' + replyIdx);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}

function toggleEditForm(replyIdx) {
  const el = document.getElementById('editForm-' + replyIdx);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}

function validateReply(textarea) {
  if (textarea.value.trim().length < 3) {
    alert('댓글은 3글자 이상 입력해주세요.');
    textarea.focus();
    return false;
  }
  return true;
}

// 로그인 후 복귀 시 답글 폼 자동 오픈
document.addEventListener('DOMContentLoaded', function () {
  const params = new URLSearchParams(window.location.search);
  const openReply = params.get('openReply');
  if (openReply) {
    const el = document.getElementById('replyForm-' + openReply);
    if (el) {
      el.style.display = 'block';
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      const textarea = el.querySelector('textarea');
      if (textarea) textarea.focus();
    }
  }
});
