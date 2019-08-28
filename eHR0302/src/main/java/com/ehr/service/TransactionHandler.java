package com.ehr.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {
	private final Logger LOG = Logger.getLogger(TransactionHandler.class);

	private PlatformTransactionManager transactionManager;
	private String pattern;// 트랜잭션을 적용할 메소드 이름.

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	private Object target;// 부가기능을 적용할 타깃 오브젝트, 어떤 오브텍트도 적용 가능.
	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (method.getName().startsWith(pattern)) {
			return invokeTransaction(method, args);
		} else {
			return method.invoke(target, args);
		}
	}

	/**
	 * 트랜잭션 처리 메소드
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	private Object invokeTransaction(Method method, Object[] args) throws Throwable {
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			//target method call
			Object retObj = method.invoke(target, args);
			//성공 commit;
			this.transactionManager.commit(status);
			
			return retObj;
		} catch (InvocationTargetException e) {
			//실패 rollback;
			this.transactionManager.rollback(status);	
			LOG.debug("=================================");
			LOG.debug("=invokeTransaction="+target);
			LOG.debug("=================================");
			throw e.getTargetException();
		}
	}

}
