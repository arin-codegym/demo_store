'use client';

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
  errors?: Record<string, string[]>;
};
const initMsg: FormState = {
  message: '',
  errors: {},
};

const FormContext = createContext<FormState | undefined>(undefined);

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
      <form action={formAction} ref={ref}>
        {children}
      </form>
    </FormContext.Provider>
  );
});

FormContainer.displayName = 'FormContainer';
export default FormContainer;

export const useFormContext = () => useContext(FormContext);
