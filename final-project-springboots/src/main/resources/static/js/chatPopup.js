(function () {
    const state = {
        isOpen: false,
        activeView: null,
        selectedRoomId: null,
        selectedRoomName: "",
        rooms: [],
        stompClient: null,
        roomSubscriptions: new Map(),
        isConnected: false,
        unreadPollTimer: null,
        roomPollTimer: null,
    };

    function el(id) {
        return document.getElementById(id);
    }

    function isMobile() {
        return window.innerWidth < 768;
    }

    function currentView() {
        return isMobile() ? "mobile" : "desktop";
    }

    function getLoginContext() {
        const context = el("chat-context");
        if (!context) {
            return { isLogin: false, memIdx: 0 };
        }
        return {
            isLogin: context.dataset.login === "Y",
            memIdx: Number(context.dataset.loginMemIdx || 0),
        };
    }

    function updateScrollButtons() {
        const topBtn = el("scrollTopBtn");
        const bottomBtn = el("scrollBottomBtn");
        if (!topBtn || !bottomBtn) return;

        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollHeight = document.documentElement.scrollHeight;
        const clientHeight = document.documentElement.clientHeight;

        topBtn.disabled = scrollTop <= 0;
        bottomBtn.disabled = scrollTop + clientHeight >= scrollHeight - 2;
    }

    function scrollToTop() {
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function scrollToBottom() {
        window.scrollTo({ top: document.documentElement.scrollHeight, behavior: "smooth" });
    }

    function updateUnreadBadge(count) {
        const badge = el("chatUnreadBadge");
        if (!badge) return;

        const unread = Math.max(0, Number(count || 0));
        if (unread > 0) {
            badge.classList.remove("hidden");
            badge.textContent = unread > 99 ? "99+" : String(unread);
        } else {
            badge.classList.add("hidden");
            badge.textContent = "0";
        }
    }

    // ✅ requestJson 단일 정의 (중복 제거)
    function requestJson(url, options) {
        return fetch(url, options).then((res) => {
            if (res.status === 401) {
                throw new Error("NOT_LOGIN");
            }
            if (!res.ok) {
                return res.text().then((text) => {
                    throw new Error(text || "REQUEST_FAILED");
                });
            }
            return res.json();
        });
    }

    // ✅ loadChatRooms 제거 (fetchRooms로 통일)
    // ✅ loginMember / if(loginMember) 블록 제거

    function fetchUnreadCount() {
        const context = getLoginContext();
        if (!context.isLogin) {
            updateUnreadBadge(0);
            return Promise.resolve(0);
        }

        return requestJson("/api/chats/unread-count")
            .then((data) => {
                const unread = Number(data.unreadCount || 0);
                updateUnreadBadge(unread);
                return unread;
            })
            .catch(() => {
                updateUnreadBadge(0);
                return 0;
            });
    }

    function normalizeRoom(room) {
        return {
            chatroomIdx: Number(room.chatroomIdx),
            opponentName: room.opponentName || "상대방",
            lastMessage: room.lastMessage || "",
            unreadCount: Number(room.unreadCount || 0),
        };
    }

    function escapeHtml(text) {
        const div = document.createElement("div");
        div.textContent = text == null ? "" : String(text);
        return div.innerHTML;
    }

    function desktopRoot() { return el("chatDesktopModalRoot"); }
    function mobileRoot() { return el("chatMobileModalRoot"); }

    function activeMessageListEl() {
        return state.activeView === "mobile" ? el("chatMobileMessageList") : el("chatDesktopMessageList");
    }

    function activeRoomTitleEl() {
        return state.activeView === "mobile" ? el("chatMobileRoomTitle") : el("chatDesktopRoomTitle");
    }

    function setModalVisibility(visible, view) {
        const desktop = desktopRoot();
        const mobile = mobileRoot();

        if (desktop) desktop.classList.toggle("hidden", !(visible && view === "desktop"));
        if (mobile) mobile.classList.toggle("hidden", !(visible && view === "mobile"));
    }

    function updateDesktopPanels() {
        const placeholder = el("chatDesktopPlaceholder");
        const conversation = el("chatDesktopConversationWrap");
        const hasSelection = Boolean(state.selectedRoomId);

        if (placeholder) placeholder.classList.toggle("hidden", hasSelection);
        if (conversation) {
            conversation.classList.toggle("hidden", !hasSelection);
            conversation.classList.toggle("flex", hasSelection);
        }
    }

    function updateMobilePanels() {
        const listPane = el("chatMobileListPane");
        const roomPane = el("chatMobileRoomPane");
        const hasSelection = Boolean(state.selectedRoomId);

        if (listPane) listPane.classList.toggle("hidden", hasSelection);
        if (roomPane) {
            roomPane.classList.toggle("hidden", !hasSelection);
            roomPane.classList.toggle("flex", hasSelection);
        }
    }

    function updateViewPanels() {
        updateDesktopPanels();
        updateMobilePanels();

        const title = activeRoomTitleEl();
        if (title) title.textContent = state.selectedRoomName || "";
    }

    function renderRoomListInto(listEl) {
        if (!listEl) return;

        if (!state.rooms.length) {
            listEl.innerHTML = '<div class="h-full flex items-center justify-center text-sm text-gray-400">채팅방이 없습니다.</div>';
            return;
        }

        listEl.innerHTML = state.rooms.map((room) => {
            const active = room.chatroomIdx === state.selectedRoomId;
            const activeClass = active ? "bg-gray-100 border-l-4 border-l-[#7CBD00]" : "hover:bg-gray-50";
            const unreadBadge = room.unreadCount > 0
                ? '<span class="inline-flex min-w-5 h-5 px-1 items-center justify-center rounded-full bg-red-500 text-white text-[10px] font-semibold">'
                    + (room.unreadCount > 99 ? "99+" : String(room.unreadCount)) + '</span>'
                : "";

            return (
                '<button type="button" data-room-id="' + room.chatroomIdx + '" '
                + 'class="chat-room-item w-full text-left px-3 py-3 border-b border-gray-100 flex items-center justify-between gap-2 ' + activeClass + '">'
                + '  <div class="min-w-0">'
                + '    <p class="text-sm font-semibold text-gray-800 truncate">' + escapeHtml(room.opponentName) + '</p>'
                + '    <p class="text-xs text-gray-500 truncate">' + escapeHtml(room.lastMessage || "대화를 시작해보세요") + '</p>'
                + '  </div>'
                + '  <div class="shrink-0">' + unreadBadge + '</div>'
                + '</button>'
            );
        }).join("");

        listEl.querySelectorAll(".chat-room-item").forEach((button) => {
            button.addEventListener("click", () => {
                selectRoom(Number(button.dataset.roomId));
            });
        });
    }

    function renderRoomLists() {
        renderRoomListInto(el("chatDesktopRoomList"));
        renderRoomListInto(el("chatMobileRoomList"));
    }

    function scrollMessagesToBottom() {
        const list = activeMessageListEl();
        if (!list) return;
        requestAnimationFrame(() => { list.scrollTop = list.scrollHeight; });
    }

    function ensureMessageStack(listEl) {
        if (!listEl) return null;

        let stack = listEl.querySelector("[data-chat-message-stack='true']");
        if (!stack) {
            stack = document.createElement("div");
            stack.setAttribute("data-chat-message-stack", "true");
            stack.className = "min-h-full flex flex-col justify-end gap-2";
            listEl.innerHTML = "";
            listEl.appendChild(stack);
        }
        return stack;
    }

    function messageBubbleHtml(msg, mine) {
        const rowClass = mine ? "justify-end" : "justify-start";
        const bubbleClass = mine
            ? "bg-[#222222] text-white"
            : "bg-white text-gray-800 border border-gray-200";

        return (
            '<div class="w-full flex ' + rowClass + '">'
            + '  <div class="max-w-[82%] rounded-2xl px-3 py-2 text-sm leading-relaxed ' + bubbleClass + '">'
            + escapeHtml(msg.messageContent)
            + '  </div>'
            + '</div>'
        );
    }

    function renderMessages(messages) {
        const list = activeMessageListEl();
        if (!list) return;

        const context = getLoginContext();
        const stack = ensureMessageStack(list);
        if (!stack) return;

        stack.innerHTML = (messages || []).map((msg) => {
            const mine = Number(msg.senderIdx) === Number(context.memIdx);
            return messageBubbleHtml(msg, mine);
        }).join("");

        scrollMessagesToBottom();
    }

    function appendMessage(message) {
        const list = activeMessageListEl();
        if (!list) return;

        const context = getLoginContext();
        const stack = ensureMessageStack(list);
        if (!stack) return;

        const mine = Number(message.senderIdx) === Number(context.memIdx);
        const wrapper = document.createElement("div");
        wrapper.className = "w-full flex " + (mine ? "justify-end" : "justify-start");

        const bubble = document.createElement("div");
        bubble.className = "max-w-[82%] rounded-2xl px-3 py-2 text-sm leading-relaxed "
            + (mine ? "bg-[#222222] text-white" : "bg-white text-gray-800 border border-gray-200");
        bubble.textContent = message.messageContent || "";

        wrapper.appendChild(bubble);
        stack.appendChild(wrapper);
        scrollMessagesToBottom();
    }

    function markSelectedRoomRead() {
        if (!state.selectedRoomId) return Promise.resolve();

        return requestJson("/api/chats/rooms/" + state.selectedRoomId + "/read", {
            method: "POST",
        }).catch(() => {});
    }

    function loadMessages(roomId) {
        return requestJson("/api/chats/rooms/" + roomId + "/messages")
            .then((data) => { renderMessages(data.messages || []); })
            .catch((error) => {
                if (error.message !== "NOT_LOGIN") renderMessages([]);
            });
    }

    function subscribeRoom(roomId) {
        if (!state.stompClient || !state.stompClient.connected) return;

        const key = String(roomId);
        if (state.roomSubscriptions.has(key)) return;

        const subscription = state.stompClient.subscribe("/topic/chatroom/" + roomId, (frame) => {
            let message;
            try {
                message = JSON.parse(frame.body || "{}");
            } catch (error) {
                message = {};
            }

            const inSelectedRoom = Number(message.chatroomIdx) === Number(state.selectedRoomId);
            const modalVisible = state.isOpen;

            if (modalVisible && inSelectedRoom) {
                appendMessage(message);
                markSelectedRoomRead().then(() => {
                    fetchRooms();
                    fetchUnreadCount();
                });
            } else {
                fetchRooms();
                fetchUnreadCount();
            }
        });

        state.roomSubscriptions.set(key, subscription);
    }

    function unsubscribeAllRooms() {
        state.roomSubscriptions.forEach((subscription) => {
            try { subscription.unsubscribe(); }
            catch (error) { console.warn("[chat] unsubscribe failed", error); }
        });
        state.roomSubscriptions.clear();
    }

    function subscribeAllRooms() {
        if (!state.stompClient || !state.stompClient.connected) return;
        state.rooms.forEach((room) => subscribeRoom(room.chatroomIdx));
    }

    function fetchRooms(preferredRoomId) {
        return requestJson("/api/chats/rooms")
            .then((data) => {
                state.rooms = (data.rooms || []).map(normalizeRoom);
                renderRoomLists();

                if (state.isConnected) {
                    unsubscribeAllRooms();
                    subscribeAllRooms();
                }

                if (!state.rooms.length) {
                    state.selectedRoomId = null;
                    state.selectedRoomName = "";
                    updateViewPanels();
                    return;
                }

                if (preferredRoomId) {
                    selectRoom(preferredRoomId);
                    return;
                }

                const stillExists = state.rooms.some((room) => room.chatroomIdx === state.selectedRoomId);
                if (!stillExists && state.activeView === "desktop") {
                    state.selectedRoomId = null;
                    state.selectedRoomName = "";
                }

                updateViewPanels();
            })
            .catch(() => {
                state.rooms = [];
                renderRoomLists();
                state.selectedRoomId = null;
                state.selectedRoomName = "";
                updateViewPanels();
            });
    }

    function selectRoom(roomId) {
        const room = state.rooms.find((item) => Number(item.chatroomIdx) === Number(roomId));
        if (!room) return;

        state.selectedRoomId = room.chatroomIdx;
        state.selectedRoomName = room.opponentName;
        updateViewPanels();
        renderRoomLists();

        loadMessages(room.chatroomIdx).then(() => {
            subscribeRoom(room.chatroomIdx);
            markSelectedRoomRead().then(() => {
                fetchRooms();
                fetchUnreadCount();
            });
        });
    }

    function sendMessage(content) {
        if (!state.selectedRoomId || !content.trim()) return;

        const context = getLoginContext();
        const payload = {
            chatroomIdx: state.selectedRoomId,
            senderIdx: context.memIdx,
            messageContent: content.trim(),
        };

        if (state.stompClient && state.stompClient.connected) {
            state.stompClient.send("/app/chat/send", {}, JSON.stringify(payload));
            return;
        }

        requestJson("/api/chats/rooms/" + state.selectedRoomId + "/messages", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ messageContent: content.trim() }),
        }).then((data) => {
            appendMessage(data);
            fetchRooms();
            fetchUnreadCount();
        }).catch(() => {});
    }

    function connectWebSocket() {
        const context = getLoginContext();
        if (!context.isLogin) return;

        if (state.stompClient && state.stompClient.connected) {
            state.isConnected = true;
            return;
        }

        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") return;

        const socket = new SockJS("/ws-chat");
        state.stompClient = Stomp.over(socket);
        state.stompClient.debug = null;

        state.stompClient.connect({}, () => {
            state.isConnected = true;
            subscribeAllRooms();
        }, () => {
            state.isConnected = false;
        });
    }

    function adjustTextareaHeight(textarea) {
        if (!textarea) return;
        textarea.style.height = "auto";
        textarea.style.height = Math.min(textarea.scrollHeight, 112) + "px";
    }

    function bindComposeForm(formId, inputId) {
        const form = el(formId);
        const input = el(inputId);
        if (!form || !input) return;

        form.addEventListener("submit", (event) => {
            event.preventDefault();
            const content = input.value;
            if (!content || !content.trim()) return;

            sendMessage(content);
            input.value = "";
            input.style.height = "auto";
            input.focus();
        });

        input.addEventListener("keydown", (event) => {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        input.addEventListener("input", () => adjustTextareaHeight(input));
    }

    function openChatModal(preferredRoomId) {
        const context = getLoginContext();
        if (!context.isLogin) {
            location.href = "/members/login?redirect=" + encodeURIComponent(location.pathname + location.search);
            return;
        }

        state.isOpen = true;
        state.activeView = currentView();

        setModalVisibility(true, state.activeView);
        updateViewPanels();

        fetchRooms(preferredRoomId).then(() => {
            fetchUnreadCount();
            if (window.feather) window.feather.replace();
        });

        if (state.roomPollTimer) clearInterval(state.roomPollTimer);

        state.roomPollTimer = window.setInterval(() => {
            if (!state.isOpen) return;
            fetchRooms();
            fetchUnreadCount();
        }, 10000);
    }

    function closeChatModal() {
        // ✅ activeView를 null 처리 전 저장 (WARN #5 수정)
        const prevView = state.activeView;

        state.isOpen = false;
        state.activeView = null;
        state.selectedRoomId = null;
        state.selectedRoomName = "";

        setModalVisibility(false, prevView);
        updateViewPanels();

        if (state.roomPollTimer) {
            clearInterval(state.roomPollTimer);
            state.roomPollTimer = null;
        }

        fetchUnreadCount();
    }

    function handleResize() {
        updateScrollButtons();
        if (!state.isOpen) return;

        const nextView = currentView();
        if (state.activeView === nextView) return;

        state.activeView = nextView;
        setModalVisibility(true, state.activeView);
        updateViewPanels();
        if (state.selectedRoomId) loadMessages(state.selectedRoomId);
    }

    function bindButtons() {
        const floatingBtn = el("chatFloatingButton");
        const desktopClose = el("chatDesktopCloseBtn");
        const mobileClose = el("chatMobileCloseBtn");
        const mobileRoomClose = el("chatMobileRoomCloseBtn");
        const mobileBack = el("chatMobileBackBtn");

        if (floatingBtn) floatingBtn.addEventListener("click", () => openChatModal());
        if (desktopClose) desktopClose.addEventListener("click", closeChatModal);
        if (mobileClose) mobileClose.addEventListener("click", closeChatModal);
        if (mobileRoomClose) mobileRoomClose.addEventListener("click", closeChatModal);
        if (mobileBack) {
            mobileBack.addEventListener("click", () => {
                state.selectedRoomId = null;
                state.selectedRoomName = "";
                updateViewPanels();
                renderRoomLists();
            });
        }

        window.addEventListener("resize", handleResize);
    }

    function startUnreadPolling() {
        if (state.unreadPollTimer) clearInterval(state.unreadPollTimer);

        fetchUnreadCount();
        state.unreadPollTimer = window.setInterval(fetchUnreadCount, 10000);
    }

    window.openChatModal = function () { openChatModal(); };
    window.openChatPopup = function (chatroomIdx) {
        if (!chatroomIdx) { openChatModal(); return; }
        openChatModal(Number(chatroomIdx));
    };
    window.scrollToTop = scrollToTop;
    window.scrollToBottom = scrollToBottom;

    document.addEventListener("DOMContentLoaded", () => {
        bindButtons();
        bindComposeForm("chatDesktopComposeForm", "chatDesktopMessageInput");
        bindComposeForm("chatMobileComposeForm", "chatMobileMessageInput");

        updateScrollButtons();
        window.addEventListener("scroll", updateScrollButtons);

        const context = getLoginContext();

        if (context.isLogin) {
            connectWebSocket();
            fetchRooms();
            startUnreadPolling();
        } else {
            updateUnreadBadge(0);
        }

        if (window.feather) window.feather.replace();
    });
})();