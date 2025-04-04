package com.renegatemaster

import java.io.File

data class Statistics(
    val totalCount: Int,
    val correctAnswersCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val correctAnswersCount = dictionary.filter { it.correctAnswersCount >= LEARNED_COUNT }.size
        val percent = correctAnswersCount * ONE_HUNDRED_PERCENT / totalCount
        return Statistics(totalCount = totalCount, correctAnswersCount = correctAnswersCount, percent = percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }
        if (notLearnedList.isEmpty()) return null

        val variants = notLearnedList.shuffled().take(NUMBER_OF_ANSWERS)
        val correctAnswer = variants.random()
        question = Question(variants = variants, correctAnswer = correctAnswer)

        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerIndex == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): MutableList<Word> {
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

    private fun saveDictionary() {
        val words = File("words.txt")
        words.writeText("")
        dictionary.forEach { word ->
            words.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}
