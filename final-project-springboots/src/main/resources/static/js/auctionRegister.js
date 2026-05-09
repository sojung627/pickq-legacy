// ── Flatpickr 초기화 ──
const min24h = new Date(Date.now() + 24 * 60 * 60 * 1000);

// ── 에러 메시지 표시 함수 ──
function showFieldError(fieldName, message) {
    const existing = document.querySelector('[data-error="' + fieldName + '"]');
    if (existing) existing.remove();

    const input = document.querySelector('[name="' + fieldName + '"], #' + fieldName);
    if (!input) return;

    const errorEl = document.createElement('p');
    errorEl.setAttribute('data-error', fieldName);
    errorEl.style.cssText = 'color:red; font-size:12px; margin:4px 0 0;';
    errorEl.textContent = '⚠️ ' + message;
    input.parentNode.insertBefore(errorEl, input.nextSibling);

    setTimeout(() => errorEl.remove(), 3000);
}

function clearFieldError(fieldName) {
    const el = document.querySelector('[data-error="' + fieldName + '"]');
    if (el) el.remove();
}

// ── 결정 마감일 Flatpickr ──
let dlPicker = flatpickr('#auctionDecisionDeadline', {
    locale: 'ko',
    enableTime: true,
    dateFormat: 'Y-m-d H:i',
    time_24hr: true,
    disableMobile: true,
    clickOpens: false,
    onOpen: function() {
        // 달력 열릴 때마다 입찰마감일 기준 24시간 후로 minDate 갱신
        const endVal = endPicker.selectedDates[0];
        if (endVal) {
            dlPicker.set('minDate', new Date(endVal.getTime() + 24 * 60 * 60 * 1000));
        }
    },
    onChange: function(selectedDates) {
        if (!selectedDates[0]) return;
        clearFieldError('auctionDecisionDeadline');
    }
});

// ── 입찰 마감일 Flatpickr ──
const endPicker = flatpickr('#auctionEndAt', {
    locale: 'ko',
    enableTime: true,
    dateFormat: 'Y-m-d H:i',
    minDate: new Date(Date.now() + 24 * 60 * 60 * 1000),
    time_24hr: true,
    disableMobile: true,
    onOpen: function() {
        // 열릴 때마다 현재 기준 24시간 후로 갱신
        endPicker.set('minDate', new Date(Date.now() + 24 * 60 * 60 * 1000));
    },
    onChange: function(selectedDates) {
        if (!selectedDates[0]) return;
        clearFieldError('auctionEndAt');

        const sel   = selectedDates[0];
        const dlMin = new Date(sel.getTime() + 24 * 60 * 60 * 1000);
        const dlMax = new Date(sel.getTime() + 3 * 24 * 60 * 60 * 1000);

        document.getElementById('auctionDecisionDeadline').classList.remove('disabled-input');
        dlPicker.set('clickOpens', true);
        dlPicker.set('minDate', dlMin);
        dlPicker.set('maxDate', dlMax);
        dlPicker.clear();
        clearFieldError('auctionDecisionDeadline');
    }
});

// ── 이미지 미리보기 ──
document.getElementById('thumbnailFile').addEventListener('change', function () {
    const file        = this.files[0];
    const previewImg  = document.getElementById('previewImg');
    const placeholder = document.getElementById('previewPlaceholder');

    if (file) {
        const reader = new FileReader();
        reader.onload = e => {
            previewImg.src = e.target.result;
            previewImg.classList.remove('hidden');
            previewImg.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);
    } else {
        previewImg.classList.add('hidden');
        previewImg.style.display = '';
        if (placeholder) placeholder.style.display = '';
    }
});

/* ── 희망 최대가: 실시간 쉼표 + blur 검증 ── */
const targetPriceInput = document.querySelector('[name="auctionTargetPrice"]');
if (targetPriceInput) {

    // 실시간 쉼표 (타이핑할 때마다)
    targetPriceInput.addEventListener('input', function () {
        const raw = this.value.replace(/[^0-9]/g, '');
        if (raw === '') { this.value = ''; return; }
        this.value = parseInt(raw).toLocaleString();
    });

    // blur 시 유효성 검사
    targetPriceInput.addEventListener('blur', function () {
        const raw = this.value.replace(/,/g, '');
        const v   = parseInt(raw) || 0;
        if (!raw || v <= 0) {
            showFieldError('auctionTargetPrice', '희망 최대가는 0원보다 커야 합니다.');
            this.value = '';
        } else if (v % 1000 !== 0) {
            showFieldError('auctionTargetPrice', '1000원 단위로 입력해주세요.');
            this.value = '';
        }
    });
}

/* ── 폼 제출: 쉼표 제거 후 전송 + 날짜 검증 ── */
document.getElementById('registerForm').addEventListener('submit', function (e) {
    let hasError = false;

    // 기존
    // const min24hNow = new Date(Date.now() + 24 * 60 * 60 * 1000);

    // 희망 최대가 검증 + 쉼표 제거
    if (targetPriceInput) {
        const raw = targetPriceInput.value.replace(/,/g, '');
        const v   = parseInt(raw) || 0;
        if (!raw || v <= 0) {
            showFieldError('auctionTargetPrice', '희망 최대가는 0원보다 커야 합니다.');
            targetPriceInput.value = '';
            hasError = true;
        } else if (v % 1000 !== 0) {
            showFieldError('auctionTargetPrice', '1000원 단위로 입력해주세요.');
            targetPriceInput.value = '';
            hasError = true;
        } else {
            targetPriceInput.value = raw;
        }
    }

    // 입찰 마감일 검사 — Flatpickr가 이미 막아줬으니 비어있는지만 체크
    const endVal = endPicker.selectedDates[0];
    if (!endVal) {
        showFieldError('auctionEndAt', '입찰 마감일을 선택해주세요.');
        hasError = true;
    }

    // 결정 마감일 검사 — 마찬가지로 비어있는지만 체크
    const dlVal = dlPicker.selectedDates[0];
    if (!dlVal) {
        showFieldError('auctionDecisionDeadline', '결정 마감일을 선택해주세요.');
        hasError = true;
    }

    if (hasError) e.preventDefault();
});