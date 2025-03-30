package com.renegatemaster

import java.io.File

const val MENU = "Меню:\n1 – Учить слова\n2 – Статистика\n0 – Выход"

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

    while (true) {
        println(MENU)
        val input = readln()
        when (input) {
            "1" -> println("Учить слова")
            "2" -> println("Статистика")
            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}