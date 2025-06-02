package com.example.chat.TCP.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Runnable {

    private ArrayList<ConnectionHandler> connections; // Danh sách các kết nối đến client
    private ServerSocket serverSocket; // Biến để lưu trữ ServerSocket
    public boolean done;
    private ExecutorService pool;

    public TCPServer() {
        connections = new ArrayList<>(); // Khởi tạo danh sách kết nối
        done = false; // Biến done để kiểm soát việc dừng server
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8080); // Tạo ServerSocket lắng nghe cổng 8080
            pool = Executors.newCachedThreadPool(); // Tạo một ExecutorService để quản lý các luồng kết nối
            while (!done) {
                Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối từ client
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket); // Tạo một ConnectionHandler cho kết nối này
                connections.add(connectionHandler);
                pool.execute(connectionHandler); // Thêm ConnectionHandler vào ExecutorService để xử lý kết nối
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for(ConnectionHandler connection : connections) {
            if(connection != null) {
                connection.sendMessage(message); // Gửi tin nhắn đến tất cả các kết nối
            }
        }
    }

    public void shutdown() {
        try {
            done = true; // Đặt biến done thành true để dừng server
            if(!serverSocket.isClosed()) {
                serverSocket.close(); // Đóng ServerSocket nếu nó chưa được đóng
            }
            for (ConnectionHandler connection : connections) {
                connection.shutdown(); // Đóng tất cả các kết nối đến client
            }
        } catch (Exception e) {
            // ignore exception
        }
    }

    public class ConnectionHandler implements Runnable {
        private Socket clientSocket; // Biến để lưu trữ kết nối socket đến client
        private BufferedReader in;
        private PrintWriter out;
        private String nickname; // Biến để lưu trữ tên người dùng

        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket; // Khởi tạo kết nối socket với client
        }
        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true); // Tạo PrintWriter để gửi dữ liệu đến client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Tạo BufferedReader để đọc dữ liệu từ client
                out.println("Welcome to the chat server!"); // Gửi thông báo chào mừng đến client
                out.println("Enter your name: "); // Yêu cầu client nhập tên
                nickname = in.readLine(); // Đọc tên người dùng từ client
                System.out.println("Client connected: " + nickname); // In ra tên người dùng đã kết nối
                broadcast(nickname + " has joined the chat!"); // Phát sóng thông báo đến tất cả các kết nối
                String message;
                while ((message = in.readLine()) != null) { // Đọc dữ liệu từ client
                    if (message.startsWith("/nick")) { // Kiểm tra nếu client gửi lệnh /quit
                        String[] messagesplit = message.split(" ", 2); // Tách lệnh và tên mới
                        if(messagesplit.length == 2) {
                            broadcast(nickname + " has changed their name to " + messagesplit[1]); // Phát sóng thông báo đổi tên
                            System.out.println(nickname + " changed name to " + messagesplit[1]); // In ra thông báo đổi tên
                            nickname = messagesplit[1]; // Cập nhật tên người dùng
                            System.out.println("Nickname changed to: " + nickname); // In ra tên mới
                        } else {
                            out.println("Usage: /nick <new_name>"); // Gửi hướng dẫn sử dụng lệnh đổi tên
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " has left the chat!"); // Phát sóng thông báo người dùng rời khỏi chat
                        shutdown(); // Đóng kết nối khi người dùng gửi lệnh /quit
                    } else {
                        broadcast(nickname + ": " + message); // Phát sóng tin nhắn đến tất cả các kết nối
                    }

                }
            } catch (Exception e) {
                shutdown();
            }
        }

        public void shutdown() {
            try {
                in.close();
                out.close(); // Đóng PrintWriter và BufferedReader
                if(!clientSocket.isClosed()) {
                    clientSocket.close(); // Đóng kết nối socket nếu nó chưa được đóng
                }
            } catch (IOException e) {
                // ignore exception
            }
        }

        public void sendMessage(String message) {
            out.println(message); // Gửi tin nhắn đến client
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer(); // Tạo một instance của TCPServer
        server.run();
    }
}