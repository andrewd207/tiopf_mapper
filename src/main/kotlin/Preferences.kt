import tiOPF.ObjectList
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.prefs.Preferences

typealias JavaPrefs = java.util.prefs.Preferences

class Preferences {
    companion object {
        private var loaded = false
        val recentFiles = mutableListOf<String>()
        var maxRecent = 5
            set(value) {
                field = value
                save()
            }
        var makeBackups = true
            set(value) {
                field = value
                save()
            }

        init {
            load()
            loaded = true
        }

        fun addRecent(name: String) {
            val index = recentFiles.indexOf(name)
            if (index > -1)
                recentFiles.removeAt(index)
            recentFiles.add(0, name)

            recentFiles.removeIf(Predicate {
                recentFiles.indexOf(it) > maxRecent
            })
            save()
        }

        fun load() {
            recentFiles.clear()
            val prefs = JavaPrefs.userRoot().node(this.javaClass.name)
            maxRecent = prefs.getInt("maxRecent", 5)
            makeBackups = prefs.getBoolean("makeBackups", true)
            val files = String(prefs.getByteArray("recentFiles", ByteArray(0)))
            if (files != "")
            recentFiles.addAll(files.split(';'))

        }

        fun save() {
            if (!loaded)
                return
            val prefs = JavaPrefs.userRoot().node(this.javaClass.name)
            prefs.putInt("maxRecent", maxRecent)
            prefs.putBoolean("makeBackups", makeBackups)
            var recent = ""
            recentFiles.forEach {
                if (recent == "")
                    recent += it
                else recent += ";$it"
            }
            prefs.putByteArray("recentFiles", recent.toByteArray())

            prefs.flush()

        }
    }



}