package com.quochuy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedRolesValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedRoles {
	String message() default "Invalid roles";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
	
	/**
	 * Danh sách role cho phép (vd: ROLE_USER, ROLE_ADMIN)
	 */
	String[] value();
	
	/**
	 * Nếu true: roles không được null. Nếu false: null được phép (coi như không update roles).
	 */
	boolean notNull() default false;
	
	/**
	 * Nếu true: roles không được rỗng (empty list).
	 */
	boolean notEmpty() default false;
	
	/**
	 * Nếu true: không cho phép trùng role trong list.
	 */
	boolean unique() default true;
	
	/**
	 * Nếu true: trim mỗi role trước khi validate.
	 */
	boolean trim() default true;
}
