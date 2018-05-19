package com.sothawo.twilikt

import com.sothawo.twilikt.twitter.TwitterService
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Button
import com.vaadin.ui.Notification
import com.vaadin.ui.UI

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@SpringUI
class MainUI(val twitterService: TwitterService) : UI() {

    override fun init(request: VaadinRequest?) {
        content = Button("Click me") { _ -> Notification.show(twitterService.twitterConfiguration.toString()) }
    }
}
