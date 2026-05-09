const clientKey = "test_ck_GePWvyJnrKPx4nM5Kogb8gLzN97E";
const tossPayments = TossPayments(clientKey);

const PaymentModule = {
  // [함수 1] 토스 결제창 호출
  request: function (data) {
    tossPayments
      .requestPayment(data.payMethod || "카드", {
        amount: data.amount,
        orderId: data.orderId,
        orderName: data.orderName,
        customerName: data.customerName,
        successUrl: window.location.origin + "/payment/success",
        failUrl: window.location.origin + "/payment/fail",
      })
      .catch((err) => alert("결제 요청 에러: " + err.message));
  },

  // [함수 2] 서버 최종 승인 요청
  confirm: function (requestData) {
    return fetch("/api/payment/confirm", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(requestData),
    }).then((res) => {
      if (!res.ok)
        return res.text().then((text) => {
          throw new Error(text);
        });
      return res.json();
    });
  },

  // [함수 3] 세션 정보 서버에서 가져오기
  getSessionInfo: function () {
    return fetch("/api/payment/session-info", {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    }).then((res) => {
      if (!res.ok) throw new Error("세션 정보를 가져올 수 없습니다.");
      return res.json();
    });
  },

  // [함수 4] 결제 버튼 자동 연결
  initButtons: function () {
    document.querySelectorAll(".pay-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        const bidIdx = btn.getAttribute("data-bid-idx");
        const amount = Number(btn.getAttribute("data-price") || 0);
        const orderName = btn.getAttribute("data-order-name") || "결제 상품";

        if (!bidIdx || !amount || !orderName) {
          alert("결제 정보가 올바르지 않습니다.");
          return;
        }

        this.getSessionInfo()
          .then((sessionInfo) => {
            const payData = {
              amount: amount,
              orderId: "ORD_" + bidIdx + "_" + new Date().getTime(),
              orderName: orderName,
              customerName: sessionInfo.memName || "구매자",
              payMethod: "카드",
            };
            this.request(payData);
          })
          .catch((err) => {
            alert("세션 정보를 불러오지 못했습니다: " + err.message);
          });
      });
    });
  },
};

// 페이지 로드 시 버튼 자동 활성화
document.addEventListener("DOMContentLoaded", () =>
  PaymentModule.initButtons(),
);

// 결제 성공 페이지 처리
document.addEventListener("DOMContentLoaded", function () {
  if (window.location.pathname.includes("/payment/success")) {
    const urlParams = new URLSearchParams(window.location.search);
    const paymentKey = urlParams.get("paymentKey");
    const orderId = urlParams.get("orderId");
    const amount = urlParams.get("amount");

    // 필수값 없으면 바로 fail로
    if (!paymentKey || !orderId || !amount) {
      alert("결제 정보가 없습니다.");
      location.href = "/payment/fail?message=" + encodeURIComponent("결제 정보가 없습니다.");
      return;
    }

    const bidIdx = orderId.split("_")[1];

    // 세션 정보 서버에서 가져온 후 결제 승인
    PaymentModule.getSessionInfo()
      .then((sessionInfo) => {
        return PaymentModule.confirm({
          paymentKey: paymentKey,
          orderId: orderId,
          amount: amount,
          bidIdx: bidIdx,
          buyerName: sessionInfo.memName || "구매자",
          buyerTel: sessionInfo.memTel || "010-0000-0000",
          buyerAddr: sessionInfo.buyerAddr || "주소없음",
          buyerZipcode: sessionInfo.buyerZipcode || "00000",
        });
      })
      .then((data) => {
        alert("결제가 완료되었습니다!");
        location.href = "/mypage/orders";
      })
      .catch((err) => {
        alert("결제 승인 실패: " + err.message);
        location.href = "/payment/fail?message=" + encodeURIComponent(err.message || "결제 승인 실패");
      });
  }
});

// 결제 실패 페이지 처리
if (window.location.pathname.includes("/payment/fail")) {
  const urlParams = new URLSearchParams(window.location.search);
  const msg = urlParams.get("message");

  const failElem = document.getElementById("fail-reason");
  if (failElem) failElem.innerText = "사유: " + (msg || "알 수 없는 오류");
}
