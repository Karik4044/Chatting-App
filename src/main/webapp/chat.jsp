<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Giao Diện Chat</title>
    <style>
        body, html {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            height: 100%;
            overflow: hidden;
            background-color: #f0f2f5;
        }
        .chat-container {
            display: flex;
            height: 100%;
        }

        /* Thanh bên trái: Danh sách cuộc trò chuyện */
        .sidebar {
            width: 320px;
            background-color: #ffffff;
            border-right: 1px solid #ced0d4;
            display: flex;
            flex-direction: column;
        }
        .sidebar-header {
            padding: 10px 15px;
            border-bottom: 1px solid #ced0d4;
        }
        .sidebar-header h2 {
            margin: 0;
            font-size: 20px;
        }
        .sidebar-search input {
            width: calc(100% - 30px);
            padding: 8px 15px;
            margin: 10px 0;
            margin-left: 10px;
            border: 1px solid #ddd;
            border-radius: 20px;
            background-color: #f0f2f5;
        }
        .chat-list {
            list-style: none;
            padding: 0;
            margin: 0;
            overflow-y: auto;
            flex-grow: 1;
        }
        .chat-list-item {
            display: flex;
            align-items: center;
            padding: 10px 15px;
            cursor: pointer;
            border-bottom: 1px solid #f0f0f0;
        }
        .chat-list-item:hover, .chat-list-item.active {
            background-color: #f0f2f5;
        }
        .chat-list-item img.avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            margin-right: 10px;
            object-fit: cover;
        }
        .chat-list-item .info .name {
            font-weight: bold;
        }
        .chat-list-item .info .preview {
            font-size: 0.9em;
            color: #65676b;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 180px; /* Giới hạn chiều rộng để tránh vỡ layout */
        }
        .chat-list-item .meta .time {
            font-size: 0.8em;
            color: #8a8d91;
        }

        /* Khu vực chat chính */
        .chat-area {
            flex-grow: 1;
            display: flex;
            flex-direction: column;
            background-color: #ffffff;
        }
        .chat-area-header {
            display: flex;
            align-items: center;
            padding: 10px 20px;
            border-bottom: 1px solid #ced0d4;
            background-color: #fff;
        }
        .chat-area-header img.avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            margin-right: 10px;
        }
        .chat-area-header .name {
            font-weight: bold;
            font-size: 1.1em;
        }
        .chat-area-header .actions {
            margin-left: auto;
        }
        .chat-area-header .actions button {
            background: none;
            border: none;
            font-size: 1.2em;
            margin-left: 15px;
            cursor: pointer;
            color: #0084ff;
        }

        .messages-display {
            flex-grow: 1;
            padding: 20px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
        }
        /* Hiển thị video/ảnh thay cho danh sách tin nhắn (nếu có) */
        .media-display-area {
            flex-grow: 1; /* Quan trọng để chiếm không gian */
            display: flex; /* Bật flexbox */
            align-items: center; /* Căn giữa theo chiều dọc */
            justify-content: center; /* Căn giữa theo chiều ngang */
            padding: 20px;
            background-color: #e9ebee; /* Màu nền nhẹ nhàng */
        }
        .media-content {
            text-align: center;
        }
        .media-content img, .media-content video { /* Áp dụng cho cả ảnh và video */
            max-width: 100%; /* Để không vượt quá chiều rộng của parent */
            max-height: calc(100vh - 200px); /* Giới hạn chiều cao, trừ đi header và input area */
            border: 1px solid #ccc;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .media-content p {
            margin-top: 10px;
            font-size: 0.9em;
            color: #606770;
        }


        .message-input {
            display: flex;
            align-items: center;
            padding: 10px 20px;
            border-top: 1px solid #ced0d4;
            background-color: #fff;
        }
        .message-input .input-actions button {
            background: none;
            border: none;
            font-size: 1.3em;
            margin: 0 5px;
            cursor: pointer;
            color: #0084ff;
        }
        .message-input input[type="text"] {
            flex-grow: 1;
            padding: 10px 15px;
            border: none;
            border-radius: 20px;
            background-color: #f0f2f5;
            margin: 0 10px;
            font-size: 1em;
        }
        .message-input input[type="text"]:focus {
            outline: none;
        }

        /* CSS cho tin nhắn (nếu bạn muốn hiển thị danh sách tin nhắn thay vì video) */
        .message {
            margin-bottom: 10px;
            padding: 8px 12px;
            border-radius: 18px;
            max-width: 70%;
            word-wrap: break-word;
        }
        .message.sent {
            background-color: #0084ff;
            color: white;
            align-self: flex-end;
            margin-left: auto; /* Đẩy sang phải */
        }
        .message.received {
            background-color: #e4e6eb;
            color: #050505;
            align-self: flex-start;
            margin-right: auto; /* Đẩy sang trái */
        }
        .message .sender-name { /* Hiển thị tên người gửi cho tin nhắn nhận (trong group) */
            font-size: 0.8em;
            color: #606770;
            margin-bottom: 3px;
            display: block;
        }

    </style>
