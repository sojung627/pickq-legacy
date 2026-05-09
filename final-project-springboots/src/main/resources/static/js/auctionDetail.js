/* ── 공통: 에러 표시 함수 ── */
function showFieldError(fieldName, message) {
    const existing = document.querySelector('[data-error="' + fieldName + '"]');
    if (existing) existing.remove();
    const input = document.getElementById(fieldName) || document.querySelector('[name="' + fieldName + '"]');
    if (!input) return;
    const el = document.createElement('p');
    el.setAttribute('data-error', fieldName);
    el.style.cssText = 'color:red; font-size:12px; margin:4px 0 0;';
    el.textContent = '⚠️ ' + message;
    input.parentNode.insertBefore(el, input.nextSibling);
    setTimeout(() => el.remove(), 3000);
}

/* ── 입찰 이미지 미리보기 ── */
const bidImgEl = document.getElementById('bidImageFile');
if (bidImgEl) {
    bidImgEl.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = e => {
                document.getElementById('bidPreviewImg').src = e.target.result;
                document.getElementById('bidPreviewBox').style.display = 'block';
            };
            reader.readAsDataURL(file);
        } else {
            document.getElementById('bidPreviewBox').style.display = 'none';
        }
    });
}

/* ── 입찰 가격: 실시간 쉼표 + blur 검증 ── */
const bidPriceInput = document.getElementById('bidPrice');
if (bidPriceInput) {
    const maxPrice = parseInt(bidPriceInput.getAttribute('data-max-price')) || 0;

    function getRaw(el) {
        return parseInt(el.value.replace(/,/g, '')) || 0;
    }

    // 실시간 쉼표
    bidPriceInput.addEventListener('input', function () {
        const raw = this.value.replace(/[^0-9]/g, '');
        if (raw === '') { this.value = ''; return; }
        this.value = parseInt(raw).toLocaleString();
    });

    // blur 검증
    bidPriceInput.addEventListener('blur', function () {
        const v = getRaw(this);
        if (!this.value || v <= 0) {
            showFieldError('bidPrice', '제안 가격은 0원보다 커야 합니다.');
            this.value = '';
        } else if (v % 1000 !== 0) {
            showFieldError('bidPrice', '1000원 단위로 입력해주세요.');
            this.value = '';
        } else if (maxPrice > 0 && v > maxPrice) {
            showFieldError('bidPrice', '희망 최대가(' + maxPrice.toLocaleString() + '원)를 초과할 수 없습니다.');
            this.value = '';
        }
    });
}

/* ── 입찰 폼 제출 최종 검증 ── */
const bidForm = document.getElementById('bidForm');
if (bidForm) {
    bidForm.addEventListener('submit', function (e) {
		
		// 이미지 필수 체크
        const imgInput = document.getElementById('bidImageFile');
        if (imgInput && (!imgInput.files || imgInput.files.length === 0)) {
            alert('제안 상품 이미지를 첨부해주세요 📸');
            imgInput.focus();
            e.preventDefault();
            return;
        }
		
		// 가격 검증
        const inp = document.getElementById('bidPrice');
        if (!inp) return;
        const raw = inp.value.replace(/,/g, '');
        const v = parseInt(raw);
        const max = parseInt(inp.getAttribute('data-max-price')) || 0;

        if (!raw || isNaN(v) || v <= 0) {
            showFieldError('bidPrice', '제안 가격은 0원보다 커야 합니다.');
            inp.value = ''; e.preventDefault();
        } else if (v % 1000 !== 0) {
            showFieldError('bidPrice', '1000원 단위로 입력해주세요.');
            inp.value = ''; e.preventDefault();
        } else if (max > 0 && v > max) {
            showFieldError('bidPrice', '희망 최대가(' + max.toLocaleString() + '원)를 초과할 수 없습니다.');
            inp.value = ''; e.preventDefault();
        } else {
            // 쉼표 제거 후 전송
            inp.value = raw;
        }
		
		
    });
}
/* ── 실시간 타이머 (상세 페이지) ── */
function initDetailTimer() {
    const timerEl = document.getElementById('realtime-timer');
    if (!timerEl) return;

    function pad(n) { return String(n).padStart(2, '0'); }

    function update() {
        const status   = parseInt(timerEl.getAttribute('data-status'));
        const endTime  = new Date(timerEl.getAttribute('data-end'));
        const deadline = new Date(timerEl.getAttribute('data-deadline'));
        const now      = new Date();

        let target = null;
        let prefix = '';
        if (status === 1)      { target = endTime;  prefix = '입찰 '; }
        else if (status === 2) { target = deadline; prefix = '결정 '; }
        else { timerEl.innerText = ''; return; }

        if (!target || isNaN(target.getTime())) { timerEl.innerText = ''; return; }

        const diff = target - now;
        if (diff <= 0) {
            timerEl.innerText = '';
            if (!timerEl.getAttribute('data-reloaded')) {
                timerEl.setAttribute('data-reloaded', 'true');
                setTimeout(() => location.reload(), 1500);
            }
            return;
        }

        const totalSeconds = Math.floor(diff / 1000);
        const d = Math.floor(totalSeconds / 86400);
        const h = Math.floor((totalSeconds % 86400) / 3600);
        const m = Math.floor((totalSeconds % 3600) / 60);
        const s = totalSeconds % 60;

        let str = prefix;
        if (d >= 1) {
            str += d + '일 ' + h + '시간';
        } else if (h >= 1) {
            str += h + '시간 ' + m + '분';
        } else {
            str += pad(m) + ':' + pad(s);
        }
        if (timerEl.innerText !== str) timerEl.innerText = str;
    }

    setInterval(update, 1000);
    update();
}

