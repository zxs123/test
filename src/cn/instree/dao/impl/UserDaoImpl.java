package cn.instree.dao.impl;

import org.springframework.stereotype.Repository;

import cn.instree.core.base.BaseDao;
import cn.instree.dao.UserDao;
import cn.instree.domain.User;

@Repository("UserDao")
public class UserDaoImpl extends BaseDao<User,String> implements UserDao{

	

	
}
