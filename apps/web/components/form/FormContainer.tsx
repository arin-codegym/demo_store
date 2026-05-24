'use client';

import { useFormState } from 'react-dom';
import React, {
  createContext,
  useActionState,
  useContext,
  useEffect,
} from 'react';
import { useToast } from '@/components/ui/use-toast';
import { actionFunction } from '@/utils/types';

const initialState = {
  message: '',
};
type FormState = {
  message: string;
  errors?: Record<string, string[]>; // Ví dụ: { name: ['Quá ngắn'], price: ['Phải là số'] }
};
const initMsg: FormState = {
  message: '',
  errors: {},
};
// Tạo một "đường ống" dẫn dữ liệu
const FormContext = createContext<FormState | undefined>(undefined);
// Export nó ra để file khác dùng xem comment tại FormInput
// export const FormContext = createContext<FormState | undefined>(undefined);

// function FormContainer({
//   action,
//   children,
// }: {
//   action: actionFunction;
//   children: React.ReactNode;
// }) {
//   const [state, formAction] = useFormState(action, initMsg);
//   const { toast } = useToast();
//   useEffect(() => {
//     if (state.message) {
//       toast({ description: state.message });
//     }
//   }, [state]);
//   return (
//     <FormContext.Provider value={state}>
//       <form action={formAction}>{children}</form>
//     </FormContext.Provider>
//   );
// } // cách làm của tutorial tôi nâng cấp lên common scale hơn

// Thêm React.forwardRef để component cha có thể truy cập thẻ <form>
const FormContainer = React.forwardRef<
  HTMLFormElement,
  { action: actionFunction; children: React.ReactNode }
>(({ action, children }, ref) => {
  const [state, formAction] = useActionState(action, initMsg);
  const { toast } = useToast();

  useEffect(() => {
    if (state.message) {
      toast({ description: state.message });
    }
  }, [state, toast]);

  return (
    <FormContext.Provider value={state}>
      {/* Gán ref vào đây */}
      <form action={formAction} ref={ref}>
        {children}
      </form>
    </FormContext.Provider>
  );
});

// Thêm dòng này để định danh component
FormContainer.displayName = 'FormContainer';
export default FormContainer;

// Hook để các input dễ dàng lấy lỗi
export const useFormContext = () => useContext(FormContext);
