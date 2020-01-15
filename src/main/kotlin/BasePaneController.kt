import javaFXMediators.*
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import tiOPF.Mapper.Project
import tiOPF.Mediator.MediatorView
import tiOPF.Mediator.ObjectUpdateMoment
import tiOPF.Object
import tiOPF.ObjectList

abstract class BasePaneController(val subject: Object, private val resource: String) {
    protected val mediators = mutableListOf<MediatorView<*>>()
    open fun finishLoad(project: Project): Node {
        val loader = FXMLLoader(this::class.java.classLoader.getResource(resource))
        loader.setController(this)
        return loader.load()
    }

    open fun stopMediators(){
        //println("stopping mediators for $this")
        mediators.forEach {
            it.active = false
            it.subject = null
        }

        mediators.clear()
    }


    fun addMediator(edit: TextField, fieldName: String){
        val mediator = TextFieldMediatorView()
        mediator.view = edit
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }

    fun addMediator(choiceBox: ChoiceBox<String>, fieldName: String){
        val mediator = ChoiceBoxMediatorView<String>()
        mediator.view = choiceBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }

    fun addMediator(edit: TextArea, fieldName: String){
        val mediator = TextAreaMediatorView()
        mediator.view = edit
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }
    fun addMediator(listView: CustomFXListView, fieldName: String, subject: ObjectList<Project.Include>){
        val mediator = ListViewListMediatorView()
        mediator.view = listView
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }

    fun <T: Any>addMediator(comboBox: ComboBox<T>, fieldName: String){
        val mediator = ComboBoxMediatorView<T>()
        mediator.view = comboBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }

    fun addMediator(tableView: CustomFXTableView, subject: Object, fieldNames: Array<String>){
        val mediator = TableViewListMediatorView()
        mediator.view = tableView
        var field = ""
        fieldNames.forEach {
            field = if (field == "")
                it
            else
                "$field;$it"
        }
        mediator.fieldName = field
        mediator.subject = subject
        //mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }


    fun addMediator(spinner: Spinner<Int>, fieldName: String){
        val mediator = SpinnerMediatorViewInt()
        mediator.view = spinner
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }

    fun addMediator(checkBox: CheckBox, fieldName: String){
        val mediator = CheckBoxMediatorView()
        mediator.view = checkBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
    }
}