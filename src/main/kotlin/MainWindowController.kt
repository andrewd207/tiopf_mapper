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
import tiOPF.Mapper.readFromXML
import tiOPF.Object
import tiOPF.ObjectList
import tiOPF.getObjectProperty
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.reflect.KFunction

typealias ProjectUnitEnum = Project.Unit.Enum
typealias ProjectUnitClassItem = Project.Unit.ClassItem

private fun addChildnodeMediator(parent: TreeItem<MediatedItem>?, itemPropName: String, thisNodeName: String, subject: Object, obj: MainWindowController.NonItemObject? = null, activateMediator: Boolean = true): TreeItem<MediatedItem>{
    var newItem: TreeItem<MediatedItem> = TreeItem(MediatedItem())
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
    if (children.count() == 0)
        children.add(value)
    else
        children[0] = value


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

class MainWindowController(val primaryStage: Stage) {
    public var project: Project? = null
    @FXML
    private lateinit var menuFileOpen: MenuItem
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
        val file = dialog.showOpenDialog(primaryStage)
        if (file != null)
            loadFile(file)
    }
    @ExperimentalStdlibApi
    @FXML
    private fun menuFileExitClick(e: Event){
        val byteArray = ByteArrayOutputStream()
        MapperXML(project!!).write(byteArray)
        println(byteArray.toByteArray().decodeToString())
        primaryStage.close()
    }

    private var activePane: BasePaneController? = null ; set(value) {
        if (field != value && field != null)
            field!!.stopMediators()
        if (value != null) {
            editPane.content = value.finishLoad(project!!)
        }
        field = value
    }
    private val baseTitle = "tiopf Mapper Schema Editor"
    fun loadFile(file: File){

        project = Project()
        project!!.readFromXML(file)
        primaryStage.title = baseTitle+" - ${project!!.projectName}"

        unitsTreeView.isEditable = true

        unitsTreeView.root = addChildnodeMediator(null, "name", "Project/Units", project!!.projectUnits, ProjectObject(project!!))
        unitsTreeView.root.isExpanded = true
        unitsTreeView.selectionModel.select(unitsTreeView.root)
    }

    internal abstract class NonItemObject: Object() {
        abstract fun listen(treeItem: TreeItem<MediatedItem>)
        open fun updateContextMenu(menu: ContextMenu){
            menu.items.clear()
        }
    }

    private class PropertyObject(val classProps: ObjectList<Project.Unit.ClassItem.Prop>): NonItemObject(){
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

    private class MappingObject(val classItem: Project.Unit.ClassItem): NonItemObject(){
        override fun listen(treeItem: TreeItem<MediatedItem>) {
           // do nothing
        }

    }

    private class EnumsObject(val unit: Project.Unit): NonItemObject() {
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

    private class ClassesObject(val unit: Project.Unit): NonItemObject() {
        override fun listen(treeItem: TreeItem<MediatedItem>) {
            treeItem.children.addListener(ListChangeListener {
                while (it.next()) {
                    if (it.wasAdded() && !it.wasReplaced()) {
                        it.addedSubList.forEach { item ->
                            val model = item.value.itemMediator.model
                            when (model) {
                                is Project.Unit.ClassItem -> {
                                    addChildnodeMediator(item, "name", "Properties", model.classProps, PropertyObject(model.classProps), false)
                                    addChildnodeMediator(item, "prop", "Mappings", model.mapping.mappings, MappingObject(model), false)
                                    addChildnodeMediator(item, "name", "Selections", model.selections)
                                    addChildnodeMediator(item, "prop", "Validators", model.validators)
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
                            val model = item.value.itemMediator.model
                            when (model) {
                                is Project.Unit -> {
                                    addChildnodeMediator(item, "baseClass", "Classes", model.classes, ClassesObject(model)).isExpanded = true
                                    addChildnodeMediator(item, "name", "Enums", model.enums, EnumsObject(model), true).isExpanded = true
                                    addChildnodeMediator(item, "name", "References", model.references)

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
                                    is Project.Unit -> {
                                        println("selected unit ${selected.name}")
                                        activePane = UnitPaneController(selected, "unit_fragment.fxml")
                                        addContextItem("Delete Unit \"${selected.name}\"", EventHandler{ action ->
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<Project.Unit>).remove(selected)

                                        })
                                    }


                                    is Project.Unit.ClassItem -> {
                                        println("selected class Item ${selected.baseClass}")
                                        activePane = ClassPaneController(selected, "class_fragment.fxml")
                                        addContextItem("Delete Class \"${selected.baseClass}\"", EventHandler{ action ->
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<Project.Unit.ClassItem>).remove(selected)

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

                                    is Project.Unit.ClassItem.Mapping.PropMap -> {
                                        activePane = MappingItemPaneController(selected, "mapping_item_fragment.fxml")
                                    }

                                    is Project.Unit.Enum -> {
                                        activePane = EnumsPaneController(selected, "enums_fragment.fxml")
                                        addContextItem("Add enum value", EventHandler{ action ->
                                            val enum = Project.Unit.Enum.EnumItem()
                                            enum.name = "_new_value"
                                            selected.values.add(enum)
                                        })
                                        addContextItem("Delete enum \"${selected.name}\"", EventHandler{ action ->
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<Project.Unit.Enum>).remove(selected)

                                        })
                                    }

                                    is Project.Unit.ClassItem.SelectionFunction -> {
                                        activePane = SelectionFunctionPaneController(selected, "selection_function_fragment.fxml")
                                        addContextItem("Delete function \"${selected.name}\"", EventHandler{ action ->
                                            if (selected.owner is ObjectList<*>)
                                                (selected.owner!! as ObjectList<Project.Unit.ClassItem.SelectionFunction>).remove(selected)

                                        })
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