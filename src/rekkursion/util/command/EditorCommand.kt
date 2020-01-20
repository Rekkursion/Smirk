package rekkursion.util.command

import rekkursion.view.control.editor.CodeCanvas

interface EditorCommand: Command {
    val mCodeCanvas: CodeCanvas
}