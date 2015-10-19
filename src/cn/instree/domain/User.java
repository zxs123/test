package cn.instree.domain;

import cn.instree.core.anno.base.PrimaryKey;
import cn.instree.core.anno.base.Table;
import cn.instree.core.anno.base.TableColumn;


@Table("user")
public class User {
	
	@PrimaryKey
	@TableColumn
	private String userid;
	
	@TableColumn
	private String username;
	
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	
}
