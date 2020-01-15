import javaFXMediators.CustomFXTableView
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

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

        return result
    }
}