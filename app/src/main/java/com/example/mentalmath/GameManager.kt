package com.example.mentalmath

object GameManager {
    const val BASE_SCORE = 10
    const val STREAK_OFFSET = 1

    var difficulty: Difficulty = Difficulty.EASY
    var gameMode: GameMode = GameMode.ENDLESS
    var config: DifficultyConfig = getDefaultConfig(Difficulty.EASY)
    var score: Int = 0
    var streak: Int = 0
    var bestStreak: Int = 0
    var questions: MutableList<QuestionResult> = mutableListOf()
    var lives: Int = 3
    var questionStartTime: Long = 0L
    var gameStartTime: Long = 0L
    var remainingTimeMs: Long = 0L

    private var currentQ: Question? = null

    fun startGame() {
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
    }

    fun generateQuestion(): Question {
        val type = config.questionTypes.random()
        val q = QuestionGenerators.generate(type, config)
        currentQ = q
        questionStartTime = System.currentTimeMillis()
        return q
    }

    fun currentQuestion(): Question? = currentQ

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
    }

    fun getGameResult(): GameResult {
        val correct = questions.count { it.isCorrect }
        val duration = System.currentTimeMillis() - gameStartTime

        val breakdown = mutableMapOf<Topic, TopicBreakdown>()
        for (qr in questions) {
            val topic = qr.question.topic
            val current = breakdown.getOrDefault(topic, TopicBreakdown())
            breakdown[topic] = TopicBreakdown(
                totalQuestions = current.totalQuestions + 1,
                correctAnswers = current.correctAnswers + (if (qr.isCorrect) 1 else 0)
            )
        }

        return GameResult(
            difficulty, gameMode, score, questions.size, correct,
            bestStreak, duration, topicsBreakdown = breakdown
        )
    }

    fun startRetryGame(missedTopics: Set<Topic>) {
        val types = missedTopics.map { it.toRetryQuestionType() }
        if (types.isNotEmpty()) {
            config = config.copy(questionTypes = types)
        }
        startGame()
    }

    fun getMissedTopics(): Set<Topic> =
        questions.filter { !it.isCorrect }.map { it.question.topic }.toSet()
}
