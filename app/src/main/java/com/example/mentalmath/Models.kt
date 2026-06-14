package com.example.mentalmath

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    CUSTOM("Custom")
}

enum class GameMode(val label: String) {
    TIMED("Timed"),
    ENDLESS("Endless"),
    SURVIVAL("Survival")
}

enum class Operator(val symbol: String) {
    ADDITION("+"),
    SUBTRACTION("−"),
    MULTIPLICATION("×"),
    DIVISION("÷")
}

enum class QuestionType(val label: String) {
    BASIC("Basic (e.g. 12 + 12)"),
    COMPOUND_2("2 Operations (e.g. 12 ÷ 3 + 4)"),
    COMPOUND_4("4 Operations (e.g. 8 × 3 + 12 ÷ 4 − 5)"),
    PERCENTAGE("Percentages (e.g. 20% of 50)")
}

data class DifficultyConfig(
    val basicNumbers: IntRange = 1..20,
    val compoundNumbers: IntRange = 1..20,
    val smallNumbers: IntRange = 2..12,
    val operators: List<Operator> = listOf(Operator.ADDITION, Operator.SUBTRACTION),
    val questionTypes: List<QuestionType> = listOf(QuestionType.BASIC),
    val timeLimitSeconds: Int = 90,
    val lives: Int = 5
)

fun getDefaultConfig(difficulty: Difficulty): DifficultyConfig = when (difficulty) {
    Difficulty.EASY -> DifficultyConfig(
        basicNumbers = 1..20,
        compoundNumbers = 1..10,
        operators = listOf(Operator.ADDITION, Operator.SUBTRACTION),
        questionTypes = listOf(QuestionType.BASIC),
        timeLimitSeconds = 90,
        lives = 5
    )
    Difficulty.MEDIUM -> DifficultyConfig(
        basicNumbers = 1..50,
        compoundNumbers = 1..20,
        operators = Operator.entries.toList(),
        questionTypes = listOf(QuestionType.COMPOUND_2, QuestionType.PERCENTAGE),
        timeLimitSeconds = 60,
        lives = 3
    )
    Difficulty.HARD -> DifficultyConfig(
        basicNumbers = 1..100,
        compoundNumbers = 1..30,
        operators = Operator.entries.toList(),
        questionTypes = listOf(
            QuestionType.COMPOUND_4, QuestionType.PERCENTAGE
        ),
        timeLimitSeconds = 45,
        lives = 2
    )
    Difficulty.CUSTOM -> DifficultyConfig(
        basicNumbers = 1..20,
        compoundNumbers = 1..20,
        operators = listOf(Operator.ADDITION, Operator.SUBTRACTION),
        questionTypes = listOf(QuestionType.BASIC),
        timeLimitSeconds = 90,
        lives = 5
    )
}

data class Question(
    val displayText: String,
    val correctAnswer: Int
)

data class QuestionResult(
    val question: Question,
    val userAnswer: Int?,
    val isCorrect: Boolean,
    val timeTakenMs: Long
)

data class GameResult(
    val difficulty: Difficulty,
    val gameMode: GameMode,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val bestStreak: Int,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
