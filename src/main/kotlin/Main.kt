package com.renegatemaster

const val MENU = "\nМеню:\n1 – Учить слова\n2 – Статистика\n0 – Выход\n"
const val ONE_HUNDRED_PERCENT = 100
val allowableAnswerValues = (0..4)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "\t${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.original + ":\n" + variants + "\n--------------\n" +
            "\t0 - Меню"
}

fun learnWords(trainer: LearnWordsTrainer) {

    while (true) {
        val question = trainer.getNextQuestion()

        if (question == null) {
            println("\nВсе слова в словаре выучены")
            return
        }

        println(question.asConsoleString())

        while (true) {
            val userAnswerInput = readln().toIntOrNull()
            if (userAnswerInput == null || userAnswerInput !in allowableAnswerValues) {
                println("Некорректный ввод, введите ваш ответ заново")
                continue
            }
            if (userAnswerInput == 0) return

            if (trainer.checkAnswer(userAnswerInput.minus(1))) {
                println("Правильно!")
                break
            } else {
                println("Неправильно! ${question.correctAnswer.original} — это ${question.correctAnswer.translate}")
                break
            }
        }
    }
}

fun printStatistics(trainer: LearnWordsTrainer) {
    val statistics = trainer.getStatistics()
    println("Выучено ${statistics.correctAnswersCount} из ${statistics.totalCount} слов | ${statistics.percent}%")
}

fun main() {
    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println(MENU)
        val input = readln()
        when (input) {
            "1" -> learnWords(trainer)
            "2" -> printStatistics(trainer)
            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}