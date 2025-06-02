package com.example.chat.TCP.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient implements Runnable {

    private Socket client;
    private BufferedReader in; // Biến để đọc dữ liệu từ server
    private PrintWriter out; // Biến để gửi dữ liệu đến server
    private boolean done; // Biến để kiểm soát việc dừng client

    @Override
    public void run() {
        try {
            client  = new Socket("localhost", 8080); // Kết nối đến server tại địa chỉ localhost và cổng 8080;
            out = new PrintWriter(client.getOutputStream(), true); // Tạo PrintWriter để gửi dữ liệu đến server
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); // Tạo BufferedReader để đọc dữ liệu từ server

            InputHandle inputHandle = new InputHandle(); // Tạo một luồng để xử lý đầu vào từ bàn phím
            Thread inputThread = new Thread(inputHandle); // Tạo một luồng mới để chạy InputHandle
            inputThread.start(); // Bắt đầu luồng InputHandle

            String inMessage; // Biến để lưu trữ tin nhắn nhận từ server
            while ((inMessage = in.readLine()) != null && !done) { // Đọc dữ liệu từ server
                System.out.println(inMessage); // In ra tin nhắn nhận được từ server
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true; // Đặt biến done thành true để dừng client
        try {
            in.close(); // Đóng BufferedReader
            out.close(); // Đóng PrintWriter
            if(!client.isClosed()) {
                client.close(); // Đóng kết nối Socket nếu nó chưa được đóng
            }
        } catch (IOException e) {
            // igore exception
        }
    }

    public class InputHandle implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in)); // Tạo BufferedReader để đọc dữ liệu từ bàn phím
                while (!done) {
                    String message = inReader.readLine(); // Đọc dữ liệu từ bàn phím
                    if(message.equals("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutdown(); // Nếu người dùng nhập "/quit", đóng kết nối
                    } else {
                        out.println(message); // Gửi tin nhắn đến server
                    }
                }
            } catch(IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient(); // Tạo một đối tượng TCPClient
        client.run(); // Gọi phương thức run để bắt đầu client
    }
}