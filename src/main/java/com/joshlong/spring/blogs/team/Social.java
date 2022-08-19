package com.joshlong.spring.blogs.team;

public enum Social {

	TWITTER("twitter-small"), GITHUB("github-small");

	private final String cn;

	Social(String className) {
		this.cn = className;
	}

	public String className() {
		return this.cn;
	}

}
