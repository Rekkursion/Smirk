package rekkursion.manager

import rekkursion.util.tool.ShortcutCommand
import java.lang.Exception
import java.lang.reflect.Modifier
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

class ShortcutManager(filename: String) {
    // the filename corresponding functions located
    private val mFilename: String = filename

    // the hash-map for storing shortcuts and their corresponding operations (function names)
    private val mShortcutMap = HashMap<ShortcutCommand, String>()

    /* ===================================================================== */

    // add or modify a shortcut's operation
    fun addOrModifyShortcut(isCtrlPressed: Boolean, isShiftPressed: Boolean, isAltPressed: Boolean, primaryKeyCode: Int, funName: String): ShortcutManager {
        val cmd = ShortcutCommand(isCtrlPressed, isShiftPressed, isAltPressed, primaryKeyCode)
        mShortcutMap[cmd] = funName
        return this
    }

    // region add or modify a shortcut's operation
    fun addOrModifyShortcut(modifierStr: String, primaryKeyCode: Int, funName: String): ShortcutManager =
            addOrModifyShortcut(
                    modifierStr[0] == '1',
                    modifierStr[1] == '1',
                    modifierStr[2] == '1',
                    primaryKeyCode,
                    funName
            )
    // endregion

    // get the corresponding operating function by a shortcut
    fun getOperationByShortcut(isCtrlPressed: Boolean, isShiftPressed: Boolean, isAltPressed: Boolean, primaryKeyCode: Int): KFunction<*>? {
        val cmd = ShortcutCommand(isCtrlPressed, isShiftPressed, isAltPressed, primaryKeyCode)
        val funName = mShortcutMap[cmd] ?: return null

        return getOperationByFunctionName(funName)
    }

    // get the corresponding operating function by a function name
    private fun getOperationByFunctionName(funName: String): KFunction<*>? {
        try {
            val selfRef = ::getOperationByFunctionName
            val currentClass = selfRef.javaMethod!!.declaringClass
            val classDefiningFunctions = currentClass.classLoader.loadClass("${mFilename}Kt")
            val javaMethod = classDefiningFunctions.methods.find { it.name == funName && Modifier.isStatic(it.modifiers) }
            return javaMethod?.kotlinFunction
        } catch (e: Exception) {}
        return null
    }
}