'use client';

import React from 'react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';

import { CreateUserForm } from './CreateUserForm';

export function CreateUserModal() {
  const [open, setOpen] = React.useState(false);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>Create user</Button>
      </DialogTrigger>

      <DialogContent className='sm:max-w-[700px]'>
        <DialogHeader>
          <DialogTitle>Create user</DialogTitle>
        </DialogHeader>

        {/* Form */}
        <CreateUserForm
          onCreated={() => setOpen(false)} // optional: tạo xong tự đóng
        />
      </DialogContent>
    </Dialog>
  );
}
