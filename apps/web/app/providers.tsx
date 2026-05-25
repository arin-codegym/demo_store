'use client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';
import { Toaster } from '@/components/ui/toaster';
import { ThemeProvider } from './theme-provider';
import { Provider as ReduxProvider } from 'react-redux';
import { chatUiReduxStore } from '@/stores/chat-ui-redux-store';

function Providers({ children }: { children: React.ReactNode }) {
  // const [queryClient] = useState(() => new QueryClient()); // quan trọng: tạo 1 lần
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 10_000,
            gcTime: 5 * 60 * 1000,
            refetchOnWindowFocus: false,
            retry: 1,
          },
          mutations: {
            retry: 0,
          },
        },
      }),
  );
  return (
    <>
      <Toaster />
      <QueryClientProvider client={queryClient}>
        <ReduxProvider store={chatUiReduxStore}>
          <ThemeProvider
            attribute='class'
            defaultTheme='system'
            enableSystem
            disableTransitionOnChange
          >
            {children}
          </ThemeProvider>
        </ReduxProvider>
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </>
  );
}
export default Providers;
