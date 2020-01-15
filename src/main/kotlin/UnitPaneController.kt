import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class UnitPaneController(unit: Project.Unit, resource: String): BasePaneController(unit, resource) {
    @FXML
    lateinit var unitNameTextField: TextField

    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(unitNameTextField, "name")
        return  result
    }
}