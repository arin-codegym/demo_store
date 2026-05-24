package com.quochuy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllowedRolesValidator implements ConstraintValidator<AllowedRoles, List<String>> {
	private Set<String> allowed;
	private boolean notNull;
	private boolean notEmpty;
	private boolean unique;
	private boolean trim;
	
	@Override
	public void initialize(AllowedRoles annotation) {
		this.allowed = new HashSet<>(Arrays.asList(annotation.value()));
		this.notNull = annotation.notNull();
		this.notEmpty = annotation.notEmpty();
		this.unique = annotation.unique();
		this.trim = annotation.trim();
	}
	
	@Override
	public boolean isValid(List<String> roles, ConstraintValidatorContext ctx) {
		// null handling
		if (roles == null) {
			return !notNull;
		}
		
		// empty handling
		if (roles.isEmpty()) {
			return !notEmpty;
		}
		
		Set<String> seen = unique ? new HashSet<>() : null;
		
		for (int i = 0; i < roles.size(); i++) {
			String r = roles.get(i);
			
			if (r == null) {
				buildViolation(ctx, "Role must not be null", i);
				return false;
			}
			
			String role = trim ? r.trim() : r;
			
			if (role.isEmpty()) {
				buildViolation(ctx, "Role must not be blank", i);
				return false;
			}
			
			if (!allowed.contains(role)) {
				buildViolation(ctx, "Invalid role: " + role, i);
				return false;
			}
			
			if (unique && !seen.add(role)) {
				buildViolation(ctx, "Duplicate role: " + role, i);
				return false;
			}
		}
		
		return true;
	}
	
	private void buildViolation(ConstraintValidatorContext ctx, String message, int index) {
		ctx.disableDefaultConstraintViolation();
		// gắn lỗi vào phần tử roles[index] để FE biết cụ thể chỗ sai
		ctx.buildConstraintViolationWithTemplate(message)
				.addPropertyNode("roles")
				.addBeanNode()
				.inIterable().atIndex(index)
				.addConstraintViolation();
	}
}
