package com.example.chat.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiverid")
    private Long receiverId;

    @Column(name = "groupname")
    private String groupName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timeStamp;

    // FIX: Sử dụng @JoinColumn với insertable=true và updatable=true
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "senderid", referencedColumnName = "id")
    private User sender;

    public Message() { }

    public Message(Long senderId, Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
        this.timeStamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // FIX: Xóa getSenderId và setSenderId để tránh xung đột
    // Thay vào đó sử dụng sender.getId()
    @Transient
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @Transient
    public String getSenderUsername() {
        return sender != null ? sender.getUsername() : null;
    }

    public LocalDateTime getCreatedAt() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + getSenderId() +
                ", receiverId=" + receiverId +
                ", groupName='" + groupName + '\'' +
                ", content='" + content + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}