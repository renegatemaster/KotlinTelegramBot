package com.renegatemaster.englishwordsbot.telegram

import com.renegatemaster.englishwordsbot.telegram.entities.*
import com.renegatemaster.englishwordsbot.trainer.model.Question
import com.renegatemaster.englishwordsbot.trainer.model.Word
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
        const val RESET_CLICKED = "reset_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getUpdates(updateId: Long): Response {
        val urlGetUpdates = "$BASE_API_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseString = response.body().also { println(it) }

        return json.decodeFromString<Response>(responseString)
    }

    fun sendMessage(chatId: Long, message: String) {
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

    fun sendMenu(chatId: Long) {
        val urlSendMenu = "$BASE_API_URL$token/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Главное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                    )
                )
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

    fun sendQuestion(chatId: Long, question: Question) {
        val urlSendQuestion = "$BASE_API_URL$token/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index: Int, word: Word ->
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