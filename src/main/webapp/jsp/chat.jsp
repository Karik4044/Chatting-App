<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
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
            gap: 24px;
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
            position: relative;
        }

        /* Styles for sender name in group chat */
        .message-bubble .sender-name {
            position: absolute;
            top: -18px;
            left: 12px;
            font-size: 0.75em;
            color: #aaaaaa;
            white-space: nowrap;
        }
        .message.sent {
            flex-direction: row-reverse;
        }
        .message.sent .message-bubble {
            background: #003554;
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/call.css" type="text/css">
</head>
<body>
<div class="sidebar">
    <div class="sidebar-header">
        <h1 class="sidebar-title">Đoạn chat</h1>
        <!-- Nút tạo nhóm chat  -->
        <svg class="edit-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg> 
        
    </div>
    <div class="search-box">
        <input type="text" class="search-input" placeholder="Tìm kiếm" oninput="filterChatList(this.value)">
    </div>
    <div class="chat-list" id="chatList"></div>
</div>

<div class="main-chat">
    <div class="chat-header">
        <div class="avatar" id="currentChatAvatar"><img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;"></div>
        <div class="chat-user-info">
            <div class="chat-user-name" id="currentChatName">Tên người dùng</div>
            <div class="chat-user-status" id="currentChatStatus">Vai trò</div>
        </div>
        <div class="chat-actions">
            <button id="voiceCallBtn" class="action-btn"><i class="bi bi-telephone"></i></button>
            <button id="videoCallBtn" class="action-btn"><i class="bi bi-camera-video"></i></button>
        </div>
    </div>

    <div class="messages-container" id="messagesContainer"></div>

    <div class="message-input-container">
        <button class="action-btn" id="attachFileBtn"><i class="bi bi-file-earmark-fill"></i></button>
        <input type="file" id="fileInput" style="display: none;" />
        <textarea class="message-input" id="messageInput" placeholder="Aa" rows="1"></textarea>
        <button class="send-btn" id="sendBtn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
        </button>
    </div>
</div>

<div id="call-container" class="hidden">
    <!-- Call UI will be dynamically created by JavaScript -->
</div>

<!-- Video elements for WebRTC -->
<video id="remote-video" autoplay playsinline style="display: none;"></video>
<video id="local-video" autoplay playsinline muted style="display: none;"></video>

<!-- Modal tạo nhóm -->
<div id="createGroupModal" style="display:none; position:fixed; z-index:1001; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.4); align-items:center; justify-content:center;">
  <div style="background:#222; padding:24px; border-radius:12px; min-width:300px;">
    <h3 style="margin-bottom:12px;">Tạo nhóm chat mới</h3>
    <input id="groupNameInput" type="text" placeholder="Tên nhóm" style="width:100%;padding:8px;border-radius:6px;border:none;margin-bottom:12px;">
    <div style="display:flex;gap:8px;justify-content:flex-end;">
      <button onclick="closeGroupModal()" style="padding:6px 16px;border:none;border-radius:6px;background:#555;color:#fff;">Hủy</button>
      <button onclick="createGroup()" style="padding:6px 16px;border:none;border-radius:6px;background:#0084ff;color:#fff;">Tạo</button>
    </div>
  </div>
</div>

<script src="${pageContext.request.contextPath}/js/webrtc.js"></script>
<script>
    const ctx = '${pageContext.request.contextPath}';
    console.log("Application Context Path (ctx):", ctx);

    const usernameFromSession = sessionStorage.getItem('username');
    let currentUser = null;
    let currentChat = null;
    let isGroupChat = false;
    let lastMessageTimestamp = null; // Track last message time
    let pollingInterval = null; // Store polling interval
    let isPolling = false; // Track if polling is active
    let displayMessageIds = new Set(); // Track displayed message IDs to avoid duplicates

    // Function to initialize the application after webrtc.js is loaded
    function initializeApp() {
        if (!usernameFromSession) {
            window.location.href = ctx + '/jsp/login.jsp';
        } else {
            currentUser = { username: usernameFromSession };
            loadUsers();
            loadGroups();
            
            // Check if initWebRTC is available
            if (typeof initWebRTC === 'function') {
                initWebRTC(usernameFromSession); // Initialize WebRTC
            } else {
                console.error('initWebRTC function not found. WebRTC functionality will not be available.');
            }
        }
    }

    // Wait for webrtc.js to load with timeout
    let webrtcLoadAttempts = 0;
    const maxAttempts = 50; // 5 seconds timeout (50 * 100ms)
    
    function waitForWebRTC() {
        webrtcLoadAttempts++;
        
        if (typeof initWebRTC === 'function') {
            console.log('WebRTC loaded successfully');
            initializeApp();
        } else if (webrtcLoadAttempts < maxAttempts) {
            // Check again after a short delay
            setTimeout(waitForWebRTC, 100);
        } else {
            console.error('WebRTC failed to load after 5 seconds. Call functionality will not be available.');
            // Initialize app without WebRTC
            initializeApp();
        }
    }

    // Start waiting for webrtc.js to load
    waitForWebRTC();

    async function loadUsers() {
        try {
            const res = await fetch(ctx + '/api/users');
            if (!res.ok) {
                console.error("Lỗi khi tải danh sách người dùng:", res.status, await res.text());
                return;
            }
            const usersArray = await res.json();
            const chatList = document.getElementById('chatList');
            // XÓA CHỈ CÁC USER ITEM, KHÔNG XÓA GROUP
            chatList.querySelectorAll('.chat-item:not(.chat-group-item)').forEach(e => e.remove());

            usersArray.forEach(userObject => {
                if (!userObject || typeof userObject.username === 'undefined') {
                    console.warn("Bỏ qua người dùng do thiếu dữ liệu:", userObject);
                    return;
                }

                if (currentUser && userObject.username !== currentUser.username) {
                    const chatItem = document.createElement('div');
                    chatItem.className = 'chat-item';
                    chatItem.onclick = function() {
                        document.querySelectorAll('.chat-item').forEach(item => item.classList.remove('active'));
                        this.classList.add('active');
                        selectChat(userObject.username, false);
                    }

                    const avatarDiv = document.createElement('div');
                    avatarDiv.className = 'avatar';
                    avatarDiv.innerHTML = '<img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';

                    const chatInfoDiv = document.createElement('div');
                    chatInfoDiv.className = 'chat-info';

                    const chatNameDiv = document.createElement('div');
                    chatNameDiv.className = 'chat-name';
                    chatNameDiv.textContent = userObject.username;

                    const chatPreviewDiv = document.createElement('div');
                    chatPreviewDiv.className = 'chat-preview';

                    chatInfoDiv.appendChild(chatNameDiv);
                    chatInfoDiv.appendChild(chatPreviewDiv);

                    const chatMetaDiv = document.createElement('div');
                    chatMetaDiv.className = 'chat-meta';

                    chatItem.appendChild(avatarDiv);
                    chatItem.appendChild(chatInfoDiv);
                    chatItem.appendChild(chatMetaDiv);

                    chatList.appendChild(chatItem);
                }
            });
        } catch (error) {
            console.error("Đã xảy ra lỗi trong hàm loadUsers:", error);
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
        console.log('selectChat called with:', { target, isGroup });

        // Stop polling for previous chat
        stopPolling();

        currentChat = target;
        isGroupChat = isGroup;
        lastMessageTimestamp = null; // Reset timestamp
        displayMessageIds.clear(); // Clear displayed message IDs

        console.log('Updated currentChat:', currentChat);
        console.log('Updated isGroupChat:', isGroupChat);

        document.getElementById('currentChatName').textContent = target;
        document.getElementById('currentChatAvatar').innerHTML = '<img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';
        document.getElementById('currentChatStatus').textContent = isGroup ? 'Group chat' : 'User';
        document.getElementById('messagesContainer').innerHTML = '';

        loadChatHistory(target, isGroup).then(() => {
            // Start polling for new messages after loading history
            startPolling();
        });
    }

    async function loadChatHistory(target, isGroup) {
        const container = document.getElementById('messagesContainer');
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
                lastMessageTimestamp = null;
                return;
            }

            messages.forEach(msg => displayMessage(msg));

            // Update last message timestamp
            if (messages.length > 0) {
                const lastMessage = messages[messages.length - 1];
                lastMessageTimestamp = lastMessage.timestamp;
                console.log('Set lastMessageTimestamp to:', lastMessageTimestamp);
            }

            scrollToBottom(container);
        } catch (e) {
            console.error('Lỗi tải lịch sử:', e);
            container.innerHTML = '<p style="text-align:center;color:red;">Error: ' + e.message + '</p>';
        }
    }

    function isScrolledToBottom(el) {
        if (!el) return false;
        const tolerance = 5;
        return el.scrollHeight - el.scrollTop - el.clientHeight < tolerance;
    }
    // NEW: Function to check for new messages
    async function checkForNewMessages() {
            if (!currentChat || isPolling) {
                return; // Exit if no chat is selected or if a check is already running
            }

            isPolling = true; // Lock to prevent other polls

            const container = document.getElementById('messagesContainer');
            const shouldScroll = isScrolledToBottom(container);

            let url = ctx + '/api/messages/history' +
                '?currentUsername=' + encodeURIComponent(usernameFromSession) +
                '&targetName=' + encodeURIComponent(currentChat) +
                '&isGroup=' + isGroupChat;

            if (lastMessageTimestamp) {
                url += '&after=' + encodeURIComponent(lastMessageTimestamp);
            }

            try {
                const response = await fetch(url);
                if (!response.ok) {
                    console.error("Failed to fetch new messages, status:", response.status);
                    return;
                }
                const newMessages = await response.json();

                if (newMessages && newMessages.length > 0) {
                    newMessages.forEach(msg => {
                        displayMessage(msg);
                    });
                    lastMessageTimestamp = newMessages[newMessages.length - 1].timestamp;

                    if (shouldScroll) {
                        scrollToBottom(container);
                    }
                }
            } catch (error) {
                console.error('Error checking for new messages:', error);
            } finally {
                isPolling = false; // Always unlock, even if an error occurs
            }
        }

    // NEW: Start polling for new messages
    function startPolling() {
        stopPolling(); // Stop any existing polling
        console.log('Starting polling for new messages...');
        pollingInterval = setInterval(checkForNewMessages, 1000); // Check every 0.1 seconds
    }

    // NEW: Stop polling
    function stopPolling() {
        if (pollingInterval) {
            console.log('Stopping polling...');
            clearInterval(pollingInterval);
            pollingInterval = null;
        }
    }

    function displayMessage(msg) {
        // Tạo một khóa duy nhất cho mỗi tin nhắn dựa trên timestamp, senderUsername và nội dung
        // Tránh hiển thị tin nhắn trùng lặp polling
        const messageKey = msg.timestamp + '-' + msg.senderUsername + '-' + msg.content;
        if (displayMessageIds.has(messageKey)) {
            return; // Skip if this message is already displayed.
        }
        displayMessageIds.add(messageKey); // Remember that this message has been displayed.

        const container = document.getElementById('messagesContainer');
        const isSent = msg.senderUsername === usernameFromSession;

        // Add a date separator if the day has changed
        let messageDate = null;
        if (msg.timestamp) {
            const parsedDate = new Date(msg.timestamp);
            if (!isNaN(parsedDate)) {
                messageDate = parsedDate.toDateString();
            }
        }

        const lastMessage = container.lastElementChild;
        let shouldAddDateSeparator = false;

        if (!lastMessage) {
            shouldAddDateSeparator = messageDate !== null;
        } else if (lastMessage.classList.contains('date-separator')) {
            const lastDateText = lastMessage.querySelector('.date-text').textContent;
            const currentDateText = formatDateForDisplay(messageDate);
            shouldAddDateSeparator = lastDateText !== currentDateText;
        } else {
            const lastMessageDate = lastMessage.getAttribute('data-date');
            shouldAddDateSeparator = messageDate && messageDate !== lastMessageDate;
        }

        if (shouldAddDateSeparator && messageDate) {
            const dateSeparator = document.createElement('div');
            dateSeparator.className = 'date-separator';
            const dateText = formatDateForDisplay(messageDate);
            dateSeparator.innerHTML = `<div class="date-line"></div><div class="date-text">\${dateText}</div><div class="date-line"></div>`;
            container.appendChild(dateSeparator);
        }

        const wrap = document.createElement('div');
        wrap.className = 'message' + (isSent ? ' sent' : '');
        wrap.setAttribute('data-date', messageDate || '');

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';

        //Chức năng hiển thị avatar
        const avatar = document.createElement('div');
        avatar.className = 'avatar';
        avatar.innerHTML = '<img src="https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';
        wrap.appendChild(avatar);

        //Hiển thị tên phía trên góc bong bóng chat
        if (isGroupChat && !isSent) {
            const senderName = document.createElement('div');
            senderName.className = 'sender-name'; /* Áp dụng class cho styling */
            senderName.textContent = msg.senderUsername;
            bubble.appendChild(senderName); // Thêm tên người gửi VÀO bubble
        }


        //Chức năng gửi file
        if (msg.content && msg.content.startsWith('[file:') && msg.content.endsWith(']')) {
            // Handle file messages
            const fullPath = msg.content.substring(6, msg.content.length - 1);
            const fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            const originalFileName = fileName.substring(fileName.indexOf('_') + 1);
            const ext = (originalFileName.split('.').pop() || '').toLowerCase();

            const fileContainer = document.createElement('div');
            fileContainer.style.display = 'flex';
            fileContainer.style.alignItems = 'center';
            fileContainer.style.gap = '8px';

            const icon = document.createElement('span');
            icon.style.fontSize = '2em';
            icon.style.lineHeight = '1';

            // Set icon based on file type
            if (['txt', 'md', 'doc', 'docx', 'pdf', 'pptx', 'xlsx'].includes(ext)) {
                icon.innerHTML = '<i class="bi bi-file-earmark-text-fill"></i>';
            } else if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)) {
                icon.innerHTML = '<i class="bi bi-image"></i>';
            } else if (['mp3', 'wav', 'ogg'].includes(ext)) {
                icon.innerHTML = '<i class="bi bi-file-earmark-music-fill"></i>';
            } else if (['mp4', 'avi', 'mov', 'mkv'].includes(ext)) {
                icon.innerHTML = '<i class="bi bi-file-earmark-play-fill"></i>';
            } else {
                icon.innerHTML = '<i class="bi bi-file-earmark-fill"></i>'; // Default icon for unknown types
            }

            const link = document.createElement('a');
            link.href = ctx + '/download?fileName=' + encodeURIComponent(fileName);
            link.download = originalFileName;
            link.textContent = originalFileName;
            link.style.textDecoration = 'none';
            link.style.wordBreak = 'break-all';

            // Set icon and link color based on sender for consistency
            icon.style.color = '#e0e0e0';
            link.style.color = '#8ab4f8'; // Light blue for links on dark background

            fileContainer.appendChild(icon);
            fileContainer.appendChild(link);
            bubble.appendChild(fileContainer);

        } else {
            // Handle regular text messages
            //Tạo 1 phần tử span riêng để hiển thị nội dung tin nhắn
            //Tránh ghi đè lên các phần tử khác như avatar, tên người gửi
            const msgContent = document.createElement('span');  // Create a span for the message content
            msgContent.textContent = msg.content || 'No content';   // Use 'No content' if msg.content is empty
            bubble.appendChild(msgContent); // Append the message content to the bubble
        }

        // Add timestamp to the bubble
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

    // Hàm cuộn xuống cuối của một phần tử
    function scrollToBottom(el) {
        el.scrollTop = el.scrollHeight;
    }

    // Hàm hiển thị thông báo của người dùng trong chức năng gọi
    function showNotification(title, message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        
        // Set background color based on type
        let backgroundColor;
        switch (type) {
            case 'success':
                backgroundColor = '#28a745';
                break;
            case 'error':
                backgroundColor = '#dc3545';
                break;
            case 'warning':
                backgroundColor = '#ffc107';
                break;
            default:
                backgroundColor = '#17a2b8';
        }
        
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${backgroundColor};
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
            z-index: 1002;
            max-width: 300px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
        `;
        
        notification.innerHTML = `
            <div style="font-weight: 600; margin-bottom: 5px;">${title}</div>
            <div style="font-size: 14px;">${message}</div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 3000);
    }

    // Updated sendMessage function
    async function sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const messageContent = messageInput.value.trim();

        if (!messageContent) {
            console.log("Message is empty, not sending.");
            return;
        }
        if (!currentChat) {
            alert("Please select a user or group to chat with first.");
            return;
        }

        const params = new URLSearchParams();
        params.append('senderUsername', usernameFromSession);
        params.append('targetName', currentChat);
        params.append('messageContent', messageContent);
        params.append('isGroup', isGroupChat);

        try {
            const response = await fetch(ctx + '/api/messages/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString(),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server returned an error:', response.status, errorText);
                throw new Error('Failed to send message.');
            }

            // Clear input
            messageInput.value = '';

            // Immediately check for new messages (including the one we just sent)
            setTimeout(() => {
                checkForNewMessages();
            }, 100); // Small delay to ensure message is saved

        } catch (error) {
            console.error('Error sending message:', error);
            alert('Could not send message. Please check the console for more details.');
        }
    }

    // Clean up polling when page is closed
    window.addEventListener('beforeunload', stopPolling);

    document.getElementById('messageInput').addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 120) + 'px';
    });

    document.getElementById('messageInput').addEventListener('keypress', function(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            sendMessage();
        }
    });

    // Send message button
    document.getElementById('sendBtn').addEventListener('click', sendMessage);

    //Send file btn
    document.getElementById('attachFileBtn').addEventListener('click', () => {
        document.getElementById('fileInput').click();
    });

    //Sử dụng input file để gửi file
    document.getElementById('fileInput').addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            sendFile(file);
        }
        // Reset input to allow selecting the same file again
        this.value = '';
    });

    //Chức năng gửi file
    async function sendFile(file) {
        if (!currentChat) {
            alert("Please select a chat first.");
            return;
        }

        const formData = new FormData();
        formData.append('file', file);  // Append the file to the FormData object
        formData.append('senderUsername', usernameFromSession);
        formData.append('targetName', currentChat);
        formData.append('isGroup', isGroupChat);

        try {
            // Note: You will need to create a new servlet endpoint (e.g., /api/messages/upload)
            // to handle the multipart/form-data request on the server.
            const response = await fetch(ctx + '/api/messages/upload', {
                method: 'POST',
                body: formData, // The browser will set the correct 'Content-Type' for FormData
            });

            if (!response.ok) {
                throw new Error('File upload failed: ' + await response.text());
            }

            // Refresh chat to show the new file message
            setTimeout(() => checkForNewMessages(), 250);

        } catch (error) {
            console.error('Error sending file:', error);
            alert('Could not send file. See console for details.');
        }
    }
    // Event Listeners for Calling
    document.getElementById('voiceCallBtn').addEventListener('click', () => {
        if (!currentChat) {
            showNotification('Cuộc gọi', 'Vui lòng chọn người dùng để gọi', 'warning');
            return;
        }
        
        // Check if WebRTC functions are available
        if (typeof startCall === 'function') {
            startCall(currentChat, false); // false for voice-only call
        } else {
            showNotification('Cuộc gọi', 'Chức năng gọi không khả dụng. Vui lòng tải lại trang.', 'error');
        }
    });

    document.getElementById('videoCallBtn').addEventListener('click', () => {
        if (!currentChat) {
            showNotification('Cuộc gọi', 'Vui lòng chọn người dùng để gọi', 'warning');
            return;
        }
        
        // Check if WebRTC functions are available
        if (typeof startCall === 'function') {
            startCall(currentChat, true); // true for video call
        } else {
            showNotification('Cuộc gọi', 'Chức năng gọi không khả dụng. Vui lòng tải lại trang.', 'error');
        }
    });

    // Hiện modal khi click icon tạo nhóm
    document.querySelector('.edit-icon').addEventListener('click', function() {
        document.getElementById('createGroupModal').style.display = 'flex';
        document.getElementById('groupNameInput').value = '';
        document.getElementById('groupNameInput').focus();
    });

    // Đóng modal tạo nhóm
    function closeGroupModal() {
        document.getElementById('createGroupModal').style.display = 'none';
    }

    // Lưu nhóm vào localStorage
    function createGroup() {
        const groupName = document.getElementById('groupNameInput').value.trim();
        if (!groupName) {
            alert('Vui lòng nhập tên nhóm!');
            return;
        }
        let groups = JSON.parse(localStorage.getItem('chatGroups') || '[]');
        if (groups.includes(groupName)) {
            alert('Nhóm đã tồn tại!');
            return;
        }
        groups.push(groupName);
        localStorage.setItem('chatGroups', JSON.stringify(groups));
        closeGroupModal();
        loadGroups();
    }

    // Hiển thị nhóm ở sidebar
    function loadGroups() {
        let groups = JSON.parse(localStorage.getItem('chatGroups') || '[]');
        const chatList = document.getElementById('chatList');
        // Xóa nhóm cũ
        chatList.querySelectorAll('.chat-group-item').forEach(e => e.remove());
        groups.forEach(groupName => {
            const groupItem = document.createElement('div');
            groupItem.className = 'chat-item chat-group-item';
            groupItem.onclick = function() {
                document.querySelectorAll('.chat-item').forEach(item => item.classList.remove('active'));
                this.classList.add('active');
                selectChat(groupName, true);
            };

            // Create elements for chat info and name
            const avatarDiv = document.createElement('div');
            avatarDiv.className = 'avatar';
            avatarDiv.style.background = 'linear-gradient(45deg,#ff9800,#f44336)';
            avatarDiv.innerHTML = '<img src="https://cdn.vectorstock.com/i/500p/57/85/group-of-people-icon-vector-2855785.jpg" alt="avatar" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';

            const chatInfoDiv = document.createElement('div');
            chatInfoDiv.className = 'chat-info';

            const chatNameDiv = document.createElement('div');
            chatNameDiv.className = 'chat-name';
            // Set the text content of the chat name using JavaScript
            chatNameDiv.textContent = groupName || 'No group name set';

            const chatPreviewDiv = document.createElement('div');
            chatPreviewDiv.className = 'chat-preview';
            chatPreviewDiv.textContent = 'Nhóm chat';

            const chatMetaDiv = document.createElement('div');
            chatMetaDiv.className = 'chat-meta';

            chatInfoDiv.appendChild(chatNameDiv);
            chatInfoDiv.appendChild(chatPreviewDiv);

            groupItem.appendChild(avatarDiv);
            groupItem.appendChild(chatInfoDiv);
            groupItem.appendChild(chatMetaDiv);

            chatList.prepend(groupItem); // Nhóm lên đầu
        });
    }

    // Gọi khi load trang và sau khi tạo nhóm
    window.addEventListener('DOMContentLoaded', loadGroups);
</script>
</body>
</html>