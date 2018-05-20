package com.sothawo.twilikt

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import twitter4j.Twitter
import twitter4j.TwitterException

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TwitterConfiguration::class, TwitterService::class, TwitterProvider::class,
    LoggingInjector::class])
internal class TwitterServiceTest {

    @Autowired
    lateinit var twitterService: TwitterService

    // backend bean to be injected into the TwitterService
    @MockBean
    lateinit var twitter4j: Twitter

    // the default user
    val user = User(4711, "johndoe", "John Doe", "")

    @Nested
    @DisplayName("a TwitterService")
    inner class AllTwitterServiceTests {

        @BeforeEach
        fun resetMock() {
            // need to reset, can't recreate as the bean is created by Spring
            reset(twitter4j)
            // when is a kotlin keyword
            `when`(twitter4j.id).thenReturn(user.id)
            // set up a mock user to be returned from twitter4j
            val twitterUser = Mockito.mock(twitter4j.User::class.java)
            `when`(twitterUser.id).thenReturn(user.id)
            `when`(twitterUser.name).thenReturn(user.name)
            `when`(twitterUser.screenName).thenReturn(user.screenName)
            `when`(twitter4j.showUser(user.id)).thenReturn(twitterUser)
        }

        @Test
        fun `returns the current user`() {
            assertThat(twitterService.currentUser()).isEqualTo(user)
            verify(twitter4j).showUser(user.id)
        }

        @Test
        fun `returns user for a valid id`() {
            assertThat(twitterService.userWithId(user.id)).isEqualTo(user)
            verify(twitter4j).showUser(user.id)
        }

        @Test
        fun `throws up on unknown id`() {
            `when`(twitter4j.showUser(42)).thenThrow(TwitterException::class.java)

            assertThatThrownBy({ twitterService.userWithId(42) })
                    .isInstanceOf(TwitterException::class.java)
        }
    }
}
