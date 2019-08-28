package com.ehr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoJdbc implements UserDao {

	static final Logger LOG = Logger.getLogger(UserDao.class);
	
	private RowMapper<User> userMapper = new RowMapper<User>() {
	      @Override
	      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
	         User tmp = new User();
	         tmp.setU_id(rs.getString("u_id"));
	         tmp.setName(rs.getString("name"));
	         tmp.setPasswd(rs.getString("passwd"));
	         tmp.sethLevel(Level.valueOf(rs.getInt("h_level")));
	         
	         tmp.setLogin(rs.getInt("login"));
	         tmp.setRecommend(rs.getInt("recommend"));
	         tmp.setEmail(rs.getString("email"));
	         tmp.setRegDt(rs.getString("reg_dt"));    
	         
	         tmp.setNum(rs.getInt("rnum"));
	         tmp.setTotalCnt(rs.getInt("total_cnt"));
	         
	         return tmp;
	      }//--mapRow            
	};
	
	
	private JdbcTemplate jdbcTemplate;
	private DataSource dataSource;
	
	public UserDaoJdbc() {} //디폴트 생성자. 중요.
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}
	
	
	
	@Override
	public int update(User user) {
		int flag = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("  UPDATE users					\n");
		sb.append("  SET                         	\n");
		sb.append("      name = ?            	    \n");
		sb.append("      ,passwd = ?             	\n");
		sb.append("      ,h_level = ?            	\n");
		sb.append("      ,login = ?              	\n");
		sb.append("      ,recommend = ?          	\n");
		sb.append("      ,email = ?              	\n");
		sb.append("      ,reg_dt = sysdate       	\n");
		sb.append("  WHERE                          \n");
		sb.append("      u_id = ?                   \n");
		
		LOG.debug("=============================");
		LOG.debug("02. sql=\n"+sb.toString());
		LOG.debug("=============================");	
		
		Object[] args = {user.getName()
				,user.getPasswd()
				,user.gethLevel().intValue()
				,user.getLogin()
				,user.getRecommend()
				,user.getEmail()
				,user.getU_id()
		};
		
		LOG.debug("========================");
		LOG.debug("02.args=\n"+args);
		LOG.debug("========================");
		
		
		return jdbcTemplate.update(sb.toString(), args);
		
	}
	
	
	
	@Override
	public List<User> retrieve(Search vo) {
		//U_ID : 10
		//NAME : 20
		//EMAIL : 30
		
		StringBuilder param = new StringBuilder();
		
		//검색조건이 선택되면
		if(vo != null && !"".equals(vo.getSearchDiv())) {
			
			//아이디검색
			if("10".equals(vo.getSearchDiv())) {
				param.append(" WHERE u_id LIKE '%' || ? || '%' \n "); // '%?%' 이런형태가 찾기는 좋지만 속도가 느려짐
			}else if("20".equals(vo.getSearchDiv())) {
				param.append(" WHERE u_id LIKE '%' || ? || '%' \n ");
			}else if("30".equals(vo.getSearchDiv())) {
				param.append(" WHERE u_id LIKE '%' || ? || '%' \n ");
			}
			
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("  SELECT T1.*,T2.*														\n");
		sb.append("  FROM                                                                   \n");
		sb.append("  (                                                                      \n");
		sb.append("  	SELECT                                                              \n");
		sb.append("  		B.u_id,                                                         \n");
		sb.append("  		B.name,                                                         \n");
		sb.append("  		B.passwd,                                                       \n");
		sb.append("  		B.h_level,                                                      \n");
		sb.append("  		B.login,                                                        \n");
		sb.append("  		B.recommend,                                                    \n");
		sb.append("  		B.email,                                                        \n");
		sb.append("  		TO_CHAR(B.reg_dt, 'yyyy/mm/dd') reg_dt,                          \n");
		sb.append("  		B.rnum                         \n");
		sb.append("  	FROM                                                                \n");
		sb.append("  	( 	SELECT ROWNUM AS rnum, A.*                                      \n");
		sb.append("  		FROM                                                            \n");
		sb.append("  		(                                                               \n");
		sb.append("  			SELECT  *                                                   \n");
		sb.append("  			FROM   users                                                \n");
		//---------------------------------------------------------------------------------------
		// where 검색조건도 파람임
		//---------------------------------------------------------------------------------------
		sb.append("  			--Search Condition                                          \n");
		sb.append(param.toString());
		sb.append("  			ORDER BY reg_dt DESC                                        \n");
//		sb.append("  		)A                                                              \n");
//		sb.append("  	   WHERE rownum <=(:PAGE_SIZE * (:PAGE_NUM-1)+:PAGE_SIZE)           \n");
//		sb.append("  	) B                                                                 \n");
//		sb.append("  	WHERE B.rnum >= (:PAGE_SIZE * (:PAGE_NUM-1)+1)                      \n");
		sb.append("  		)A                                                              \n");
		sb.append("  	   WHERE rownum <=(? * (?-1)+?)    						        \n");
		sb.append("  	) B                                                                 \n");
		sb.append("  	WHERE B.rnum >= (? * (?-1)+1)                  				    \n");
		sb.append("  )T1                                                                    \n");
		sb.append("  NATURAL JOIN                                                           \n");
		sb.append("  (                                                                      \n");
		sb.append("      SELECT COUNT(*) total_cnt                                          \n");
		sb.append("      FROM users                                                         \n");
		//---------------------------------------------------------------------------------------
		// where 검색조건도 파람임
		//---------------------------------------------------------------------------------------
		sb.append("  	--Search Condition       		                                    \n");
		sb.append(param.toString());
		sb.append("  )T2                                                                    \n");
		
		
		LOG.debug("=========================");
		LOG.debug("01. sql=\n"+sb.toString());
		LOG.debug("=========================");
		
		
		LOG.debug("=========================");
		LOG.debug("02. param=\n"+vo.toString());
		LOG.debug("=========================");
		
		
		//param to List<Object>
		List<Object> listArgs = new ArrayList<Object>();
		
		
		if(vo != null && !"".equals(vo.getSearchDiv())) {
			listArgs.add(vo.getSearchWord()); //where절 파람
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageNum());
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageNum());
			listArgs.add(vo.getSearchWord()); //where절 파람
			
		}else{
			//파람 들어오는 순서
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageNum());
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageSize());
			listArgs.add(vo.getPageNum());
			
		}
		
		
		return jdbcTemplate.query(sb.toString(), listArgs.toArray(), userMapper);
		
		
	}
	
	
	
	@Override
	public List<User> getAll() {
		StringBuilder sb = new StringBuilder();
		sb.append("  SELECT											\n");
		sb.append("      u_id,                                      \n");
		sb.append("      name,                                      \n");
		sb.append("      passwd,                                    \n");
		sb.append("      h_level,                                   \n");
		sb.append("      login,                                     \n");
		sb.append("      recommend,                                 \n");
		sb.append("      email,                                     \n");
		sb.append("      TO_CHAR(reg_dt, 'yyyy/mm/dd') reg_dt,      \n");
		sb.append("      1 as rnum,    							    \n");
		sb.append("      1 as total_cnt     					    \n");
		sb.append("  FROM                                           \n");
		sb.append("      users                                      \n");
	    sb.append("  ORDER BY u_id									\n");
	    LOG.debug("========================");
	    LOG.debug("02.sql=\n"+sb.toString());
	    LOG.debug("========================");
	    List<User> list = jdbcTemplate.query(sb.toString(), userMapper);
	    
	    
		return list;
	}

	@Override
	public int count(String uId) {
		User outVO = null;
		int cnt = 0; //조회카운트
		//-----------------------------
		//02.SQL문
		//-----------------------------
		StringBuilder sb = new StringBuilder();
		sb.append("  SELECT COUNT(*) cnt		 \n");
		sb.append("  FROM users             	 \n");
		sb.append("  WHERE u_id LIKE ?		     \n");
		LOG.debug("========================");
		LOG.debug("02.sql=\n"+sb.toString());
		LOG.debug("========================");
		
		
		cnt = jdbcTemplate.queryForObject(sb.toString() //sql
										, new Object[] {"%"+uId+"%"} //Object배열. ?에 들어갈 값
										, Integer.class); ////리턴 타입. 건 수
		
		
		return cnt;
	}

	
	
	@Override
	public int deleteUser(User user) {
		String query = " DELETE FROM users WHERE u_id = ? \n";
//		int flag = jdbcContext.executeSql(query, user);
		
		Object[] args = {user.getU_id()};
		int flag = jdbcTemplate.update(query, args);
		
		return flag;
	}
	
	
	//전부 지우는 로직
	public int deleteAll() {
		String query = " DELETE FROM users";
		int flag = jdbcTemplate.update(query);
		
		return flag;
	}
	
	
	

	@Override
	public int add(User user) {
		int flag = 0;
		//-----------------------------
		//02.SQL문
		//-----------------------------
		StringBuilder sb = new StringBuilder();
		sb.append("  INSERT INTO users (		 \n");
		sb.append("      u_id,                   \n");
		sb.append("      name,                   \n");
		sb.append("      passwd,                 \n");
		sb.append("      h_level,                \n");
		sb.append("      login,                  \n");
		sb.append("      recommend,              \n");
		sb.append("      email,                  \n");
		sb.append("      reg_dt                  \n");
		sb.append("  ) VALUES (                  \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      ?,                      \n");
		sb.append("      SYSDATE                 \n");
		sb.append("  )                           \n");
		LOG.debug("========================");
		LOG.debug("02.sql=\n"+sb.toString());
		LOG.debug("========================");
		
		Object[] args = {user.getU_id()
						,user.getName()
						,user.getPasswd()
						,user.gethLevel().intValue()
						,user.getLogin()
						,user.getRecommend()
						,user.getEmail()
						
		};
		
		LOG.debug("========================");
		LOG.debug("02.param=\n"+user.toString());
		LOG.debug("========================");
		
		
		flag = jdbcTemplate.update(sb.toString(), args);
		
		return flag;
	}

	@Override
	public User get(String id) {
		User outVO=null;

		//----------------------------------------
		//02.SQL작성
		//----------------------------------------		
		StringBuilder sb=new StringBuilder();
		sb.append("  SELECT											\n");
		sb.append("      u_id,                                      \n");
		sb.append("      name,                                      \n");
		sb.append("      passwd,                                    \n");
		sb.append("      h_level,                                   \n");
		sb.append("      login,                                     \n");
		sb.append("      recommend,                                 \n");
		sb.append("      email,                                     \n");
		sb.append("      TO_CHAR(reg_dt, 'yyyy/mm/dd') reg_dt,      \n");
		//rowmapper를 retrieve와 공유하기 때문에 아래 칼럼 추가
		sb.append("      1 as rnum,                      			\n");
		sb.append("      1 as total_cnt                      		\n");
		sb.append("  FROM                                           \n");
		sb.append("      users                                      \n");
		sb.append("  WHERE u_id = ?                                 \n");
		
		LOG.debug("=============================");
		LOG.debug("02. sql=\n"+sb.toString());
		LOG.debug("=============================");		
		
		outVO = jdbcTemplate.queryForObject(sb.toString() //sql
											,new Object[] {id} //파람(?)에 넘길 값을 Object배열로 
											,userMapper);//--jdbcTemplate
		
		//----------------------------------------
		//06.outVO==null 예외발생
		//----------------------------------------
		if(null == outVO) {
			throw new EmptyResultDataAccessException(1);
		}		
		return outVO;
	}





}
