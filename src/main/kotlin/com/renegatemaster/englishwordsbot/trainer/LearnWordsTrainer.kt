package com.renegatemaster.englishwordsbot.trainer

import com.renegatemaster.englishwordsbot.trainer.model.Question
import com.renegatemaster.englishwordsbot.trainer.model.Statistics
import com.renegatemaster.englishwordsbot.trainer.model.Word
import com.renegatemaster.englishwordsbot.console.ONE_HUNDRED_PERCENT
import java.io.File

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedWordsCount: Int = 3,
    private val numberOfAnswers: Int = 4
) {

    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val correctAnswersCount = dictionary.filter { it.correctAnswersCount >= learnedWordsCount }.size
        val percent = if (totalCount > 0) correctAnswersCount * ONE_HUNDRED_PERCENT / totalCount else 0
        return Statistics(totalCount = totalCount, correctAnswersCount = correctAnswersCount, percent = percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedWordsCount }
        if (notLearnedList.isEmpty()) return null
        val variants = if (notLearnedList.size < numberOfAnswers) {
            val learnedList = dictionary.filter { it.correctAnswersCount > learnedWordsCount }.shuffled()
            notLearnedList.shuffled().take(numberOfAnswers) +
                    learnedList.take(numberOfAnswers - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(numberOfAnswers)
        }.shuffled()

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
        val words = File(fileName)
        if (!words.exists()) {
            File("words.txt").copyTo(words)
        }
        val dictionary = mutableListOf<Word>()
        val numberOfComponents = 3

        for (word in words.readLines()) {
            val parts = word.split("|")
            if (parts.size < numberOfComponents) continue

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
        val words = File(fileName)
        words.writeText("")
        dictionary.forEach { word ->
            words.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}
