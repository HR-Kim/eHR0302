package com.ehr.service;

import java.sql.SQLException;

import com.ehr.User;

public interface UserService {

	// 최초 사용자 베이직 레벨
	public void add(User user);

	public void upgradeLevels() throws SQLException;
}
