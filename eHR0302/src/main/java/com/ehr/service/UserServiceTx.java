package com.ehr.service;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.ehr.User;

public class UserServiceTx implements UserService {
	private final Logger LOG= Logger.getLogger(UserServiceTx.class);
	
	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void add(User user) {
		userService.add(user);

	}

	@Override
	public void upgradeLevels() throws SQLException {
		// 트랜잭션 시작
		int upCnt = 0;
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			
			userService.upgradeLevels();
			// 정상이면 Commit
			transactionManager.commit(status);
			
		} catch (RuntimeException e) {
			LOG.debug("=========================");
			LOG.debug("=Exception=" + e.toString());
			LOG.debug("=rollback ="+ transactionManager.toString());
			LOG.debug("=status ="+ status.toString());
			LOG.debug("=========================");		
			// 실패면 rollback
			transactionManager.rollback(status);
			throw e;
		}
		LOG.debug("=========================");
		LOG.debug("=upCnt=" + upCnt);
		LOG.debug("=========================");
		// 트랜잭션 완료
	}

}
