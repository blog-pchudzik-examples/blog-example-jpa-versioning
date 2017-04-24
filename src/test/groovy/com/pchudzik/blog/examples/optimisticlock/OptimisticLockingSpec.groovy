package com.pchudzik.blog.examples.optimisticlock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.OptimisticLockException

@SpringBootTest
class OptimisticLockingSpec extends Specification {
	@Autowired
	EntityManager entityManager

	@Autowired
	TransactionTemplate transactionTemplate

	def tearDown() {
		transactionTemplate.execute({ status ->
			entityManager
					.createQuery("from Post", Post.class)
					.getResultList()
					.each { post -> entityManager.remove(post) }
		})
	}

	def "entity manager merge example"() {
		given: "Bob saves first post version"
		final firstPostVersion = transactionTemplate.execute({ status ->
			entityManager.merge(Post.builder()
					.title("post")
					.content("content")
					.build())
		})

		when: "Alice changes paragraphs order while Bob is fixing typos"
		transactionTemplate.execute({ status ->
			entityManager
					.find(Post.class, firstPostVersion.id)
					.update("Alice's title", "Alice's content")
		})

		and: "Bob fixed some typos in first version"
		transactionTemplate.execute({ status ->
			entityManager
					.merge(firstPostVersion)
					.update("Bob's title", "Bob's content")
		})

		then: "conflict is expected because Alice updated post while Bob was editing it"
		thrown OptimisticLockException
	}

	def "using DTO without version example"() {
		given: "Bob saves first post version"
		final postId = transactionTemplate.execute({ status ->
			entityManager.merge(Post.builder()
					.title("post")
					.content("content")
					.build())
		}).id

		when: "Alice changes paragraphs order while Bob is fixing typos"
		final alicePostDto = transactionTemplate.execute({ status -> new PostDtoWithoutVersion(entityManager.find(Post.class, postId)) })
		alicePostDto.title = "Alice's title"
		alicePostDto.content = "Alice's content"


		and: "Bob is fixing typos in first version"
		final bobPostDto = transactionTemplate.execute({ status -> new PostDtoWithoutVersion(entityManager.find(Post.class, postId)) })
		bobPostDto.title = "Bob's title"
		bobPostDto.content = "Bob's content"

		and: "Alice clicks save"
		transactionTemplate.execute({ status ->
			entityManager
					.find(Post.class, alicePostDto.id)
					.update(alicePostDto.title, alicePostDto.content)
		})

		and: "Bob clicks save"
		transactionTemplate.execute({ status ->
			entityManager
					.find(Post.class, bobPostDto.id)
					.update(bobPostDto.title, bobPostDto.content)
		})

		then: "database state when Alice and Bob are finished with post edition"
		final postInDb = transactionTemplate.execute({ status -> entityManager.find(Post.class, postId) })
		postInDb.title == "Bob's title"
		postInDb.content == "Bob's content"
	}

	def "using DTO with version example"() {
		given: "Bob saves first post version"
		final Post firstPostVersion = transactionTemplate.execute({ status ->
			entityManager.merge(Post.builder()
					.title("post")
					.content("content")
					.build())
		})

		when: "Alice changes paragraphs order while Bob is fixing typos"
		final PostDto alicePostDto = transactionTemplate.execute({ status -> new PostDto(entityManager.find(Post.class, firstPostVersion.getId())) })
		alicePostDto.title = "Alice's title"
		alicePostDto.content = "Alice's content"


		and: "Bob is fixing typos in first version"
		final PostDto bobPost = transactionTemplate.execute({ status -> new PostDto(entityManager.find(Post.class, firstPostVersion.getId())) })
		bobPost.title = "Bob's title"
		bobPost.content = "Bob's content"

		and: "Alice clicks save"
		transactionTemplate.execute({ status ->
			findPost(alicePostDto.id, alicePostDto.version)
					.update(alicePostDto.title, alicePostDto.content)
		})

		and: "Bob clicks save"
		transactionTemplate.execute({ status ->
			findPost(bobPost.id, bobPost.version)
					.update(bobPost.title, bobPost.content)
		})

		then:
		thrown OptimisticLockException
	}

	private Post findPost(Long id, Long version) {
		final post = entityManager.find(Post.class, id)
		if (post.version != version) {
			throw new OptimisticLockException()
		}
		return post
	}
}
