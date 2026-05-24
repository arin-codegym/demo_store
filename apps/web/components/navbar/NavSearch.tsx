import { Input } from '../ui/input';
import { Suspense } from 'react';
import SearchInput from './SearchInput';

function NavSearch() {
  return (
    <Suspense fallback={<Input placeholder='Loading search...' />}>
      <SearchInput />
    </Suspense>
  );
}
export default NavSearch;
