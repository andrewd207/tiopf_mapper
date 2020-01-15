import javaFXMediators.CustomFXTableView
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import tiOPF.Mapper.Project

class MappingPaneController(val classItem: Project.Unit.ClassItem, resource: String): BasePaneController(classItem.mapping, resource) {
    @FXML
    lateinit var tableEdit: TextField
    @FXML
    lateinit var pkChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var pkFieldEdit: TextField
    @FXML
    lateinit var oidTypeComboBox: ComboBox<String>
    @FXML
    lateinit var mappingsTableView: CustomFXTableView
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)

        pkChoiceBox.items.add("OID")
        classItem.classProps.forEach {
            pkChoiceBox.items.add(it.name)
        }
        addMediator(tableEdit, "table")
        addMediator(pkChoiceBox, "pk")
        addMediator(pkFieldEdit, "pkField")






        mappingsTableView.isEditable = true

        oidTypeComboBox.items.addAll("String", "Int")

        addMediator(oidTypeComboBox, "oidType")
        addMediator(mappingsTableView, classItem.mapping.mappings, arrayOf(
            "prop(100,\"Property\")",
            "field(100,\"Table field\")",
            "type(100,\"Type\")"
        ))


        return result
    }
}