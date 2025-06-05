package com.example.chat.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "senderid", nullable = false)
    private Long senderId;

    @Column(name = "receiverid")
    private Long receiverId;

    @Column(name = "groupname")
    private String groupName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timeStamp; // Đổi tên từ createdAt để nhất quán với constructor và getter/setter

    @ManyToOne(fetch = FetchType.LAZY) // Nên là EAGER nếu bạn luôn muốn lấy thông tin User sender
    @JoinColumn(name = "senderid", referencedColumnName = "id", insertable = false, updatable = false)
    private User sender;

    public Message() { }

    public Message(Long senderId, Long receiverId, String content) {
        this.senderId = senderId;
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

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
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

    public LocalDateTime getTimeStamp() { // Đổi tên getter cho nhất quán
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) { // Đổi tên setter cho nhất quán
        this.timeStamp = timeStamp;
    }

    // Getter và Setter cho trường 'sender'
    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @Transient
    public String getSenderUsername() {
        // Phương thức này vẫn hữu ích và có thể giữ lại
        // Nó sẽ hoạt động đúng nếu đối tượng 'sender' đã được load
        return sender != null ? sender.getUsername() : null;
    }

    // Đổi tên getCreatedAt và setCreatedAt thành getTimeStamp và setTimeStamp cho nhất quán
    // Nếu bạn vẫn muốn giữ getCreatedAt, bạn có thể tạo thêm một getter nữa trỏ về timeStamp
    public LocalDateTime getCreatedAt() {
        return timeStamp;
    }
}