package ds.kotlinx.util

/**
 * A function intended to replace `null` to help the compiler resolve umbiquity when the use of
 * `null` resolves to more than one method invocation.
 *
 * Some examples:
 *
 * ```java
 * void doSomething(String a, String b, URL url)...
 * void doSomething(String a, String b, String url)..
 *
 * // Compiler error:
 * doSomething("1","2",null)
 *
 * // Resolve in java:
 * doSomething("1","2",(URL)null)
 * ```
 *
 * Resolving from kotlin:
 * - Without extension: `doSomething("1","2",null as URL?)`
 * - With extension: `doSomething("1","2",<URL>nullOf())`
 */
inline fun <reified T> nullOf(): T? = null

/**
 * Function to translate boolean to either one of the values. Used to when a function,
 * or an API, expect a string "yes", or "no", but you still want to keep a simple boolean value
 * around.
 *
 * # Samples
 *
 * ```kotlin
 * factory.setParameter("omit-xml-declaration", omit.toTruthOf("yes","no"))
 *
 */
fun <T> Boolean.toTruthOf(truth: T, notTrue: T): T = if (this) truth else notTrue
