package com.pchudzik.blog.examples.optimisticlock;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {
	@Id
	@GeneratedValue
	private Long id;

	@Version
	private Long version;

	private String title;
	private String content;

	public Post(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public void update(String newTitle, String newContent) {
		this.title = newTitle;
		this.content = newContent;
	}
}
