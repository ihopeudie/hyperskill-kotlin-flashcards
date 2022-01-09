package flashcards

import flashcards.domain.Card
import java.io.File
import java.lang.IllegalStateException
import kotlin.random.Random
import kotlin.system.exitProcess

const val SPLITTER: String = ":::"
val cardList = mutableListOf<Card>()
val logList = mutableListOf<String>()
var fileToExportOnExit: String? = null

fun main(args: Array<String>) {

    processArgs(args)

    while (true) {
        printAndLog("Input the action (add, remove, import, export, ask, exit):")
        processCommand()
    }
}

fun processArgs(args: Array<String>) {
    if (args.isEmpty()) {
        return
    }
    val command = args[0]
    val fName = args[1]
    processArg(command, fName)

    if (args.size > 2) {
        val command2 = args[2]
        val fName2 = args[3]
        processArg(command2, fName2)
    }
}

fun processArg(command: String, fName: String) {
    if (command == "-import") {
        importFile(fName)
    }
    if (command == "-export") {
        fileToExportOnExit = fName
    }
}

fun printAndLog(record: Any, hasToBePrinted: Boolean = true) {
    if (hasToBePrinted) {
        println(record)
    }
    logList.add(record.toString())
}

private fun processCommand() {
    val command = readLine()!!
    printAndLog(command, false)
    when (command) {
        "add" -> {
            addCard()
        }
        "remove" -> {
            removeCard()
        }
        "import" -> {
            processImportCommand()
        }
        "export" -> {
            processExportCommand()
        }
        "ask" -> {
            ask()
        }
        "view" -> {
            printAndLog(cardList)
        }
        "exit" -> {
            printAndLog("Bye bye!")
            fileToExportOnExit?.let { exportFile(it) }
            exitProcess(0)
        }
        "log" -> {
            saveLog()
        }
        "hardest card" -> {
            printHardestCard()
        }
        "reset stats" -> {
            cardList.forEach { card -> card.errors = 0 }
            printAndLog("Card statistics have been reset.")
        }
        else -> {
            throw IllegalStateException("unknown command")
        }
    }
}

private fun printHardestCard() {
    var maxErrors: Int = if (cardList.size > 0) cardList.maxOf { it.errors } else -1
    if (maxErrors == 0) maxErrors = -1
    val errorCards = cardList.filter { it.errors == maxErrors }
    val message: String = when (errorCards.size) {
        0 -> "There are no cards with errors"
        1 -> {
            val hardestCard = errorCards[0]
            "The hardest card is \"${hardestCard.term}\". You have ${hardestCard.errors} errors answering it."
        }
        else -> {
            "The hardest cards are ${
                errorCards
                    .map { "\"${it.term}\"" }
                    .joinToString(", ")
            }. You have $maxErrors errors answering them."
        }
    }
    printAndLog(message)
}

fun saveLog() {
    printAndLog("File name:")
    val fileName = readLine()!!
    printAndLog(fileName, false)
    File(fileName).writeText(logList.joinToString("\n"))
    printAndLog("The log has been saved.")
}

fun processExportCommand() {
    printAndLog("File name:")
    val fName = readLine()!!
    printAndLog(fName, false)
    exportFile(fName)
}

private fun exportFile(fName: String) {
    val file = File(fName)
    file.writeText(cardList.joinToString("\n") { it.toString() })
    printAndLog("${cardList.size} cards have been saved}")
}

fun processImportCommand() {
    printAndLog("File name:")
    val fileName = readLine()!!
    printAndLog(fileName, false)
    importFile(fileName)
}

private fun importFile(fileName: String) {
    val file = File(fileName)
    if (!file.exists()) {
        printAndLog("File not found.")
        return
    }
    val list = mutableListOf<Card>()
    file.forEachLine { line ->
        val (term, definition, errors) = line.split(SPLITTER)
        list.add(Card(term, definition, errors.toInt()))
    }
    list.forEach { card ->
        cardList.removeIf { it.term == card.term }
    }
    cardList.addAll(list)
    printAndLog("${list.size} cards have been loaded.")
}

fun removeCard() {
    printAndLog("Which card?")
    val term = readLine()!!
    printAndLog(term, false)
    if (cardList.removeIf { it.term == term }) {
        printAndLog("The card has been removed.")
    } else {
        printAndLog("Can't remove \"$term\": there is no such card.")
    }
}

fun addCard() {
    val term = readTerm()
    if (term != null) {
        val definition = readDefinition()
        if (definition != null) {
            cardList.add(Card(term, definition))
            printAndLog("The pair (\"$term\":\"$definition\") has been added.")
        }
    }
}

fun ask() {
    printAndLog("How many times to ask?")
    var cardAmount = readLine()!!.toInt()
    printAndLog(cardAmount, false)
    if (cardList.size < cardAmount) {
        cardAmount = cardList.size
    }
    for (i in 0 until cardAmount) {
        val card = cardList[Random.Default.nextInt(cardList.size)]
        printAndLog("Print the definition of \"${card.term}\"")
        val answer = readLine()!!
        printAndLog(answer, false)
        if (answer == card.definition) {
            printAndLog("Correct!")
        } else {
            val existsCard: Card? = cardList.find { it.definition == answer }
            if (existsCard != null) {
                printAndLog("Wrong. The right answer is \"${card.definition}\", but your definition is correct for \"${existsCard.term}\".")
            } else {
                printAndLog("Wrong. The right answer is \"${card.definition}\"")
            }
            card.errors++
        }
    }
}

fun readDefinition(): String? {
    printAndLog("The definition of the card:")
    val definition = readLine()!!
    printAndLog(definition, false)
    if (cardList.any { it.definition == definition }) {
        printAndLog("The definition \"$definition\" already exists.")
        return null
    }
    return definition;
}

private fun readTerm(): String? {
    printAndLog("The card:")
    val term = readLine()!!
    printAndLog(term, false)
    if (cardList.any { it.term == term }) {
        printAndLog("The card \"$term\" already exists.")
        return null
    }
    return term
}
