export type actionFunction = (
  prevState: any,
  formData: FormData,
) => Promise<{ message: string }>;

export type CartItem = {
  productId: string;
  image: string;
  title: string;
  price: string;
  amount: number;
  company: string;
};

export type CartState = {
  cartItems: CartItem[];
  numItemsInCart: number;
  cartTotal: number;
  shipping: number;
  tax: number;
  orderTotal: number;
};

export interface CartItemWithProduct {
  cartItemId: string;
  amount: number;
  createdAt: Date;
  updatedAt: Date;
  productId: string;
  cartId: string;
  product: {
    productId: string;
    image: string;
    name: string;
    company: string;
    price: number;
  };
}

export interface User {
  userId: string;
  username: string;
  email: string;
  avatarUrl?: string; // URL ảnh đại diện
  fullName?: string;
  roles: ['ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_USER'];
}

export interface Order {
  id: string;
  products: string;
  orderTotal: number;
  tax: number;
  shipping: number;
  createdAt: string;
  email: string;
}

export interface Review {
  reviewId: string;
  rating: number;
  comment: string;
  authorName: string;
  authorImageUrl: string;
  createdAt: string;
  updatedAt: string;
  userId: string;
  productId: string;
  // product :  Product ,
}

export interface Cart {
  id: string;
  orderTotal: number;
  tax: number;
  shipping: number;
  createdAt: Date;
  numItemsInCart: number;
  cartTotal: number;
  taxRate: number;
  updatedAt: Date;
  userId: string;
}

export interface Product {
  name: string;
  price: number;
  image: string;
  productId: string;
  company: string;
  featured: boolean;
  description: string;
  createdAt: Date;
  updatedAt: Date;
  userId: string;
}
export type FavoriteItem = {
  productId: string;
  favoriteId: string;
};
/* Chat */

export type Id = string;

export type ConversationSummary = {
  conversationId: Id;
  // direct chat info
  otherUserId: Id;
  otherUserName: string;
  otherUserAvatarUrl?: string | null;
  // last message summary
  lastMessageId?: Id | null;
  lastMessageContent?: string | null;
  lastMessageAt?: string | null;

  unreadCount: number;
};
export type ConversationItem = ConversationSummary;

export type UserChatItem = {
  userId: string;
  username: string;
  avatarUrl?: string | null;
};

export type MessageItem = {
  messageId: string;
  conversationId: string;
  senderUserId: string;
  content: string;
  createdAt: string;
};

export type Message = {
  messageId: Id;
  conversationId: Id;
  senderUserId: Id;
  senderType: 'USER' | 'AI';
  clientMessageId?: string | null;
  content: string;
  status?: 'PENDING' | 'SENT' | 'FAILED';
  createdAt: string;
  isTemp?: boolean;
};

export type ChatableUser = {
  userId: string;
  username: string;
  avatarUrl?: string | null;
  email?: string;
};

export type SendMessageRequest = {
  conversationId: Id;
  clientMessageId: string;
  content: string;
};
export type SendMessageResponse = Message;

export type CreateDirectConversationRequest = {
  targetUserId: Id;
};

export type MarkSeenRequest = {
  lastReadMessageId: Id;
};

export type WsEnvelope<T = unknown> = {
  type:
    | 'message.created'
    | 'conversation.updated'
    | 'conversation.seen'
    | 'notification.created'
    | 'notification.read'
    | 'notification.read-all';
  data: T;
};

export type MessageCreatedPayload = Message;

export type ConversationUpdatedPayload = {
  conversationId: Id;
  lastMessageId?: Id | null;
  lastMessageContent?: string | null;
  lastMessageAt?: string | null;
};

export type ConversationSeenPayload = {
  conversationId: Id;
  userId: Id;
  lastReadMessageId: Id;
};

export type CreateDirectConversationResponse = {
  conversationId: string;
};

/* Notification */
export type NotificationType =
  | 'chat.message.created'
  | 'order.updated'
  | 'ticket.assigned'
  | 'comment.reply.created'
  | 'system.announcement';

export type NotificationTarget = {
  type: string;
  id: string;
  url?: string | null;
} | null;

export type NotificationActor = {
  id: string;
  name: string;
  avatarUrl?: string | null;
} | null;

export type AppNotification = {
  notificationId: string;
  type: NotificationType | string;
  title: string;
  body: string;
  read: boolean;
  readAt: string | null;
  createdAt: string;

  actor?: NotificationActor;
  target?: NotificationTarget;
  imageUrl?: string | null;
  metadata?: Record<string, unknown> | null;
};

export type NotificationListResponse = {
  items: AppNotification[];
  nextCursor: string | null;
};

export type UnreadNotificationCountResponse = {
  unreadCount: number;
};

export type NotificationCreatedPayload = {
  notification: AppNotification;
};

export type NotificationReadPayload = {
  notificationId: string;
  userId: string;
  readAt: string;
};

export type NotificationReadAllPayload = {
  userId: string;
  readAt: string;
};
