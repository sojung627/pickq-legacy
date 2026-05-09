(function () {
	
	if (window._notificationSocketInitialized) return;
	    window._notificationSocketInitialized = true;
	
    var state = {
        memIdx: null,
        stompClient: null,
        unreadCountKnown: false,
        unreadCount: 0,
        hasUnread: false,
        pageFilter: null,
    };

    function byId(id) {
        return document.getElementById(id);
    }

    function getPageRoot() {
        return byId('notification-page-root');
    }

    function getListWrap() {
        return byId('notification-list-wrap');
    }

    function getOrCreateListWrap() {
        var wrap = getListWrap();
        if (wrap) {
            return wrap;
        }

        var section = byId('notification-list-section');
        if (!section) {
            return null;
        }

        wrap = document.createElement('div');
        wrap.id = 'notification-list-wrap';
        wrap.className = 'space-y-4';
        section.appendChild(wrap);
        return wrap;
    }

    function escapeHtml(value) {
        var div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    }

    function formatDate(value) {
        if (!value) {
            return '';
        }

        var parsed = new Date(value);
        if (isNaN(parsed.getTime())) {
            return String(value).replace('T', ' ').slice(0, 16);
        }

        var year = parsed.getFullYear();
        var month = String(parsed.getMonth() + 1).padStart(2, '0');
        var day = String(parsed.getDate()).padStart(2, '0');
        var hour = String(parsed.getHours()).padStart(2, '0');
        var minute = String(parsed.getMinutes()).padStart(2, '0');
        return year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
    }

    function renderUnreadIndicator() {
        var dots = Array.from(document.querySelectorAll('[data-notification-bell-dot]'));
        var badges = Array.from(document.querySelectorAll('[data-notification-bell-badge]'));

        if (dots.length === 0 || badges.length === 0) {
            return;
        }

        if (state.unreadCountKnown) {
            if (state.unreadCount > 0) {
                var badgeText = state.unreadCount > 99 ? '99+' : String(state.unreadCount);
                badges.forEach(function (badge) {
                    badge.textContent = badgeText;
                    badge.classList.remove('hidden');
                });
                dots.forEach(function (dot) {
                    dot.classList.add('hidden');
                });
            } else {
                badges.forEach(function (badge) {
                    badge.textContent = '0';
                    badge.classList.add('hidden');
                });
                dots.forEach(function (dot) {
                    dot.classList.add('hidden');
                });
            }
            return;
        }

        badges.forEach(function (badge) {
            badge.classList.add('hidden');
        });
        if (state.hasUnread) {
            dots.forEach(function (dot) {
                dot.classList.remove('hidden');
            });
        } else {
            dots.forEach(function (dot) {
                dot.classList.add('hidden');
            });
        }
    }

    function setKnownUnreadCount(count) {
        state.unreadCountKnown = true;
        state.unreadCount = Math.max(0, Number(count) || 0);
        renderUnreadIndicator();
    }

    function setKnownUnreadDelta(delta) {
        setKnownUnreadCount(state.unreadCount + delta);
    }

    function setUnknownHasUnread(hasUnread) {
        state.unreadCountKnown = false;
        state.hasUnread = !!hasUnread;
        renderUnreadIndicator();
    }

    function getCardByIdx(notificationIdx) {
        return document.querySelector('[data-notification-card="true"][data-notification-idx="' + notificationIdx + '"]');
    }

    function getCardClasses(isRead) {
        return isRead
            ? 'notification-item bg-white rounded-lg border border-gray-200 transition-all hover:shadow-md'
            : 'notification-item bg-[#f5f5f5] rounded-lg border border-gray-300 transition-all hover:shadow-md';
    }

    function getIconWrapClasses(isRead) {
        return isRead
            ? 'flex-shrink-0 w-11 h-11 rounded-full bg-gray-100 flex items-center justify-center'
            : 'flex-shrink-0 w-11 h-11 rounded-full bg-gray-200 flex items-center justify-center';
    }

    function getIconClasses(isRead) {
        return isRead ? 'text-gray-400' : 'text-gray-700 text-lg';
    }

    function getTitleClasses(isRead) {
        return isRead
            ? 'text-sm sm:text-base font-semibold text-gray-700'
            : 'text-sm sm:text-base font-semibold text-gray-900';
    }

    function getMessageClasses(isRead) {
        return isRead
            ? 'text-sm mb-2 text-gray-500 leading-6'
            : 'text-sm mb-2 text-gray-700 leading-6';
    }

    function applyCardState(card, isRead) {
        if (!card) {
            return;
        }

        card.dataset.isRead = isRead ? 'Y' : 'N';
        card.className = getCardClasses(isRead);

        var iconWrap = card.querySelector('[data-notification-icon-wrap]');
        var icon = card.querySelector('[data-notification-icon]');
        var title = card.querySelector('[data-notification-title]');
        var message = card.querySelector('[data-notification-message]');
        var readButton = card.querySelector('[data-action="mark-read"]');

        if (iconWrap) {
            iconWrap.className = getIconWrapClasses(isRead);
        }
        if (icon) {
            icon.className = getIconClasses(isRead);
        }
        if (title) {
            title.className = getTitleClasses(isRead);
        }
        if (message) {
            message.className = getMessageClasses(isRead);
        }
        if (readButton) {
            readButton.classList.toggle('hidden', isRead);
        }
    }

    function buildNotificationCard(notification) {
        var isRead = notification && notification.isRead === 'Y';
        var card = document.createElement('div');
        card.dataset.notificationCard = 'true';
        card.className = getCardClasses(isRead);
        card.dataset.notificationIdx = notification.notificationIdx != null ? String(notification.notificationIdx) : '';
        card.dataset.isRead = isRead ? 'Y' : 'N';
        card.dataset.targetUrl = notification.targetUrl || '';

        var targetHtml = notification.targetUrl
            ? '<a href="' + escapeHtml(notification.targetUrl) + '" class="inline-block text-sm text-[#7CBD00] hover:text-[#6BAD00] font-medium hover:underline">관련 페이지로 이동 →</a>'
            : '<span class="text-xs text-gray-400">이동 가능한 페이지가 없습니다</span>';

        card.innerHTML = ''
            + '<div class="p-4 sm:p-5">'
            + '  <div class="flex items-start gap-4">'
            + '    <div data-notification-icon-wrap class="' + getIconWrapClasses(isRead) + '">'
            + '      <span data-notification-icon class="' + getIconClasses(isRead) + '">⏰</span>'
            + '    </div>'
            + '    <div class="flex-1 min-w-0">'
            + '      <div class="flex items-start justify-between gap-2 mb-2">'
            + '        <h3 data-notification-title class="' + getTitleClasses(isRead) + '">' + escapeHtml(notification.notificationTitle || '새 알림') + '</h3>'
            + '        <div class="flex items-center gap-2 flex-shrink-0 text-gray-400">'
            + '          <button type="button" data-action="mark-read" data-notification-idx="' + escapeHtml(card.dataset.notificationIdx) + '" class="' + (isRead ? 'hidden' : 'inline-flex text-xs items-center hover:text-[#7CBD00]') + '">✔</button>'
            + '        </div>'
            + '      </div>'
            + '      <p class="text-[11px] font-semibold tracking-wide text-gray-400 mb-1">상세</p>'
            + '      <p data-notification-message class="' + getMessageClasses(isRead) + '">' + escapeHtml(notification.notificationMessage || '') + '</p>'
            + '      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">'
            + '        <div data-notification-target>'
            + targetHtml
            + '        </div>'
            + '        <p data-notification-created-at class="text-xs text-gray-400">' + escapeHtml(formatDate(notification.createdAt)) + '</p>'
            + '      </div>'
            + '    </div>'
            + '  </div>'
            + '</div>';

        return card;
    }

    function removeEmptyStateIfNeeded() {
        var empty = byId('notification-empty-state');
        if (empty) {
            empty.remove();
        }
    }

    function prependNotification(notification) {
        var pageRoot = getPageRoot();
        if (!pageRoot) {
            return;
        }

        var filter = pageRoot.getAttribute('data-filter') || 'all';
        if (filter === 'unread' && notification && notification.isRead === 'Y') {
            return;
        }

        var wrap = getOrCreateListWrap();
        if (!wrap) {
            return;
        }

        removeEmptyStateIfNeeded();
        wrap.prepend(buildNotificationCard(notification || {}));
    }

    function postForm(url, data) {
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: new URLSearchParams(data || {}).toString()
        });
    }

    function handleMarkRead(button) {
        var notificationIdx = button.getAttribute('data-notification-idx');
        if (!notificationIdx) {
            return;
        }

        var card = button.closest('[data-notification-card="true"]');

        postForm('/notifications/read', { notificationIdx: notificationIdx })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (text) {
                if (text !== 'OK') {
                    return;
                }

                if (!card) {
                    card = getCardByIdx(notificationIdx);
                }
                applyCardState(card, true);

                if (state.unreadCountKnown) {
                    setKnownUnreadCount(state.unreadCount - 1);
                } else {
                    state.hasUnread = state.hasUnread && state.unreadCount !== 0;
                    renderUnreadIndicator();
                }
            })
            .catch(function (error) {
                console.error('[notification] read failed:', error);
            });
    }

    function markReadByIdx(notificationIdx) {
        if (!notificationIdx) {
            return Promise.resolve();
        }
        return postForm('/notifications/read', { notificationIdx: notificationIdx })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (text) {
                if (text !== 'OK') {
                    return;
                }

                var card = getCardByIdx(notificationIdx);
                applyCardState(card, true);
                if (state.unreadCountKnown) {
                    setKnownUnreadCount(state.unreadCount - 1);
                } else {
                    state.hasUnread = state.hasUnread && state.unreadCount !== 0;
                    renderUnreadIndicator();
                }
            })
            .catch(function () {
                // 네비게이션 전 읽음 처리는 실패해도 이동은 허용
            });
    }

    function handleMarkAllRead() {
        postForm('/notifications/read-all', {})
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (text) {
                if (text !== 'OK') {
                    return;
                }

                document.querySelectorAll('[data-notification-card="true"]').forEach(function (card) {
                    applyCardState(card, true);
                });

                setKnownUnreadCount(0);
            })
            .catch(function (error) {
                console.error('[notification] read-all failed:', error);
            });
    }

    function fetchUnreadPresence() {
        fetch('/notifications/has-unread', {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (text) {
                setUnknownHasUnread(text === 'Y');
            })
            .catch(function () {
                // 초기 상태 조회 실패는 치명적이지 않으므로 무시
            });
    }

    function initializePageState() {
        var pageRoot = getPageRoot();
        if (pageRoot) {
            state.pageFilter = pageRoot.getAttribute('data-filter') || 'all';
            setKnownUnreadCount(Number(pageRoot.getAttribute('data-unread-count') || 0));
            return;
        }

        fetchUnreadPresence();
    }

    function connectNotificationSocket(memIdx) {
        if (!window.SockJS || !window.Stomp) {
            return;
        }

        var socket = new SockJS('/ws-notification');
        state.stompClient = Stomp.over(socket);
        state.stompClient.debug = null;

        state.stompClient.connect({}, function () {
            state.stompClient.subscribe('/topic/notifications/' + memIdx, function (frame) {
                var payload;

                try {
                    payload = JSON.parse(frame.body || '{}');
                } catch (error) {
                    payload = {};
                }

                console.log('[notification] received:', payload);

				if (payload && payload.notificationType !== 'SYSTEM_WS_CONNECTED') {
				    if (state.unreadCountKnown) {
				        setKnownUnreadDelta(1);
				    } else {
				        setUnknownHasUnread(true);
				    }
				    prependNotification(payload);

				    if (typeof showNotificationToast === 'function') {
				        showNotificationToast(
				            payload.notificationTitle || '새 알림',
				            payload.notificationMessage || '',
				            payload.targetUrl || '/notifications'
				        );
				    }
				}
            });
        }, function () {
            console.warn('[notification] websocket disconnected');
        });
    }

    function onDocumentClick(event) {
        var readButton = event.target.closest('[data-action="mark-read"]');
        if (readButton) {
            event.preventDefault();
            handleMarkRead(readButton);
            return;
        }

        var readAllButton = event.target.closest('[data-action="mark-all-read"]');
        if (readAllButton) {
            event.preventDefault();
            handleMarkAllRead();
            return;
        }

        var targetLink = event.target.closest('[data-notification-card="true"] [data-notification-target] a');
        if (targetLink) {
            var card = targetLink.closest('[data-notification-card="true"]');
            var idx = card ? card.dataset.notificationIdx : null;
            var isRead = card ? card.dataset.isRead === 'Y' : true;
            if (!idx || isRead) {
                return;
            }

            event.preventDefault();
            var href = targetLink.getAttribute('href');
            markReadByIdx(idx).finally(function () {
                if (href) {
                    window.location.href = href;
                }
            });
            return;
        }

        var cardWrap = event.target.closest('[data-notification-card="true"]');
        if (cardWrap && !event.target.closest('button, a')) {
            var targetUrl = cardWrap.dataset.targetUrl;
            if (!targetUrl) {
                return;
            }
            var cardIdx = cardWrap.dataset.notificationIdx;
            var cardRead = cardWrap.dataset.isRead === 'Y';
            event.preventDefault();
            if (!cardIdx || cardRead) {
                window.location.href = targetUrl;
                return;
            }
            markReadByIdx(cardIdx).finally(function () {
                window.location.href = targetUrl;
            });
        }
    }

	// 변경 — pageshow persisted 시 재연결 차단
	document.addEventListener('DOMContentLoaded', function () {
	    var context = byId('notification-context');
	    if (!context) return;
	    state.memIdx = context.getAttribute('data-mem-idx');
	    if (!state.memIdx) return;

	    initializePageState();
	    connectNotificationSocket(state.memIdx);
	    document.addEventListener('click', onDocumentClick);
	    renderUnreadIndicator();
	});

	// 뒤로가기(bfcache) 시 WebSocket 토스트 재발생 방지
	window.addEventListener('pageshow', function(event) {
	    if (event.persisted && state.stompClient) {
	        // 이미 연결된 소켓 해제 — 재연결 안 함
	        try { state.stompClient.disconnect(); } catch(e) {}
	        state.stompClient = null;
	    }
	});
})();
