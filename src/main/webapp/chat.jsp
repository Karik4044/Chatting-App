<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Web Messenger</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .chat-container {
            width: 90%;
            max-width: 1200px;
            height: 80vh;
            background: white;
            border-radius: 20px;
            display: flex;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .sidebar {
            width: 300px;
            background: #f8f9fa;
            border-right: 1px solid #e9ecef;
            display: flex;
            flex-direction: column;
        }

        .sidebar-header {
            padding: 20px;
            background: #4a5568;
            color: white;
            text-align: center;
        }

        .sidebar-header h2 {
            margin-bottom: 10px;
        }

        .connection-status {
            font-size: 12px;
            padding: 5px 10px;
            border-radius: 15px;
            background: rgba(255,255,255,0.2);
        }

        .online-users {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
        }

        .online-users h3 {
            margin-bottom: 15px;
            color: #4a5568;
            font-size: 14px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .user-item {
            display: flex;
            align-items: center;
            padding: 10px;
            margin-bottom: 5px;
            border-radius: 10px;
            background: white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }

        .user-avatar {
            width: 35px;
            height: 35px;
            border-radius: 50%;
            background: linear-gradient(45deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            margin-right: 12px;
            font-size: 14px;
        }

        .chat-area {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .chat-header {
            padding: 20px;
            background: #4a5568;
            color: white;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .chat-title {
            display: flex;
            align-items: center;
        }

        .chat-title i {
            margin-right: 10px;
            font-size: 20px;
        }

        .messages-container {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            background: #f8f9fa;
        }

        .message {
            margin-bottom: 15px;
            display: flex;
            align-items: flex-start;
        }

        .message.own {
            justify-content: flex-end;
        }

        .message.system {
            justify-content: center;
        }

        .message-avatar {
            width: 30px;
            height: 30px;
            border-radius: 50%;
            background: linear-gradient(45deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            margin-right: 10px;
            font-size: 12px;
            flex-shrink: 0;
        }

        .message-content {
            max-width: 60%;
            background: white;
            padding: 12px 16px;
            border-radius: 18px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            position: relative;
        }

        .message.own .message-content {
            background: linear-gradient(45deg, #667eea, #764ba2);
            color: white;
            margin-left: 10px;
        }

        .message.system .message-content {
            background: #e9ecef;
            color: #6c757d;
            font-style: italic;
            text-align: center;
            max-width: 80%;
        }

        .message-sender {
            font-weight: bold;
            margin-bottom: 4px;
            font-size: 12px;
            color: #667eea;
        }

        .message.own .message-sender {
            color: rgba(255,255,255,0.8);
        }

        .message-text {
            word-wrap: break-word;
            line-height: 1.4;
        }

        .message-time {
            font-size: 10px;
            color: rgba(0,0,0,0.5);
            margin-top: 5px;
        }

        .message.own .message-time {
            color: rgba(255,255,255,0.7);
        }

        .input-area {
            padding: 20px;
            background: white;
            border-top: 1px solid #e9ecef;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .name-input {
            flex: 1;
            padding: 12px 16px;
            border: 2px solid #e9ecef;
            border-radius: 25px;
            font-size: 14px;
            outline: none;
            transition: border-color 0.3s;
        }

        .name-input:focus {
            border-color: #667eea;
        }

        .message-input {
            flex: 1;
            padding: 12px 16px;
            border: 2px solid #e9ecef;
            border-radius: 25px;
            font-size: 14px;
            outline: none;
            transition: border-color 0.3s;
        }

        .message-input:focus {
            border-color: #667eea;
        }

        .send-btn, .name-btn {
            background: linear-gradient(45deg, #667eea, #764ba2);
            color: white;
            border: none;
            width: 45px;
            height: 45px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: transform 0.2s;
        }

        .send-btn:hover, .name-btn:hover {
            transform: scale(1.1);
        }

        .name-setup {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            background: rgba(255,255,255,0.9);
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            z-index: 1000;
        }

        .name-setup h2 {
            margin-bottom: 20px;
            color: #4a5568;
        }

        .name-setup-form {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .hidden {
            display: none !important;
        }

        @media (max-width: 768px) {
            .sidebar {
                display: none;
            }
            
            .chat-container {
                width: 95%;
                height: 90vh;
            }
            
            .message-content {
                max-width: 80%;
            }
        }
    </style>
</head>
<body>
    <div class="chat-container">
        <div class="sidebar">
            <div class="sidebar-header">
                <h2>Web Messenger</h2>
                <div class="connection-status" id="connectionStatus">
                    <i class="fas fa-circle"></i> Connecting...
                </div>
            </div>
            <div class="online-users">
                <h3>Online Users (<span id="userCount">0</span>)</h3>
                <div id="usersList"></div>
            </div>
        </div>

        <div class="chat-area">
            <div class="name-setup" id="nameSetup">
                <h2>Enter Your Name</h2>
                <div class="name-setup-form">
                    <input type="text" id="nameInput" class="name-input" placeholder="Your name..." maxlength="20">
                    <button id="nameBtn" class="name-btn">
                        <i class="fas fa-check"></i>
                    </button>
                </div>
            </div>

            <div class="chat-header">
                <div class="chat-title">
                    <i class="fas fa-comments"></i>
                    <h3>General Chat</h3>
                </div>
                <div id="currentUser">
                    <p>Hello, ${currentUser}!</p>
                </div>
            </div>

            <div class="messages-container" id="messagesContainer">
            </div>

            <div class="input-area">
                <input type="text" id="messageInput" class="message-input" placeholder="Type your message..." disabled>
                <button id="sendBtn" class="send-btn" disabled>
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    </div>

    <script>
        class WebMessenger {
            constructor() {
                this.socket = null;
                this.currentUser = null;
                this.isConnected = false;
                this.initializeElements();
                this.bindEvents();
                this.connect();
            }

            initializeElements() {
                this.nameSetup = document.getElementById('nameSetup');
                this.nameInput = document.getElementById('nameInput');
                this.nameBtn = document.getElementById('nameBtn');
                this.messageInput = document.getElementById('messageInput');
                this.sendBtn = document.getElementById('sendBtn');
                this.messagesContainer = document.getElementById('messagesContainer');
                this.connectionStatus = document.getElementById('connectionStatus');
                this.usersList = document.getElementById('usersList');
                this.userCount = document.getElementById('userCount');
                this.currentUserElement = document.getElementById('currentUser');
            }

            bindEvents() {
                this.nameBtn.addEventListener('click', () => this.setName());
                this.nameInput.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') this.setName();
                });

                this.sendBtn.addEventListener('click', () => this.sendMessage());
                this.messageInput.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') this.sendMessage();
                });

                window.addEventListener('beforeunload', () => {
                    if (this.socket) {
                        this.socket.close();
                    }
                });
            }

            connect() {
                try {
                    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                    const wsUrl = `${protocol}//${window.location.host}/chat`;
                    
                    this.socket = new WebSocket(wsUrl);

                    this.socket.onopen = () => {
                        this.isConnected = true;
                        this.updateConnectionStatus('Connected', 'success');
                    };

                    this.socket.onmessage = (event) => {
                        this.handleMessage(JSON.parse(event.data));
                    };

                    this.socket.onclose = () => {
                        this.isConnected = false;
                        this.updateConnectionStatus('Disconnected', 'error');
                        setTimeout(() => this.connect(), 3000);
                    };

                    this.socket.onerror = (error) => {
                        this.updateConnectionStatus('Connection Error', 'error');
                        console.error('WebSocket error:', error);
                    };
                } catch (error) {
                    this.updateConnectionStatus('Connection Failed', 'error');
                    console.error('Connection error:', error);
                }
            }

            updateConnectionStatus(status, type) {
                const icon = type === 'success' ? 'fas fa-circle text-success' : 'fas fa-exclamation-circle text-danger';
                this.connectionStatus.innerHTML = `<i class="${icon}"></i> ${status}`;
                this.connectionStatus.className = `connection-status ${type}`;
            }

            setName() {
                const name = this.nameInput.value.trim();
                if (name && this.isConnected) {
                    this.sendToServer('setName', name);
                }
            }

            sendMessage() {
                const message = this.messageInput.value.trim();
                if (message && this.isConnected && this.currentUser) {
                    if (message.startsWith('/nick ')) {
                        const newName = message.substring(6).trim();
                        this.sendToServer('changeName', newName);
                    } else if (message === '/quit') {
                        this.sendToServer('quit', '');
                    } else {
                        this.sendToServer('chat', message);
                    }
                    this.messageInput.value = '';
                }
            }

            sendToServer(type, content) {
                if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                    this.socket.send(JSON.stringify({ type, content }));
                }
            }

            handleMessage(data) {
                switch (data.type) {
                    case 'system':
                        this.displaySystemMessage(data.content);
                        if (data.content.includes('Name set to:')) {
                            this.currentUser = data.content.split(': ')[1];
                            this.nameSetup.classList.add('hidden');
                            this.messageInput.disabled = false;
                            this.sendBtn.disabled = false;
                            this.currentUserElement.textContent = `Hello, ${this.currentUser}!`;
                        }
                        break;
                    case 'user':
                        this.displayUserMessage(data.sender, data.content, data.sender === this.currentUser);
                        break;
                    case 'userList':
                        this.updateUsersList(data.users);
                        break;
                }
            }

            displaySystemMessage(content) {
                const messageEl = document.createElement('div');
                messageEl.className = 'message system';
                messageEl.innerHTML = `
                    <div class="message-content">
                        <div class="message-text">${this.escapeHtml(content)}</div>
                        <div class="message-time">${newDate().toLocaleTimeString()}</div>
                    </div>
                `;
                this.messagesContainer.appendChild(messageEl);
                this.scrollToBottom();
            }

            displayUserMessage(sender, content, isOwn) {
                const messageEl = document.createElement('div');
                messageEl.className = `message ${isOwn ? 'own' : ''}`;
                
                const avatarHtml = isOwn ? '' : `<div class="message-avatar">${sender.charAt(0).toUpperCase()}</div>`;
                const senderHtml = isOwn ? '' : `<div class="message-sender">${this.escapeHtml(sender)}</div>`;
                
                messageEl.innerHTML = `
                    ${avatarHtml}
                    <div class="message-content">
                        ${senderHtml}
                        <div class="message-text">${this.escapeHtml(content)}</div>
                        <div class="message-time">${newDate().toLocaleTimeString()}</div>
                    </div>
                `;
                this.messagesContainer.appendChild(messageEl);
                this.scrollToBottom();
            }

            updateUsersList(users) {
                this.usersList.innerHTML = '';
                this.userCount.textContent = users.length;
                
                users.forEach(user => {
                    const userEl = document.createElement('div');
                    userEl.className = 'user-item';
                    userEl.innerHTML = `
                        <div class="user-avatar">${user.charAt(0).toUpperCase()}</div>
                        <div>${this.escapeHtml(user)}</div>
                    `;
                    this.usersList.appendChild(userEl);
                });
            }

            scrollToBottom() {
                this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
            }

            escapeHtml(text) {
                const div = document.createElement('div');
                div.textContent = text;
                return div.innerHTML;
            }
        }

        // Initialize the messenger when the page loads
        document.addEventListener('DOMContentLoaded', () => {
            new WebMessenger();
        });
    </script>
</body>
</html>