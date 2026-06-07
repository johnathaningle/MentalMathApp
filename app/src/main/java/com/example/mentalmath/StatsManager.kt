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

    fun saveGameResult(result: GameResult) {
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, getGamesPlayed() + 1)
            .putInt(KEY_TOTAL_QUESTIONS, getTotalQuestions() + result.totalQuestions)
            .putInt(KEY_TOTAL_CORRECT, getTotalCorrect() + result.correctAnswers)
            .putInt(KEY_BEST_SCORE, maxOf(getBestScore(), result.score))
            .putInt(KEY_BEST_STREAK, maxOf(getBestStreak(), result.bestStreak))
            .putLong(KEY_TOTAL_TIME, getTotalTimeMs() + result.durationMs)
            .apply()
    }
}
