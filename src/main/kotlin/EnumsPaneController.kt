import javaFXMediators.CustomFXTableView
import javafx.fxml.FXML
import javafx.scene.Node
import tiOPF.Mapper.Project
import tiOPF.ObjectList

class EnumsPaneController(val enum: Project.Unit.Enum, resource: String): BasePaneController(enum, resource) {
    @FXML
    lateinit var enumsTableView: CustomFXTableView

    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(enumsTableView, enum.values, arrayOf(
            "name(100,\"Name\")",
            "value(100,\"Value\")"
        ))

        return result
    }
}