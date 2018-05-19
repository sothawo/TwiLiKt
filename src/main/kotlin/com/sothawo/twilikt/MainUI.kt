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

import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Button
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import org.slf4j.Logger

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@SpringUI
class MainUI(val twitterService: TwitterService) : UI() {

    override fun init(request: VaadinRequest?) {
        content = Button("Click me") { _ ->
            run {
                try {
                    Notification.show(twitterService.currentUser().toString())
                } catch (e: Exception) {
                    Notification.show(e.message, Notification.Type.ERROR_MESSAGE)
                }
            }
        }
        log.info("MainUI initialized")
    }

    companion object {
        @Slf4jLogger
        lateinit var log: Logger
    }
}
