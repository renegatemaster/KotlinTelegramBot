package com.renegatemaster.englishwordsbot.telegram

import com.renegatemaster.englishwordsbot.trainer.LearnWordsTrainer
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.CALLBACK_DATA_ANSWER_PREFIX
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.LEARN_WORDS_CLICKED
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.START
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.STATISTICS_CLICKED
import com.renegatemaster.englishwordsbot.telegram.entities.Response

fun getStatisticsAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val statistics = trainer.getStatistics().let {
        "Выучено ${it.correctAnswersCount} из ${it.totalCount} слов | ${it.percent}%"
    }
    telegramBotService.sendMessage(chatId, statistics)
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(chatId, question)
    }
}

fun handleAnswer(
    data: String,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
    if (trainer.checkAnswer(userAnswerIndex)) {
        telegramBotService.sendMessage(chatId, "Правильно!")
    } else {
        telegramBotService.sendMessage(
            chatId,
            "Неправильно! ${trainer.question?.correctAnswer?.original} — это ${trainer.question?.correctAnswer?.translate}",
        )
    }
    checkNextQuestionAndSend(trainer, telegramBotService, chatId)
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    var lastUpdateId = 0L

    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val response: Response = bot.getUpdates(lastUpdateId)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val data = firstUpdate.callbackQuery?.data

        when {
            message?.lowercase() == START -> bot.sendMenu(chatId)
            data?.lowercase() == STATISTICS_CLICKED -> getStatisticsAndSend(trainer, bot, chatId)
            data?.lowercase() == LEARN_WORDS_CLICKED -> checkNextQuestionAndSend(trainer, bot, chatId)
            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> handleAnswer(data, trainer, bot, chatId)
        }
    }
}