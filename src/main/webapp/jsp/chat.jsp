<%--@elvariable id="username" type=""--%>
<%--@elvariable id="ctx" type=""--%>
<%--@elvariable id="protocol" type=""--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giao diện Chat</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
            background: #1a1a1a;
            color: #ffffff;
            height: 100vh;
            display: flex;
        }
        .sidebar {
            width: 320px;
            background: #2a2a2a;
            border-right: 1px solid #404040;
            display: flex;
            flex-direction: column;
            height: 100vh;
        }
        .sidebar-header {
            padding: 16px 20px;
            border-bottom: 1px solid #404040;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .sidebar-title {
            font-size: 20px;
            font-weight: 600;
        }
        .edit-icon {
            margin-left: auto;
            width: 20px;
            height: 20px;
            cursor: pointer;
            opacity: 0.7;
        }
        .search-box {
            padding: 12px 20px;
            border-bottom: 1px solid #404040;
        }
        .search-input {
            width: 100%;
            background: #404040;
            border: none;
            border-radius: 20px;
            padding: 10px 16px;
            color: #ffffff;
            font-size: 14px;
            outline: none;
        }
        .search-input::placeholder {
            color: #aaaaaa;
        }
        .chat-list {
            flex: 1;
            overflow-y: auto;
        }
        .chat-item {
            padding: 12px 20px;
            display: flex;
            align-items: center;
            gap: 12px;
            cursor: pointer;
            transition: background 0.2s;
            border-bottom: 1px solid #333;
        }
        .chat-item:hover {
            background: #333333;
        }
        .chat-item.active {
            background: #404040;
        }
        .avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(45deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 18px;
            position: relative;
        }
        .avatar.online::after {
            content: '';
            position: absolute;
            bottom: 2px;
            right: 2px;
            width: 12px;
            height: 12px;
            background: #00d484;
            border: 2px solid #2a2a2a;
            border-radius: 50%;
        }
        .chat-info {
            flex: 1;
            min-width: 0;
        }
        .chat-name {
            font-weight: 600;
            font-size: 15px;
            margin-bottom: 4px;
        }
        .chat-preview {
            color: #aaaaaa;
            font-size: 13px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .chat-meta {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 4px;
        }
        .chat-time {
            color: #aaaaaa;
            font-size: 12px;
        }
        .unread-badge {
            background: #0084ff;
            color: white;
            border-radius: 10px;
            padding: 2px 6px;
            font-size: 12px;
            min-width: 18px;
            text-align: center;
        }
        .main-chat {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #1a1a1a;
        }
        .chat-header {
            padding: 16px 20px;
            border-bottom: 1px solid #404040;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .chat-header .avatar {
            width: 40px;
            height: 40px;
            font-size: 16px;
        }
        .chat-user-info {
            flex: 1;
        }
        .chat-user-name {
            font-weight: 600;
            font-size: 16px;
        }
        .chat-user-status {
            color: #aaaaaa;
            font-size: 13px;
        }
        .chat-actions {
            display: flex;
            gap: 12px;
        }
        .action-btn {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: #404040;
            border: none;
            color: #ffffff;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background 0.2s;
        }
        .action-btn:hover {
            background: #555555;
        }
        .messages-container {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .message {
            display: flex;
            gap: 8px;
            align-items: flex-start;
        }
        .message .avatar {
            width: 32px;
            height: 32px;
            font-size: 12px;
        }
        .message-bubble {
            background: #333333;
            padding: 8px 12px;
            border-radius: 16px;
            max-width: 70%;
            word-wrap: break-word;
        }
        .message.sent {
            flex-direction: row-reverse;
        }
        .message.sent .message-bubble {
            background: #0084ff;
            color: white;
        }
        .message-input-container {
            padding: 16px 20px;
            border-top: 1px solid #404040;
            display: flex;
            gap: 12px;
            align-items: center;
        }
        .message-input {
            flex: 1;
            background: #333333;
            border: none;
            border-radius: 20px;
            padding: 12px 16px;
            color: #ffffff;
            font-size: 14px;
            outline: none;
            resize: none;
            min-height: 40px;
            max-height: 120px;
        }
        .message-input::placeholder {
            color: #aaaaaa;
        }
        .send-btn {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #0084ff;
            border: none;
            color: white;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background 0.2s;
        }
        .send-btn:hover {
            background: #0066cc;
        }
        .send-btn:disabled {
            background: #555555;
            cursor: not-allowed;
        }
        ::-webkit-scrollbar {
            width: 6px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: #555555;
            border-radius: 3px;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: #666666;
        }

        .date-separator {
            display: flex;
            align-items: center;
            margin: 20px 0 15px 0;
            gap: 15px;
        }

        .date-line {
            flex: 1;
            height: 1px;
            background: #404040;
        }

        .date-text {
            background: #333333;
            color: #aaaaaa;
            padding: 6px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
            white-space: nowrap;
        }
    </style>
</head>
<body>
<div class="sidebar">
    <div class="sidebar-header">
        <h1 class="sidebar-title">Đoạn chat</h1>
        <svg class="edit-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
    </div>
    <div class="search-box">
        <input type="text" class="search-input" placeholder="Tìm kiếm trên Messenger" oninput="filterChatList(this.value)">
    </div>
    <div class="chat-list" id="chatList"></div>
</div>

<div class="main-chat">
    <div class="chat-header">
        <div class="avatar" id="currentChatAvatar"><img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;"></div>
        <div class="chat-user-info">
            <div class="chat-user-name" id="currentChatName">Tên người dùng</div>
            <div class="chat-user-status" id="currentChatStatus">Trạng thái</div>
        </div>
        <div class="chat-actions">
            <button class="action-btn">📞</button>
            <button class="action-btn">📹</button>
            <button class="action-btn">ℹ️</button>
        </div>
    </div>

    <div class="messages-container" id="messagesContainer"></div>

    <div class="message-input-container">
        <button class="document-btn">📎</button>
        <textarea class="message-input" id="messageInput" placeholder="Aa" rows="1"></textarea>
        <button class="send-btn" id="sendBtn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
        </button>
    </div>
</div>

<script>
    const ctx = '${pageContext.request.contextPath}';
    console.log("Application Context Path (ctx):", ctx);

    const usernameFromSession = sessionStorage.getItem('username'); // Đổi tên để rõ ràng hơn

    let ws; // Khai báo ws ở phạm vi toàn cục của script
    let currentUser = null;
    let currentChat = null;
    let isGroupChat = false;

    if (!usernameFromSession) {
        window.location.href = ctx + '/jsp/login.jsp';
    } else {
        currentUser = { username: usernameFromSession };
        connectWebSocket(usernameFromSession); // Gọi hàm connectWebSocket với username từ session
        loadUsers();
    }

    function connectWebSocket(usernameForSocket) {
        if (!usernameForSocket || usernameForSocket.trim() === "") {
            console.error("Username is missing. Cannot establish WebSocket connection.");
            return;
        }

        // Sửa lại URL để match với context path
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const hostname = location.hostname || 'localhost';
        const port = location.port || '8083';
        
        // Đảm bảo context path được thêm đúng cách
        const wsUrl = protocol + '//' + hostname + ':' + port + '/FinalChatting/chat?username=' + encodeURIComponent(usernameForSocket);
        console.log("Connecting to WebSocket:", wsUrl);

        try {
            ws = new WebSocket(wsUrl);
        } catch (e) {
            console.error("Error creating WebSocket:", e);
            return;
        }

        ws.onopen = () => {
            console.log("WebSocket connected successfully");
        };

        ws.onmessage = (event) => {
            console.log("Received WebSocket message:", event.data);
            try {
                const msg = JSON.parse(event.data);
                if (msg.senderUsername !== usernameFromSession) {
                    displayMessage(msg);
                    scrollToBottom(document.getElementById('messagesContainer'));
                }
            } catch (e) {
                console.error("Error parsing received message:", e);
            }
        };

        ws.onerror = (event) => {
            console.error("WebSocket error:", event);
        };
        
        ws.onclose = (event) => {
            console.log("WebSocket closed:", event.code, event.reason);
        };
    }

    async function loadUsers() {
        try {
            const res = await fetch(ctx + '/api/users');
            if (!res.ok) {
                console.error("Lỗi khi tải danh sách người dùng:", res.status, await res.text());
                // Có thể hiển thị thông báo lỗi cho người dùng ở đây
                return;
            }
            const usersArray = await res.json();
            const chatList = document.getElementById('chatList');
            chatList.innerHTML = ''; // Xóa các mục cũ trong danh sách

            usersArray.forEach(userObject => {
                // Kiểm tra nếu userObject hoặc userObject.username không hợp lệ
                if (!userObject || typeof userObject.username === 'undefined') {
                    console.warn("Bỏ qua người dùng do thiếu dữ liệu:", userObject);
                    return; // Bỏ qua vòng lặp này nếu dữ liệu không đúng
                }

                // Log để kiểm tra username cụ thể đang được xử lý
                // console.log("Đang xử lý user:", userObject.username);

                if (currentUser && userObject.username !== currentUser.username) {
                    const chatItem = document.createElement('div');
                    chatItem.className = 'chat-item';
                    chatItem.onclick = function() {
                        // Xoá lớp 'active' khỏi tất cả các mục chat
                        document.querySelectorAll('.chat-item').forEach(item => item.classList.remove('active'));


                        this.classList.add('active');
                        selectChat(userObject.username, false); // Gọi hàm selectChat với tên người dùng và isGroup là false
                    }

                    // Tạo phần tử avatar
                    const avatarDiv = document.createElement('div');
                    avatarDiv.className = 'avatar';
                    avatarDiv.innerHTML = `<img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">`;

                    // Tạo phần tử chat-info
                    const chatInfoDiv = document.createElement('div');
                    chatInfoDiv.className = 'chat-info';

                    const chatNameDiv = document.createElement('div');
                    chatNameDiv.className = 'chat-name';
                    chatNameDiv.textContent = userObject.username; // *** Gán tên bằng textContent ***

                    const chatPreviewDiv = document.createElement('div');
                    chatPreviewDiv.className = 'chat-preview';
                    chatPreviewDiv.textContent = 'No recent messages'; // Hoặc nội dung xem trước tin nhắn

                    chatInfoDiv.appendChild(chatNameDiv);
                    chatInfoDiv.appendChild(chatPreviewDiv);

                    // Tạo phần tử chat-meta
                    const chatMetaDiv = document.createElement('div');
                    chatMetaDiv.className = 'chat-meta';
                    // chatMetaDiv.innerHTML = `<div class="chat-time"></div>`; // Thời gian có thể cập nhật sau

                    // Gắn các phần tử con vào chatItem
                    chatItem.appendChild(avatarDiv);
                    chatItem.appendChild(chatInfoDiv);
                    chatItem.appendChild(chatMetaDiv);

                    // Gắn chatItem vào danh sách chat
                    chatList.appendChild(chatItem);
                }
            });
        } catch (error) {
            console.error("Đã xảy ra lỗi trong hàm loadUsers:", error);
            // Có thể hiển thị thông báo lỗi cho người dùng ở đây
        }
    }

    function filterChatList(query) {
        const chatItems = document.querySelectorAll('.chat-item');
        chatItems.forEach(item => {
            const name = item.querySelector('.chat-name').textContent.toLowerCase();
            item.style.display = name.includes(query.toLowerCase()) ? 'flex' : 'none';
        });
    }

    function selectChat(target, isGroup) {
        currentChat = target;
        isGroupChat = isGroup;
        document.getElementById('currentChatName').textContent = target;
        document.getElementById('currentChatAvatar').innerHTML = `
        <img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">`;
        document.getElementById('currentChatStatus').textContent = isGroup ? 'Group chat' : 'Online';
        document.getElementById('messagesContainer').innerHTML = '';        // clear

        // gửi lệnh chọn chat lên WS
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send((isGroup ? '/group' : '/chat') + ' ' + target);
        }
        // tải lịch sử chat từ REST API
        loadChatHistory(target, isGroup);
    }

    async function loadChatHistory(target, isGroup) {
        const container = document.getElementById('messagesContainer');
        // thay “…” bằng "..." để tránh ký tự lạ
        container.innerHTML = '<p style="text-align:center;color:#aaa">Đang tải lịch sử...</p>';

        const url = ctx + '/api/messages/history' + '?currentUsername=' + encodeURIComponent(usernameFromSession) + '&targetName=' + encodeURIComponent(target) + '&isGroup=' + isGroup;
        console.log('Loading chat history from:', url);
        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error('Status ' + res.status);
            const messages = await res.json();

            container.innerHTML = '';
            if (!messages.length) {
                container.innerHTML = '<p style="text-align:center;color:#aaa">Chưa có tin nhắn.</p>';
                return;
            }
            messages.forEach(msg => displayMessage(msg));
            scrollToBottom(container);
        } catch (e) {
            console.error('Lỗi tải lịch sử:', e);
            container.innerHTML = '<p style="text-align:center;color:red;">Error: ' + e.message + '</p>';
        }
    }

    function displayMessage(msg) {
        const container = document.getElementById('messagesContainer');
        const isSent = msg.senderUsername === usernameFromSession;

        // Xử lý mốc thời gian
        let messageDate = null;
        if (msg.timestamp) {
            const parsedDate = new Date(msg.timestamp);
            if (!isNaN(parsedDate)) {
                messageDate = parsedDate.toDateString(); // Sửa lỗi chính tả: prasedDate -> parsedDate
            }
        }

        const lastMessage = container.lastElementChild;
        let shouldAddDateSeparator = false;

        if (!lastMessage) {
            // Tin nhắn đầu tiên
            shouldAddDateSeparator = messageDate !== null;
        } else if (lastMessage.classList.contains('date-separator')) {
            // Tin nhắn cuối cùng là thanh ngày
            const lastDateText = lastMessage.querySelector('.date-text').textContent;
            const currentDateText = formatDateForDisplay(messageDate);
            shouldAddDateSeparator = lastDateText !== currentDateText;
        } else {
            // Tin nhắn cuối cùng là tin nhắn thường
            const lastMessageDate = lastMessage.getAttribute('data-date');
            shouldAddDateSeparator = messageDate && messageDate !== lastMessageDate;
        }

        // Thêm thanh ngày nếu cần
        if (shouldAddDateSeparator && messageDate) {
            const dateSeparator = document.createElement('div');
            dateSeparator.className = 'date-separator';
            
            // Sử dụng innerHTML riêng biệt thay vì template literal
            const dateText = formatDateForDisplay(messageDate);
            dateSeparator.innerHTML = '<div class="date-line"></div><div class="date-text">' + dateText + '</div><div class="date-line"></div>';
            
            container.appendChild(dateSeparator);
        }

        // Tạo phần tử tin nhắn
        const wrap = document.createElement('div');
        wrap.className = 'message' + (isSent ? ' sent' : '');
        wrap.setAttribute('data-date', messageDate || ''); // Thêm data-date

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';
        bubble.textContent = msg.content;

        const time = document.createElement('div');
        time.style.fontSize = '0.75em';
        time.style.color = isSent ? '#e0e0e0' : '#aaa';
        time.style.textAlign = 'right';
        time.style.marginTop = '4px';

        if (msg.timestamp) {
            const parsedDate = new Date(msg.timestamp);
            if (!isNaN(parsedDate)) {
                time.textContent = parsedDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            } else {
                time.textContent = 'Invalid Date';
            }
        } else {
            time.textContent = 'No Timestamp';
        }

        bubble.appendChild(time);
        wrap.appendChild(bubble);
        container.appendChild(wrap);
    }

    function formatDateForDisplay(dateString) {
        if (!dateString) return '';
        
        const date = new Date(dateString);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        // So sánh ngày (bỏ qua giờ)
        const isToday = date.toDateString() === today.toDateString();
        const isYesterday = date.toDateString() === yesterday.toDateString();

        if (isToday) {
            return 'Hôm nay';
        } else if (isYesterday) {
            return 'Hôm qua';
        } else {
            return date.toLocaleDateString('vi-VN', { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
            });
        }
    }

    function scrollToBottom(el) {
        el.scrollTop = el.scrollHeight;
    }

    function sendMessage() {
        const input = document.getElementById('messageInput');
        const message = input.value.trim();
        if (!message || !currentChat) return;

        const msg = {
            content: message,
            senderUsername: usernameFromSession,
            targetUsername: currentChat,
            isGroup: isGroupChat,
            timestamp: new Date().toISOString()
        };

        // Hiển thị tin nhắn ngay lập tức cho người gửi
        displayMessage(msg);

        // Gửi tin nhắn qua WebSocket
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(msg));
            console.log("Message sent via WebSocket:", JSON.stringify(msg));
        } else {
            console.error("WebSocket is not open. Cannot send message.");
        }

        input.value = '';
        input.style.height = 'auto';
        scrollToBottom(document.getElementById('messagesContainer'));
    }

    document.getElementById('messageInput').addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 120) + 'px';
    });

    document.getElementById('messageInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    document.getElementById('sendBtn').addEventListener('click', sendMessage);
</script>
</body>
</html>