import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import Providers from './providers';
import { GlobalAdminChat } from '@/components/chat/GlobalAdminChat';
import { GlobalChatSocketListenerRefactor } from '@/lib/websocket/global-chat-socket-listener-refactor';
import { GlobalAiChat } from '@/components/chat/GlobalAiChat';
import { GlobalConversationChat } from '@/components/chat/GlobalConversationChat';
const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Next Storefront',
  description: 'A nifty store built with Next.js',
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang='en' suppressHydrationWarning>
      <body className={inter.className}>
        <Providers>
          <GlobalChatSocketListenerRefactor />
          {children}
          <GlobalAdminChat />
          <GlobalConversationChat />
          <GlobalAiChat />
        </Providers>
      </body>
    </html>
  );
}
