'use client';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { toast } from '@/components/ui/use-toast';
import useGetUsers from '@/query/admin/useGetUsers';
import useUpdateUser from '@/query/admin/useUpdateUser';
import { formatDate } from '@/utils/format';
import React from 'react';
import { CreateUserModal } from './CreateUserModal';

type UserRow = {
  userId: string | number;
  userName: string;
  fullName: string;
  email: string;
  avatarUrl: string;
  status: UserStatus;
  roles: string[] | string;
  authProvider?: string;
  providerUserId?: string;
  createdAt: string | Date;
  updateAt?: string | Date;
};
type UserStatus = 'ACTIVE' | 'BANNED' | 'DELETED';
type Draft = {
  status: UserStatus;
  roles: string[];
};
const ALL_ROLES = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER'] as const;
const ALL_STATUS: UserStatus[] = ['ACTIVE', 'BANNED', 'DELETED'];
const EDITABLE_STATUS: Exclude<UserStatus, 'DELETED'>[] = ['ACTIVE', 'BANNED'];

function normalizeRoles(roles: UserRow['roles']): string[] {
  if (Array.isArray(roles)) return roles;

  return String(roles)
    .split(',')
    .map((r) => r.trim())
    .filter(Boolean);
}
function rolesEqual(a: string[], b: string[]) {
  const sa = [...a].sort().join('|');
  const sb = [...b].sort().join('|');
  return sa === sb;
}
function statusLabel(s: UserStatus) {
  return s.charAt(0) + s.slice(1).toLowerCase();
}

function statusVariant(
  s: UserStatus,
): 'default' | 'secondary' | 'destructive' | 'outline' {
  if (s === 'ACTIVE') return 'default';
  if (s === 'BANNED') return 'secondary';
  return 'destructive'; // DELETED
}

