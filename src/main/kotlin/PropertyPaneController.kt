import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Control
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class PropertyPaneController(prop: Project.Unit.ClassItem.Prop, resource: String): BasePaneController(prop, resource) {
    @FXML
    lateinit var nameEdit: TextField
    @FXML
    lateinit var typeEdit: TextField
    @FXML
    lateinit var virtualCheckBox: CheckBox
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(nameEdit, "name")
        addMediator(typeEdit, "type")
        addMediator(virtualCheckBox, "virtual")

        return result
    }
}