/*
 Copyright 2018 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.twilikt

import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

/**
 * service configuration class.
 */
@ConfigurationProperties("twitter")
class TwitterConfiguration {
    var oauthConsumerKey = "not set"
    var oauthConsumerSecret = "not set"
    var oauthAccessToken = "not set"
    var oauthAccessTokenSecret = "not set"
    override fun toString(): String {
        return "TwitterConfiguration(" +
                "oauthConsumerKey='$oauthConsumerKey'," +
                "oauthConsumerSecret='$oauthConsumerSecret'," +
                "oauthAccessToken='$oauthAccessToken'," +
                "oauthAccessTokenSecret='$oauthAccessTokenSecret'," +
                ")"
    }

}

/**
 * service encapsulating twitter any method call may throw a TwitterException or a NullPointerException.
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@Service
class TwitterService(private val twitter: Twitter) {

    fun currentUser(): User = userWithId(twitter.id).also { log.debug("current user: $it") }

    fun userWithId(id: Long): User {
        log.debug("retrieving user with id $id...")
        return twitter.showUser(id)!!.let {
            User(it.id, it.screenName, it.name, it.profileImageURLHttps)
        }
    }

    fun loadFriends(user: User): List<User> {
        log.debug("retrieving friends for @${user.name}...")
        val friends = mutableListOf<User>()
        twitter.showUser(user.id)!!.run {
            log.debug("number of friends: $friendsCount")

            var cursor: Long = 0
            var keepGoing = true
            do {
                twitter.getFriendsList(id, cursor, 200)
                        .apply {
                            stream()
                                    .filter { it != null }
                                    .forEach { friends += User(it.id, it.screenName, it.name, it.profileImageURLHttps) }
                            cursor = nextCursor
                            keepGoing = hasNext()
                        }
            } while (keepGoing)
        }
        return friends.toList()
    }

    companion object {
        @Slf4jLogger
        lateinit var log: Logger
    }
}

/**
 * configuration class providing the Twitter Bean.
 */
@Configuration
class TwitterProvider(private val config: TwitterConfiguration) {
    @Bean
    fun realTwitter(): Twitter {
        return TwitterFactory(ConfigurationBuilder()
                .setDebugEnabled(false)
                .setOAuthConsumerKey(config.oauthConsumerKey)
                .setOAuthConsumerSecret(config.oauthConsumerSecret)
                .setOAuthAccessToken(config.oauthAccessToken)
                .setOAuthAccessTokenSecret(config.oauthAccessTokenSecret)
                .build()).instance
    }
}

/**
 * custom user class
 */
data class User(val id: Long, val screenName: String, val name: String, val profileImageUrl: String)
