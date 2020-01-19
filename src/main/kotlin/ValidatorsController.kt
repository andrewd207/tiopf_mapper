import javaFXMediators.CustomFXTableView
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit.ClassItem.Validator

class ValidatorsController(private val classItem: ProjectUnitClassItem, resource: String): BasePaneController(classItem, resource) {
    @FXML lateinit var validatorsTable: CustomFXTableView
    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)

        addMediator(validatorsTable, classItem.validators, arrayOf(
            "prop(100,\"Property\")",
            "type(100,\"Type\")",
            "value(100,\"Value\")"
        ))

        val addItem = MenuItem("Add validator")
        val delItem = MenuItem("Delete validator")
        val typeItem = Menu("Change Type")
        val types = Validator.Type.values()
        types.forEach {
            val t = MenuItem(it.toString())
            typeItem.items.add(t)
            t.onAction  = EventHandler { _ ->
                val selected = validatorsTable.selectionModel.selectedItem
                if (selected != null) {
                    val model = selected.itemMediator.model
                    if (model is Validator) {
                        model.type = it
                    }
                }
            }
        }
        typeItem.isDisable = true
        delItem.isDisable = true


        validatorsTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            typeItem.isDisable = delItem.isDisable
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete validator \"$name\""
            } else
                delItem.text = "Delete validator"
        }

        validatorsTable.contextMenu = ContextMenu(typeItem, addItem, SeparatorMenuItem(),delItem)
        validatorsTable.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {

                    var newName = "Prop_New"
                    var brk = false
                    classItem.classProps.forEach {property ->
                        if (brk)
                            return@forEach
                        if (classItem.validators.find { validator -> validator.prop == property.name} == null) {
                            newName = property.name
                            brk = true
                            return@forEach
                        }
                    }

                    val validator = classItem.validators.addUniqueItem(newName, "prop", ::Validator)
                    // some defaults
                    validator.type
                }
                delItem -> {
                    val tableItem = validatorsTable.selectionModel.selectedItem
                    if (tableItem != null) {
                        val item = tableItem.itemMediator.model
                        if (item != null && item is Validator)
                            classItem.validators.remove(item)
                    }
                }
            }

        }

        return result
    }
}