package com.renegatemaster

import java.io.File

const val MENU = "Меню:\n1 – Учить слова\n2 – Статистика\n0 – Выход"
const val LEARNED_COUNT = 3
const val ONE_HUNDRED_PERCENT = 100

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
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

fun main() {
    val dictionary = loadDictionary()
    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }

    while (true) {
        println(MENU)
        val input = readln()
        when (input) {
            "1" -> {
                if (notLearnedList.isEmpty()) {
                    println("Все слова в словаре выучены")
                    continue
                }
                val questionWords = notLearnedList.shuffled().take(4)
                val correctAnswer = questionWords.random()
                println("${correctAnswer.original}:")
                questionWords.forEachIndexed { index, word ->
                    println("\t${index + 1} - ${word.translate}")
                }
                val answer = readln()
            }

            "2" -> {
                val totalCount = dictionary.size
                val correctAnswersCount = dictionary.filter { it.correctAnswersCount >= LEARNED_COUNT }.size
                val percent = correctAnswersCount * ONE_HUNDRED_PERCENT / totalCount
                println("Выучено $correctAnswersCount из $totalCount слов | $percent%\n")
            }

            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}