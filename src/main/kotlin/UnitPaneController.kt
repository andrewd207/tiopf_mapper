import javaFXMediators.CustomFXListView
import javaFXMediators.MediatedItem
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import tiOPF.Mapper.Project

class UnitPaneController(val unit: Project.Unit, resource: String): BasePaneController(unit, resource) {
    @FXML
    lateinit var unitNameTextField: TextField
    @FXML
    lateinit var referencesListView: CustomFXListView

    override fun finishLoad(project: Project): Node {
        val result = super.finishLoad(project)
        addMediator(unitNameTextField, "name")
        addMediator(referencesListView, "name", unit.references)

        val addItem = MenuItem("Add reference")
        val delItem = MenuItem("Delete reference")
        delItem.isDisable = true


        referencesListView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete reference \"$name\""
            }
            else
                delItem.text = "Delete reference"
        }

        referencesListView.contextMenu = ContextMenu(addItem, delItem)
        referencesListView.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    val textDialog = TextInputDialog("")
                    textDialog.title = "Adding new reference"
                    textDialog.headerText = "Enter a reference name"
                    val result = textDialog.showAndWait()
                    if (result.isPresent && result.get().trim() != ""){
                        val reference = Project.Unit.Reference()
                        reference.name = result.get().trim()
                        unit.references.add(reference)
                    }

                }
                delItem -> {
                    val selectedItem = referencesListView.selectionModel.selectedItem
                    if (selectedItem != null) {
                        val item = selectedItem.itemMediator.model
                        if (item != null && item is Project.Unit.Reference)
                            unit.references.remove(item)
                    }
                }
            }

        }




        return  result
    }
}