<!-- filepath: src/main/webapp/index.jsp -->
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.example.chat.model.User" %>
<%
    // Check if user is logged in
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Messenger - <%= currentUser.getFullName() %></title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            height: 100vh;
            overflow: hidden;
        }

        .messenger-container {
            display: flex;
            height: 100vh;
            background: white;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
        }

        /* Sidebar */
        .sidebar {
            width: 360px;
            background: #fff;
            border-right: 1px solid #e4e6ea;
            display: flex;
            flex-direction: column;
        }

        /* User Info Section */
        .user-info {
            padding: 16px 20px;
            border-bottom: 1px solid #e4e6ea;
            background: #f8f9fa;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        
        .current-user {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .current-user-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
            font-size: 14px;
        }

        .current-user-info {
            display: flex;
            flex-direction: column;
        }

        .current-user-name {
            font-weight: 600;
            color: #050505;
            font-size: 14px;
        }

        .current-user-status {
            font-size: 12px;
            color: #42b883;
        }
        
        .user-actions {
            display: flex;
            gap: 8px;
        }
        
        .user-action-btn {
            background: transparent;
            border: 1px solid #e4e6ea;
            color: #65676b;
            padding: 6px 12px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.3s ease;
        }
        
        .user-action-btn:hover {
            background: #f0f2f5;
        }

        .logout-btn {
            background: #e74c3c;
            color: white;
            border-color: #e74c3c;
        }
        
        .logout-btn:hover {
            background: #c0392b;
            border-color: #c0392b;
        }

        .sidebar-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e4e6ea;
            background: #fff;
        }

        .sidebar-title {
            font-size: 24px;
            font-weight: 800;
            color: #050505;
            margin-bottom: 16px;
        }

        .search-box {
            position: relative;
            margin-bottom: 8px;
        }

        .search-input {
            width: 100%;
            padding: 8px 12px 8px 36px;
            border: none;
            border-radius: 20px;
            background: #f0f2f5;
            font-size: 15px;
            outline: none;
            transition: all 0.2s ease;
        }

        .search-input:focus {
            background: #e4e6ea;
        }

        .search-icon {
            position: absolute;
            left: 12px;
            top: 50%;
            transform: translateY(-50%);
            color: #65676b;
            font-size: 14px;
        }

        .conversations-list {
            flex: 1;
            overflow-y: auto;
            padding: 8px 0;
        }

        .conversation-item {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            cursor: pointer;
            transition: background-color 0.2s ease;
            position: relative;
        }

        .conversation-item:hover {
            background: #f0f2f5;
        }

        .conversation-item.active {
            background: #e7f3ff;
        }

        .conversation-avatar {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
            font-size: 18px;
            margin-right: 12px;
            position: relative;
        }

        .conversation-avatar.online::after {
            content: '';
            position: absolute;
            bottom: 2px;
            right: 2px;
            width: 14px;
            height: 14px;
            background: #42b883;
            border: 2px solid white;
            border-radius: 50%;
        }

        .conversation-info {
            flex: 1;
            min-width: 0;
        }

        .conversation-name {
            font-weight: 600;
            color: #050505;
            font-size: 15px;
            margin-bottom: 2px;
        }

        .conversation-preview {
            color: #65676b;
            font-size: 13px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .conversation-time {
            color: #65676b;
            font-size: 12px;
            position: absolute;
            top: 16px;
            right: 20px;
        }

        .unread-badge {
            position: absolute;
            bottom: 16px;
            right: 20px;
            background: #0084ff;
            color: white;
            border-radius: 10px;
            padding: 2px 6px;
            font-size: 11px;
            font-weight: 600;
            min-width: 18px;
            text-align: center;
        }

        /* Loading State */
        .loading-conversations {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 40px;
            color: #65676b;
        }

        .loading-spinner {
            width: 24px;
            height: 24px;
            border: 2px solid #e4e6ea;
            border-top: 2px solid #0084ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-right: 12px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Main Chat Area */
        .chat-area {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #fff;
        }

        .chat-header {
            padding: 12px 20px;
            border-bottom: 1px solid #e4e6ea;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #fff;
        }

        .chat-user-info {
            display: flex;
            align-items: center;
        }

        .chat-user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
            margin-right: 12px;
        }

        .chat-user-details h3 {
            font-size: 16px;
            font-weight: 600;
            color: #050505;
            margin-bottom: 2px;
        }

        .chat-user-status {
            font-size: 12px;
            color: #42b883;
        }

        .chat-actions {
            display: flex;
            gap: 8px;
        }

        .action-btn {
            width: 36px;
            height: 36px;
            border: none;
            border-radius: 50%;
            background: #f0f2f5;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
            color: #0084ff;
            font-size: 16px;
        }

        .action-btn:hover {
            background: #e4e6ea;
            transform: scale(1.05);
        }

        /* Chat Box */
        .chat-box {
            flex: 1;
            overflow-y: auto;
            padding: 20px;
            background: linear-gradient(to bottom, #f8f9fa, #ffffff);
        }

        .chat-box.empty {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            color: #65676b;
            font-size: 16px;
        }

        .empty-chat-icon {
            font-size: 48px;
            margin-bottom: 16px;
            opacity: 0.5;
        }

        .message {
            margin: 8px 0;
            display: flex;
            align-items: flex-end;
            animation: messageSlide 0.3s ease-out;
        }

        @keyframes messageSlide {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .message.sent {
            justify-content: flex-end;
        }

        .message.received {
            justify-content: flex-start;
        }

        .message-bubble {
            max-width: 70%;
            padding: 12px 16px;
            border-radius: 18px;
            position: relative;
            word-wrap: break-word;
        }

        .message.sent .message-bubble {
            background: linear-gradient(135deg, #0084ff, #0066cc);
            color: white;
            border-bottom-right-radius: 4px;
        }

        .message.received .message-bubble {
            background: #f0f2f5;
            color: #050505;
            border-bottom-left-radius: 4px;
        }

        .message-sender {
            font-weight: 600;
            font-size: 12px;
            margin-bottom: 4px;
            opacity: 0.8;
        }

        .message-time {
            font-size: 11px;
            opacity: 0.7;
            margin-top: 4px;
        }

        /* Input Area */
        .input-area {
            padding: 16px 20px;
            border-top: 1px solid #e4e6ea;
            background: #fff;
        }

        .input-container {
            display: flex;
            align-items: flex-end;
            gap: 8px;
            background: #f0f2f5;
            border-radius: 20px;
            padding: 8px 12px;
            transition: all 0.2s ease;
        }

        .input-container:focus-within {
            background: #e4e6ea;
            box-shadow: 0 0 0 2px #0084ff22;
        }

        .input-actions {
            display: flex;
            gap: 4px;
        }

        .input-btn {
            width: 32px;
            height: 32px;
            border: none;
            border-radius: 50%;
            background: transparent;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #0084ff;
            font-size: 16px;
            transition: all 0.2s ease;
        }

        .input-btn:hover {
            background: #0084ff22;
            transform: scale(1.1);
        }

        .input-btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        .message-input {
            flex: 1;
            border: none;
            background: transparent;
            resize: none;
            outline: none;
            font-size: 15px;
            color: #050505;
            max-height: 100px;
            min-height: 20px;
            line-height: 20px;
            padding: 6px 0;
        }

        .message-input::placeholder {
            color: #65676b;
        }

        .message-input:disabled {
            cursor: not-allowed;
            opacity: 0.6;
        }

        .send-button {
            background: linear-gradient(135deg, #0084ff, #0066cc);
            color: white;
        }

        .send-button:hover:not(:disabled) {
            background: linear-gradient(135deg, #0066cc, #0052a3);
        }

        /* Animation */
        #animation {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: rgba(0, 132, 255, 0.9);
            color: white;
            padding: 20px 40px;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            z-index: 1000;
            display: none;
        }

        .animate {
            animation: bounce 1s ease-in-out;
        }

        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% {
                transform: translate(-50%, -50%) translateY(0);
            }
            40% {
                transform: translate(-50%, -50%) translateY(-20px);
            }
            60% {
                transform: translate(-50%, -50%) translateY(-10px);
            }
        }

        /* Error State */
        .error-message {
            text-align: center;
            padding: 40px 20px;
            color: #e74c3c;
            font-size: 14px;
        }

        /* Scrollbar Styling */
        .conversations-list::-webkit-scrollbar,
        .chat-box::-webkit-scrollbar {
            width: 6px;
        }

        .conversations-list::-webkit-scrollbar-track,
        .chat-box::-webkit-scrollbar-track {
            background: transparent;
        }

        .conversations-list::-webkit-scrollbar-thumb,
        .chat-box::-webkit-scrollbar-thumb {
            background: #c4c4c4;
            border-radius: 3px;
        }

        .conversations-list::-webkit-scrollbar-thumb:hover,
        .chat-box::-webkit-scrollbar-thumb:hover {
            background: #a8a8a8;
        }

        /* Hidden file input */
        #file-input {
            display: none;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .sidebar {
                width: 100%;
                position: absolute;
                z-index: 100;
                height: 100vh;
                transform: translateX(-100%);
                transition: transform 0.3s ease;
            }

            .sidebar.mobile-show {
                transform: translateX(0);
            }

            .chat-area {
                width: 100%;
            }

            .messenger-container {
                position: relative;
            }

            .mobile-menu-btn {
                display: block;
                position: absolute;
                top: 16px;
                left: 16px;
                z-index: 101;
                background: #0084ff;
                color: white;
                border: none;
                border-radius: 8px;
                padding: 8px 12px;
                cursor: pointer;
            }
        }

        .mobile-menu-btn {
            display: none;
        }
    </style>
</head>
<body>
<div class="messenger-container">
    <!-- Mobile Menu Button -->
    <button class="mobile-menu-btn" onclick="toggleMobileMenu()">☰ Menu</button>

    <!-- Sidebar -->
    <div class="sidebar" id="sidebar">
        <!-- User Info -->
        <div class="user-info">
            <div class="current-user">
                <div class="current-user-avatar">
                    <%= currentUser.getFullName().substring(0, 1).toUpperCase() %>
                </div>
                <div class="current-user-info">
                    <div class="current-user-name"><%= currentUser.getFullName() %></div>
                    <div class="current-user-status">Đang hoạt động</div>
                </div>
            </div>
            <div class="user-actions">
                <button class="user-action-btn" onclick="showProfile()">Hồ sơ</button>
                <button class="user-action-btn logout-btn" onclick="logout()">Đăng xuất</button>
            </div>
        </div>
        
        <div class="sidebar-header">
            <h1 class="sidebar-title">Chats</h1>
            <div class="search-box">
                <span class="search-icon">🔍</span>
                <input type="text" class="search-input" placeholder="Tìm kiếm trong Messenger" 
                       onkeyup="searchUsers(this.value)">
            </div>
        </div>

        <div class="conversations-list" id="conversations-list">
            <div class="loading-conversations">
                <div class="loading-spinner"></div>
                Đang tải danh sách bạn bè...
            </div>
        </div>
    </div>

    <!-- Chat Area -->
    <div class="chat-area">
        <div class="chat-header">
            <div class="chat-user-info">
                <div class="chat-user-avatar" id="chat-user-avatar">💬</div>
                <div class="chat-user-details">
                    <h3 id="current-chat-user">Chọn một cuộc trò chuyện</h3>
                    <div class="chat-user-status" id="chat-user-status">Offline</div>
                </div>
            </div>

            <div class="chat-actions">
                <button class="action-btn" id="voice-call-button" title="Gọi thoại" disabled>📞</button>
                <button class="action-btn" id="video-call-button" title="Gọi video" disabled>📹</button>
                <button class="action-btn" title="Thông tin" disabled>ℹ️</button>
            </div>
        </div>

        <div class="chat-box empty" id="chat-box">
            <div class="empty-chat-icon">💬</div>
            <div>Chọn một cuộc trò chuyện để bắt đầu nhắn tin</div>
        </div>

        <div class="input-area">
            <div class="input-container">
                <div class="input-actions">
                    <button class="input-btn" onclick="document.getElementById('file-input').click()" title="Đính kèm file" disabled>📎</button>
                    <button class="input-btn" title="Ảnh" disabled>📷</button>
                    <button class="input-btn" title="Sticker" disabled>😊</button>
                </div>

                <textarea class="message-input" id="message-input" placeholder="Chọn cuộc trò chuyện để nhắn tin" rows="1" disabled></textarea>

                <button class="input-btn send-button" id="send-button" title="Gửi" disabled>➤</button>
            </div>
        </div>
    </div>
</div>

<!-- Hidden elements -->
<input type="file" id="file-input" onchange="handleFileSelect(this.files)">
<div id="animation">Tin nhắn đã được gửi!</div>

<script>
    // Global variables
    const currentUserId = <%= currentUser.getId() %>;
    const currentUserName = '<%= currentUser.getFullName() %>';
    let selectedUserId = null;
    let selectedUserName = '';
    let users = [];
    let messages = [];
    let tcpSocket = null;

    // DOM elements
    const chatBox = document.getElementById("chat-box");
    const messageInput = document.getElementById("message-input");
    const sendButton = document.getElementById("send-button");
    const conversationsList = document.getElementById("conversations-list");

    // Initialize application
    document.addEventListener('DOMContentLoaded', function() {
        loadUsers();
        connectToTCPServer();
        setupEventListeners();
    });

    // Load users from server
    function loadUsers() {
        fetch('chat?action=getUsers')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    users = data.users.filter(user => user.id !== currentUserId);
                    displayUsers(users);
                } else {
                    showError('Không thể tải danh sách người dùng');
                }
            })
            .catch(error => {
                console.error('Error loading users:', error);
                showError('Lỗi kết nối server');
            });
    }

    // Display users in sidebar
    function displayUsers(userList) {
        if (userList.length === 0) {
            conversationsList.innerHTML = '<div class="error-message">Chưa có người dùng nào</div>';
            return;
        }

        conversationsList.innerHTML = '';
        userList.forEach(user => {
            const userElement = createUserElement(user);
            conversationsList.appendChild(userElement);
        });
    }

    // Create user element
    function createUserElement(user) {
        const div = document.createElement('div');
        div.className = 'conversation-item';
        div.setAttribute('data-user-id', user.id);
        div.onclick = () => selectConversation(user.id, user.fullName, div);
        
        div.innerHTML = `
            <div class="conversation-avatar ${user.online ? 'online' : ''}">
                ${user.fullName.charAt(0).toUpperCase()}
            </div>
            <div class="conversation-info">
                <div class="conversation-name">${user.fullName}</div>
                <div class="conversation-preview">Nhấn để bắt đầu trò chuyện</div>
            </div>
            <div class="conversation-time">${user.online ? 'Online' : 'Offline'}</div>
        `;
        
        return div;
    }

    // Select conversation
    function selectConversation(userId, userName, element) {
        // Remove active class from all items
        document.querySelectorAll('.conversation-item').forEach(item => 
            item.classList.remove('active'));
        
        // Add active class to selected item
        if (element) {
            element.classList.add('active');
        }

        selectedUserId = userId;
        selectedUserName = userName;
        
        // Update chat header
        document.getElementById('current-chat-user').textContent = userName;
        document.getElementById('chat-user-avatar').textContent = userName.charAt(0).toUpperCase();
        document.getElementById('chat-user-status').textContent = 'Online';
        
        // Enable input and buttons
        messageInput.disabled = false;
        sendButton.disabled = false;
        messageInput.placeholder = `Nhắn tin cho ${userName}`;
        
        // Enable action buttons
        document.querySelectorAll('.action-btn, .input-btn').forEach(btn => {
            btn.disabled = false;
        });
        
        // Remove empty state
        chatBox.classList.remove('empty');
        
        // Load messages
        loadMessages(userId);

        // Hide mobile menu on mobile
        if (window.innerWidth <= 768) {
            document.getElementById('sidebar').classList.remove('mobile-show');
        }
    }

    // Load messages between users
    function loadMessages(otherUserId) {
        fetch(`chat?action=getMessages&otherUserId=${otherUserId}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    messages = data.messages;
                    displayMessages();
                } else {
                    chatBox.innerHTML = '<div class="error-message">Không thể tải tin nhắn</div>';
                }
            })
            .catch(error => {
                console.error('Error loading messages:', error);
                chatBox.innerHTML = '<div class="error-message">Lỗi tải tin nhắn</div>';
            });
    }

    // Display messages
    function displayMessages() {
        chatBox.innerHTML = '';
        if (messages.length === 0) {
            chatBox.innerHTML = '<div style="text-align: center; padding: 50px; color: #65676b;">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</div>';
        } else {
            messages.forEach(message => {
                displayMessage(
                    message.sender.id === currentUserId ? 'You' : message.sender.fullName,
                    message.content,
                    formatTime(message.sentAt),
                    message.sender.id === currentUserId
                );
            });
        }
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // Send message
    function sendMessage() {
        if (!messageInput || !selectedUserId) return;
        
        const messageText = messageInput.value.trim();
        if (messageText === "") return;

        // Disable send button temporarily
        sendButton.disabled = true;
        
        // Send via WebSocket bridge to TCP server
        if (tcpSocket && tcpSocket.readyState === WebSocket.OPEN) {
            const message = {
                type: 'chat',
                recipientId: selectedUserId,
                content: messageText
            };
            tcpSocket.send(JSON.stringify(message));
            
            // Optimistically display message
            displayMessage(currentUserName, messageText, formatTime(new Date()), true);
            messageInput.value = "";
            autoResize();
            
            animation();
            updateConversationPreview(selectedUserId, messageText);
            sendButton.disabled = false;
        } else {
            showError('Không thể gửi tin nhắn. Mất kết nối.');
            sendButton.disabled = false;
        }
    }

    // Display single message
    function displayMessage(sender, messageText, time, isSent = false) {
        if (!chatBox) return;
        
        const messageElement = document.createElement("div");
        messageElement.classList.add('message');
        messageElement.classList.add(isSent ? 'sent' : 'received');

        const messageBubble = document.createElement("div");
        messageBubble.classList.add('message-bubble');

        if (!isSent) {
            const senderElement = document.createElement("div");
            senderElement.classList.add('message-sender');
            senderElement.textContent = sender;
            messageBubble.appendChild(senderElement);
        }

        const contentElement = document.createElement("div");
        contentElement.textContent = messageText;
        messageBubble.appendChild(contentElement);

        if (time) {
            const timeElement = document.createElement("div");
            timeElement.classList.add('message-time');
            timeElement.textContent = time;
            messageBubble.appendChild(timeElement);
        }

        messageElement.appendChild(messageBubble);
        chatBox.appendChild(messageElement);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // Connect to TCP server (WebSocket bridge)
    function connectToTCPServer() {
        try {
            // Use the TCP bridge endpoint
            const wsUrl = `ws://${window.location.host}${window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/'))}/tcp-bridge`;
            tcpSocket = new WebSocket(wsUrl);
            
            tcpSocket.onopen = function() {
                console.log('Connected to TCP server via WebSocket bridge');
                // Send login to establish TCP connection
                const loginMessage = {
                    type: 'login',
                    username: '<%= currentUser.getUsername() %>'
                };
                tcpSocket.send(JSON.stringify(loginMessage));
            };
            
            tcpSocket.onmessage = function(event) {
                const data = JSON.parse(event.data);
                handleTCPMessage(data);
            };
            
            tcpSocket.onclose = function() {
                console.log('Disconnected from TCP server bridge');
                setTimeout(connectToTCPServer, 3000);
            };
            
            tcpSocket.onerror = function(error) {
                console.error('TCP bridge connection error:', error);
            };
        } catch (error) {
            console.error('Failed to connect to TCP server bridge:', error);
        }
    }

    // Handle TCP messages
    function handleTCPMessage(data) {
        switch (data.type) {
            case 'login_success':
                console.log('TCP connection established successfully');
                break;
            case 'new_message':
                if (data.senderId !== currentUserId) {
                    displayMessage(data.senderName, data.content, formatTime(data.timestamp), false);
                }
                updateConversationPreview(data.senderId === currentUserId ? data.recipientId : data.senderId, data.content);
                break;
            case 'message_sent':
                console.log('Message sent via TCP');
                break;
            case 'user_online':
                updateUserOnlineStatus(data.userId, true);
                break;
            case 'user_offline':
                updateUserOnlineStatus(data.userId, false);
                break;
            case 'error':
                console.error('TCP Error:', data.message);
                showError(data.message);
                break;
        }
    }

    // Search users
    function searchUsers(query) {
        if (!query.trim()) {
            displayUsers(users);
            return;
        }
        
        const filteredUsers = users.filter(user => 
            user.fullName.toLowerCase().includes(query.toLowerCase()) ||
            user.username.toLowerCase().includes(query.toLowerCase())
        );
        displayUsers(filteredUsers);
    }

    // Utility functions
    function formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'});
    }

    function updateUserOnlineStatus(userId, isOnline) {
        const userElement = document.querySelector(`[data-user-id="${userId}"] .conversation-avatar`);
        if (userElement) {
            if (isOnline) {
                userElement.classList.add('online');
            } else {
                userElement.classList.remove('online');
            }
        }
    }

    function updateConversationPreview(userId, content) {
        const previewElement = document.querySelector(`[data-user-id="${userId}"] .conversation-preview`);
        if (previewElement) {
            previewElement.textContent = content.length > 30 ? content.substring(0, 30) + '...' : content;
        }
    }

    function showError(message) {
        conversationsList.innerHTML = `<div class="error-message">${message}</div>`;
    }

    function autoResize() {
        messageInput.style.height = 'auto';
        messageInput.style.height = Math.min(messageInput.scrollHeight, 100) + 'px';
    }

    function animation() {
        const element = document.getElementById("animation");
        if (element) {
            element.style.display = 'block';
            element.classList.add("animate");
            setTimeout(() => {
                element.classList.remove("animate");
                element.style.display = 'none';
            }, 1000);
        }
    }

    function logout() {
        if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
            if (tcpSocket) {
                tcpSocket.close();
            }
            window.location.href = 'logout';
        }
    }

    function showProfile() {
        alert(`Thông tin người dùng:\nTên: ${currentUserName}\nTrạng thái: Đang hoạt động`);
    }

    function toggleMobileMenu() {
        document.getElementById('sidebar').classList.toggle('mobile-show');
    }

    function handleFileSelect(files) {
        if (files.length === 0) return;
        
        const file = files[0];
        console.log('Selected file:', file.name);
        animation();
        alert(`File đã chọn: ${file.name}. Tính năng upload sẽ được triển khai.`);
        
        document.getElementById('file-input').value = '';
    }

    // Setup event listeners
    function setupEventListeners() {
        if (sendButton) {
            sendButton.addEventListener('click', sendMessage);
        }

        if (messageInput) {
            messageInput.addEventListener('input', autoResize);
            messageInput.addEventListener('keypress', function(event) {
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    sendMessage();
                }
            });
        }

        document.getElementById('voice-call-button')?.addEventListener('click', () => {
            if (selectedUserId) {
                alert('Tính năng gọi thoại sẽ được triển khai với WebRTC.');
            }
        });

        document.getElementById('video-call-button')?.addEventListener('click', () => {
            if (selectedUserId) {
                alert('Tính năng gọi video sẽ được triển khai với WebRTC.');
            }
        });
    }

    // Initialize
    setupEventListeners();
    autoResize();
</script>
</body>
</html>