package com.quochuy.helper;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseExceptionUtil {
	public boolean isDuplicateKey(
			DataIntegrityViolationException e,
			String constraintName) {
		Throwable root = e.getRootCause();
		if (root instanceof org.postgresql.util.PSQLException psql) {
			return "23505".equals(
					psql.getSQLState()) && psql.getMessage() != null && psql.getMessage()
					.contains(constraintName);
		}
		return false;
	}
}
