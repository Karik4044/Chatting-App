package com.example.chat.servlet;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
// import org.mindrot.jbcrypt.BCrypt; // Nếu bạn cần tạo user mẫu với password hash

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@WebServlet(name = "ChatServlet", urlPatterns = {"/chat"})
public class ChatServlet extends HttpServlet {

    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        messageDAO = new MessageDAO();
        userDAO = new UserDAO();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        gson = gsonBuilder.create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(); // Tạo session nếu chưa có
        User currentUser = (User) session.getAttribute("user");

        // --- Giả lập đăng nhập cho mục đích demo ---
        if (currentUser == null) {
            // CỐ GẮNG ĐĂNG NHẬP VỚI USER "karik". HÃY ĐẢM BẢO USER NÀY TỒN TẠI TRONG DB.
            // HOẶC THAY "karik" BẰNG MỘT USERNAME CÓ SẴN.
            String demoUsername = "karik"; // <<<< THAY ĐỔI USERNAME NÀY NẾU CẦN
            currentUser = userDAO.getUserByUsername(demoUsername);

            if (currentUser == null) {
                // Nếu không có user "karik", thử lấy user đầu tiên trong DB
                List<User> allDbUsers = userDAO.getAllUsers();
                if (allDbUsers != null && !allDbUsers.isEmpty()) {
                    currentUser = allDbUsers.get(0);
                    System.out.println("Không tìm thấy user '" + demoUsername + "', đã đăng nhập giả lập bằng user: " + currentUser.getUsername());
                } else {
                    // Nếu DB trống, có thể tạo user mẫu (nhưng cần cẩn thận với việc này trong môi trường thực)
                    System.out.println("Không có user nào trong CSDL. Hãy tạo user trước.");
                    // Ví dụ tạo user (chỉ khi DB trống hoàn toàn):
                    // if (userDAO.registerUser("testuser", BCrypt.hashpw("password", BCrypt.gensalt()))) {
                    //    currentUser = userDAO.getUserByUsername("testuser");
                    //    System.out.println("Đã tạo và đăng nhập giả lập bằng user 'testuser'");
                    // } else {
                    response.getWriter().println("Lỗi nghiêm trọng: Không có người dùng nào trong CSDL và không thể tạo người dùng mẫu. Vui lòng kiểm tra CSDL và tạo người dùng thủ công.");
                    return;
                    // }
                }
            }

            if (currentUser != null) {
                session.setAttribute("user", currentUser);
                System.out.println("Người dùng giả lập đã đăng nhập: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            } else {
                response.getWriter().println("Không thể xác định người dùng hiện tại. Vui lòng đảm bảo có người dùng trong CSDL.");
                return;
            }
        }
        // --- Kết thúc giả lập đăng nhập ---

        request.setCharacterEncoding("UTF-8"); // Đảm bảo request encoding
        response.setCharacterEncoding("UTF-8"); // Đảm bảo response encoding

        String action = request.getParameter("action");

        if ("getUsers".equals(action)) {
            handleGetUsers(request, response, currentUser);
        } else if ("getMessages".equals(action)) {
            handleGetMessages(request, response, currentUser);
        } else {
            // Default action: Tải trang JSP ban đầu
            List<User> allUsers = userDAO.getAllUsers();
            if (allUsers == null) allUsers = new ArrayList<>(); // Tránh NullPointerException

            User finalCurrentUser = currentUser;
            List<User> conversations = allUsers.stream()
                    .filter(user -> !user.getId().equals(finalCurrentUser.getId()))
                    .collect(Collectors.toList());

            request.setAttribute("conversations", conversations); // Danh sách này có thể rỗng
            request.setAttribute("currentUser", currentUser);

            // Không cần tải active chat messages ban đầu nữa, JavaScript sẽ làm việc đó
            request.getRequestDispatcher("/chat.jsp").forward(request, response); // Đảm bảo tên file JSP đúng
        }
    }

    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        List<User> allUsers = userDAO.getAllUsers();
        if (allUsers == null) allUsers = new ArrayList<>();

