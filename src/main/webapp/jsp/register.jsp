<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký</title>
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

        .register-container {
            background: #2a2a2a2a;
            width: 300px;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }

        .register-container input {
            width: 100%;
            padding: 10px;
            margin: 0 0;
            border: none;
            margin-bottom: 10px;
            border-radius: 4px;
            background: #404040;
            color: #ffffff;
            width: 100%;
        }

        .register-container {
            padding: 10px;
            background: #0084ff;
            border: none;
            border-radius: 4px;
            color: #ffffff;
            cursor: pointer;
        }

        .register-container button:hover {
            background: #0066cc;
        }
    </style>
</head>
<body>
<div class="register-container">
  <h2>Đăng ký</h2>
  <input type="text" id="username" placeholder="Tên đăng nhập">
  <input type="password" id="password" placeholder="Mật khẩu">
  <button onclick="registerUser()">Đăng ký</button>
  <p>Đã có tài khoản?
    <a href="${pageContext.request.contextPath}/jsp/login.jsp">Đăng nhập</a>
  </p>
</div>

<script>
  async function registerUser() {
    const ctx = '${pageContext.request.contextPath}';
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const response = await fetch(ctx + '/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:
        'username=' + encodeURIComponent(username)
        + '&password=' + encodeURIComponent(password)
    });

    if (response.ok) {
      alert('Đăng ký thành công! Vui lòng đăng nhập.');
      window.location.href = ctx + '/jsp/login.jsp';
    } else {
      alert('Đăng ký thất bại');
    }
  }
</script>
</body>
</html>