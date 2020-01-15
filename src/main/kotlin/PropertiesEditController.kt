import javaFXMediators.CustomFXTableView
import javafx.fxml.FXML
import javafx.scene.Node
import tiOPF.Mapper.Project
import tiOPF.ObjectList

class PropertiesEditController(private val properties: ObjectList<Project.Unit.ClassItem.Prop>, resource: String): BasePaneController(properties, resource) {
    @FXML
    lateinit var propertiesTable: CustomFXTableView
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)

        addMediator(propertiesTable, properties, arrayOf(
            "name(100,\"Name\")",
            "type(100,\"Type\")",
            "virtual(100,\"Virtual\",|)"
        ))

        return result
    }
}