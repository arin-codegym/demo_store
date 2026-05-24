import { useInfiniteQuery } from '@tanstack/react-query';
import { getNotifications } from '@/lib/api/notifications';
import { queryKeys } from '@/query/query-keys';

type Params = {
  unreadOnly?: boolean;
};

export function useNotifications(params?: Params) {
  return useInfiniteQuery({
    queryKey: queryKeys.infiniteNotifications(params),
    initialPageParam: null as string | null,
    queryFn: ({ pageParam }) =>
      getNotifications({
        cursor: pageParam,
        limit: 20,
        unreadOnly: params?.unreadOnly,
      }),
    getNextPageParam: (lastPage) => lastPage.nextCursor,
  });
}
