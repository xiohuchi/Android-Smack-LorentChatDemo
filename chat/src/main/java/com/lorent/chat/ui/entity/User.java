package com.lorent.chat.ui.entity;

public class User {
	private String userName;
	private String nickName;
	private String email;
	
	public void setUserName(String username)
	{
		this.userName = username;	
	}
	
	public void setNickName(String nickname)
	{
		this.nickName = nickname;
	}
	
	public  void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getUserName()
	{
		return this.userName;
	}
	
	public String getNickName()
	{
		return this.nickName;
	}
	
	public String getEmail()
	{
		return this.email;
	}
}
