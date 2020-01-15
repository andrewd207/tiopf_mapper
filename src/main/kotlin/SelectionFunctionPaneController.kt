import javaFXMediators.CustomFXTableView
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class SelectionFunctionPaneController(val selection: Project.Unit.ClassItem.SelectionFunction, resource: String): BasePaneController(selection, resource) {
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


        return result
    }
}