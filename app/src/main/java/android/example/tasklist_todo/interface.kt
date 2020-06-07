package android.example.tasklist_todo


interface ItemRowListener {

    abstract val adapter: Any
    abstract val toDoItemList: Any

    fun modifyItemState(itemObjectId: String, isDone: Boolean)
    fun onItemDelete(itemObjectId: String)
}
