import javaFXMediators.*
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import javafx.stage.Stage
import tiOPF.Mapper.MapperXML
import tiOPF.Mapper.Project
import tiOPF.Mapper.Project.Unit
import tiOPF.Mapper.Project.Unit.ClassItem.Prop
import tiOPF.Mapper.loadFromFile
import tiOPF.Object
import tiOPF.ObjectList
import tiOPF.getObjectProperty
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths

typealias ProjectUnitEnum = Unit.Enum
typealias ProjectUnitClassItem = Unit.ClassItem
typealias ClassItemSelectionFunction = Unit.ClassItem.SelectionFunction

private fun addChildNodeMediator(parent: TreeItem<MediatedItem>?, itemPropName: String, thisNodeName: String, subject: Object, obj: MainWindowController.NonItemObject? = null, activateMediator: Boolean = true): TreeItem<MediatedItem>{
    val newItem: TreeItem<MediatedItem> = TreeItem(MediatedItem())
    newItem.value.text = thisNodeName
    val mediator = TreeViewNodeMediatorView(newItem, subject, obj)
    mediator.fieldsInfo!!.addFieldInfo(itemPropName, thisNodeName, -1)


    parent?.children?.add(newItem)
    obj?.listen(newItem)

    mediator.subject = subject
    mediator.active = activateMediator

    return newItem
}

var Pane.content: Node? get() { return this.children.first() } set(value) {
    if (value == null)
        children.clear()
    else {
        if (children.count() == 0)
            children.add(value)
        else
            children[0] = value
    }
}



fun <T: Object>ObjectList<T>.addUniqueItem(nameBase: String, propName: String, constructor: () -> T): T{
    var newItemName: String = nameBase
    var existing = this.find { getObjectProperty<String>(it, propName) == newItemName } != null
    if (existing) {
        for (i in 1..Int.MAX_VALUE) {
            existing = this.find { getObjectProperty<String>(it, propName) == "$newItemName$i" } != null
            if (!existing) {
                newItemName += i.toString()
                break
            }

        }
    }
    val item = constructor.invoke()
    item.setPropValue(propName, newItemName)
    this.add(item)
    return item

}

class MainWindowController(private val primaryStage: Stage): Object() {
    init {
        primaryStage.onCloseRequest = EventHandler {
            if (modified && !trySave())
                it.consume()
        }
    }
    var project: Project? = null
    var projectFile: File? = null
    var modified = false
        get() {
            if (activePane != null)
                return activePane!!.modified || field
            return field
        }
        set(value) {
            field = value
            if (activePane != null && !value)
                activePane!!.modified = value
            updateState()
        }
    @FXML
    private lateinit var menuFileNew: MenuItem
    @FXML
    private lateinit var menuFileOpen: MenuItem
    @FXML
    private lateinit var menuRecent: Menu
    @FXML
    private lateinit var menuFileSave: MenuItem
    @FXML
    private lateinit var menuFileSaveAs: MenuItem
    @FXML
    private lateinit var menuFileClose: MenuItem
    @FXML
    private lateinit var menuFileExit: MenuItem
    @FXML
    private lateinit var unitsTreeView: CustomFXTreeView
    @FXML
    private lateinit var editPane: Pane

    @FXML
    private fun menuFileOpenClick(e: Event){
        val dialog = FileChooser()
        dialog.title = "Open schema files"
        dialog.extensionFilters.add(FileChooser.ExtensionFilter("schema files", mutableListOf("*.xml")))
        dialog.extensionFilters.add(FileChooser.ExtensionFilter("all files", mutableListOf("*")))
        dialog.initialDirectory = File(lastFolder)
        projectFile = dialog.showOpenDialog(primaryStage)
        if (projectFile != null)
            loadFile(projectFile!!)

        updateState()
    }
    fun updateRecent(){
        if (projectFile != null)
        Preferences.addRecent(projectFile!!.absolutePath)

        menuRecent.items.clear()
        Preferences.recentFiles.forEach {fileName ->
            val file = File(fileName)
            val p = Project()
            p.loadFromFile(file)
            val item = MenuItem("${p.projectName} -> ${file}")
            item.onAction = EventHandler{
                if (!modified || (modified && trySave())){
                    loadFile(File(fileName))
                }
            }
            item.isDisable = (projectFile != null && projectFile!!.absolutePath == fileName)
            menuRecent.items.add(item)
        }

        menuRecent.isDisable = menuRecent.items.count() == 0

    }

