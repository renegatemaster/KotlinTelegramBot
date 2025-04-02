package com.renegatemaster

import java.io.File

const val MENU = "\nМеню:\n1 – Учить слова\n2 – Статистика\n0 – Выход\n"
const val LEARNED_COUNT = 3
const val ONE_HUNDRED_PERCENT = 100
val allowableAnswerValues = (0..4)

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun loadDictionary(): MutableList<Word> {
    val words = File("words.txt")
    val dictionary = mutableListOf<Word>()

    words.readLines().forEach {
        val parts = it.split("|")
        val original: String = parts.getOrNull(0) ?: ""
        val translate: String = parts.getOrNull(1) ?: ""
        val correctAnswersCount: Int = parts.getOrNull(2)?.toIntOrNull() ?: 0
        dictionary.add(
            Word(
                original = original,
                translate = translate,
                correctAnswersCount = correctAnswersCount,
            )
        )
    }

    return dictionary
}

fun saveDictionary(dictionary: MutableList<Word>) {
    val words = File("words.txt")
    words.writeText("")
    dictionary.forEach { word ->
        words.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
    }
}

fun learnWords(dictionary: MutableList<Word>) {

    while (true) {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }

        if (notLearnedList.isEmpty()) {
            println("\nВсе слова в словаре выучены")
            return
        }

        val questionWords = notLearnedList.shuffled().take(4)
        val correctAnswer = questionWords.random()
        val correctAnswerIndex = questionWords.indexOf(correctAnswer)
        println("\n${correctAnswer.original}:")
        questionWords.forEachIndexed { index, word ->
            println("\t${index + 1} - ${word.translate}")
        }
        println("--------------\n\t0 - Меню")

        while (true) {
            val userAnswerInput = readln().toIntOrNull()
            if (userAnswerInput == null || userAnswerInput !in allowableAnswerValues) {
                println("Некорректный ввод, введите ваш ответ заново")
                continue
            }
            if (userAnswerInput == 0) return
            if (userAnswerInput - 1 == correctAnswerIndex) {
                println("Правильно!")
                correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                break
            } else {
                println("Неправильно! ${correctAnswer.original} — это ${correctAnswer.translate}")
                break
            }
        }
    }
}

fun getStatistics(dictionary: MutableList<Word>) {
    val totalCount = dictionary.size
    val correctAnswersCount = dictionary.filter { it.correctAnswersCount >= LEARNED_COUNT }.size
    val percent = correctAnswersCount * ONE_HUNDRED_PERCENT / totalCount
    println("Выучено $correctAnswersCount из $totalCount слов | $percent%")
}

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println(MENU)
        val input = readln()
        when (input) {
            "1" -> learnWords(dictionary)
            "2" -> getStatistics(dictionary)
            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}