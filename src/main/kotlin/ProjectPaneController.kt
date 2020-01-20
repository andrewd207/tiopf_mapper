import javaFXMediators.*
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import tiOPF.Mapper.Project

class ProjectPaneController(project: Project, resource: String): BasePaneController(project, resource) {
    @FXML lateinit var projectNameEdit: TextField
    @FXML lateinit var outputPathEdit: TextField
    @FXML lateinit var enumTypeCombo: ComboBox<Project.EnumType>
    @FXML lateinit var includesList: CustomFXListView
    @FXML lateinit var tabSpacesSpinner: Spinner<Int>
    @FXML lateinit var visibilityTabsSpinner: Spinner<Int>
    @FXML lateinit var beginEndTabsSpinner: Spinner<Int>

    override fun finishLoad(project: Project): Node{
        val result = super.finishLoad(project)

        enumTypeCombo.items.addAll(Project.EnumType.values())

        addMediator(projectNameEdit, "projectName")
        addMediator(outputPathEdit, "outputdir")
        addMediator(enumTypeCombo, "enumType")
        addMediator(includesList, "fileName", project.includes)
        addMediator(tabSpacesSpinner, "tabSpaces")
        addMediator(visibilityTabsSpinner, "visibilityTabs")
        addMediator(beginEndTabsSpinner, "beginEndTabs")

        val addItem = MenuItem("Add include")
        val delItem = MenuItem("Delete include")
        delItem.isDisable = true


        includesList.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            delItem.isDisable = newValue == null
            if (newValue != null) {
                val name = newValue.text
                delItem.text = "Delete include \"$name\""
            }
            else
                delItem.text = "Delete include"
        }

        includesList.contextMenu = ContextMenu(addItem, delItem)
        includesList.contextMenu.onAction = EventHandler { event ->
            when (event.target) {
                addItem -> {
                    val textDialog = TextInputDialog("")
                    textDialog.title = "Adding new include"
                    textDialog.headerText = "Enter an include name"
                    val result = textDialog.showAndWait()
                    if (result.isPresent && result.get().trim() != ""){
                        val include = Project.Include()
                        include.fileName = result.get().trim()
                        project.includes.add(include)
                    }

                }
                delItem -> {
                    val selectedItem = includesList.selectionModel.selectedItem
                    if (selectedItem != null) {
                        val item = selectedItem.itemMediator.model
                        if (item != null && item is Project.Include)
                            project.includes.remove(item)
                    }
                }
            }

        }

        return result
    }
}