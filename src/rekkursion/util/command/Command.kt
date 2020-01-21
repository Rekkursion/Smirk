package rekkursion.util.command

interface Command {
    // execute the operation
    fun execute(vararg args: Any?)
}