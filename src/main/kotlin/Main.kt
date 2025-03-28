package com.renegatemaster

import java.io.File

fun main() {
    val words = File("words.txt")
    words.readLines().forEach { println(it) }
}