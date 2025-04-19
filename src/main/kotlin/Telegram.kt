package com.renegatemaster

import com.renegatemaster.TelegramBotService.Companion.STATISTICS_CLICKED
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    private val token: String
) {
    companion object {
        const val BASE_API_URL = "https://api.telegram.org/bot"
        const val LEARN_WORDS_CLICKED = "learn_words_clicked"
        const val STATISTICS_CLICKED = "statistics_clicked"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_API_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, message: String) {
        val allowableMessageSize = 1..4096
        if (message.length !in allowableMessageSize) return

        val encoded = URLEncoder.encode(
            message,
            StandardCharsets.UTF_8,
        )

        val urlSendMessage = "$BASE_API_URL$token/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }

    fun sendMenu(chatId: Int) {
        val urlSendMenu = "$BASE_API_URL$token/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Главное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Учить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val bot = TelegramBotService(botToken)

    var lastUpdateId = 0
    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates = bot.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1

        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "hello") bot.sendMessage(chatId, "Hello")
        if (message?.lowercase() == "/start") bot.sendMenu(chatId)
        if (data?.lowercase() == STATISTICS_CLICKED) {
            val statistics = trainer.getStatistics().let {
                "Выучено ${it.correctAnswersCount} из ${it.totalCount} слов | ${it.percent}%"
            }
            bot.sendMessage(chatId, statistics)
        }
    }
}