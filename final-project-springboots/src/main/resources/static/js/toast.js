/**
 * PickQ 통합 토스트 시스템
 * - 모든 토스트: 우측 하단, 위로 쌓임
 * - 일반 알림: 초록/빨강/파랑
 * - 알림(벨): 흰색 + 초록 테두리
 * - 채팅: 노란 카카오 스타일
 */

// ── 토스트 제거 ──
function _removeToast(el) {
    if (!el || !el.parentNode) return;
    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    setTimeout(() => {
        if (el.parentNode) el.remove();
        _repositionToasts();
    }, 300);
}

// ── 모든 토스트 위치 재정렬 (우측 하단, 위로 쌓임) ──
function _repositionToasts() {
    const toasts = [...document.querySelectorAll('.pickq-toast')].reverse();
    let bottom = 30;
    toasts.forEach(t => {
        t.style.bottom = bottom + 'px';
        bottom += t.offsetHeight + 10;
    });
}

// ── 토스트 생성 공통 함수 ──
function _createToast(innerHTML, bgColor, borderLeft, onClick) {
    const toast = document.createElement('div');
    toast.className = 'pickq-toast';
    toast.style.cssText = `
        position: fixed;
        bottom: 30px;
        right: 30px;
        background: ${bgColor};
        padding: 14px 18px;
        border-radius: 12px;
        font-size: 13px;
        font-weight: 500;
        box-shadow: 0 4px 16px rgba(0,0,0,0.18);
        z-index: 9999;
        display: flex;
        flex-direction: column;
        gap: 4px;
        max-width: 320px;
        word-break: keep-all;
        opacity: 0;
        transform: translateY(20px);
        transition: opacity 0.3s ease, transform 0.3s ease, bottom 0.2s ease;
        cursor: ${onClick ? 'pointer' : 'default'};
        ${borderLeft ? 'border-left: 4px solid ' + borderLeft + ';' : ''}
    `;
    toast.innerHTML = innerHTML;
    if (onClick) toast.addEventListener('click', () => { _removeToast(toast); onClick(); });
    document.body.appendChild(toast);

    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
        _repositionToasts();
    });

    const timer = setTimeout(() => _removeToast(toast), 5000);
    toast.addEventListener('click', () => clearTimeout(timer));
    return toast;
}

function _escapeHtml(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function _pushPendingToast(payload) {
    try {
        const key = 'pickq_pending_toasts';
        const existing = sessionStorage.getItem(key);
        const list = existing ? JSON.parse(existing) : [];
        list.push(payload);
        sessionStorage.setItem(key, JSON.stringify(list));
    } catch (e) {
        // storage 사용 불가 시 무시
    }
}

function _flushPendingToasts() {
    try {
        const key = 'pickq_pending_toasts';
        const existing = sessionStorage.getItem(key);
        if (!existing) return;

        sessionStorage.removeItem(key);
        const list = JSON.parse(existing);
        if (!Array.isArray(list) || list.length === 0) return;

        list.forEach((item) => {
            if (!item || !item.type) return;
            if (item.type === 'notification') {
                showNotificationToast(item.title || '새 알림', item.message || '', item.targetUrl || '/notifications', true);
            } else if (item.type === 'chat') {
                showChatToast(item.senderName || '새 메시지', item.message || '', item.chatroomIdx, true);
            }
        });
    } catch (e) {
        // 파싱 실패 시 무시
    }
}

// ── 일반 토스트 (성공/에러/정보) ──
function showToast(message, type, onClick) {
    const styles = {
        success: { bg: '#2ecc71', color: 'white', icon: '✅' },
        error:   { bg: '#e74c3c', color: 'white', icon: '⚠️' },
        info:    { bg: '#3498db', color: 'white', icon: 'ℹ️'  }
    };
    const s = styles[type] || styles.info;

    // 패널티 키워드 강조 + 줄바꿈(\n) 처리
    const formatted = message
        .replace(/\n/g, '<br>')
        .replace(/(패널티[^<]*)/g, '<span style="background:rgba(0,0,0,0.2);padding:1px 6px;border-radius:4px;font-weight:700;">⚠️ $1</span>');

    _createToast(
        `<div style="display:flex;align-items:center;gap:8px;color:${s.color};">
            <span>${s.icon}</span><span>${_escapeHtml(message)}</span>
        </div>`,
        s.bg, null, onClick
    );
}

// ── 알림 토스트 (벨 알림, 클릭 시 알림창 이동) ──
function showNotificationToast(title, message, targetUrl, disableClick) {
    _createToast(
        `<div style="display:flex;align-items:center;gap:6px;">
            <span style="font-size:15px;">🔔</span>
            <strong style="font-size:13px;color:#222;">${_escapeHtml(title)}</strong>
            <span style="font-size:10px;margin-left:auto;color:#888;">알림</span>
        </div>
        <div style="font-size:12px;color:#555;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:270px;">
            ${_escapeHtml(message)}
        </div>`,
        '#ffffff', '#7CBD00',
        disableClick ? null : () => {
            _pushPendingToast({
                type: 'notification',
                title: title,
                message: message,
                targetUrl: targetUrl || '/notifications'
            });
            window.location.href = targetUrl || '/notifications';
        }
    );
}

// ── 채팅 토스트 (카카오 스타일, 클릭 시 채팅창 이동) ──
function showChatToast(senderName, message, chatroomIdx, disableClick) {
    _createToast(
        `<div style="display:flex;align-items:center;gap:6px;color:#3C1E1E;">
            <span style="font-size:15px;">💬</span>
            <strong style="font-size:13px;">${_escapeHtml(senderName)}</strong>
            <span style="font-size:10px;margin-left:auto;opacity:0.6;">채팅</span>
        </div>
        <div style="font-size:12px;color:#3C1E1E;opacity:0.85;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:270px;">
            ${_escapeHtml(message)}
        </div>`,
        '#FEE500', '#3C1E1E',
        disableClick ? null : () => {
            _pushPendingToast({
                type: 'chat',
                senderName: senderName,
                message: message,
                chatroomIdx: chatroomIdx
            });
            if (chatroomIdx) {
                window.open(
                    '/chats/' + chatroomIdx,
                    'chat_' + chatroomIdx,
                    'width=420,height=600,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=yes'
                );
            }
        }
    );
}

// ── 페이지 로드 시 Thymeleaf 플래시 메시지 자동 감지 ──
(function() {
    if (window._pickqToastInitialized) return;
    window._pickqToastInitialized = true;

    window.addEventListener('pageshow', function (event) {
        // 뒤로가기면 무조건 아무것도 안 함
        if (event.persisted) return;

        _flushPendingToasts();

        var successEl = document.getElementById('toast-success');
        var errorEl   = document.getElementById('toast-error');
        var bidErrEl  = document.getElementById('toast-bid-error');

        var hasMsg = (successEl && successEl.dataset.msg)
                  || (errorEl   && errorEl.dataset.msg)
                  || (bidErrEl  && bidErrEl.dataset.msg);

        if (!hasMsg) return;

        // 새로고침 중복 방지
        var key = 'toastShown_' + location.pathname + location.search;
        if (sessionStorage.getItem(key)) {
            sessionStorage.removeItem(key);
            return;
        }

        if (successEl && successEl.dataset.msg) showToast(successEl.dataset.msg, 'success');
        if (errorEl   && errorEl.dataset.msg)   showToast(errorEl.dataset.msg,   'error');
        if (bidErrEl  && bidErrEl.dataset.msg)  showToast(bidErrEl.dataset.msg,  'error');
        sessionStorage.setItem(key, '1');
    });
})();