        List<Map<String, Object>> usersForJson = allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    // Bạn có thể cải thiện việc lấy tin nhắn cuối và thời gian sau
                    userMap.put("lastMessagePreview", "Nhấp để xem tin nhắn...");
                    userMap.put("lastMessageTime", "");
                    userMap.put("avatarUrl", "https://i.pravatar.cc/50?u=" + user.getUsername().replaceAll("\\s+", "")); // Loại bỏ khoảng trắng cho URL
                    return userMap;
                })
                .collect(Collectors.toList());

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(usersForJson));
    }

    private void handleGetMessages(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        String chatWithUserIdStr = request.getParameter("chatWithUserId");
        List<Message> messages = Collections.emptyList();

        if (chatWithUserIdStr != null && !chatWithUserIdStr.isEmpty()) {
            try {
                Long chatWithUserId = Long.parseLong(chatWithUserIdStr);
                User partner = userDAO.getUserById(chatWithUserId);
                if (partner != null) {
                    messages = messageDAO.getMessagesBetweenUsers(currentUser.getId(), chatWithUserId);
                } else {
                    System.err.println("Không tìm thấy partner với ID: " + chatWithUserIdStr);
                }
            } catch (NumberFormatException e) {
                System.err.println("chatWithUserId không hợp lệ: " + chatWithUserIdStr);
            }
        }

        List<Map<String, Object>> messagesForJson = messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("senderId", msg.getSenderId());
            // Lấy senderUsername từ User object được fetch cùng Message (nếu có)
            // Hoặc query lại nếu cần, nhưng join fetch trong MessageDAO là tốt hơn
            String senderUsername = "Unknown";
            if (msg.getSender() != null) {
                senderUsername = msg.getSender().getUsername();
            } else {
                User senderUser = userDAO.getUserById(msg.getSenderId());
                if (senderUser != null) senderUsername = senderUser.getUsername();
            }
            map.put("senderUsername", senderUsername);
            map.put("content", msg.getContent());
            map.put("timestamp", msg.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
            map.put("isSentByCurrentUser", msg.getSenderId().equals(currentUser.getId()));
            return map;
        }).collect(Collectors.toList());

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(messagesForJson));
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        Map<String, Object> jsonResponse = new HashMap<>();

        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "User not authenticated. Please login.");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        String action = request.getParameter("action");

        if ("sendMessage".equals(action)) {
            String content = request.getParameter("content");
            String receiverIdStr = request.getParameter("receiverId");
            // String groupName = request.getParameter("groupName"); // Cho group chat sau

            if (content == null || content.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Nội dung tin nhắn không thể trống.");
            } else if (receiverIdStr == null || receiverIdStr.isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Cần có ID người nhận.");
            } else {
                try {
                    Long receiverId = Long.parseLong(receiverIdStr);
                    if (userDAO.getUserById(receiverId) == null) {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "Người nhận không tồn tại.");
                    } else {
                        Message newMessage = new Message(currentUser.getId(), receiverId, content);
                        // newMessage.setCreatedAt(LocalDateTime.now()); // Constructor đã làm

                        if (messageDAO.saveMessage(newMessage)) {
                            jsonResponse.put("status", "success");
                            jsonResponse.put("message", "Tin nhắn đã gửi!");

                            Map<String, Object> msgMap = new HashMap<>();
                            // ID của tin nhắn newMessage sẽ được tự động gán sau khi persist thành công
                            // Để lấy ID này, bạn cần load lại Message object hoặc đảm bảo persist cập nhật ID.
                            // Hiện tại, chúng ta sẽ không trả về ID của tin nhắn mới vì nó phức tạp hơn chút.
                            // msgMap.put("id", newMessage.getId());
                            msgMap.put("senderId", newMessage.getSenderId());
                            msgMap.put("senderUsername", currentUser.getUsername());
                            msgMap.put("receiverId", newMessage.getReceiverId());
                            msgMap.put("content", newMessage.getContent());
                            msgMap.put("timestamp", newMessage.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
                            msgMap.put("isSentByCurrentUser", true);
                            jsonResponse.put("sentMessage", msgMap);

                        } else {
                            jsonResponse.put("status", "error");
                            jsonResponse.put("message", "Lỗi khi gửi tin nhắn (không lưu được vào DB).");
                        }
                    }
                } catch (NumberFormatException e) {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "ID người nhận không hợp lệ.");
                }
            }
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Hành động không hợp lệ.");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}