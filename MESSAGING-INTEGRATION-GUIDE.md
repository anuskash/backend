# In-App Messaging System - Frontend Integration Guide

## Overview
This messaging system allows buyers to contact sellers about products, with automatic email notifications.

---

## Backend API Endpoints

### 1. **Send Message**
```
POST /messages/send?senderId={userId}
Content-Type: application/json

Body:
{
  "receiverId": 123,
  "productId": 456,
  "content": "Is this item still available?"
}

Response:
{
  "messageId": 789,
  "senderId": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "receiverId": 123,
  "receiverName": "Jane Smith",
  "receiverEmail": "jane@example.com",
  "productId": 456,
  "productTitle": "iPhone 13 Pro",
  "productImageUrl": "/uploads/products/abc123.jpg",
  "content": "Is this item still available?",
  "sentAt": "2025-11-05T10:30:00",
  "isRead": false,
  "readAt": null
}
```

### 2. **Get Conversations (Inbox)**
```
GET /messages/conversations?userId={userId}

Response:
[
  {
    "otherUserId": 123,
    "otherUserName": "Jane Smith",
    "otherUserEmail": "jane@example.com",
    "productId": 456,
    "productTitle": "iPhone 13 Pro",
    "productImageUrl": "/uploads/products/abc123.jpg",
    "lastMessage": "Yes, it's still available!",
    "lastMessageTime": "2025-11-05T10:35:00",
    "hasUnread": true,
    "unreadCount": 2
  },
  {
    "otherUserId": 789,
    "otherUserName": "Bob Johnson",
    "otherUserEmail": "bob@example.com",
    "productId": 101,
    "productTitle": "MacBook Air M2",
    "productImageUrl": "/uploads/products/xyz789.jpg",
    "lastMessage": "Can we meet tomorrow?",
    "lastMessageTime": "2025-11-04T15:20:00",
    "hasUnread": false,
    "unreadCount": 0
  }
]
```

### 3. **Get Conversation Messages**
```
GET /messages/conversation?userId={userId}&otherUserId={otherUserId}&productId={productId}

Response:
[
  {
    "messageId": 1,
    "senderId": 1,
    "senderName": "John Doe",
    "content": "Is this item still available?",
    "productId": 456,
    "productTitle": "iPhone 13 Pro",
    "productImageUrl": "/uploads/products/abc123.jpg",
    "sentAt": "2025-11-05T10:30:00",
    "isRead": true,
    "readAt": "2025-11-05T10:32:00"
  },
  {
    "messageId": 2,
    "senderId": 123,
    "senderName": "Jane Smith",
    "content": "Yes, it's still available!",
    "productId": 456,
    "productTitle": "iPhone 13 Pro",
    "productImageUrl": "/uploads/products/abc123.jpg",
    "sentAt": "2025-11-05T10:35:00",
    "isRead": true,
    "readAt": "2025-11-05T10:36:00"
  }
]

Note: Messages are automatically marked as read when you fetch them
```

### 4. **Get Unread Count**
```
GET /messages/unread-count?userId={userId}

Response: 5
```

### 5. **Mark Message as Read**
```
PUT /messages/{messageId}/mark-read?userId={userId}

Response: "Message marked as read"
```

---

## Frontend Implementation

### 1. **Message Service (Angular)**

