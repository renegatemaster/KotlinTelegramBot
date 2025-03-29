package com.renegatemaster

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {
    val words = File("words.txt")
    val dictionary = mutableListOf<Word>()

    words.readLines().forEach {
        val parts = it.split("|")
        val original: String = parts.getOrNull(0) ?: ""
        val translate: String = parts.getOrNull(1) ?: ""
        val correctAnswersCount: Int? = parts.getOrNull(2)?.toIntOrNull()
        dictionary.add(
            Word(
                original = original,
                translate = translate,
                correctAnswersCount = correctAnswersCount ?: 0,
            )
        )
    }
    println(dictionary)
}