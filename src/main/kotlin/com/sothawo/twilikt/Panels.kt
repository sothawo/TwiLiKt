package com.sothawo.twilikt

import com.vaadin.data.Binder
import com.vaadin.server.ExternalResource
import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.*
import com.vaadin.ui.renderers.HtmlRenderer
import com.vaadin.ui.renderers.ImageRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory.getLogger

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
    private var gridData: GridData? = null

    private var grid = Grid<GridEntry>().apply {
        setSizeFull()
        bodyRowHeight = 50.0
        setSelectionMode(Grid.SelectionMode.SINGLE)
    }

    init {
        content = grid
        setSizeFull()
    }

    fun setupColumns() {
        gridData?.let { data ->
            grid.addColumn({ it -> ExternalResource(it.user.profileImageUrl) }, ImageRenderer())
                    .apply {
                        width = 80.0
                        setSortable(false)
                    }
            grid.addColumn({ it -> it.user.htmlName() }, HtmlRenderer())
                    .apply {
                        expandRatio = 1
                    }

            var binder: Binder<GridEntry> = grid.editor.binder

            data.userLists.forEach { userList ->
                val binding = binder.bind(CheckBox(),
                        { gridEntry -> data.isUserInList(gridEntry.user.id, userList.id) },
                        { gridEntry, b -> data.setUserInList(gridEntry.user.id, userList.id, b) })

                grid.addColumn({ it -> data.isUserInList(it.user.id, userList.id) })
                        .apply {
                            caption = userList.name
                            editorBinding = binding
                        }
            }

            grid.editor.isEnabled = true
        }
    }

    fun setGridData(gridData: GridData) {
        this.gridData = gridData
        setupColumns()
        grid.setItems(gridData.users.map(::GridEntry))
    }
}


class GridData(val users: List<User>, val userLists: List<UserList>) {
    fun statusInfo(): String {
        return "${users.size} friends, ${userLists.size} lists"
    }

    fun isUserInList(userId: Long, listId: Long): Boolean {
        return userListWithId(listId)?.userIds?.contains(userId) ?: false
    }

    fun setUserInList(userId: Long, listId: Long, flag: Boolean?) {
        log.debug("set user $userId to list $listId $flag")
    }

    fun userListWithId(id: Long): UserList? {
        return userLists.find { it.id == id }
    }

    companion object {
        var log=  getLogger(javaClass.canonicalName)
    }

}

data class GridEntry(val user: User)

