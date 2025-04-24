package com.renegatemaster

import com.renegatemaster.TelegramBotService.Companion.CALLBACK_DATA_ANSWER_PREFIX
import com.renegatemaster.TelegramBotService.Companion.LEARN_WORDS_CLICKED
import com.renegatemaster.TelegramBotService.Companion.START
import com.renegatemaster.TelegramBotService.Companion.STATISTICS_CLICKED
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val token: String
) {
    companion object {
        const val BASE_API_URL = "https://api.telegram.org/bot"
        const val START = "/start"
        const val LEARN_WORDS_CLICKED = "learn_words_clicked"
        const val STATISTICS_CLICKED = "statistics_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$BASE_API_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Long, message: String, json: Json) {
        val allowableMessageSize = 1..4096
        if (message.length !in allowableMessageSize) return

        val urlSendMessage = "$BASE_API_URL$token/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }

    fun sendMenu(chatId: Long, json: Json) {
        val urlSendMenu = "$BASE_API_URL$token/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Главное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                    InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                ))
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }

    fun sendQuestion(chatId: Long, question: Question, json: Json) {
        val urlSendQuestion = "$BASE_API_URL$token/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed {index: Int, word: Word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                })
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }
}

fun getStatisticsAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
    json: Json,
) {
    val statistics = trainer.getStatistics().let {
        "Выучено ${it.correctAnswersCount} из ${it.totalCount} слов | ${it.percent}%"
    }
    telegramBotService.sendMessage(chatId, statistics, json)
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
    json: Json,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены", json)
    } else {
        telegramBotService.sendQuestion(chatId, question, json)
    }
}

fun handleAnswer(
    data: String,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
    json: Json
) {
    val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
    if (trainer.checkAnswer(userAnswerIndex)) {
        telegramBotService.sendMessage(chatId, "Правильно!", json)
    } else {
        telegramBotService.sendMessage(
            chatId,
            "Неправильно! ${trainer.question?.correctAnswer?.original} — это ${trainer.question?.correctAnswer?.translate}",
            json,
        )
    }
    checkNextQuestionAndSend(trainer, telegramBotService, chatId, json)
}

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val responseString = bot.getUpdates(lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val data = firstUpdate.callbackQuery?.data

        when {
            message?.lowercase() == START -> bot.sendMenu(chatId, json)
            data?.lowercase() == STATISTICS_CLICKED -> getStatisticsAndSend(trainer, bot, chatId, json)
            data?.lowercase() == LEARN_WORDS_CLICKED -> checkNextQuestionAndSend(trainer, bot, chatId, json)
            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> handleAnswer(data, trainer, bot, chatId, json)
        }
    }
}