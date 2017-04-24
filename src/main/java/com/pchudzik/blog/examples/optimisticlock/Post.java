package com.pchudzik.blog.examples.optimisticlock;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {
	@Id
	@GeneratedValue
	private Long id;

	@Version
	private Long version;

	private String title;
	private String content;

	public Post update(String newTitle, String newContent) {
		this.title = newTitle;
		this.content = newContent;
		return this;
	}
}
