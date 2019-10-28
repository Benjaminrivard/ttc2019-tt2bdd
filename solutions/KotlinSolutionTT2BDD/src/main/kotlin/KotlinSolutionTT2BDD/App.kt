/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package KotlinSolutionTT2BDD



class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

fun main(args: Array<String>) {
 
	val Model : String = System.getenv("Model");
	val ModelPath: String = System.getenv("ModelPath");
	val RunIndex  : String = System.getenv("RunIndex");
	val Tool : String = System.getenv("Tool");
    println(String.format("%s;%s;%s;%s;Time;%s", Tool, Model, RunIndex, "load", 0))

}
