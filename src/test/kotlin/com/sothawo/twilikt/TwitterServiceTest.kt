package com.sothawo.twilikt

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import twitter4j.PagableResponseList
import twitter4j.Twitter
import twitter4j.TwitterException
import java.util.*
import java.util.stream.Stream

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
    val user = User(4711, "johndoe", "John Doe", "http://image.ugly")

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
            val twitterUser = mock(twitter4j.User::class.java)
            `when`(twitterUser.id).thenReturn(user.id)
            `when`(twitterUser.name).thenReturn(user.name)
            `when`(twitterUser.screenName).thenReturn(user.screenName)
            `when`(twitterUser.profileImageURLHttps).thenReturn(user.profileImageUrl)
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

        @Mock
        lateinit var responseList: PagableResponseList<twitter4j.User>

        @Test
        fun `retrieves the friend's list`() {
            val expectedFriends = listOf(
                    User(1, "friend1", "Friend 1", "http://image.ugly/friend1"),
                    User(2, "friend2", "Friend 2", "http://image.ugly/friend2")
            )

            // set up mock users to be returned from twitter4j
            val twitterUser1 = mock(twitter4j.User::class.java)
            `when`(twitterUser1.id).thenReturn(expectedFriends[0].id)
            `when`(twitterUser1.name).thenReturn(expectedFriends[0].name)
            `when`(twitterUser1.screenName).thenReturn(expectedFriends[0].screenName)
            `when`(twitterUser1.profileImageURLHttps).thenReturn(expectedFriends[0].profileImageUrl)
            val twitterUser2 = mock(twitter4j.User::class.java)
            `when`(twitterUser2.id).thenReturn(expectedFriends[1].id)
            `when`(twitterUser2.name).thenReturn(expectedFriends[1].name)
            `when`(twitterUser2.screenName).thenReturn(expectedFriends[1].screenName)
            `when`(twitterUser2.profileImageURLHttps).thenReturn(expectedFriends[1].profileImageUrl)


            `when`(responseList.hasNext()).thenReturn(false)
            `when`(responseList.stream()).thenReturn(Arrays.asList(twitterUser1, twitterUser2).stream())

            `when`(twitter4j.getFriendsList(eq(user.id), anyLong(), anyInt())).thenReturn(responseList)

            assertThat(twitterService.loadFriends(user)).containsExactlyInAnyOrderElementsOf(expectedFriends)

            verify(twitter4j, atLeastOnce()).getFriendsList(eq(user.id), anyLong(), anyInt())
        }
    }
}
