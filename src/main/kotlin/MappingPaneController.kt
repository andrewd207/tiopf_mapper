import javaFXMediators.CustomFXTableView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit.ClassItem.Mapping.PropMap

class MappingPaneController(private val classItem: Project.Unit.ClassItem, resource: String): BasePaneController(classItem.mapping, resource) {
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

        val addItem = MenuItem("Add Mapping")
        val delItem = MenuItem("Delete Mapping")
        delItem.isDisable = true


        mappingsTableView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete Mapping \"$name\""
            } else
                delItem.text = "Delete Mapping"
        }

        mappingsTableView.contextMenu = ContextMenu(addItem, delItem)
        mappingsTableView.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    var newName = "Prop_New"
                    var newType = "String"
                    var brk = false
                    classItem.classProps.forEach {property ->
                        if (brk)
                          return@forEach
                        if (classItem.mapping.mappings.find { propMap -> propMap.prop == property.name} == null) {
                            newName = property.name
                            newType = property.type
                            brk = true
                            return@forEach
                        }
                    }

                    val prop = classItem.mapping.mappings.addUniqueItem(newName, "prop", ::PropMap)
                    // some defaults
                    prop.type = newType
                    prop.field = prop.prop.toUpperCase()
                }
                delItem -> {
                    val tableItem = mappingsTableView.selectionModel.selectedItem
                    if (tableItem != null) {
                        val item = tableItem.itemMediator.model
                        if (item != null && item is PropMap)
                            classItem.mapping.mappings.remove(item)
                    }
                }
            }

        }


        return result
    }
}