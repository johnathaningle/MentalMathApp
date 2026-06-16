package com.example.mentalmath

import android.content.Context
import android.content.SharedPreferences

object StatsManager {
    private const val PREFS_NAME = "mental_math_stats"
    private const val KEY_GAMES_PLAYED = "games_played"
    private const val KEY_TOTAL_QUESTIONS = "total_questions"
    private const val KEY_TOTAL_CORRECT = "total_correct"
    private const val KEY_BEST_SCORE = "best_score"
    private const val KEY_BEST_STREAK = "best_streak"
    private const val KEY_TOTAL_TIME = "total_time_ms"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getGamesPlayed(): Int = prefs.getInt(KEY_GAMES_PLAYED, 0)
    fun getTotalQuestions(): Int = prefs.getInt(KEY_TOTAL_QUESTIONS, 0)
    fun getTotalCorrect(): Int = prefs.getInt(KEY_TOTAL_CORRECT, 0)
    fun getBestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)
    fun getBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)
    fun getTotalTimeMs(): Long = prefs.getLong(KEY_TOTAL_TIME, 0L)

    fun getAccuracy(): Float {
        val total = getTotalQuestions()
        return if (total > 0) getTotalCorrect().toFloat() / total * 100f else 0f
    }

    fun getTopicTotalQuestions(topic: Topic): Int =
        prefs.getInt("topic_${topic.name}_questions", 0)

    fun getTopicTotalCorrect(topic: Topic): Int =
        prefs.getInt("topic_${topic.name}_correct", 0)

    fun getTopicAccuracy(topic: Topic): Float {
        val total = getTopicTotalQuestions(topic)
        return if (total > 0) getTopicTotalCorrect(topic).toFloat() / total * 100f else 0f
    }

    fun getTopicBestScore(topic: Topic): Int =
        prefs.getInt("topic_${topic.name}_best_score", 0)

    fun getPlayedTopics(): List<Topic> {
        return Topic.entries.filter { getTopicTotalQuestions(it) > 0 }
    }

    fun saveGameResult(result: GameResult) {
        val editor = prefs.edit()
            .putInt(KEY_GAMES_PLAYED, getGamesPlayed() + 1)
            .putInt(KEY_TOTAL_QUESTIONS, getTotalQuestions() + result.totalQuestions)
            .putInt(KEY_TOTAL_CORRECT, getTotalCorrect() + result.correctAnswers)
            .putInt(KEY_BEST_SCORE, maxOf(getBestScore(), result.score))
            .putInt(KEY_BEST_STREAK, maxOf(getBestStreak(), result.bestStreak))
            .putLong(KEY_TOTAL_TIME, getTotalTimeMs() + result.durationMs)

        for ((topic, breakdown) in result.topicsBreakdown) {
            val keyQuestions = "topic_${topic.name}_questions"
            val keyCorrect = "topic_${topic.name}_correct"
            val keyBestScore = "topic_${topic.name}_best_score"

            editor
                .putInt(keyQuestions, prefs.getInt(keyQuestions, 0) + breakdown.totalQuestions)
                .putInt(keyCorrect, prefs.getInt(keyCorrect, 0) + breakdown.correctAnswers)
                .putInt(keyBestScore, maxOf(prefs.getInt(keyBestScore, 0), result.score))
        }
        editor.apply()
    }
}
