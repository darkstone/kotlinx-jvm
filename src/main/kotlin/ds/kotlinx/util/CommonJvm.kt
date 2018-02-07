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
inline fun <reified T> nullOf():T? = null