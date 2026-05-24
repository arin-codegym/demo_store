'use client';

import { AuthGuard } from '@/components/auth/auth-guard';
import { ConversationSidebar } from '@/components/chat/conversation-sidebar';
import { MessagePanel } from '@/components/chat/message-panel';
import { NewChatModal } from '@/components/chat/new-chat-modal';
import { useState } from 'react';
import { ChatPageConversationSync } from '@/components/chat/ChatPageConversationSync';

export default function ChatPage() {
  const [openNewChat, setOpenNewChat] = useState(false);
  return (
    <AuthGuard>
      <div className='h-[calc(100vh-110px)] overflow-hidden bg-slate-50'>
        {/* <ChatTopbar /> */}
        <div className='h-full max-w-6xl mx-auto px-4 py-4'>
          <div className='h-full overflow-hidden rounded-2xl border bg-white grid grid-cols-[320px_1fr]'>
            <ChatPageConversationSync />
            <ConversationSidebar onOpenNewChat={() => setOpenNewChat(true)} />
            <MessagePanel />
          </div>
        </div>
      </div>
      <NewChatModal open={openNewChat} onClose={() => setOpenNewChat(false)} />
    </AuthGuard>
  );
}
