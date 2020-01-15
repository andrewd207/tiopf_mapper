import javaFXMediators.*
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

        return result
    }
}