```typescript
// services/message.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SendMessageRequest {
  receiverId: number;
  productId: number;
  content: string;
}

export interface MessageResponse {
  messageId: number;
  senderId: number;
  senderName: string;
  senderEmail: string;
  receiverId: number;
  receiverName: string;
  receiverEmail: string;
  productId: number;
  productTitle: string;
  content: string;
  sentAt: string;
  isRead: boolean;
  readAt: string | null;
}

export interface ConversationResponse {
  otherUserId: number;
  otherUserName: string;
  otherUserEmail: string;
  productId: number;
  productTitle: string;
  lastMessage: string;
  lastMessageTime: string;
  hasUnread: boolean;
  unreadCount: number;
}

@Injectable({ providedIn: 'root' })
export class MessageService {
  private apiUrl = 'http://localhost:8080/messages';

  constructor(private http: HttpClient) {}

  sendMessage(senderId: number, request: SendMessageRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.apiUrl}/send?senderId=${senderId}`, 
      request
    );
  }

  getConversations(userId: number): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(
      `${this.apiUrl}/conversations?userId=${userId}`
    );
  }

  getConversationMessages(
    userId: number, 
    otherUserId: number, 
    productId: number
  ): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(
      `${this.apiUrl}/conversation?userId=${userId}&otherUserId=${otherUserId}&productId=${productId}`
    );
  }

  getUnreadCount(userId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/unread-count?userId=${userId}`
    );
  }

  markAsRead(messageId: number, userId: number): Observable<string> {
    return this.http.put<string>(
      `${this.apiUrl}/${messageId}/mark-read?userId=${userId}`,
      {}
    );
  }
}
```

### 2. **Contact Seller Button (Product Details Page)**

```html
<!-- product-details.component.html -->
<div class="product-details">
  <h1>{{ product.productName }}</h1>
  <p>{{ product.productDescription }}</p>
  <p>Price: ${{ product.price }}</p>
  
  <!-- Show button only if user is NOT the seller -->
  <button 
    *ngIf="currentUserId !== product.sellerId"
    (click)="openMessageDialog()" 
    class="btn-contact-seller">
    <i class="fa fa-envelope"></i> Contact Seller
  </button>
</div>

<!-- Message dialog/modal -->
<div class="message-modal" *ngIf="showMessageDialog">
  <div class="modal-content">
    <h3>Send Message to {{ sellerName }}</h3>
    <p>Regarding: <strong>{{ product.productName }}</strong></p>
    
    <textarea 
      [(ngModel)]="messageContent"
      placeholder="Type your message here..."
      rows="5">
    </textarea>
    
    <div class="modal-actions">
      <button (click)="sendMessage()" class="btn-primary">Send</button>
      <button (click)="closeMessageDialog()" class="btn-secondary">Cancel</button>
    </div>
  </div>
</div>
```

```typescript
// product-details.component.ts
export class ProductDetailsComponent implements OnInit {
  product: any;
  currentUserId: number;
  sellerName: string;
  showMessageDialog = false;
  messageContent = '';

  constructor(
    private messageService: MessageService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {
    this.currentUserId = this.authService.getUserId();
  }

  openMessageDialog() {
    this.showMessageDialog = true;
  }

  closeMessageDialog() {
    this.showMessageDialog = false;
    this.messageContent = '';
  }

  sendMessage() {
    if (!this.messageContent.trim()) {
      alert('Please enter a message');
      return;
    }

    const request: SendMessageRequest = {
      receiverId: this.product.sellerId,
      productId: this.product.productId,
      content: this.messageContent
    };

    this.messageService.sendMessage(this.currentUserId, request).subscribe(
      response => {
        alert('Message sent successfully! The seller will be notified via email.');
        this.closeMessageDialog();
      },
      error => {
        alert('Failed to send message: ' + error.error);
      }
    );
  }
}
```

### 3. **Inbox Page (Conversations List)**

```html
<!-- inbox.component.html -->
<div class="inbox-container">
  <h1>Messages</h1>
  
  <div class="unread-badge" *ngIf="unreadCount > 0">
    {{ unreadCount }} unread message{{ unreadCount > 1 ? 's' : '' }}
  </div>

  <div class="conversations-list">
    <div 
      *ngFor="let conv of conversations" 
      class="conversation-item"
      [class.unread]="conv.hasUnread"
      (click)="openConversation(conv)">
      
      <div class="conversation-header">
        <strong>{{ conv.otherUserName }}</strong>
        <span class="time">{{ conv.lastMessageTime | date:'short' }}</span>
      </div>
      
      <div class="conversation-product">
        <i class="fa fa-box"></i> {{ conv.productTitle }}
      </div>
      
      <div class="conversation-preview">
        {{ conv.lastMessage }}
      </div>
      
      <div class="unread-indicator" *ngIf="conv.hasUnread">
        <span class="badge">{{ conv.unreadCount }}</span>
      </div>
    </div>
  </div>

  <div *ngIf="conversations.length === 0" class="empty-state">
    <p>No messages yet</p>
  </div>
</div>
```

```typescript
// inbox.component.ts
export class InboxComponent implements OnInit {
  conversations: ConversationResponse[] = [];
  unreadCount = 0;
  currentUserId: number;

  constructor(
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUserId = this.authService.getUserId();
  }

  ngOnInit() {
    this.loadConversations();
    this.loadUnreadCount();
  }

  loadConversations() {
    this.messageService.getConversations(this.currentUserId).subscribe(
      data => this.conversations = data,
      error => console.error('Failed to load conversations', error)
    );
  }

  loadUnreadCount() {
    this.messageService.getUnreadCount(this.currentUserId).subscribe(
      count => this.unreadCount = count,
      error => console.error('Failed to load unread count', error)
    );
  }

  openConversation(conv: ConversationResponse) {
    this.router.navigate(['/messages/conversation'], {
      queryParams: {
        userId: conv.otherUserId,
        productId: conv.productId
      }
    });
  }
}
```

### 4. **Conversation View (Chat Interface)**

```html
<!-- conversation.component.html -->
<div class="conversation-container">
  <div class="conversation-header">
    <button (click)="goBack()" class="btn-back">← Back</button>
    <div class="header-info">
      <h3>{{ otherUserName }}</h3>
      <p>{{ productTitle }}</p>
    </div>
  </div>

  <div class="messages-container" #messagesContainer>
    <div 
      *ngFor="let msg of messages" 
      class="message"
      [class.sent]="msg.senderId === currentUserId"
      [class.received]="msg.senderId !== currentUserId">
      
      <div class="message-bubble">
        <div class="message-sender">{{ msg.senderName }}</div>
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">
          {{ msg.sentAt | date:'short' }}
          <span *ngIf="msg.senderId === currentUserId && msg.isRead" class="read-indicator">
            ✓✓ Read
          </span>
        </div>
      </div>
    </div>
  </div>

  <div class="message-input">
    <textarea 
      [(ngModel)]="newMessage"
      (keydown.enter)="$event.shiftKey ? null : sendMessage(); $event.preventDefault()"
      placeholder="Type a message..."
      rows="2">
    </textarea>
    <button (click)="sendMessage()" class="btn-send">Send</button>
  </div>
</div>
```

```typescript
// conversation.component.ts
export class ConversationComponent implements OnInit, AfterViewChecked {
  messages: MessageResponse[] = [];
  newMessage = '';
  currentUserId: number;
  otherUserId: number;
  productId: number;
  otherUserName = '';
  productTitle = '';
  
  @ViewChild('messagesContainer') messagesContainer: ElementRef;
  private shouldScroll = false;

  constructor(
    private messageService: MessageService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.currentUserId = this.authService.getUserId();
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.otherUserId = +params['userId'];
      this.productId = +params['productId'];
      this.loadMessages();
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  loadMessages() {
    this.messageService.getConversationMessages(
      this.currentUserId,
      this.otherUserId,
      this.productId
    ).subscribe(
      data => {
        this.messages = data;
        if (data.length > 0) {
          const firstMsg = data[0];
          this.otherUserName = firstMsg.senderId === this.currentUserId 
            ? firstMsg.receiverName 
            : firstMsg.senderName;
          this.productTitle = firstMsg.productTitle;
        }
        this.shouldScroll = true;
      },
      error => console.error('Failed to load messages', error)
    );
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    const request: SendMessageRequest = {
      receiverId: this.otherUserId,
      productId: this.productId,
      content: this.newMessage
    };

    this.messageService.sendMessage(this.currentUserId, request).subscribe(
      response => {
        this.messages.push(response);
        this.newMessage = '';
        this.shouldScroll = true;
      },
      error => alert('Failed to send message: ' + error.error)
    );
  }

  scrollToBottom() {
    try {
      this.messagesContainer.nativeElement.scrollTop = 
        this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) {}
  }

  goBack() {
    this.router.navigate(['/messages']);
  }
}
```

### 5. **Navbar Unread Count Badge**

```html
<!-- navbar.component.html -->
<nav>
  <ul>
    <li>
      <a routerLink="/messages">
        <i class="fa fa-envelope"></i> Messages
        <span class="badge" *ngIf="unreadCount > 0">{{ unreadCount }}</span>
      </a>
    </li>
  </ul>
</nav>
```

```typescript
// navbar.component.ts
export class NavbarComponent implements OnInit {
  unreadCount = 0;

  constructor(
    private messageService: MessageService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.loadUnreadCount();
      // Refresh every 30 seconds
      setInterval(() => this.loadUnreadCount(), 30000);
    }
  }

  loadUnreadCount() {
    const userId = this.authService.getUserId();
    this.messageService.getUnreadCount(userId).subscribe(
      count => this.unreadCount = count
    );
  }
}
```

### 6. **Basic Styling (CSS)**

```css
/* inbox.component.css */
.conversation-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: #f5f5f5;
}

.conversation-item.unread {
  background: #e3f2fd;
  font-weight: bold;
}

.unread-indicator {
  float: right;
}

.badge {
  background: #2196F3;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* conversation.component.css */
.messages-container {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.sent {
  justify-content: flex-end;
}

.message.received {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 18px;
}

.message.sent .message-bubble {
  background: #2196F3;
  color: white;
}

.message.received .message-bubble {
  background: #e0e0e0;
  color: black;
}

.message-input {
  display: flex;
  gap: 10px;
  padding: 15px;
  border-top: 1px solid #eee;
}

.message-input textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-send {
  background: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}
```

---

## Email Notification Details

When a user sends a message, the receiver automatically gets an email like this:

```
Subject: New message about "iPhone 13 Pro"

Hi,

You have received a new message from John Doe regarding the product "iPhone 13 Pro".

Message:
Is this item still available?

Log in to your UON Marketplace account to view and reply to this message.

— UON Marketplace
```

---

## Testing Flow

1. **User A** views **User B's** product
2. **User A** clicks "Contact Seller"
3. **User A** types message and sends
4. **User B** receives email notification
5. **User B** logs in and sees unread count badge
6. **User B** opens inbox and sees conversation
7. **User B** clicks conversation to view messages
8. Messages auto-mark as read when viewed
9. **User B** replies
10. **User A** receives email notification
11. Cycle continues...

---

## Future Enhancements (Optional)

- Image attachments in messages
- Push notifications (browser notifications)
- Real-time messaging with WebSockets
- Typing indicators
- Message search functionality
- Block/report users
- Message templates for common questions
- Read receipts with timestamps

---

## Key Features ✅

✅ Email notifications on new messages  
✅ Unread message count  
✅ Auto-mark as read when viewed  
✅ Grouped conversations by product  
✅ Message history persistence  
✅ Product context in every message  
✅ Sender/receiver details included  
✅ Timestamp on all messages  
