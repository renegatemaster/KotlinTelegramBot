package com.renegatemaster.englishwordsbot.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)