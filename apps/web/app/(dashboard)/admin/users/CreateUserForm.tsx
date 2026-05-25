'use client';

import React from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { toast } from '@/components/ui/use-toast';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

import useCreateUser from '@/query/admin/useCreateUser';

const ALL_ROLES = ['ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMIN'] as const;
const EDITABLE_STATUS = ['ACTIVE', 'BANNED'] as const;

const schema = z.object({
  userName: z.string().min(3, 'UserName min 3 ký tự'),
  fullName: z.string().min(1, 'FullName không được trống'),
  email: z
    .string()
    .trim()
    .refine((v) => v === '' || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v), {
      message: 'Email không hợp lệ',
    }),

  password: z.string().min(6, 'Password min 6 ký tự'),
  status: z.enum(EDITABLE_STATUS),
  roles: z.array(z.string()).min(1, 'Chọn ít nhất 1 role'),
});
const apiSchema = schema.extend({
  email: z
    .string()
    .trim()
    .transform((v) => (v === '' ? null : v)),
});

type FormValues = z.infer<typeof schema>;
export type ApiPayload = z.output<typeof apiSchema>;

export function CreateUserForm({ onCreated }: { onCreated?: () => void }) {
  const createUserMutation = useCreateUser();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      userName: '',
      fullName: '',
      email: '',
      password: '',
      status: 'ACTIVE',
      roles: ['ROLE_USER'],
    },
  });

  const roles = form.watch('roles');

  const toggleRole = (role: string) => {
    const cur = roles ?? [];
    const next = cur.includes(role)
      ? cur.filter((r) => r !== role)
      : [...cur, role];
    form.setValue('roles', next, { shouldDirty: true, shouldValidate: true });
  };

  const onSubmit = async (values: FormValues) => {
    try {
      const payload: ApiPayload = apiSchema.parse(values);
      await createUserMutation.mutateAsync(payload);
      toast({
        title: 'Created',
        description: `User ${values.userName} created`,
      });
      form.reset({
        ...form.getValues(),
        password: '',
        userName: '',
        fullName: '',
        email: '',
      });
      onCreated?.();
    } catch (e: any) {
      toast({
        title: 'Create failed',
        description: e?.message ?? 'Something went wrong',
        variant: 'destructive',
      });
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create user</CardTitle>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-4'>
            <div className='grid gap-4 md:grid-cols-2'>
              <FormField
                control={form.control}
                name='userName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>User Name</FormLabel>
                    <FormControl>
                      <Input placeholder='john_doe' {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='fullName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Full Name</FormLabel>
                    <FormControl>
                      <Input placeholder='John Doe' {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='email'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input placeholder='john@company.com' {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='password'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Temp Password</FormLabel>
                    <FormControl>
                      <Input
                        type='password'
                        placeholder='••••••••'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Status</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {EDITABLE_STATUS.map((s) => (
                          <SelectItem key={s} value={s}>
                            {s}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name='roles'
              render={() => (
                <FormItem>
                  <FormLabel>Roles</FormLabel>
                  <div className='flex flex-wrap gap-4 rounded-md border p-3'>
                    {ALL_ROLES.map((role) => (
                      <label
                        key={role}
                        className='flex items-center gap-2 text-sm cursor-pointer'
                      >
                        <Checkbox
                          checked={(roles ?? []).includes(role)}
                          onCheckedChange={() => toggleRole(role)}
                        />
                        <span>{role.replace('ROLE_', '')}</span>
                      </label>
                    ))}
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className='flex items-center justify-end gap-2'>
              <Button
                type='button'
                variant='outline'
                onClick={() => form.reset()}
                disabled={createUserMutation.isPending}
              >
                Reset
              </Button>
              <Button type='submit' disabled={createUserMutation.isPending}>
                {createUserMutation.isPending ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
