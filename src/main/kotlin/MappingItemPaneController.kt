import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class MappingItemPaneController(map: Project.Unit.ClassItem.Mapping.PropMap, resource: String): BasePaneController(map, resource) {
    @FXML
    lateinit var propEdit: TextField
    @FXML
    lateinit var fieldEdit: TextField
    @FXML
    lateinit var typeEdit: TextField

    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(propEdit, "prop")
        addMediator(fieldEdit, "field")
        addMediator(typeEdit, "type")

        return result
    }
}