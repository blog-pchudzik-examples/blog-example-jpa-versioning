package com.pchudzik.blog.examples.optimisticlock;

import lombok.Data;

@Data
public class PostDto {
	private final Long id;
	private final Long version;
	private String title;
	private String content;

	public PostDto(Post post) {
		this.id = post.getId();
		this.version = post.getVersion();
		this.title = post.getTitle();
		this.content = post.getContent();
	}
}
