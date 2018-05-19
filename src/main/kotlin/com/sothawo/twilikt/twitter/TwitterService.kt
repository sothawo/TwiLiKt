package com.sothawo.twilikt.twitter

import com.vaadin.spring.annotation.UIScope
import org.springframework.stereotype.Service

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@Service
@UIScope
class TwitterService(val twitterConfiguration: TwitterConfiguration)
