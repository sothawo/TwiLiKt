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

import com.vaadin.spring.annotation.UIScope
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import javax.annotation.PostConstruct

/**
 * service configuration class.
 */
@ConfigurationProperties("twitter")
class Configuration {
    var oauthConsumerKey = "not set"
    var oauthConsumerSecret = "not set"
    var oauthAccessToken = "not set"
    var oauthAccessTokenSecret = "not set"
    override fun toString(): String {
        return "Configuration(" +
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
@UIScope
class TwitterService(val config: Configuration) {
    private val twitter: Twitter = setupTwitter()

    private fun setupTwitter(): Twitter {
        return TwitterFactory(ConfigurationBuilder()
                .setDebugEnabled(false)
                .setOAuthConsumerKey(config.oauthConsumerKey)
                .setOAuthConsumerSecret(config.oauthConsumerSecret)
                .setOAuthAccessToken(config.oauthAccessToken)
                .setOAuthAccessTokenSecret(config.oauthAccessTokenSecret)
                .build()).instance
    }

    fun currentUser(): User {
        return getUser(twitter.id)
    }

    fun getUser(id: Long): User {
        return twitter.showUser(id)!!.let { User(id, it.screenName, it.name) }
    }

    @PostConstruct
    fun postConstruct() {
        try {
            log.info("Twitter initialized for ${getUser(twitter.id)}.")
        } catch (e: Exception) {
            log.error("error initializing Twitter", e)
        }
    }

    companion object {
        @Slf4jLogger
        lateinit var log: Logger
    }
}

/**
 * custom user class
 */
data class User(val id: Long, val screenName: String, val name: String)
