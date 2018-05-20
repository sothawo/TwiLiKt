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

import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.*
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@SpringUI
@Push
class MainUI(val twitterService: TwitterService) : UI() {

    private val grid = gridComponent()


    override fun init(request: VaadinRequest?) {
        content = VerticalLayout().apply {
            setSizeFull()

            val userPanel =
                    try {
                        UserPanel(twitterService.currentUser())
                    } catch (e: Exception) {
                        Label("could not retrieve user: ${e.message}")
                    }

            addComponents(userPanel, grid, buttonComponent())
            setExpandRatio(grid, 1F)
        }
        log.info("MainUI initialized")
    }

    /**
     * [Component] displaying the followed users.
     */
    private fun gridComponent(): Component {
        return Grid<User>().apply {
            setSizeFull()
        }
    }

    // test grid with a button
    private fun buttonComponent(): Component = Button("Click me") { _ ->
        launch {
            try {
                val friends = twitterService.loadFriends(twitterService.currentUser())
                friends.forEach { log.debug("friend: $it") }
                notification("loaded ${friends.size} friends")
            } catch (e: Exception) {
                notification(e.message ?: "unknown error", Notification.Type.ERROR_MESSAGE)
            }
        }
    }

    private fun notification(msg: String, type: Notification.Type = Notification.Type.HUMANIZED_MESSAGE) {
        access { Notification.show(msg, type) }
    }

    companion object {
        @Slf4jLogger
        lateinit var log: Logger
    }
}
