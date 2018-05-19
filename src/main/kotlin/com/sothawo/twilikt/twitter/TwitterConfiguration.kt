package com.sothawo.twilikt.twitter

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@ConfigurationProperties("twitter")
class TwitterConfiguration {
    var oauthConsumerKey = "not set"
    var oauthConsumerSecret = "not set"
    var oauthAccessKey = "not set"
    var oauthAccessSecret = "not set"

    override fun toString(): String {
        return "TwitterConfiguration(oauthConsumerKey='$oauthConsumerKey', oauthConsumerSecret='$oauthConsumerSecret', oauthAccessKey='$oauthAccessKey', oauthAccessSecret='$oauthAccessSecret')"
    }


}
