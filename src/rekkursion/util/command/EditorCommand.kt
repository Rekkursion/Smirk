package rekkursion.util.command

import rekkursion.model.EditorModel

interface EditorCommand: Command {
    val mEditor: EditorModel
}