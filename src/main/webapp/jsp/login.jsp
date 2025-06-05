<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đăng nhập</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background: #1a1a1a;
      color: #ffffff;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      margin: 0;
    }
    .login-container {
      background: #2a2a2a;
      padding: 20px;
      border-radius: 8px;
      width: 300px;
      text-align: center;
    }
    .login-container input {
      width: 100%;
      padding: 10px;
      margin: 10px 0;
      border: none;
      border-radius: 4px;
      background: #404040;
      color: #ffffff;
    }
    .login-container button {
      padding: 10px;
      background: #0084ff;
      border: none;
      border-radius: 4px;
      color: #ffffff;
      cursor: pointer;
    }
    .login-container button:hover {
      background: #0066cc;
    }
    .login-container button:disabled {
      background: #666;
      cursor: not-allowed;
    }
  </style>
</head>
<body>
<div class="login-container">
  <h2>Đăng nhập</h2>
  <form id="loginForm" onsubmit="event.preventDefault(); login();">
    <input type="text" id="username" placeholder="Tên đăng nhập" required>
    <input type="password" id="password" placeholder="Mật khẩu" required>
    <button type="submit">Đăng nhập</button>
  </form>
  <p>Chưa có tài khoản?
    <a href="${pageContext.request.contextPath}/jsp/register.jsp">Đăng ký</a>
  </p>
</div>

<script>
  let isRequestPending = false;

  async function login() {
    if (isRequestPending) {
      console.log('Request already in progress, ignoring...');
      return;
    }

    const ctx = '${pageContext.request.contextPath}';
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const loginButton = document.querySelector('#loginForm button');

    if (!username || !password) {
      alert('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu');
      return;
    }

    isRequestPending = true;
    loginButton.disabled = true;
    console.log('Sending:', { username, password });

    try {
      const response = await fetch(ctx + '/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(password)
      });

      console.log('Response status:', response.status);
      if (response.ok) {
        const user = await response.json();
        console.log('Response data:', user);
        if (user && user.username) {
          sessionStorage.setItem('username', user.username);
          console.log('Stored username:', sessionStorage.getItem('username'));
          console.log('Redirecting to:', ctx + '/jsp/chat.jsp');
          window.location.href = ctx + '/jsp/chat.jsp';
        } else {
          alert('Đăng nhập thất bại: Dữ liệu người dùng không hợp lệ');
        }
      } else {
        const errorText = await response.text();
        console.log('Error response:', errorText);
        alert('Đăng nhập thất bại: ' + errorText);
      }
    } catch (error) {
      console.error('Connection error:', error);
      alert('Lỗi kết nối: ' + error.message);
    } finally {
      isRequestPending = false;
      loginButton.disabled = false;
    }
  }
</script>
</body>
</html>