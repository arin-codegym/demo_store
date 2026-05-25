const CHAT_HIDDEN_ROUTES = [
  '/chat',
  '/login',
  '/register',
  '/activate',
  '/forgot-password',
  '/reset-password',
] as const;

export function shouldHideGlobalChat(pathname: string) {
  return CHAT_HIDDEN_ROUTES.some(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
}
