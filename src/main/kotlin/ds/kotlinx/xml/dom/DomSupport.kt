package ds.kotlinx.xml.dom

import ds.kotlinx.util.toTruthOf
import org.intellij.lang.annotations.Language
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.w3c.dom.Node as DomNode


sealed class DOMToXmlStringStyle {

    object Compact : DOMToXmlStringStyle() {
        @Language("XML")
        private val xsl = """|<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version="1.0">
                             |  <xsl:output method='xml' omit-xml-declaration='yes' indent='no'/>
                             |  <xsl:strip-space elements="*"/>
                             |  <xsl:template match='@*|node()'>
                             |      <xsl:copy>
                             |          <xsl:apply-templates select='@*|node()'/>
                             |      </xsl:copy>
                             |  </xsl:template>
                             |</xsl:stylesheet>
                             |""".trimMargin()

        override fun transformer(): Transformer = TransformerFactory.newInstance().newTransformer(StreamSource(StringReader(xsl)))
    }

    open class Pretty(val indent: Int, val omitXmlDeclaration: Boolean) : DOMToXmlStringStyle() {
        override fun transformer(): Transformer {
            return TransformerFactory.newInstance().apply {
                setAttribute(INDENT_WITH_ATTRIBUTE, indent)
            }.newTransformer().apply {
                setOutputProperty(OutputKeys.INDENT, YES)
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration.toTruthOf(YES, NO))
            }
        }
    }

    object HumanFriendly : Pretty(indent = DEFAULT_INDENT_WIDTH, omitXmlDeclaration = true)


    internal abstract fun transformer(): Transformer

    companion object {
        private const val YES = "yes"
        private const val NO = "no"
        private const val INDENT_WITH_ATTRIBUTE = "indent-number"
        private const val DEFAULT_INDENT_WIDTH = 3
    }


}

fun <T : DomNode> OutputStream.write(node: T, styleDOMTo: DOMToXmlStringStyle) {
    styleDOMTo.transformer().transform(DOMSource(node), StreamResult(this))
}

fun <T : DomNode> Writer.write(node: T, styleDOMTo: DOMToXmlStringStyle) {
    styleDOMTo.transformer().transform(DOMSource(node), StreamResult(this))
}

fun <T : DomNode> T.toXml(styleDOMTo: DOMToXmlStringStyle): String = StringWriter()
        .apply { write(this@toXml, styleDOMTo) }
        .toString()

interface FriendlyToXmlString {
    fun toXmlString(styleDOMTo: DOMToXmlStringStyle): String
}

open class DocumentWithFriendlyToString(private val doc: Document) : Document by doc, FriendlyToXmlString {

    override fun toXmlString(styleDOMTo: DOMToXmlStringStyle): String = with(StringWriter()) {
        write(doc, styleDOMTo)
        toString()
    }

    override fun toString(): String = toXmlString(DOMToXmlStringStyle.HumanFriendly)
}

interface DomConfiguration {

    fun configureFactory(configure: DocumentBuilderFactory.() -> Unit)

    fun configureBuilder(configure: DocumentBuilder.() -> Unit)

}

typealias DocumentBuilderConfigurationBlock = (DomConfiguration.() -> Unit)

fun newDocumentBuilder(configure: DocumentBuilderConfigurationBlock? = null): DocumentBuilder {

    var configureFactory: (DocumentBuilderFactory.() -> Unit)? = null
    var configureBuilder: (DocumentBuilder.() -> Unit)? = null

    object : DomConfiguration {

        override fun configureFactory(configure: DocumentBuilderFactory.() -> Unit) {
            configureFactory = configure
        }

        override fun configureBuilder(configure: DocumentBuilder.() -> Unit) {
            configureBuilder = configure
        }


        init {
            if (configure != null) {
                configure()
            }
        }
    }

    return DocumentBuilderFactory
            .newInstance().apply { configureFactory?.let { it() } }
            .newDocumentBuilder().apply { configureBuilder?.let { it() } }
}

fun Document.toDocumentWithFriendlyToString(): DocumentWithFriendlyToString = this as? DocumentWithFriendlyToString
        ?: DocumentWithFriendlyToString(this)

fun InputSource.readDocument(configuration: DocumentBuilderConfigurationBlock? = null): DocumentWithFriendlyToString {
    return newDocumentBuilder(configuration).parse(this).toDocumentWithFriendlyToString()
}

fun InputStream.readDocument(configuration: DocumentBuilderConfigurationBlock? = null): DocumentWithFriendlyToString {
    return InputSource(this).readDocument(configuration)
}

fun File.readDocument(configuration: DocumentBuilderConfigurationBlock? = null): DocumentWithFriendlyToString {
    return inputStream().use { InputSource(it).readDocument(configuration) }
}

fun Reader.readDocument(configuration: DocumentBuilderConfigurationBlock? = null): DocumentWithFriendlyToString {
    return InputSource(this).readDocument(configuration)
}

fun String.toDocument(configuration: DocumentBuilderConfigurationBlock? = null): DocumentWithFriendlyToString {
    return InputSource(StringReader(this)).readDocument(configuration)
}

