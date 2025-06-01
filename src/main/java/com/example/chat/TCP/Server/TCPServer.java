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

public class TCPServer implements Runnable{

    //Danh sách các kết nối
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket serverSocket;
    public boolean done; // Biến để kiểm soát việc dừng server
    private ExecutorService pool; // ExecutorService để quản lý các luồng

    public TCPServer() {
        connections = new ArrayList<>(); // Khởi tạo danh sách kết nối
        done = false; // Mặc định server chưa dừng
    }

    //Ham run để khởi tạo server
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8080); // Khởi tạo server socket trên cổng 8080
            pool = Executors.newCachedThreadPool(); // Tạo một ExecutorService để quản lý các luồng
            while(!done)  {
                Socket clientSocket = serverSocket.accept();            // Chấp nhận kết nối từ client
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket); // Tạo một ConnectionHandler cho client
                connections.add(connectionHandler); // Thêm kết nối vào danh sách
                pool.execute(connectionHandler); // Gửi ConnectionHandler vào ExecutorService để xử lý
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    //Lop Broadcast để phát sóng tin nhắn đến client
    public void broadcast(String message) {
        for (ConnectionHandler connection : connections) {
            connection.sendMessage(message); // Gửi tin nhắn đến tất cả các client
        }
    }

    //Ham shutdown neu xay ra ngoai lệ hoặc khi server cần dừng
    public void shutdown() {
        try {
            done = true; // Đặt biến done thành true để dừng server
            pool.shutdown(); // Dừng ExecutorService
            if (!serverSocket.isClosed()) {
                serverSocket.close(); // Đóng server socket
            }
            for(ConnectionHandler ch : connections) {
                ch.shutdown(); // Gọi phương thức shutdown của từng ConnectionHandler
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Lớp ConnectionHandler để xử lý kết nối với từng client
    public class ConnectionHandler implements Runnable {
        public Socket client;           // Kết nối của client
        private BufferedReader in;      // Đọc dữ liệu từ client
        private PrintWriter out;        // Gửi dữ liệu đến client

        // Constructor để khởi tạo kết nối với client
        public ConnectionHandler (Socket clientSocket) {
            this.client = clientSocket;
        }

        // Phương thức để xử lý kết nối với client
        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Welcome to the chat server!, please enter your username:");
                String username = in.readLine();
                System.out.println(username + " Connected");
                broadcast(username + " has joined the chat!"); // Phát sóng thông báo khi client kết nối
                String message;
                while ((message = in.readLine()) != null) {
                    if(message.startsWith("/quit")) {
                        broadcast(username + "quit the chat");
                        shutdown(); // Đóng kết nối khi client gửi lệnh /quit
                    } else if (message.startsWith("/nick")) {
                        String messageParts[] = message.split(" ", 2);
                        if(messageParts.length == 2) {
                            broadcast(username + " changed their nickname to " + messageParts[1]);
                            System.out.println(username + " changed their nickname to " + messageParts[1]);
                            username = messageParts[1]; // Cập nhật tên người dùng
                            out.println("Your nickname has been changed to " + username);
                        } else {
                            out.println("Usage: /nick <new_nickname>");
                        }
                    }

                    broadcast(username + ": " + message); // Phát sóng tin nhắn đến tất cả các client
                }
            } catch (Exception e) {
                shutdown(); // Đóng kết nối nếu có lỗi xảy ra
            }
        }

        // Phương thức để gửi tin nhắn đến client
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message); // Gửi tin nhắn đến client
            }
        }

        // Phương thức để đóng kết nối với client
        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close(); // Đóng kết nối với client
                }
            } catch (IOException e) {
                e.printStackTrace(); // In ra lỗi nếu có
            }
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer(); // Tạo một instance của TCPServer
        server.run();
    }
}
