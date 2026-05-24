package com.quochuy.mybatis.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.UUID;

public class UUIDTypeHandler extends BaseTypeHandler<UUID> {
	
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
			throws SQLException {
		// PostgreSQL uuid works well with setObject
		ps.setObject(i, parameter);
	}
	
	@Override
	public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Object obj = rs.getObject(columnName);
		if (obj == null) return null;
		if (obj instanceof UUID) return (UUID) obj;
		return UUID.fromString(obj.toString());
	}
	
	@Override
	public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		Object obj = rs.getObject(columnIndex);
		if (obj == null) return null;
		if (obj instanceof UUID) return (UUID) obj;
		return UUID.fromString(obj.toString());
	}
	
	@Override
	public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		Object obj = cs.getObject(columnIndex);
		if (obj == null) return null;
		if (obj instanceof UUID) return (UUID) obj;
		return UUID.fromString(obj.toString());
	}
}