package android.example.tasklist_todo


class ToDoItem {

    companion object Factory {
        fun create(): ToDoItem = ToDoItem()
    }

    var objectId: String? = null
    var itemText: String? = null
    var done: Boolean? = false
}