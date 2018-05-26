package com.sothawo.twilikt

import com.vaadin.server.ExternalResource
import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.*
import com.vaadin.ui.renderers.HtmlRenderer
import com.vaadin.ui.renderers.ImageRenderer

/**
 * a [Panel] to display a [user]. The panel has the users image, the screen name and name
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
class UserPanel(private val user: User) : Panel() {
    init {
        val image = Image().apply { source = ExternalResource(user.profileImageUrl) }
        val names = Label(user.htmlName(), ContentMode.HTML)

        content = HorizontalLayout().apply {
            addComponents(image, names)
            setComponentAlignment(image, Alignment.MIDDLE_LEFT)
            setComponentAlignment(names, Alignment.MIDDLE_LEFT)
        }
    }
}

/**
 * [Panel] to display the friends and their list memeberships.
 */
class GridPanel : Panel() {
    private var grid = Grid<GridEntry>().apply {
        setSizeFull()
        bodyRowHeight = 50.0

        addColumn({ it -> ExternalResource(it.user.profileImageUrl) }, ImageRenderer())
                .apply {
                    width = 80.0
                    setSortable(false)
                }
        val nameColumn = addColumn({ it -> it.user.htmlName() }, HtmlRenderer()).apply {
            setExpandRatio(1)
        }
        setSelectionMode(Grid.SelectionMode.SINGLE)
        sort(nameColumn)

    }

    init {
        content = grid
        setSizeFull()
    }

    fun setGridData(gridData: GridData) {
        grid.setItems(gridData.users.map(::GridEntry))
    }
}

class GridData(val users: List<User>, val userLists: List<UserList>) {
    fun statusInfo(): String {
        return "${users.size} friends, ${userLists.size} lists"
    }
}

data class GridEntry(val user: User)
