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
  </style>
</head>
<body>
<div class="login-container">
  <h2>Đăng nhập</h2>
  <input type="text" id="username" placeholder="Tên đăng nhập">
  <input type="password" id="password" placeholder="Mật khẩu">
  <button onclick="login()">Đăng nhập</button>
  <p>Chưa có tài khoản?
    <a href="${pageContext.request.contextPath}/jsp/register.jsp">Đăng ký</a>
  </p>
</div>

<script>
  async function login() {
    const ctx = '${pageContext.request.contextPath}';
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const response = await fetch(ctx + '/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:
        'username=' + encodeURIComponent(username)
        + '&password=' + encodeURIComponent(password)
    });

    if (response.ok) {
      const user = await response.json();
      // lưu lại để chat.jsp dùng
      sessionStorage.setItem('username', user.username);
      window.location.href = ctx + '/jsp/chat.jsp';
    } else {
      alert('Đăng nhập thất bại');
    }
  }
</script>
</body>
</html>