    @ExperimentalStdlibApi
    @FXML
    private fun menuFileExitClick(e: Event){
        if (modified && !trySave())
            return

        //val byteArray = ByteArrayOutputStream()
        //MapperXML(project!!).write(byteArray)
        //println(byteArray.toByteArray().decodeToString())
        primaryStage.close()
    }

    @FXML
    private fun menuFileCloseClick(e: Event){
        checkCloseProject()
        updateState()
        updateRecent()

    }

    override fun update(subject: Object) {
        modified = true
    }

    @FXML
    private fun menuFileSaveClick(e: Event){
        doSave()
        updateState()
    }

    @FXML
    private fun menuFileSaveAsClick(e: Event){
        doSave(true)
        updateState()
    }

    @FXML
    private fun menuFileNewClick(e: Event){
        if (checkCloseProject()) {
            val newProject = Project()
            newProject.projectName = "untitled"
            loadProject(newProject)
            modified = true
        }
        updateState()
    }


    private fun checkCloseProject(): Boolean{

        if (!modified || (modified && trySave())) {
            setProjectClosed()
            return true
        }
        return false
    }

    private fun setProjectClosed(){
        project = null
        projectFile = null
        modified = false
        activePane = null
        if (unitsTreeView.root != null) {
            unitsTreeView.root.value.itemMediator.active = false
            unitsTreeView.root.value.itemMediator.model = null
            unitsTreeView.root = null
        }
        updateState()
    }

    private fun trySave(): Boolean {
        if (project == null || !modified)
            return true

        var contentText = "Project '${project!!.projectName}' is modified.\nYour changes will be lost! Save?"

        val alert = Alert(Alert.AlertType.WARNING, contentText, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        alert.showAndWait()

        when (alert.result) {
            ButtonType.YES -> return doSave()
            ButtonType.NO -> {}
            else -> return false
        }

        return true

    }

    private fun doSave(saveAs: Boolean = false): Boolean{
        val doSaveAs = saveAs || projectFile == null
        var saveFile: File? = projectFile
        if (doSaveAs){
            val dialog = FileChooser()
            dialog.title = "Save Schema"
            dialog.extensionFilters.add(FileChooser.ExtensionFilter("schema files", mutableListOf("*.xml")))
            dialog.extensionFilters.add(FileChooser.ExtensionFilter("all files", mutableListOf("*")))
            dialog.initialDirectory = File(lastFolder)
            if (projectFile != null) {
                dialog.initialFileName = projectFile!!.absolutePath
            }
            else
                dialog.initialFileName = "schema.xml"

            saveFile = dialog.showSaveDialog(primaryStage)

            if (saveFile == null)
                return false

            lastFolder = saveFile.parent
        }

        MapperXML(project!!).write(FileOutputStream(saveFile))
        projectFile = saveFile
        modified = false
        updateRecent()
        return true
    }



    private fun updateState(){
        val noFile = project == null
        println("menu items: $modified. No file? $noFile")

        menuFileClose.isDisable = noFile
        menuFileSave.isDisable = noFile || (!noFile && !modified)
        menuFileSaveAs.isDisable = noFile
        unitsTreeView.isDisable = noFile
    }

    private var activePane: BasePaneController? = null ; set(value) {
        if (field != value && field != null) {
            field!!.stopMediators()
            stopObserving(field!!)
        }
        if (value != null) {
            val child = value.finishLoad(project!!) as Pane
            editPane.content = child
            child.prefWidthProperty().bind(editPane.widthProperty())
            child.prefHeightProperty().bind(editPane.heightProperty())
            println("pane loaded: ${value.modified}")
            value.modified = false
            value.attachObserver(this)
        }
        else {
            editPane.content = null
        }

        if (field != null){
            modified = modified || field!!.modified
        }

        field = value

    }
    private val baseTitle = "tiOPF Mapper Schema Editor"

    fun loadProject(value: Project){
        project = value
        primaryStage.title = baseTitle+" - ${project!!.projectName}"

        unitsTreeView.isEditable = true

        unitsTreeView.root = addChildNodeMediator(null, "name", "Project/Units", project!!.projectUnits, ProjectObject(project!!))
        unitsTreeView.root.isExpanded = true
        unitsTreeView.selectionModel.select(unitsTreeView.root)

        modified = false

        updateState()
        updateRecent()
    }

    var lastFolder = Paths.get("").toAbsolutePath().toString()

    fun loadFile(file: File){
        val newProject = Project()
        newProject.loadFromFile(file)
        projectFile = file

        loadProject(newProject)

        lastFolder = file.parent

    }

    internal abstract class NonItemObject: Object() {
        abstract fun listen(treeItem: TreeItem<MediatedItem>)
        open fun updateContextMenu(menu: ContextMenu){
            menu.items.clear()
        }
    }

    private class PropertyObject(val classProps: ObjectList<Prop>): NonItemObject(){
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            // do nothing, there are no subnodes
        }

        override fun updateContextMenu(menu: ContextMenu) {
            super.updateContextMenu(menu)
            val addProperty = MenuItem("Add Property")
            addProperty.onAction = EventHandler {
                val property = classProps.addUniqueItem("New_Property", "name", ::Prop)
                property.type = "String"
            }
            menu.items.add(addProperty)

        }
    }

