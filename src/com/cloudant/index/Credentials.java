package com.cloudant.index;

import javax.servlet.http.Cookie;

public class Credentials {
	private String user = null;
	private String password = null;
	private Cookie[] dbCoreCookie = null;
	private String authorization = null;
	public Credentials(String user, String password, String authorization, Cookie[] cookie) {
		this.user = user;
		this.password = password;
		this.authorization = authorization;
		this.dbCoreCookie = cookie;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getUser() {
		return user;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}
	public void setDbCoreCookie(Cookie[] dbCoreCookie) {
		this.dbCoreCookie = dbCoreCookie;
	}
	public Cookie[] getDbCoreCookie() {
		return dbCoreCookie;
	}
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	public String getAuthorization() {
		return authorization;
	}
}
