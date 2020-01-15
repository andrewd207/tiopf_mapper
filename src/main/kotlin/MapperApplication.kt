import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import tiOPF.Mapper.Project
import java.io.File

fun main(args: Array<String>?) {
    MapperApplication().execute(args)

}
class MapperApplication: Application() {

    private lateinit var mainWindowController: MainWindowController
    fun execute(args: Array<String>?){
        launch("")
    }



    override fun start(primaryStage: Stage?) {
        if (primaryStage == null)
            return

        mainWindowController = MainWindowController(primaryStage)

        val loader = FXMLLoader(this::class.java.classLoader.getResource("mapper_layout.fxml"))
        loader.setController(mainWindowController)
        primaryStage.scene = Scene(loader.load())

        mainWindowController.finishLoad()
        mainWindowController.loadFile(File("/home/andrew/programming/GroupProjects/tiopf_apps/tiOPFMapper/demos/schema/sample.xml"))
        val unit = Project.Unit()
        unit.name = "woohoo"
        //mainWindowController.project!!.projectUnits.beginUpdate()
        //mainWindowController.project!!.projectUnits.add(unit)
        //mainWindowController.project!!.projectUnits.endUpdate()
        primaryStage.show()





    }
}