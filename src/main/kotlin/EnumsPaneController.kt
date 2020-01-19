import javaFXMediators.CustomFXTableView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit.Enum.EnumItem

class EnumsPaneController(val enum: Project.Unit.Enum, resource: String): BasePaneController(enum, resource) {
    @FXML
    lateinit var nameText: TextField
    @FXML
    lateinit var enumSetNameText: TextField
    @FXML
    lateinit var enumsTableView: CustomFXTableView

    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(nameText, "name")
        addMediator(enumSetNameText, "set")
        addMediator(enumsTableView, enum.values, arrayOf(
            "name(100,\"Name\")",
            "value(100,\"Value\")"
        ))

        val addItem = MenuItem("Add value")
        val delItem = MenuItem("Delete value")
        delItem.isDisable = true


        enumsTableView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete value \"$name\""
            } else
                delItem.text = "Delete value"
        }

        enumsTableView.contextMenu = ContextMenu(addItem, delItem)
        enumsTableView.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    enum.values.addUniqueItem("Value", "name", ::EnumItem)
                }
                delItem -> {
                    val tableItem = enumsTableView.selectionModel.selectedItem
                    if (tableItem != null) {
                        val item = tableItem.itemMediator.model
                        if (item != null && item is EnumItem)
                            enum.values.remove(item)
                    }
                }
            }

        }

        return result
    }
}