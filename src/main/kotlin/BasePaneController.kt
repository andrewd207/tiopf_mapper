import javaFXMediators.*
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import tiOPF.Mapper.Project
import tiOPF.Mediator.MediatorView
import tiOPF.Mediator.ObjectUpdateMoment
import tiOPF.Object
import tiOPF.ObjectList

abstract class BasePaneController(val subject: Object, private val resource: String): Object() {
    var modified = false
    protected val mediators = mutableListOf<MediatorView<*>>()
    open fun finishLoad(project: Project): Node {
        val loader = FXMLLoader(this::class.java.classLoader.getResource(resource))
        loader.setController(this)
        return loader.load()
    }

    override fun update(subject: Object) {
        if (!modified) {
            modified = true
            notifyObservers()
        }
    }

    override fun update(subject: Object, operation: NotifyOperation, data: Object?) {
        if (!modified) {
            modified = true
            notifyObservers()
        }
    }

    open fun stopMediators(){
        //println("stopping mediators for $this")
        mediators.forEach {
            if (it.subject != null)
                stopObserving(it.subject!!)
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
        subject.attachObserver(this)
    }

    fun addMediator(choiceBox: ChoiceBox<String>, fieldName: String){
        val mediator = ChoiceBoxMediatorView<String>()
        mediator.view = choiceBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
        subject.attachObserver(this)
    }

    fun addMediator(edit: TextArea, fieldName: String){
        val mediator = TextAreaMediatorView()
        mediator.view = edit
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
        subject.attachObserver(this)
    }
    fun addMediator(listView: CustomFXListView, fieldName: String, subject: ObjectList<*>){
        val mediator = ListViewListMediatorView()
        mediator.view = listView
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
        subject.attachObserver(this)
    }

    fun <T: Any>addMediator(comboBox: ComboBox<T>, fieldName: String){
        val mediator = ComboBoxMediatorView<T>()
        mediator.view = comboBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
        subject.attachObserver(this)
    }

    fun addMediator(tableView: CustomFXTableView, subject: ObjectList<*>, fieldNames: Array<String>){
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
        subject.attachObserver(this)
    }


    fun addMediator(spinner: Spinner<Int>, fieldName: String){
        val mediator = SpinnerMediatorViewInt()
        mediator.setup(spinner, subject, fieldName)
        mediators.add(mediator)
        subject.attachObserver(this)
    }

    fun addMediator(checkBox: CheckBox, fieldName: String){
        val mediator = CheckBoxMediatorView()
        mediator.view = checkBox
        mediator.fieldName = fieldName
        mediator.subject = subject
        mediator.objectUpdateMoment = ObjectUpdateMoment.OnExit
        mediators.add(mediator)
        subject.attachObserver(this)
    }
}