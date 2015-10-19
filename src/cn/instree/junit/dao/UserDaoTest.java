package cn.instree.junit.dao;

import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.instree.dao.UserDao;
import cn.instree.domain.User;

public class UserDaoTest {

	@Test
	public void testSaveUser() {
		
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"config/applicationContext.xml"});
		UserDao userdao = (UserDao)ctx.getBean("UserDao");
		User user = new User();
		user.setUsername("'why'");
		userdao.save(user);
		
	}
	
}