window.addEventListener('load', initDetailTimer);

/* ── 경매 상태 실시간 반영 (입찰폼 강제 이동 / 상세 즉시 반영) ── */
function initAuctionStatusSocket() {
    const root = document.getElementById('auction-page-root');
    if (!root) return;

    const auctionIdx = root.getAttribute('data-auction-idx');
    const pageType = root.getAttribute('data-page-type') || '';
    if (!auctionIdx || !window.SockJS || !window.Stomp) return;

    const terminalTypes = new Set([
        'AUCTION_BID_CLOSED',
        'AUCTION_DECISION_CLOSED',
        'AUCTION_CANCELED',
        'AUCTION_DELETED',
        'AUCTION_FAILED',
        'AUCTION_WINNER_SELECTED'
    ]);

    const socket = new SockJS('/ws-notification');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/auctions/' + auctionIdx + '/status', function (frame) {
            let payload = {};
            try {
                payload = JSON.parse(frame.body || '{}');
            } catch (e) {
                payload = {};
            }

            const type = payload.notificationType || '';
            if (!terminalTypes.has(type)) {
                return;
            }

            if (pageType === 'bid-form') {
                if (window._auctionBidFormLocked) return;
                window._auctionBidFormLocked = true;

                const modal = document.createElement('div');
                modal.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.35);display:flex;align-items:center;justify-content:center;z-index:10000;';
                modal.innerHTML = ''
                    + '<div style="width:min(92vw,420px);background:#fff;border-radius:12px;padding:20px 18px;box-shadow:0 12px 28px rgba(0,0,0,.2);text-align:center;">'
                    + '  <p style="margin:0;font-size:16px;font-weight:600;color:#222;line-height:1.5;">입찰할 수 없는 경매입니다. 경매 목록으로 이동합니다.</p>'
                    + '</div>';
                document.body.appendChild(modal);

                setTimeout(function () {
                    window.location.href = '/auctions';
                }, 1200);
                return;
            }

            root.setAttribute('data-auction-status', '3');
            const shell = root.querySelector('.auction-state-shell');
            const row = shell ? shell.querySelector('.row.g-4') : null;
            if (shell) shell.classList.add('is-inactive');
            if (row) row.classList.add('auction-terminal-locked');

            document.querySelectorAll('form[action*="/bids"], form[action*="/close"], form[action*="/fail"], form[action*="/delete"], form[action*="/win"]').forEach(function (form) {
                form.querySelectorAll('button[type="submit"]').forEach(function (btn) {
                    btn.disabled = true;
                    btn.classList.add('opacity-50');
                    btn.classList.add('cursor-not-allowed');
                });
            });

            if (!window._auctionDetailReloadScheduled) {
                window._auctionDetailReloadScheduled = true;
                setTimeout(function () {
                    window.location.reload();
                }, 800);
            }
        });
    });
}

window.addEventListener('load', initAuctionStatusSocket);

/* ── 낙찰 전 최종 확인 및 제출 (초간결 버전) ── */
function confirmWin(e, form) {
    // 일단 폼 제출을 막음 (확인 버튼 누르기 전까지)
    e.preventDefault();

    // 사용자한테 한 번 더 물어보기
    if (!confirm('이 입찰을 낙찰하시겠습니까?')) return;

    // 중복 클릭 방지 (이거 안 하면 낙찰이 두 번 될 수도 있음)
    const btn = form.querySelector('button');
    if(btn) {
        btn.disabled = true;
        btn.innerText = '처리 중...';
    }

    // 이제 진짜 폼을 제출
    // 인터셉터를 피해서 컨트롤러로 바로 꽂아줌
    form.submit();
}