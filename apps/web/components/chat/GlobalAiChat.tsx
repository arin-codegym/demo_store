'use client';

import { useEffect, useRef, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';

import {
  chatUiActions,
  useChatUiReduxDispatch,
  useChatUiReduxSelector,
} from '@/stores/chat-ui-redux-store';
import { AiChatLauncher } from './AiChatLauncher';
import { AiChatWidget } from './AiChatWidget';
import { ensureAiConversation } from '@/lib/api/internal/conversations';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { useMarkReadByConversationId } from '@/query/notifications/useMarkReadByConversation';
import { shouldHideGlobalChat } from './chat-route-visibility';

export function GlobalAiChat() {
  const pathname = usePathname();
  const router = useRouter();
  const { data: user, isLoading } = useCurrentUser();

  const [isBootstrapping, setIsBootstrapping] = useState(false);
  const [openError, setOpenError] = useState<string | null>(null);

  const dispatch = useChatUiReduxDispatch();
  // Zustand: useChatUiStore((s) => s.setAiConversationId)
  const setAiConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setAiConversationId(conversationId));
  // Zustand: useChatUiStore((s) => s.setAiWidgetOpen)
  const setAiWidgetOpen = (open: boolean) =>
    dispatch(chatUiActions.setAiWidgetOpen(open));
  // Zustand: useChatUiStore((s) => s.aiConversationId)
  const aiConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.aiConversationId,
  );
  // Zustand: useChatUiStore((s) => s.aiWidgetOpen)
  const aiWidgetOpen = useChatUiReduxSelector(
    (state) => state.chatUi.aiWidgetOpen,
  );
  const { mutate: markReadByConversation } = useMarkReadByConversationId();
  const lastMarkedAiConversationRef = useRef<string | null>(null);

  const handleOpen = async () => {
    if (isLoading || isBootstrapping) return;

    if (!user) {
      router.push('/login?redirect=' + encodeURIComponent(pathname));
      return;
    }

    try {
      setIsBootstrapping(true);
      setOpenError(null);

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
      setOpenError('Không mở được Chat AI. Vui lòng thử lại.');
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

  if (shouldHideGlobalChat(pathname)) return null;

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
        <>
          <AiChatLauncher
            onClick={handleOpen}
            disabled={isLoading || isBootstrapping}
            loading={isBootstrapping}
          />
          {openError && (
            <div className='fixed bottom-40 right-4 z-40 max-w-[260px] rounded-md border border-red-200 bg-white px-3 py-2 text-sm text-red-600 shadow'>
              {openError}
            </div>
          )}
        </>
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
