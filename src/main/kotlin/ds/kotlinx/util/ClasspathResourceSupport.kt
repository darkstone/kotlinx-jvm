@file:Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")

package ds.kotlinx.util

import java.io.IOException
import java.io.InputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias InputStreamTransformationFunction<T> = (InputStream) -> T
typealias InputStreamProvider = () -> InputStream

class ResourceNotFoundException(val root: Class<*>, val path: String)
    : IOException("Failed to load resource from at [${root.name}] from path [$path]")


inline fun <reified T> resourceStreamOf(path: String): InputStreamProvider {
    val url = T::class.java.getResource(path) ?: throw ResourceNotFoundException(T::class.java, path)
    return url::openStream
}


inline fun <reified T> classpathResourceAt(resource: String): ReadOnlyProperty<Any?,InputStreamProvider> {
    return object : ReadOnlyProperty<Any?,InputStreamProvider> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): InputStreamProvider {
            val url = T::class.java.getResource(resource) ?: throw ResourceNotFoundException(T::class.java, resource)
            return url::openStream
        }
    }
}