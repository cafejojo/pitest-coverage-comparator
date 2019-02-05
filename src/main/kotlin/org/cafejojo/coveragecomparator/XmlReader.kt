package org.cafejojo.coveragecomparator

import org.w3c.dom.DOMException
import org.w3c.dom.DOMException.NOT_FOUND_ERR
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

fun main(args: Array<String>) {
    val mutationsA = readXml(File(args[0]))
    val mutationsB = readXml(File(args[1]))

    val coveredMutationsInBbutNotInA = mutationsB
        .filter { it.killed() }
        .filter { b -> mutationsA.none { a -> a.coversSameMutationTypeAndLocationAs(b) && a.detected } }

    println(
        """
        B kills ${coveredMutationsInBbutNotInA.count()} mutants that A does not cover.

        A killed ${mutationsA.filter { it.killed() }.count()} mutants in total
        B killed ${mutationsB.filter { it.killed() }.count()} mutants in total
        """.trimIndent()
    )
}

fun readXml(file: File) =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).apply { documentElement.normalize() }
        .getElementsByTagName("mutation")
        .mapElements { node ->
            Mutation(
                node.attribute("detected").toBoolean(),
                node.attribute("status"),
                Location(
                    node.element("sourceFile"),
                    node.element("mutatedClass"),
                    node.element("mutatedMethod"),
                    node.element("methodDescription"),
                    node.element("lineNumber").toInt(),
                    node.element("index").toInt(),
                    node.element("block").toInt()
                ),
                node.element("mutator")
            )
        }

fun <R : Any> NodeList.mapElements(mapFunction: (Element) -> R): List<R> =
    (0 until length)
        .filter { item(it).nodeType == Node.ELEMENT_NODE }
        .mapNotNull { (item(it) as? Element)?.let { node -> mapFunction(node) } }

fun Element.attribute(name: String) = this.attributes.getNamedItem(name).nodeValue
    ?: throw DOMException(NOT_FOUND_ERR, "Could not read attribute $name.")

fun Element.element(name: String) = this.getElementsByTagName(name).item(0).textContent
    ?: throw DOMException(NOT_FOUND_ERR, "Could not read element $name.")
