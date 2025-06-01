package com.example.chat.TCP.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient implements Runnable{

    private Socket clientSocket; // Biến để lưu trữ kết nối socket đến server
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            clientSocket = new Socket("localhost", 8080); // Kết nối đến server tại địa chỉ localhost và cổng 8080
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Tạo PrintWriter để gửi dữ liệu đến server
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Tạo BufferedReader để đọc dữ liệu từ server

            InputHandler inputHandler = new InputHandler(); // Tạo một InputHandler để xử lý đầu vào từ người dùng
            Thread inputThread = new Thread(inputHandler); // Tạo một luồng mới để chạy InputHandler
            inputThread.start(); // Bắt đầu luồng InputHandler

            String inmessage;
            while((inmessage = in.readLine()) != null && !done) { // Đọc dữ liệu từ server
                System.out.println(inmessage); // In ra dữ liệu nhận được từ server
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true; // Đặt biến done thành true để dừng client
        try {
            in.close();
            out.close(); // Đóng PrintWriter và BufferedReader
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close(); // Đóng kết nối socket nếu nó chưa được đóng
            }
        } catch (Exception e) {
            // ignore exception
        }
    }

    // Lớp InputHandler để xử lý đầu vào từ người dùng
    public class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); // Tạo BufferedReader để đọc đầu vào từ bàn phím
                while(!done) {
                    String message = inputReader.readLine(); // Đọc dòng nhập từ bàn phím
                    if(message.equals("/quit")) {
                        out.println(message); // Gửi lệnh /quit đến server
                        inputReader.close();
                        shutdown(); // Đóng kết nối khi người dùng nhập lệnh /quit
                    } else  {
                        out.println(message); // Gửi tin nhắn đến server
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient(); // Tạo một đối tượng TCPClient
        client.run(); // Gọi phương thức run để bắt đầu kết nối đến server
    }
}