</head>
<body>
<div class="chat-container">
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>Đoạn chat</h2>
        </div>
        <div class="sidebar-search">
            <input type="text" placeholder="Tìm kiếm trên Messenger">
        </div>
        <ul class="chat-list">
        </ul>
            <!-- Thêm các mục chat khác ở đây -->
    </aside>

    <main class="chat-area">
        <header class="chat-area-header">
            <img src="https://i.pravatar.cc/40?u=mui_ten" alt="Avatar" class="avatar">
            <div class="user-info">
                <div class="name">Child Predator</div>
            </div>
            <div class="actions">
                <button title="Gọi thoại">📞</button>
                <button title="Gọi Video">📹</button>
                <button title="Thông tin">ℹ️</button>
            </div>
        </header>

        <div class="messages-display" style="display:none;flex-grow:1;overflow-y:auto;padding:20px;">
            <ul id="msgs" style="list-style:none;padding:0;margin:0;"></ul>
        </div>

        <div class="media-display-area">
            <div class="media-content">
                <img src="https://i.imgur.com/YQx2m9g.jpeg" alt="Nội dung media">
                <p>Nội dung media đang hiển thị</p>
                <p><strong>Mũi Tên</strong> - 14:49 CN</p>
            </div>
        </div>

        <footer class="message-input">
            <div class="input-actions">
                <button title="Thêm file">➕</button>
                <button title="Biểu tượng cảm xúc">😀</button>
            </div>
            <input type="text" placeholder="Aa">
            <div class="input-actions">
                <button title="Gửi">➤</button> </div>
        </footer>
    </main>
</div>

<script>
    let currentTarget=null, currentType=null;
    const msgsUL = document.getElementById('msgs');
    const messagesDisplay = document.querySelector('.messages-display');
    const mediaArea = document.querySelector('.media-display-area');

    document.querySelectorAll('.chat-list-item').forEach(item => {
        item.addEventListener('click', function() {
            // active class
            document.querySelectorAll('.chat-list-item.active').forEach(a=>a.classList.remove('active'));
            this.classList.add('active');

            // header update
            const avatar = this.querySelector('img.avatar').src.replace('50?u=','40?u=');
            const name  = this.querySelector('.info .name').textContent;
            document.querySelector('.chat-area-header img.avatar').src = avatar;
            document.querySelector('.chat-area-header .user-info .name').textContent = name;

            // xác định target & type rồi fetch lịch sử
            currentTarget = name;
            currentType   = 'private'; // hoặc 'group' nếu danh sách nhóm
            fetch(`chat?target=${encodeURIComponent(currentTarget)}&type=${currentType}`)
                .then(r=>r.json())
                .then(data=>{
                    msgsUL.innerHTML = '';
                    data.forEach(m=>{
                        const li = document.createElement('li');
                        li.className = 'message ' + (m.senderId === /* server side ghi senderId */ null ? 'sent' : 'received');
                        li.textContent = `${m.content}`;
                        msgsUL.appendChild(li);
                    });
                    mediaArea.style.display='none';
                    messagesDisplay.style.display='flex';
                    messagesDisplay.scrollTop = messagesDisplay.scrollHeight;
                });
        });
    });

    // gửi tin
    document.querySelector('.message-input button[title="Gửi"]').addEventListener('click',()=>{
        const txt = document.querySelector('.message-input input[type="text"]');
        if(!currentTarget||!txt.value) return;
        fetch('chat', {
            method:'POST',
            headers:{'Content-Type':'application/x-www-form-urlencoded'},
            body:`target=${encodeURIComponent(currentTarget)}&type=${currentType}&message=${encodeURIComponent(txt.value)}`
        })
        .then(r=>r.json())
        .then(m=>{
            const li = document.createElement('li');
            li.className='message sent';
            li.textContent = m.content;
            msgsUL.appendChild(li);
            messagesDisplay.scrollTop = messagesDisplay.scrollHeight;
            txt.value='';
        });
    });
</script>
</body>
</html>