package com.pchudzik.blog.examples.optimisticlock;

import lombok.Data;

@Data
class PostDtoWithoutVersion {
	private final Long id;
	private String title;
	private String content;

	public PostDtoWithoutVersion(Post post) {
		this.id = post.getId();
		this.title = post.getTitle();
		this.content = post.getContent();
	}
}
