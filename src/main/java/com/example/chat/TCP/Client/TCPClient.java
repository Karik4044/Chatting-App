package com.example.chat.TCP.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean done;
    private String currentChatContext = null;

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 8081);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("Welcome! Please use /register <username> <password> or /login <username> <password> to begin.");

            InputHandle inputHandle = new InputHandle();
            Thread inputThread = new Thread(inputHandle);
            inputThread.start();

            String inMessage;
            while (!done && (inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            if (!done) {
                System.err.println("Connection error or server shut down.");
            }
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (done) {
            return;
        }
        done = true;
        System.out.println("Shutting down client...");
        try {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (out != null) {
                out.close();
            }
            if (client != null && !client.isClosed()) {
                try {
                    client.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            // ignore
        }
        System.out.println("Client shut down complete.");
    }

    public class InputHandle implements Runnable {
        @Override
        public void run() {
            String line = null;
            boolean exitedWithQuit = false;

            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in))) {
                while (!TCPClient.this.done) {
                    line = inReader.readLine();

                    if (line == null) {
                        System.out.println("Client: Input stream ended. Exiting.");
                        break;
                    }

                    if (TCPClient.this.done) {
                        break;
                    }

                    if (out == null || (client != null && client.isOutputShutdown())) {
                        System.out.println("Client: Cannot send message. Output stream is not available.");
                        break;
                    }

                    if (line.equals("/quit")) {
                        if (out != null) out.println(line);
                        exitedWithQuit = true;
                        break;
                    } else if (line.startsWith("/register ") || line.startsWith("/login ")) {
                        TCPClient.this.currentChatContext = null;
                        if (out != null) out.println(line);
                    } else if (line.startsWith("/chat ")) {
                        String[] parts = line.split(" ", 2);
                        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                            TCPClient.this.currentChatContext = parts[1].trim();
                            System.out.println("Client: Attempting to chat with " + TCPClient.this.currentChatContext);
                        } else {
                            TCPClient.this.currentChatContext = null;
                            System.out.println("Client: Usage: /chat <username>");
                        }
                        if (out != null) out.println(line);
                    } else if (line.startsWith("/group ")) {
                        String[] parts = line.split(" ", 2);
                        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                            TCPClient.this.currentChatContext = parts[1].trim();
                            System.out.println("Client: Attempting to join group " + TCPClient.this.currentChatContext);
                        } else {
                            TCPClient.this.currentChatContext = null;
                            System.out.println("Client: Usage: /group <groupname>");
                        }
                        if (out != null) out.println(line);
                    } else {
                        if (TCPClient.this.currentChatContext == null && !(line.startsWith("/"))) {
                            System.out.println("Client: Please use /chat or /group to select a recipient before sending messages.");
                            continue;
                        }
                        if (out != null) out.println(line);
                    }
                }
            } catch (IOException e) {
                if (!TCPClient.this.done) {
                    System.err.println("Client: Error reading input: " + e.getMessage());
                }
            } finally {
                if (!TCPClient.this.done) {
                    if (exitedWithQuit) {
                        System.out.println("Client: Exited due to /quit command.");
                    } else if (line == null) {
                        System.out.println("Client: Exited due to end of input stream.");
                    } else {
                        System.out.println("Client: Exited unexpectedly.");
                    }
                    TCPClient.this.shutdown();
                }
            }
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.run();
    }
}