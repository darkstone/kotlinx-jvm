package ds.kotlinx.util

import org.junit.Test
import java.net.URL
import kotlin.test.assertTrue

class CommonJvmKtTest {

    @Test
    fun testNillFunction() {

        var correctOneCalled = false

        fun doSomething(a:String, b:String, url: URL?) {
            correctOneCalled = true
        }

        fun doSomething(a:String, b:String, url: String?) = Unit

        doSomething("1", "2", nullOf<URL>())

        assertTrue(correctOneCalled)

    }



}