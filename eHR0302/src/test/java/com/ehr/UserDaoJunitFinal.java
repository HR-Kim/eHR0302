package com.ehr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")

public class UserDaoJunitFinal {
	
	private Logger LOG = Logger.getLogger(UserDaoJunitFinal.class);
	
	@Autowired
	ApplicationContext context;
	
	private UserDao dao;
	private User user01;
	private User user02;
	private User user03;
	
	
	@Before
	public void setUp() {
		LOG.debug("^^^^^^^^^^^^^^^^^^");
		LOG.debug("setUp()");
		LOG.debug("^^^^^^^^^^^^^^^^^^");
		
		user01 = new User("j01_126","강슬기01","1234",Level.BASIC,1,0,"zz@hanmail.net","2019/08/23");
		user02 = new User("j02_126","강슬기02","1234",Level.SILVER,51,10,"zz@hanmail.net","2019/08/23");
		user03 = new User("j03_126","강슬기03","1234",Level.GOLD,51,21,"zz@hanmail.net","2019/08/23");
		
		dao = context.getBean("userDao", UserDaoJdbc.class);
		
		LOG.debug("========================");
		LOG.debug("context:"+context);
		LOG.debug("dao:"+dao);
		LOG.debug("========================");
		
	}
	
	
	
	@After
	public void tearDown() {
		LOG.debug("^^^^^^^^^^^^^^^^^^");
		LOG.debug("99 tearDown()");
		LOG.debug("^^^^^^^^^^^^^^^^^^");
	}
	
	
	
	
	@Test
	public void update() {
		
		LOG.debug("======================================");
		LOG.debug("=01. 기존 데이터 삭제=");
		LOG.debug("======================================");
		dao.deleteUser(user01);
		dao.deleteUser(user02);
		dao.deleteUser(user03);
		
		
		LOG.debug("======================================");
		LOG.debug("=02. 데이터 입력=");
		LOG.debug("======================================");
		dao.add(user01);
		
		
		LOG.debug("======================================");
		LOG.debug("=03. 입력데이터 수정=");
		LOG.debug("=03.1 user01 수정=");
		LOG.debug("=03.1 user01 업데이트 수행=");
		LOG.debug("======================================");
		
		//User user01 = new User("j01_126","강슬기UU","1234",Level.BASIC,1,0,"zz@hanmail.net","2019/08/23");
		user01.setName("강슬기U");
		user01.setPasswd("1234U");
		user01.sethLevel(Level.GOLD);
		user01.setLogin(99);
		user01.setRecommend(999);
		user01.setEmail("Uzz@hanmail.net");
		
		dao.update(user01);
		
		
		LOG.debug("======================================");
		LOG.debug("=04. 수정데이터와 3번 비교=");
		LOG.debug("======================================");
		User vsUser = dao.get(user01.getU_id());
		
		
		assertTrue(user01.equals(vsUser));
		
		
	}
	
	
	
	@Test
	@Ignore
	public void retrieve() {
		Search vo = new Search(10,1,"",""); //생성자 참고해서 하드코딩
		List<User> list = dao.retrieve(vo); //List에 담아야함. User에 대한내용을
		
		for(User user : list) {
			LOG.debug(user);
		}
		
		
	}
	
	
	
	@Test
	@Ignore
	public void getAll() throws SQLException {
//		dao.deleteUser(user01);
//		dao.deleteUser(user02);
//		dao.deleteUser(user03);
		
		List<User> list = dao.getAll();
		for(User user:list) {
			LOG.debug(user);
		}
	}
	
	
	
	@Test(expected = EmptyResultDataAccessException.class) //예외 발생 시 true
	@Ignore
	public void getFailure() throws ClassNotFoundException, SQLException {
		//----------------------------------
		//삭제
		//----------------------------------
		dao.deleteUser(user01);
		dao.deleteUser(user02);
		dao.deleteUser(user03);
		assertThat(dao.count("_126"), is(0));
		
		dao.get("unknownUserId");
	}
	
	
	@Test
	@Ignore
	public void count() throws ClassNotFoundException, SQLException {
		//----------------------------------
		//삭제
		//----------------------------------
		dao.deleteUser(user01);
		dao.deleteUser(user02);
		dao.deleteUser(user03);
		assertThat(dao.count("_126"), is(0));
		
		
		//----------------------------------
		//1건 추가
		//----------------------------------
		dao.add(user01);
		assertThat(dao.count("_126"), is(1));
		
		//----------------------------------
		//1건 추가
		//----------------------------------
		dao.add(user02);
		assertThat(dao.count("_126"), is(2));
		
		//----------------------------------
		//1건 추가
		//----------------------------------
		dao.add(user03);
		assertThat(dao.count("_126"), is(3));
		
		//----------------------------------
		//count:3
		//----------------------------------
		
		
	}
	
	
	
	@Test(timeout=5000) //1.JUnit에게 테스트용 메소드임을 알려줌
	@Ignore //테스트할 때 스킵함
	public void addAndGet() { //2.public
		//j04_ip : j01_126
		
		
		LOG.debug("======================================");
		LOG.debug("=02. 삭제=");
		LOG.debug("======================================");
		int delflag = dao.deleteUser(user01);
		delflag = dao.deleteUser(user02);
		delflag = dao.deleteUser(user03);
		assertThat(dao.count("_126"), is(0)); //전부 지워서 0개여야함
	    LOG.debug("======================================");
	    LOG.debug("=01.01 delflag="+delflag);
	    LOG.debug("======================================");
		
		
		LOG.debug("======================================");
		LOG.debug("=02. 단건등록=");
		LOG.debug("======================================");
        int flag = dao.add(user01);         
        flag = dao.add(user02);         
        flag = dao.add(user03);      
	    LOG.debug("======================================");
	    LOG.debug("=01.01 add flag="+flag);
	    LOG.debug("======================================");
		assertThat(flag, is(1));
        assertThat(dao.count("_126"), is(3)); //3건 넣었으니 3개여야 함
        
        User  userOne = dao.get(user01.getU_id());
		
		LOG.debug("======================================");
		LOG.debug("=02. 단건조회=");
		LOG.debug("======================================");
		assertThat(userOne.getU_id(), is(user01.getU_id()));
		assertThat(userOne.getPasswd(), is(user01.getPasswd()));
		assertThat(userOne.getPasswd(), is(user01.getPasswd()));
			
	}
	
}
