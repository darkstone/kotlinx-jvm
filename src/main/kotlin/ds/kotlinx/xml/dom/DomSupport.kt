package ds.kotlinx.xml.dom

import ds.kotlinx.util.*
import org.w3c.dom.Document
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.w3c.dom.Node as DomNode


sealed class DOMToXmlStringStyle {

    object Compact : DOMToXmlStringStyle() {

        private val trimWhitespace: InputStreamProvider by classpathResourceAt<DOMToXmlStringStyle>("TrimWhitespace.xsl")

        override fun transformer(): Transformer = TransformerFactory.newInstance().newTransformer(StreamSource(trimWhitespace()))
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

fun <T : DomNode> Writer.write(node: T, xmlToStringStyle: DOMToXmlStringStyle) {
    xmlToStringStyle.transformer().transform(DOMSource(node), StreamResult(this))
}

fun <T : DomNode> T.toXml(styleDOMTo: DOMToXmlStringStyle): String = StringWriter()
        .apply { write(this@toXml, styleDOMTo) }
        .toString()

interface FriendlyToXmlString {
    fun toXmlString(styleDOMTo: DOMToXmlStringStyle): String
}

open class DocumentWithFriendlyToString(private val doc: Document) : Document by doc, FriendlyToXmlString {

    override fun toXmlString(styleDOMTo: DOMToXmlStringStyle): String = doc.toXml(styleDOMTo)

    override fun toString(): String = toXmlString(DOMToXmlStringStyle.HumanFriendly)
}

fun Document.toHumanFriendly(): DocumentWithFriendlyToString {
    return when (this) {
        is DocumentWithFriendlyToString -> this
        else -> let(::DocumentWithFriendlyToString)
    }
}


typealias DocumentBuilderConfigurationBlock = DocumentBuilder.() -> Unit
typealias DocumentBuilderFactoryConfigurationBlock = DocumentBuilderFactory.() -> Unit

private val defaultBuilderFactory by lazy {
    DocumentBuilderFactory.newInstance().apply {
        isValidating = false
        isNamespaceAware = true
    }
}

private val defaultDocumentBuilder by lazy { defaultBuilderFactory.newDocumentBuilder() }

fun documentBuilderFactory(config: DocumentBuilderFactoryConfigurationBlock? = null): DocumentBuilderFactory {
    return when (config) {
        null -> defaultBuilderFactory
        else -> DocumentBuilderFactory.newInstance().apply { config() }
    }
}

fun documentBuilder(factory: DocumentBuilderFactory? = null, config: DocumentBuilderConfigurationBlock? = null): DocumentBuilder {
    return when {
        factory == null && config == null -> defaultDocumentBuilder
        factory == null -> defaultBuilderFactory.newDocumentBuilder().apply { if (config != null) config() }
        else -> factory.newDocumentBuilder().apply { if (config != null) config() }
    }
}


