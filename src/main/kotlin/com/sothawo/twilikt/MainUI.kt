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
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.Logger

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@SpringUI
@Push
class MainUI(val twitterService: TwitterService) : UI() {

    val bottomPanel = BottomPanel { saveData() }

    val gridPanel = GridPanel { bottomPanel.button.isEnabled = true }


    override fun init(request: VaadinRequest?) {
        content = VerticalLayout().apply {
            setSizeFull()

            val userPanel =
                    try {
                        UserPanel(twitterService.currentUser())
                    } catch (e: Exception) {
                        Label("could not retrieve user: ${e.message}")
                    }

            addComponents(userPanel, gridPanel, bottomPanel)
            setExpandRatio(gridPanel, 1F)
        }
        log.info("MainUI initialized")
        showStatus("start loading friends")
        loadData()
    }

    /**
     * loads the data - friends (the user is following) - and sends it to the [gridPanel] async. data is loaded using
     * coroutines.
     */
    private fun loadData() = runBlocking {

        // load the friends
        val friendsJob = async {
            try {
                val friends = twitterService.loadFriends(twitterService.currentUser())
                log.info("loaded ${friends.size} friends")
                friends
            } catch (e: Exception) {
                notification(e.message ?: "unknown error", Notification.Type.ERROR_MESSAGE)
                emptyList<User>()
            }
        }

        // load the users's lists
        val listsJob = async {
            try {
                val userLists = twitterService.loadUserLists(twitterService.currentUser())
                log.info("loaded ${userLists.size} lists")
                userLists
            } catch (e: Exception) {
                notification(e.message ?: "unknown error", Notification.Type.ERROR_MESSAGE)
                emptyList<UserList>()
            }
        }

        // set the data in the gridPanel
        val gridData = GridData(friendsJob.await(), listsJob.await())
        access { gridPanel.setGridData(gridData) }
        showStatus("loaded grid data: ${gridData.statusInfo()}")
    }

    private fun saveData() {
        bottomPanel.button.isEnabled = false
        showStatus("should save data")
    }


    /**
     * shows a notification with the [msg] text and type [type]. Uses the UI thread.
     */
    private fun notification(msg: String, type: Notification.Type = Notification.Type.TRAY_NOTIFICATION) {
        access { Notification.show(msg, type) }
    }

    /**
     * show the [msg] ins the [statusLine] and logs it. Uses the UI thread.
     */
    fun showStatus(msg: String) {
        access { bottomPanel.statusLine.value = msg }
        log.info(msg)
    }

    companion object {
        @Slf4jLogger
        lateinit var log: Logger
    }
}
