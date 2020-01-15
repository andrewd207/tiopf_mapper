import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class ClassPaneController(classItem: Project.Unit.ClassItem, resource: String): BasePaneController(classItem, resource) {
    @FXML
    lateinit var baseClassEdit: TextField
    @FXML
    lateinit var baseClassParentEdit: TextField
    @FXML
    lateinit var autoMapCheckBox: CheckBox
    @FXML
    lateinit var autoCreateListCheckBox: CheckBox
    @FXML
    lateinit var oidTypeComboBox: ComboBox<String>
    @FXML
    lateinit var notifyObserversCheckBox: CheckBox
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        oidTypeComboBox.items.add("Int")
        oidTypeComboBox.items.add("String")

        addMediator(baseClassEdit, "baseClass")
        addMediator(baseClassParentEdit, "baseClassParent")
        addMediator(oidTypeComboBox, "oidType")
        addMediator(autoMapCheckBox, "autoMap")
        addMediator(autoCreateListCheckBox, "autoCreateList")
        addMediator(notifyObserversCheckBox, "notifyObservers")

        return result

    }

}