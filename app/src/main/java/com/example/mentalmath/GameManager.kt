package com.example.mentalmath

object GameManager {
    const val BASE_SCORE = 10
    const val STREAK_OFFSET = 1

    var difficulty: Difficulty = Difficulty.EASY
    private var _gameMode: GameMode = GameMode.ENDLESS
    var gameMode: GameMode
        get() = if (retryQuestions.isNotEmpty()) GameMode.RETRY else _gameMode
        set(value) { _gameMode = value }
    var config: DifficultyConfig = getDefaultConfig(Difficulty.EASY)
    var score: Int = 0
    var streak: Int = 0
    var bestStreak: Int = 0
    var questions: MutableList<QuestionResult> = mutableListOf()
    var lives: Int = 3
    var questionStartTime: Long = 0L
    var gameStartTime: Long = 0L
    var remainingTimeMs: Long = 0L

    var retryQuestions: List<Question> = emptyList()
    var retryIndex: Int = 0

    private var currentQ: Question? = null

    fun startGame() {
        resetGameState()
    }

    private fun resetGameState() {
        config = when (difficulty) {
            Difficulty.CUSTOM -> config
            else -> getDefaultConfig(difficulty)
        }
        score = 0
        streak = 0
        bestStreak = 0
        questions = mutableListOf()
        lives = config.lives
        remainingTimeMs = config.timeLimitSeconds * 1000L
        gameStartTime = System.currentTimeMillis()
        currentQ = null
        retryQuestions = emptyList()
        retryIndex = 0
    }

    fun getNextQuestion(): Question? {
        if (retryQuestions.isNotEmpty()) {
            if (retryIndex >= retryQuestions.size) return null
            val q = retryQuestions[retryIndex]
            retryIndex++
            currentQ = q
            questionStartTime = System.currentTimeMillis()
            return q
        }
        val type = config.questionTypes.random()
        val q = QuestionGenerators.generate(type, config)
        currentQ = q
        questionStartTime = System.currentTimeMillis()
        return q
    }

    fun currentQuestion(): Question? = currentQ

    fun skipAnswer(): QuestionResult {
        val question = currentQ ?: return QuestionResult(
            Question("", 0, Topic.BASIC), 0, false, 0, isSkipped = true
        )
        val result = QuestionResult(question, null, false, 0, isSkipped = true)
        questions.add(result)
        currentQ = null
        return result
    }

    fun submitAnswer(userAnswer: Int): QuestionResult {
        val question = currentQ ?: return QuestionResult(
            Question("", 0, Topic.BASIC), userAnswer, false, 0
        )
        val timeTaken = System.currentTimeMillis() - questionStartTime
        val isCorrect = userAnswer == question.correctAnswer

        if (isCorrect) {
            streak++
            if (streak > bestStreak) bestStreak = streak
            score += BASE_SCORE + (streak - STREAK_OFFSET)
        } else {
            streak = 0
            lives--
        }

        val result = QuestionResult(question, userAnswer, isCorrect, timeTaken)
        questions.add(result)
        currentQ = null
        return result
    }

    fun isGameOver(): Boolean = when (gameMode) {
        GameMode.TIMED -> remainingTimeMs <= 0
        GameMode.SURVIVAL -> lives <= 0
        GameMode.ENDLESS -> false
        GameMode.EXAM -> questions.size >= config.questionCount
        GameMode.RETRY -> retryIndex >= retryQuestions.size
    }

    fun getGameResult(): GameResult {
        val correct = questions.count { it.isCorrect }
        val duration = System.currentTimeMillis() - gameStartTime
        return GameResult(
            difficulty, gameMode, score, questions.size, correct,
            bestStreak, duration
        )
    }

    fun startRetryGame(missedQuestions: List<Question>) {
        resetGameState()
        retryQuestions = missedQuestions.toList()
        retryIndex = 0
    }
}
