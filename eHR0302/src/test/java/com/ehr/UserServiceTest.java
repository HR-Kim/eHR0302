package com.ehr;

import static com.ehr.service.UserServiceImpl.MIN_LOGINCOUNT_FOR_SILVER;
import static com.ehr.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.ehr.service.TransactionHandler;
import com.ehr.service.UserService;
import com.ehr.service.UserServiceImpl;
import com.ehr.service.UserServiceTx;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
	private Logger LOG = Logger.getLogger(this.getClass());
	@Autowired
	MailSender mailSender;
	
	@Autowired //location에서 파일에서 객체를 찾아 자동으로 매칭시켜줌
	UserServiceImpl userService;
	
	@Autowired //location에서 파일에서 객체를 찾아 자동으로 매칭시켜줌
	UserDao userDao;
	
	@Autowired
	DataSource dataSource;
	
	List<User> users;
	
	
	@Autowired
	PlatformTransactionManager transactionManager;	
	
	//대상 데이터 생성
	@Before
	public void setUp() {
		users = Arrays.asList(
				 new User("j01_124","이상무01","1234",Level.BASIC,MIN_LOGINCOUNT_FOR_SILVER-1,0,"jamesol@paran.com","2019/08/23")
				,new User("j02_124","이상무02","1234",Level.BASIC,MIN_LOGINCOUNT_FOR_SILVER,0,"jamesol@paran.com","2019/08/23") //BASIC -> SILVER
				,new User("j03_124","이상무03","1234",Level.SILVER,50,MIN_RECCOMEND_FOR_GOLD-1,"jamesol@paran.com","2019/08/23")
				,new User("j04_124","이상무04","1234",Level.SILVER,50,MIN_RECCOMEND_FOR_GOLD,"jamesol@paran.com","2019/08/23") //SILVER -> GOLD
				,new User("j05_124","이상무05","1234",Level.GOLD,99,99,"jamesol@paran.com","2019/08/23")
				);
	}
	
	
	//최초 사용자 등록 처리 : Level == null 이면 BASIC처리
	@Test
	@Ignore
	public void add() {
		//1. 전체삭제
		//2. Level 있는 사람
		//3. Level == null
		//4. 등업

		//1. 전체삭제
		userDao.deleteAll();
		
		//2. Level 있는 사람
		User userExistLevel = users.get(4);  //셋팅해준 값 활용
		
		//3. Level == null
		User userNoLevel = users.get(0); //셋팅해준 값 활용
		userNoLevel.sethLevel(null); //널로 셋팅해줌
		
		userService.add(userExistLevel); //서비스에 add 메소드 따로 만들어줌 (Level 검사하는)
		//Level == null -> Level.BASIC
		userService.add(userNoLevel); //서비스에 add 메소드 따로 만들어줌 (Level 검사하는)
		
		//4. 등록 데이턱 get
		User userExistLevelRead = userDao.get(userExistLevel.getU_id()); //GOLD 예상
		User userNoLevelRead = userDao.get(userNoLevel.getU_id()); //BASIC 예상
		
		//5. 확인
		assertThat(userExistLevelRead.gethLevel(), is(userExistLevel.gethLevel()));
		assertThat(userNoLevelRead.gethLevel(), is(Level.BASIC));
		
		
//		내가 짠 로직, 어느 부분에서 막혔는지 봐봐		
//		//최초 사용자등록
//		//베이직으로 업그레이드
//		//업그레이드 확인
//		
//		
//		//최초 사용자등록
//		User user = new User();
//		userDao.add(user);
//		
//		
//		//베이직으로 셋팅
//		
//		user.setName("강슬기U");
//		user.setPasswd("1234U");
//		user.sethLevel(Level.GOLD);
//		user.setLogin(99);
//		user.setRecommend(999);
//		user.setEmail("Uzz@hanmail.net");
//		
//		
//		user.setU_id("");
//		user.sethLevel(Level.BASIC);
//		this.userService.upgradeLevels();
//		
//		
//		//업그레이드 확인
//		checkUser(user.gethLevel(),Level.BASIC);
	}
	
	
	@Test
	public void upgreadeLevel() throws SQLException {
		//1.전체삭제
		//2.users데이터 등록
		//3.upgradeLevels 호출
		//4. 02,04 업그레이드 대상
		
		//1.
		userDao.deleteAll();
		
		//2.
		for(User user : users) {
			userDao.add(user);
		}
		
		//3.
		this.userService.upgradeLevels();
		//4.
		checkUser(users.get(0),Level.BASIC);
		checkUser(users.get(1),Level.SILVER); //업그레이드 될 것 예상
		checkUser(users.get(2),Level.SILVER);
		checkUser(users.get(3),Level.GOLD); //업그레이드 될 것 예상
		checkUser(users.get(4),Level.GOLD);
	}
	
	private void checkUser(User user, Level expectedLevel) {
		User userUpdate = userDao.get(user.getU_id());
		assertThat(userUpdate.gethLevel(), is(expectedLevel));
	}
	
	
	@Test
	//@Ignore
	public void serviceBean() {
		assertThat(this.userService, is(notNullValue())); //검증
		assertThat(this.userDao, is(notNullValue())); //검증
		assertThat(this.dataSource, is(notNullValue())); //검증
		assertThat(this.transactionManager, is(notNullValue())); //검증
		assertThat(this.mailSender, is(notNullValue())); //검증
		
		LOG.info("----------------------------");
		LOG.info("-userService-"+userService);
		LOG.info("-userDao-"+userDao);
		LOG.info("-dataSource-"+dataSource);
		LOG.info("-transactionManager-"+dataSource);
		LOG.info("-mailSender-"+mailSender);
		LOG.info("----------------------------");
	}
	
	@Test
	public void upgradeAllOrNothing() throws SQLException {
		TestUserService testUserService = new TestUserService(users.get(3).getU_id());
		testUserService.setUserDao(userDao);//userDao 수동 DI
		//testUserService.setDataSource(dataSource);
		//testUserService.setTransactionManager(transactionManager);
		testUserService.setMailSender(mailSender);
		
		TransactionHandler transactionHandler=new TransactionHandler();
		transactionHandler.setTarget(testUserService);
		transactionHandler.setTransactionManager(transactionManager);
		transactionHandler.setPattern("upgradeLevels");

		
		UserService txUserService = (UserService) Proxy.newProxyInstance(
				getClass().getClassLoader(), 
				new Class[] {UserService.class}, 
				transactionHandler);
		
		
		//전체 삭제
		userDao.deleteAll();
		
		//users입력
		for(User user: users) {
			userDao.add(user);
		}
		
		try {
			
			txUserService.upgradeLevels();
			
		}catch(TestUserServiceException t) {
			
		}
		//트랜잭션처리 되면 Level.BASIC rollback;
		checkUser(users.get(1), Level.BASIC);
	}

}

class TestUserService extends UserServiceImpl{
	private String id;
	
	public TestUserService(String id) {
		this.id=id;
	}
	
	public void upgradeLevel(User user) {
		if(user.getU_id().equals(id)) {
			throw new TestUserServiceException();
		}
		
		super.upgradeLevel(user);
	}
	
}


class TestUserServiceException extends RuntimeException{
	public TestUserServiceException() {
		System.out.println("=================");
		System.out.println("=estUserServiceException()=");
		System.out.println("=================");
	}
	
}














