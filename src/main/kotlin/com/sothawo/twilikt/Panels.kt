package com.sothawo.twilikt

import com.vaadin.server.ExternalResource
import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.*

/**
 * a [Panel] to display a [user]. The panel has the users image, the screen name and name
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
class UserPanel(private val user: User) : Panel() {
    init {
        val image = Image().apply { source = ExternalResource(user.profileImageUrl) }
        val names = Label("<b>${user.name}</b> @${user.screenName}", ContentMode.HTML)

        content = HorizontalLayout().apply {
            addComponents(image, names)
            setComponentAlignment(image, Alignment.MIDDLE_LEFT)
            setComponentAlignment(names, Alignment.MIDDLE_LEFT)
        }
    }
}