    private class MappingObject(val classItem: Unit.ClassItem): NonItemObject(){
        override fun listen(treeItem: TreeItem<MediatedItem>) {
           // do nothing
        }

    }

    private class ValidatorObject(val classItem: Unit.ClassItem): NonItemObject(){
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            // nothing to do
        }
    }

    private class SelectionObject(val classItem: Unit.ClassItem): NonItemObject(){
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            // do nothing
        }

        override fun updateContextMenu(menu: ContextMenu) {
            super.updateContextMenu(menu)
            val menuItem = MenuItem("Add Selection Function")
            menuItem.onAction = EventHandler {
                val item = classItem.selections.addUniqueItem("FindBy_New", "name", ::ClassItemSelectionFunction) as ClassItemSelectionFunction
                val s = '$' // workaround for $
                item.sql = """
                    SELECT 
                    $s{field_list}
                    FROM 
                    *your table name*
                    WHERE 
                    *field* = :*sql_param_name* 
                """.trimIndent()
            }
            menu.items.add(menuItem)
        }
    }

    private class EnumsObject(val unit: Unit): NonItemObject() {
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            // do nothing
        }
        override fun updateContextMenu(menu: ContextMenu) {
            super.updateContextMenu(menu)
            val addEnum = MenuItem("Add Enum")
            addEnum.onAction = EventHandler {
                val enum = unit.enums.addUniqueItem("New_Enum", "name", ::ProjectUnitEnum)
                enum.set = "true"
            }
            menu.items.add(addEnum)
        }
    }

    private class ClassesObject(val unit: Unit): NonItemObject() {
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            treeItem.children.addListener(ListChangeListener {
                while (it.next()) {
                    if (it.wasAdded() && !it.wasReplaced()) {
                        it.addedSubList.forEach { item ->
                            when (val model = item.value.itemMediator.model) {
                                is Unit.ClassItem -> {
                                    addChildNodeMediator(item, "name", "Properties", model.classProps, PropertyObject(model.classProps), false)
                                    addChildNodeMediator(item, "prop", "Mappings", model.mapping.mappings, MappingObject(model), false)
                                    addChildNodeMediator(item, "name", "Selections", model.selections, SelectionObject(model))
                                    addChildNodeMediator(item, "prop", "Validators", model.validators, ValidatorObject(model), false)
                                }
                            }

                        }

                    } ;break
                }


            })
        }

        override fun updateContextMenu(menu: ContextMenu) {
            super.updateContextMenu(menu)
            val addClass = MenuItem("Add Class")
            addClass.onAction = EventHandler {
                unit.classes.addUniqueItem("TBase_Untitled", "baseClass", ::ProjectUnitClassItem)
            }
            menu.items.add(addClass)

            // reference menu item?
        }
    }
    private class ProjectObject(val project: Project): NonItemObject() {
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            treeItem.children.addListener(ListChangeListener { c ->
                while (c.next()) {
                    if (c.wasAdded() && !c.wasReplaced()) {
                        c.addedSubList.forEach { item ->
                            when (val model = item.value.itemMediator.model) {
                                is Unit -> {
                                    addChildNodeMediator(item, "baseClass", "Classes", model.classes, ClassesObject(model)).isExpanded = true
                                    addChildNodeMediator(item, "name", "Enums", model.enums, EnumsObject(model), true).isExpanded = true
                                    addChildNodeMediator(item, "name", "References", model.references)

                                    item.isExpanded = true
                                }
                            }
                        }
                    }
                }
            })

        }



        override fun updateContextMenu(menu: ContextMenu) {
            super.updateContextMenu(menu)
            val addUnit = MenuItem("Add Unit")
            addUnit.onAction = EventHandler {
                project.projectUnits.addUniqueItem("new_unit", "name", ::Unit)
            }
            menu.items.add(addUnit)
        }
    }


    private fun addContextItem(text: String, action: EventHandler<ActionEvent>? = null){
        val item = MenuItem(text)
        item.onAction = action
        contextMenu.items.add(item)
    }

    private val contextMenu = ContextMenu()

    fun finishLoad(){
        unitsTreeView.isShowRoot = true
        unitsTreeView.selectionModel.selectedItems.addListener(ListChangeListener {
            while (it.next()) {
                val list = it.addedSubList
                if (list != null){
                    if (it.wasAdded()) {
                        val item = list.first()
                        if (item != null) {

                            val selected = item.value.itemMediator.model
                            if (selected is NonItemObject)
                                selected.updateContextMenu(contextMenu)
                            else if (selected is Object){
                                contextMenu.items.clear()
                            }
                            println("selected ${selected?.toString()}")
                            if (selected != null) {
                                when (selected) {
                                    is Unit -> {
                                        println("selected unit ${selected.name}")
                                        activePane = UnitPaneController(selected, "unit_fragment.fxml")
                                        addContextItem("Delete Unit \"${selected.name}\"", EventHandler{
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<*>).remove(selected)

                                        })
                                    }


                                    is Unit.ClassItem -> {
                                        println("selected class Item ${selected.baseClass}")
                                        activePane = ClassPaneController(selected, "class_fragment.fxml")
                                        addContextItem("Delete Class \"${selected.baseClass}\"", EventHandler{
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<*>).remove(selected)

                                        })

                                    }

                                    is PropertyObject -> {
                                        activePane = PropertiesEditController(selected.classProps, "properties_fragment.fxml")
                                    }

                                    /*is Project.Unit.ClassItem.Prop -> {
                                        activePane = PropertyPaneController(selected, "property_fragment.fxml")
                                    }*/
                                    is MappingObject -> {
                                        activePane = MappingPaneController(selected.classItem, "mapping_fragment.fxml")
                                    }

                                    is Unit.ClassItem.Mapping.PropMap -> {
                                        activePane = MappingItemPaneController(selected, "mapping_item_fragment.fxml")
                                    }

                                    is Unit.Enum -> {
                                        activePane = EnumsPaneController(selected, "enums_fragment.fxml")
                                        addContextItem("Add enum value", EventHandler{
                                            val enum = Unit.Enum.EnumItem()
                                            enum.name = "_new_value"
                                            selected.values.add(enum)
                                        })
                                        addContextItem("Delete enum \"${selected.name}\"", EventHandler{
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<*>).remove(selected)

                                        })
                                    }

                                    is Unit.ClassItem.SelectionFunction -> {
                                        activePane = SelectionFunctionPaneController(selected, "selection_function_fragment.fxml")
                                        addContextItem("Delete function \"${selected.name}\"", EventHandler{
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<*>).remove(selected)

                                        })
                                    }
                                    is ValidatorObject -> {
                                        activePane = ValidatorsController(selected.classItem, "validators_fragment.fxml")
                                    }

                                    is ProjectObject -> {
                                        println("selected project ${project!!.projectName}")
                                        activePane = ProjectPaneController(project!!, "project_fragment.fxml")
                                    }
                                }
                            }

                        }
                    }
                }
            }
        })

        unitsTreeView.contextMenu = contextMenu




        primaryStage.title = baseTitle


    }
}