// webapp/js/chat.js

// Các biến global này sẽ được khởi tạo từ chat.jsp thông qua scriptlet
// let loggedInUserId = null; // Sẽ được gán từ JSP
// let loggedInUsername = null; // Sẽ được gán từ JSP

let currentChatTarget = null;
let currentChatType = null;
let allFetchedUsers = [];
let ws;

document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra xem loggedInUserId đã được gán từ JSP chưa
    if (typeof loggedInUserId !== 'undefined' && loggedInUserId !== null) {
        fetchUserList();
    } else {
        // Nếu loggedInUserId không được định nghĩa hoặc là null, có nghĩa là scriptlet trong JSP
        // không gán giá trị (ví dụ: user chưa đăng nhập).
        // console.log ở đây (trong chat.js) để thông báo nếu scriptlet trong JSP không chạy đúng.
        console.log("User not logged in or user ID not passed from JSP to chat.js.");
        // Bạn có thể quyết định chuyển hướng ở đây nếu muốn,
        // ví dụ: window.location.href = 'login.jsp';
        // Hoặc vô hiệu hóa các tính năng chat.
        const chatContainer = document.querySelector('.chat-container');
    }

    const sendButton = document.getElementById('sendButton');
    if (sendButton) {
        sendButton.onclick = sendMessage;
    }

    const messageInputField = document.getElementById('messageInputField');
    if (messageInputField) {
        messageInputField.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keyup', filterUserList);
    }

    const mediaDisplayArea = document.getElementById('mediaDisplayArea');
    const messagesDisplay = document.getElementById('messagesDisplay');

    if (mediaDisplayArea && messagesDisplay) {
        // Initially show media display area / welcome message if no chat is selected
        mediaDisplayArea.style.display = 'flex';
        messagesDisplay.style.display = 'none';
    }
});

function filterUserList() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const filteredUsers = allFetchedUsers.filter(user =>
        user.username.toLowerCase().includes(searchTerm)
    );
    populateChatList(filteredUsers);
}

function populateChatList(usersToDisplay) {
    const chatListElement = document.getElementById('chat-list');
    if (!chatListElement) return;
    chatListElement.innerHTML = '';

    usersToDisplay.forEach(user => {
        const listItem = document.createElement('li');
        listItem.classList.add('chat-list-item');

        const avatarImg = document.createElement('img');
        avatarImg.classList.add('avatar');
        avatarImg.src = `https://i.pravatar.cc/50?u=${encodeURIComponent(user.username)}`;
        avatarImg.alt = 'Avatar';

        const infoDiv = document.createElement('div');
        infoDiv.classList.add('info');

        const nameSpan = document.createElement('span');
        nameSpan.classList.add('name');
        nameSpan.textContent = user.username;

        infoDiv.appendChild(nameSpan);

        listItem.appendChild(avatarImg);
        listItem.appendChild(infoDiv);

        listItem.onclick = function() {
            document.querySelectorAll('.chat-list-item.active').forEach(active => active.classList.remove('active'));
            listItem.classList.add('active');
            console.log('Start chat with ' + user.username);
            loadMessagesForUser(user.username, 'private');
        };
        chatListElement.appendChild(listItem);
    });
}

function fetchUserList() {
    fetch('users')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    console.error('Unauthorized: Not logged in or session expired.');
                } else {
                    console.error('Failed to fetch user list. Status: ' + response.status);
                }
                return null;
            }
            return response.json();
        })
        .then(users => {
            if (!users) return;
            allFetchedUsers = users;
            populateChatList(allFetchedUsers);
        })
        .catch(error => {
            console.error('Error fetching user list:', error);
        });
}

