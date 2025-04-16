package com.renegatemaster

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val token: String
) {
    companion object {
        const val BASE_API_URL = "https://api.telegram.org/bot"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_API_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, text: String) {
        val allowableMessageSize = 1..4096
        if (text.length !in allowableMessageSize) return

        val urlSendMessage = "$BASE_API_URL$token/sendMessage?chat_id=$chatId&text=$text"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
    }
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val bot = TelegramBotService(botToken)

    var updateId = 0
    val updateIdRegex: Regex = "\"update_id\":(\\d+),\\n\"message\"".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"id\":(\\d+),\"first_name\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = bot.getUpdates(updateId)
        println(updates)

        val matchUpdateId: MatchResult = updateIdRegex.find(updates) ?: continue
        val updateIdString = matchUpdateId.groups[1]!!.value
        updateId = updateIdString.toInt() + 1

        val matchText: MatchResult? = messageTextRegex.find(updates)
        val text = matchText?.groups?.get(1)?.value
        println(text)

        val matchChatId: MatchResult? = chatIdRegex.find(updates)
        val chatId = matchChatId?.groups?.get(1)?.value?.toInt() ?: continue

        if (text?.lowercase() == "hello") bot.sendMessage(chatId, "Hello")
    }
}