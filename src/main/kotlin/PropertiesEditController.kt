import javaFXMediators.CustomFXTableView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit.ClassItem.Prop
import tiOPF.ObjectList

class PropertiesEditController(private val properties: ObjectList<Prop>, resource: String): BasePaneController(properties, resource) {
    @FXML
    lateinit var propertiesTable: CustomFXTableView
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)

        addMediator(propertiesTable, properties, arrayOf(
            "name(100,\"Name\")",
            "type(100,\"Type\")",
            "virtual(100,\"Virtual\",|)"
        ))

        val addItem = MenuItem("Add Property")
        val delItem = MenuItem("Delete Property")
        delItem.isDisable = true


        propertiesTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete Property \"$name\""
            }
            else
                delItem.text = "Delete Property"
        }

        propertiesTable.contextMenu = ContextMenu(addItem, delItem)
        propertiesTable.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    val prop = properties.addUniqueItem("Property_New", "name", :: Prop)
                    // some defaults
                    prop.type = "String"
                }
                delItem -> {
                    val tableItem = propertiesTable.selectionModel.selectedItem
                    if (tableItem != null) {
                        val item = tableItem.itemMediator.model
                        if (item != null && item is Prop)
                            properties.remove(item)
                    }
                }
            }

        }

        return result
    }
}