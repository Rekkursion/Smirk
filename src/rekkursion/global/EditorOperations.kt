package rekkursion.global

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import rekkursion.view.CodeCanvas

// TODO: some editor operations

// copy the selected text of the passed code-canvas
fun copySelectedText(codeCanvas: CodeCanvas) { codeCanvas.copySelectedText() }

// cut the selected text of the passed code-canvas
fun cutSelectedText(codeCanvas: CodeCanvas) { codeCanvas.cutSelectedText() }

// paste the copied/cut text on the passed code-canvas
fun pasteSelectedText(codeCanvas: CodeCanvas) { codeCanvas.pasteSelectedText() }