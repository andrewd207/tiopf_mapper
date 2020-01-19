import javaFXMediators.CustomFXTableView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit.ClassItem.SelectionFunction.Param

class SelectionFunctionPaneController(private val selection: Project.Unit.ClassItem.SelectionFunction, resource: String): BasePaneController(selection, resource) {
    @FXML
    lateinit var nameEdit: TextField
    @FXML
    lateinit var paramsTable: CustomFXTableView
    @FXML
    lateinit var sqlTextArea: TextArea
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        paramsTable.isEditable = true
        //paramsTable.selectionModel.cellSelectionEnabledProperty().value = true
        addMediator(nameEdit, "name")
        addMediator(paramsTable, selection.params, arrayOf(
            "name(    -1,\"Name\")",
            "type(    -1,\"Type\")",
            "typeName(-1,\"Type name\")",
            "passBy(  -1,\"Pass By\")",
            "sqlParam(-1,\"SQL param\")"
        ))

        addMediator(sqlTextArea, "sql")

        val addItem = MenuItem("Add parameter")
        val delItem = MenuItem("Delete parameter")
        delItem.isDisable = true


        paramsTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete parameter \"$name\""
            }
            else
                delItem.text = "Delete parameter"
        }

        paramsTable.contextMenu = ContextMenu(addItem, delItem)
        paramsTable.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    val param = selection.params.addUniqueItem("Param", "name", ::Param)
                    // some defaults
                    param.type = "String"
                    param.sqlParam = param.name.toUpperCase()
                }
                delItem -> {
                    val tableItem = paramsTable.selectionModel.selectedItem
                    if (tableItem != null) {
                        val item = tableItem.itemMediator.model
                        if (item != null && item is Param)
                            selection.params.remove(item)
                    }
                }
            }

        }



        return result
    }
}