export default function UsersPage() {
  const { data: users = [], isLoading, error, refetch } = useGetUsers();
  const updateUserMutation = useUpdateUser();

  /**
   * Each edited row owns a draft keyed by userId. This lets admins edit
   * multiple rows before saving them one by one or as a batch.
   */
  const [draftById, setDraftById] = React.useState<Record<string, Draft>>({});

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>{(error as Error).message}</div>;

  const idKey = (id: string | number) => String(id);
  const startEdit = (u: UserRow) => {
    const key = idKey(u.userId);
    setDraftById((prev) => {
      // Do not overwrite unsaved edits if the row is already in draft mode.
      if (prev[key]) return prev;
      return {
        ...prev,
        [key]: { status: u.status, roles: normalizeRoles(u.roles) },
      };
    });
  };

  const cancelEdit = (userId: string | number) => {
    const key = idKey(userId);
    setDraftById((prev) => {
      const { [key]: _, ...next_prev } = prev;
      return next_prev;
    });
  };
  const cancelAll = () => setDraftById({});

  const setDraftStatus = (userId: string | number, status: UserStatus) => {
    const key = idKey(userId);
    setDraftById((prev) => ({
      ...prev,
      [key]: { ...prev[key], status },
    }));
  };
  const toggleDraftRole = (userId: string | number, role: string) => {
    const key = idKey(userId);
    setDraftById((prev) => {
      const cur = prev[key];
      if (!cur) return prev;

      const has = cur.roles.includes(role);
      const roles = has
        ? cur.roles.filter((r) => r !== role)
        : [...cur.roles, role];

      return { ...prev, [key]: { ...cur, roles } };
    });
  };
  const isRowDirty = (u: UserRow) => {
    const key = idKey(u.userId);
    const draft = draftById[key];
    if (!draft) return false;

    const originalRoles = normalizeRoles(u.roles);
    return draft.status !== u.status || !rolesEqual(draft.roles, originalRoles);
  };

  const saveRow = async (u: UserRow) => {
    const key = idKey(u.userId);
    const draft = draftById[key];
    if (!draft) return;

    const roles = draft.roles;

    try {
      await updateUserMutation.mutateAsync({
        userId: u.userId,
        payload: { status: draft.status, roles },
      });

      toast({ title: 'Saved', description: `Updated ${u.userName}` });
      cancelEdit(u.userId);
      refetch();
    } catch (e: any) {
      toast({
        title: 'Save failed',
        description: e?.message ?? 'Something went wrong',
        variant: 'destructive',
      });
    }
  };
  const saveAll = async () => {
    const dirtyUsers = users.filter((u) => isRowDirty(u));
    if (dirtyUsers.length === 0) {
      toast({ title: 'Nothing to save' });
      return;
    }

    try {
      // Save sequentially so one failed update can stop the batch cleanly.
      for (const u of dirtyUsers) {
        const key = idKey(u.userId);
        const draft = draftById[key];
        if (!draft) continue;

        const roles = draft.roles;
        await updateUserMutation.mutateAsync({
          userId: u.userId,
          payload: { status: draft.status, roles },
        });

        setDraftById((prev) => {
          const next = { ...prev };
          delete next[key];
          return next;
        });
      }

      toast({
        title: 'Saved',
        description: `Updated ${dirtyUsers.length} users`,
      });
      refetch();
    } catch (e: any) {
      toast({
        title: 'Save all failed',
        description: e?.message ?? 'Something went wrong',
        variant: 'destructive',
      });
    }
  };

  const editingCount = Object.keys(draftById).length;
  const dirtyCount = users.filter((u) => isRowDirty(u)).length;
  return (
    <div className='space-y-4'>
      <div className='flex items-center justify-between gap-3'>
        <div className='text-sm text-muted-foreground'>
          Editing: <span className='font-medium'>{editingCount}</span> · Dirty:{' '}
          <span className='font-medium'>{dirtyCount}</span>
        </div>

        <div className='flex gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={cancelAll}
            disabled={editingCount === 0}
          >
            Cancel all
          </Button>
          <Button
            size='sm'
            onClick={saveAll}
            disabled={dirtyCount === 0 || updateUserMutation.isPending}
          >
            {updateUserMutation.isPending ? 'Saving...' : 'Save all'}
          </Button>
        </div>
      </div>

      <Table>
        <TableCaption>Total users : {users.length}</TableCaption>

        <TableHeader>
          <TableRow>
            <TableHead>User Name</TableHead>
            <TableHead className='w-[160px]'>Full Name</TableHead>
            <TableHead>Email</TableHead>
            <TableHead className='w-[120px]'>Status</TableHead>
            <TableHead>Roles</TableHead>
            <TableHead className='w-[160px]'>Date</TableHead>
            <TableHead className='w-[260px] text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>

        <TableBody>
          {users.map((user: UserRow) => {
            const key = idKey(user.userId);
            const draft = draftById[key];
            const isEditing = Boolean(draft);

            const originalRoles = normalizeRoles(user.roles);
            // const currentActive = isEditing ? draft!.active : user.active;
            const currentStatus = isEditing ? draft!.status : user.status;
            const currentRoles = isEditing ? draft!.roles : originalRoles;

            const dirty = isRowDirty(user);
            const canEdit = user.status !== 'DELETED';

            return (
              <TableRow
                key={user.userId}
                className={dirty ? 'bg-muted/30' : ''}
              >
                <TableCell className='font-medium'>{user.userName}</TableCell>
                <TableCell>{user.fullName}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>
                  {!isEditing ? (
                    <Badge variant={statusVariant(currentStatus)}>
                      {statusLabel(currentStatus)}
                    </Badge>
                  ) : (
                    <Select
                      value={currentStatus}
                      onValueChange={(v) =>
                        setDraftStatus(user.userId, v as UserStatus)
                      }
                    >
                      <SelectTrigger className='w-[140px]'>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {EDITABLE_STATUS.map((s) => (
                          <SelectItem key={s} value={s}>
                            {statusLabel(s)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                </TableCell>
                <TableCell>
                  <div className='flex items-center gap-3'>
                    <div className='flex flex-wrap gap-2'>
                      {currentRoles.length ? (
                        currentRoles.map((r) => (
                          <Badge
                            key={r}
                            variant={
                              r === 'ROLE_ADMIN' ? 'default' : 'secondary'
                            }
                          >
                            {r.replace('ROLE_', '')}
                          </Badge>
                        ))
                      ) : (
                        <span className='text-sm text-muted-foreground'>
                          No roles
                        </span>
                      )}
                    </div>

                    {isEditing && (
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button variant='outline' size='sm'>
                            Edit roles
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent align='start' className='w-64'>
                          <div className='space-y-3'>
                            <div className='text-sm font-medium'>Set roles</div>

                            <div className='space-y-2'>
                              {ALL_ROLES.map((role) => (
                                <label
                                  key={role}
                                  className='flex items-center gap-2 text-sm cursor-pointer'
                                >
                                  <Checkbox
                                    checked={currentRoles.includes(role)}
                                    onCheckedChange={() =>
                                      toggleDraftRole(user.userId, role)
                                    }
                                  />
                                  <span>{role.replace('ROLE_', '')}</span>
                                </label>
                              ))}
                            </div>

                            <div className='text-xs text-muted-foreground'>
                              Tick để thêm quyền, bỏ tick để remove.
                            </div>
                          </div>
                        </PopoverContent>
                      </Popover>
                    )}
                  </div>
                </TableCell>

                <TableCell>{formatDate(user.createdAt)}</TableCell>
                {/* {Button Edit} */}

                <TableCell className='text-right'>
                  {!isEditing ? (
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => startEdit(user)}
                      disabled={!canEdit}
                    >
                      Edit
                    </Button>
                  ) : (
                    <div className='flex justify-end gap-2'>
                      <Button
                        variant='ghost'
                        size='sm'
                        onClick={() => cancelEdit(user.userId)}
                      >
                        Cancel
                      </Button>

                      <Button
                        size='sm'
                        disabled={!dirty || updateUserMutation.isPending}
                        onClick={() => saveRow(user)}
                      >
                        {updateUserMutation.isPending ? 'Saving...' : 'Save'}
                      </Button>
                    </div>
                  )}
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
      <div className='flex items-center justify-between'>
        <div className='text-lg font-semibold'>Users</div>
        <CreateUserModal />
      </div>

      <div className='text-xs text-muted-foreground'>
        Hàng nào đang “dirty” sẽ được highlight nhẹ. Bạn có thể edit nhiều hàng
        rồi Save all.
      </div>
    </div>
  );
}
