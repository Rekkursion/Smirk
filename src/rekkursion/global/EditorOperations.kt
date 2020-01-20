package rekkursion.global

import rekkursion.view.control.editor.CodeCanvas

// TODO: some editor operations

// copy the selected text of the passed code-canvas
fun copySelectedText(codeCanvas: CodeCanvas) { codeCanvas.copySelectedText() }

// cut the selected text of the passed code-canvas
fun cutSelectedText(codeCanvas: CodeCanvas) { codeCanvas.cutSelectedText() }

// paste the copied/cut text on the passed code-canvas
fun pasteSelectedText(codeCanvas: CodeCanvas) { codeCanvas.pasteSelectedText() }

// select all text
fun selectAllText(codeCanvas: CodeCanvas) { codeCanvas.selectAllText() }

// jump to the designated line
fun jumpToDesignatedLine(codeCanvas: CodeCanvas) { codeCanvas.jumpToDesignatedLine() }