function loadMessagesForUser(targetName, type) {
    if (!loggedInUserId) {
        console.error("Cannot load messages, user not logged in.");
        return;
    }
    currentChatTarget = targetName;
    currentChatType = type;

    document.getElementById('chattingWithName').textContent = targetName;
    document.getElementById('chattingWithAvatar').src = `https://i.pravatar.cc/40?u=${encodeURIComponent(targetName)}`;

    document.getElementById('messagesDisplay').style.display = 'flex';
    document.getElementById('mediaDisplayArea').style.display = 'none';

    const messagesUl = document.getElementById('msgs');
    if (!messagesUl) return;
    messagesUl.innerHTML = '';

    fetch(`chat?target=${encodeURIComponent(targetName)}&type=${encodeURIComponent(type)}`)
        .then(response => {
            if (!response.ok) throw new Error(`Failed to load messages. Status: ${response.status}`);
            return response.json();
        })
        .then(messages => {
            messages.forEach(msg => {
                appendMessage(msg, messagesUl);
            });
            scrollToBottom(document.getElementById('messagesDisplay'));
        })
        .catch(error => console.error('Error loading messages:', error));
}

function appendMessage(message, messagesUlElement) {
    const li = document.createElement('li');
    li.classList.add('message');

    const isSentByCurrentUser = message.senderId === loggedInUserId;

    if (isSentByCurrentUser) {
        li.classList.add('sent');
    } else {
        li.classList.add('received');
        // If you add senderUsername to your Message JSON from server:
        // if (currentChatType === 'group' && message.senderUsername && message.senderUsername !== loggedInUsername) {
        //     const senderNameSpan = document.createElement('span');
        //     senderNameSpan.classList.add('sender-name');
        //     senderNameSpan.textContent = message.senderUsername;
        //     li.appendChild(senderNameSpan);
        // }
    }

    const contentDiv = document.createElement('div');
    contentDiv.textContent = message.content;
    li.appendChild(contentDiv);

    if (message.timeStamp) {
        const timeStampSpan = document.createElement('span');
        timeStampSpan.classList.add('timestamp');
        try {
            let date;
            if (typeof message.timeStamp === 'object' && message.timeStamp.year !== undefined) {
                date = new Date(message.timeStamp.year, message.timeStamp.monthValue - 1, message.timeStamp.dayOfMonth,
                    message.timeStamp.hour, message.timeStamp.minute, message.timeStamp.second);
            } else { // Try to parse if it's a string (e.g., ISO format)
                date = new Date(message.timeStamp);
            }
            if (!isNaN(date.getTime())) { // Check if date is valid
                timeStampSpan.textContent = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            } else {
                timeStampSpan.textContent = "time"; // Fallback for invalid date
            }
        } catch (e) {
            console.warn("Could not parse timestamp: ", message.timeStamp, e);
            timeStampSpan.textContent = "time";
        }
        li.appendChild(timeStampSpan);
    }
    messagesUlElement.appendChild(li);
}

function sendMessage() {
    const messageInput = document.getElementById('messageInputField');
    if (!messageInput || !currentChatTarget || !loggedInUserId) {
        console.error("Cannot send message. Check target or login status.");
        return;
    }

    const content = messageInput.value.trim();
    if (content === '') return;

    const params = new URLSearchParams();
    params.append('target', currentChatTarget);
    params.append('type', currentChatType);
    params.append('message', content);

    fetch('chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
    })
        .then(response => {
            if (!response.ok) throw new Error(`Failed to send message. Status: ${response.status}`);
            return response.json();
        })
        .then(sentMessage => {
            const messagesUl = document.getElementById('msgs');
            if (messagesUl) {
                appendMessage(sentMessage, messagesUl);
                scrollToBottom(document.getElementById('messagesDisplay'));
            }
            messageInput.value = '';
        })
        .catch(error => console.error('Error sending message:', error));
}

function scrollToBottom(element) {
    if (element) {
        element.scrollTop = element.scrollHeight;
    }
}

function connectWebSocket(username) {
    ws = new WebSocket(`ws://${window.location.host}${window.location.pathname.replace(/\/[^/]*$/, '')}/ws/chat/${username}`);
    ws.onmessage = function(event) {
        const msg = JSON.parse(event.data);
        // Update UI with new message
    };
    ws.onclose = function() {
        // Optionally try to reconnect
    };
}
function sendMessage(target, type, content) {
    ws.send(JSON.stringify({ target, type, content }));
}