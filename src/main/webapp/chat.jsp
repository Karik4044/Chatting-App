<%-- webapp/chat.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.Entity.User" %>
<html>
<head>
    <title>Giao Diện Chat</title>
    <%-- Link đến file CSS ngoài --%>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/chat.css">
</head>
<body>
<div class="chat-container">
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>Đoạn chat</h2>
            <div id="currentUserInfo" style="font-size: 0.8em; margin-top: 5px;">
                <%
                    User loggedInUserForDisplay = (User) session.getAttribute("user");
                    if (loggedInUserForDisplay != null) {
                %>
                Đang đăng nhập: <strong><%= loggedInUserForDisplay.getUsername() %></strong>
                <%
                } else {
                %>
                <em>Chưa đăng nhập</em>
                <%
                    }
                %>
            </div>
        </div>
        <div class="sidebar-search">
            <input type="text" placeholder="Tìm kiếm trên Messenger" id="searchInput">
        </div>
        <ul class="chat-list" id="chat-list">
            <%-- User list items will be populated here by JavaScript from chat.js --%>
        </ul>
    </aside>

    <main class="chat-area">
        <header class="chat-area-header">
            <img src="https://i.pravatar.cc/40" alt="Avatar" class="avatar" id="chattingWithAvatar">
            <div class="user-info">
                <div class="name" id="chattingWithName">Chọn một cuộc trò chuyện</div>
            </div>
            <div class="actions">
                <button title="Gọi thoại">📞</button>
                <button title="Gọi Video">📹</button>
                <button title="Thông tin">ℹ️</button>
            </div>
        </header>

        <div class="messages-display" id="messagesDisplay" style="display:none;">
            <ul id="msgs" style="list-style:none;padding:0;margin:0;"></ul>
        </div>

        <div class="media-display-area" id="mediaDisplayArea">
            <div class="media-content">
                <p>Chào mừng bạn đến với ứng dụng chat!</p>
                <p>Hãy chọn một người dùng từ danh sách bên trái để bắt đầu.</p>
            </div>
        </div>

        <footer class="message-input">
            <div class="input-actions">
                <button title="Thêm file">➕</button>
                <button title="Biểu tượng cảm xúc">😀</button>
            </div>
            <input type="text" placeholder="Aa" id="messageInputField">
            <div class="input-actions">
                <button title="Gửi" id="sendButton">➤</button>
            </div>
        </footer>
    </main>
</div>

<%--
    Khởi tạo các biến JavaScript global từ session Java.
    File chat.js sẽ sử dụng các biến này.
--%>
<script>
    const loggedInUsername = "<%= session.getAttribute("user") != null ? ((com.example.chat.Entity.User)session.getAttribute("user")).getUsername() : "" %>";
    const loggedInUserId = "<%= session.getAttribute("user") != null ? ((com.example.chat.Entity.User)session.getAttribute("user")).getId() : 0L %>";
</script>
<script src="chat.js"></script>

</body>
</html>