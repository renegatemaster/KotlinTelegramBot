package com.renegatemaster.englishwordsbot.telegram

import com.renegatemaster.englishwordsbot.trainer.LearnWordsTrainer
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.CALLBACK_DATA_ANSWER_PREFIX
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.LEARN_WORDS_CLICKED
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.MENU
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.RESET_CLICKED
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.START
import com.renegatemaster.englishwordsbot.telegram.TelegramBotService.Companion.STATISTICS_CLICKED
import com.renegatemaster.englishwordsbot.telegram.entities.Response
import com.renegatemaster.englishwordsbot.telegram.entities.Update

fun main(args: Array<String>) {

    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val response: Response = bot.getUpdates(lastUpdateId)
        if (response.result.isEmpty()) continue

        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, bot) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

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

fun resetProgress(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    trainer.resetProgress()
    telegramBotService.sendMessage(chatId, "Прогресс сброшен")
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService,
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }
    val data = update.callbackQuery?.data

    when {
        message?.lowercase() == START -> telegramBotService.sendMenu(chatId)
        data?.lowercase() == MENU -> telegramBotService.sendMenu(chatId)
        data?.lowercase() == STATISTICS_CLICKED -> getStatisticsAndSend(trainer, telegramBotService, chatId)
        data?.lowercase() == LEARN_WORDS_CLICKED -> checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        data?.lowercase() == RESET_CLICKED -> resetProgress(trainer, telegramBotService, chatId)
        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> handleAnswer(data, trainer, telegramBotService, chatId)
    }
}