'use client';

import { useEffect, useRef, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';

import { useChatUiStore } from '@/stores/chat-ui-store';
import { AiChatLauncher } from './AiChatLauncher';
import { AiChatWidget } from './AiChatWidget';
import { ensureAiConversation } from '@/lib/api/internal/conversations';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { useMarkReadByConversationId } from '@/query/notifications/useMarkReadByConversation';

export function GlobalAiChat() {
  const pathname = usePathname();
  const router = useRouter();
  const { data: user, isLoading } = useCurrentUser();

  const [isBootstrapping, setIsBootstrapping] = useState(false);

  const setAiConversationId = useChatUiStore((s) => s.setAiConversationId);
  const setAiWidgetOpen = useChatUiStore((s) => s.setAiWidgetOpen);
  const aiConversationId = useChatUiStore((s) => s.aiConversationId);
  const aiWidgetOpen = useChatUiStore((s) => s.aiWidgetOpen);
  const { mutate: markReadByConversation } = useMarkReadByConversationId();
  const lastMarkedAiConversationRef = useRef<string | null>(null);
  const isChatPage = pathname.startsWith('/chat');
  if (isChatPage) return null;

  const handleOpen = async () => {
    if (isLoading || isBootstrapping) return;

    if (!user) {
      router.push('/login?redirect=' + encodeURIComponent(pathname));
      return;
    }

    try {
      setIsBootstrapping(true);

      let id = aiConversationId;
      if (!id) {
        id = await ensureAiConversation('general');
        setAiConversationId(id);
      }

      if (!id) return;
      setAiConversationId(id);
      setAiWidgetOpen(true);
    } catch (error) {
      console.error('[ai-chat] open failed', error);
    } finally {
      setIsBootstrapping(false);
    }
  };
  useEffect(() => {
    if (!aiWidgetOpen || !aiConversationId) return;

    if (lastMarkedAiConversationRef.current === aiConversationId) return;
    lastMarkedAiConversationRef.current = aiConversationId;

    markReadByConversation(aiConversationId);
  }, [aiWidgetOpen, aiConversationId, markReadByConversation]);

  const handleClose = () => {
    setAiWidgetOpen(false);
  };

  const handleOpenInMain = async () => {
    let id = aiConversationId;

    if (!id) {
      try {
        setIsBootstrapping(true);
        id = await ensureAiConversation('general');
        setAiConversationId(id);
      } catch (error) {
        console.error('[ai-chat] open in main failed', error);
      } finally {
        setIsBootstrapping(false);
      }
    }

    if (!id) return;

    setAiConversationId(id);
    setAiWidgetOpen(false);
    router.push(`/chat?conversationId=${id}&type=ai`);
  };

  return (
    <>
      {!aiWidgetOpen && (
        <AiChatLauncher
          onClick={handleOpen}
          disabled={isLoading || isBootstrapping}
        />
      )}

      {aiWidgetOpen && aiConversationId && (
        <AiChatWidget
          conversationId={aiConversationId}
          currentUserId={user?.userId}
          onClose={handleClose}
          onOpenInMain={handleOpenInMain}
        />
      )}
    </>
  );
}
