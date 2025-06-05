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

        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const hostname = location.hostname || 'localhost'; // Fallback to 'localhost' if hostname is empty
        const port = '8082'; // Ensure this matches your WebSocket server's port
        const path = '/FinalChatting/chat'; // Include the application context path

        
        console.log("--- DEBUG WebSocket URL Components ---");
        console.log("Protocol:", protocol);
        console.log("Hostname:", hostname);
        console.log("Port:", port);
        console.log("Path:", path);
        console.log("Username:", usernameForSocket);
        console.log("--- End DEBUG ---");

        const wsUrl = "" + protocol + "//" + hostname + ":" + port + path + "?username=" + encodeURIComponent(usernameForSocket);
        console.log("Connecting to WebSocket:", wsUrl);

        try {
            ws = new WebSocket(wsUrl);
        } catch (e) {
            console.error("LỖI KHI KHỞI TẠO WEBSOCKET (trong try-catch):", e);
            console.error("URL đã gây lỗi (trong try-catch):", wsUrl);
            return;
        }

        ws.onopen = () => console.log("WebSocket connected");
        ws.onerror = (event) => console.error("WebSocket error:", event);
        ws.onclose = (event) => console.log("WebSocket closed:", event);
    }

        async function loadUsers() {
        const res = await fetch(ctx + '/api/users');
        const usersArray = await res.json(); // Đổi tên biến để rõ ràng hơn, hoặc giữ nguyên là 'users'
        const chatList = document.getElementById('chatList');
        chatList.innerHTML = '';

        // Lặp qua mảng các ĐỐI TƯỢNG user
        usersArray.forEach(userObject => { // Đổi 'user' thành 'userObject' để phân biệt
            // Bây giờ userObject là một đối tượng đầy đủ, ví dụ: { username: 'mambo', id: 2, ... }
            if (currentUser && userObject.username !== currentUser.username) {
                const chatItem = document.createElement('div');
                chatItem.className = 'chat-item';
                // Truyền userObject.username (là chuỗi) vào hàm selectChat
                chatItem.onclick = () => selectChat(userObject.username, false);

                console.log("Đang xử lý user cho danh sách (đối tượng):", userObject);
                console.log("Giá trị userObject.username:", userObject.username);

                chatItem.innerHTML = `
                    <div class="avatar"><img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;"></div>
                    <div class="chat-info">
                        <div class="chat-name">${userObject.username}</div>
                        <div class="chat-preview">No recent messages</div>
                    </div>
                    <div class="chat-meta">
                        <div class="chat-time"></div>
                    </div>
                `;
                chatList.appendChild(chatItem);
            }
        });
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
        <img src="https://static.vectee.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg"
             alt="avatar"
             style="width:100%;height:100%;border-radius:50%;object-fit:cover;">`;
        document.getElementById('currentChatStatus').textContent = isGroup ? 'Group chat' : 'Online';
        document.getElementById('messagesContainer').innerHTML = '';

        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send((isGroup ? '/group' : '/chat') + ' ' + target);
        } else {
            console.error("Lỗi selectChat: WebSocket không ở trạng thái OPEN. Không thể gửi lệnh.");
            alert("WebSocket connection is not open. Please try again later.");
        }
    }

    function sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const messageText = messageInput.value.trim();
        if (messageText && currentChat) {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(messageText);
                // ... (code còn lại)
            } else {
                console.error("Lỗi sendMessage: WebSocket không ở trạng thái OPEN. Không thể gửi tin nhắn.");
                // Có thể thông báo cho người dùng ở đây
            }
        }
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