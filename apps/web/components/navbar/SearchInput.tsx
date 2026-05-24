'use client';
import { useSearchParams, useRouter } from 'next/navigation';
import { Input } from '../ui/input';
import React, { useEffect, useState } from 'react';
import { useDebouncedCallback } from 'use-debounce';

function SearchInput() {
  const searchParams = useSearchParams();
  const { replace } = useRouter();
  // 1. Trích xuất giá trị ra biến riêng
  const searchTerm = searchParams.get('search');
  const [search, setSearch] = useState(searchTerm?.toString() || '');

  const handleSearch = useDebouncedCallback((value: string) => {
    const params = new URLSearchParams(searchParams);
    if (value) {
      params.set('search', value);
    } else {
      params.delete('search');
    }
    replace(`/products?${params.toString()}`);
  }, 300);

  useEffect(() => {
    if (!searchParams.get('search')) {
      setSearch('');
    }
  }, [searchTerm, searchParams]);
  return (
    <Input
      type='search'
      placeholder='search product...'
      className='max-w-xs dark:bg-muted '
      onChange={(e) => {
        setSearch(e.target.value);
        handleSearch(e.target.value);
      }}
      value={search}
    />
  );
}

export default SearchInput;
