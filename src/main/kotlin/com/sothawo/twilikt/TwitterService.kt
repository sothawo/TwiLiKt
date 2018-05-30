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

    /**
     * loads the friends for the given [user].
     */
    fun loadFriends(user: User): List<User> {
        log.debug("retrieving friends for @${user.screenName}...")
        val friends = mutableListOf<User>()

        var cursor = 0L
        var keepGoing = true
        do {
            try {
                twitter.getFriendsList(user.id, cursor, 200)
                        .apply {
                            toList().filterNotNull()
                                    .forEach { friends += User(it.id, it.screenName, it.name, it.profileImageURLHttps) }
                            cursor = nextCursor
                            keepGoing = hasNext()
                        }
            } catch (e: Exception) {
                log.warn("error retrieving friends for user $user", e)
                keepGoing = false
            }
        } while (keepGoing)
        return friends.toList()
    }

    /**
     * loads the [UserList]s for the given [user].
     */
    fun loadUserLists(user: User): List<UserList> {
        log.debug("retrieving lists for @${user.screenName}")
        return try {
            twitter.getUserLists(user.id)
                    .toList().filterNotNull()
                    .map { it: twitter4j.UserList ->
                        UserList(it.id, it.name, loadUserIdsForList(it.id).toMutableList())
                    }
        } catch (ex: Exception) {
            log.warn("could not load user lists for $user", ex)
            emptyList()
        }
    }

    private fun loadUserIdsForList(listId: Long): List<Long> {
        log.debug("retrieving users for list with id $listId")
        val users = mutableListOf<User>()

        // getUserListMembers starts paging with -1
        var cursor = -1L
        var keepGoing = true
        do {
            try {
                twitter.getUserListMembers(listId, 5_000, cursor)
                        .apply {
                            toList().filterNotNull()
                                    .forEach { users += User(it.id, it.screenName, it.name, it.profileImageURLHttps) }
                            cursor = nextCursor
                            keepGoing = hasNext()
                        }

            } catch (e: Exception) {
                log.warn("error getting users for list id $listId", e)
                keepGoing = false
            }

        } while (keepGoing)

        return users.map(User::id)
    }

    /**
     * try to update the given [userList] for the current user. Exceptions are passed out.
     */
    fun updateUserList(userList: UserList) {
        val (elementsToAdd, elementsToDelete) = userList.snapshotDifferences()
        log.debug("list: ${userList.name}, should add $elementsToAdd and should delete $elementsToDelete")

        if (elementsToAdd.isNotEmpty())
            twitter.createUserListMembers(userList.id, *elementsToAdd.toLongArray())

        if (elementsToDelete.isNotEmpty())
            twitter.destroyUserListMembers(userList.id, elementsToDelete.toLongArray())

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

fun User.htmlName() = "<b>$name</b> @$screenName"

data class UserList(val id: Long, val name: String, val userIds: MutableList<Long>) {
    private val userIdsSnapshot: MutableList<Long> = userIds.toMutableList()
    /**
     * compares the actual [userIds] of the UserList to [userIdsSnapshot] list and returns two lists. The first with the
     * elements to add, the second with the elements to remove from the snapshot list to get the current list. It also
     * sets the content of the snapshot list to the actual list.
     */
    fun snapshotDifferences(): Pair<List<Long>, List<Long>> {
        val elementsToDelete = userIdsSnapshot - userIds
        val elementsToAdd = userIds - userIdsSnapshot
        userIdsSnapshot.clear()
        userIdsSnapshot.addAll(userIds)
        return Pair(elementsToAdd, elementsToDelete)
    }
}

