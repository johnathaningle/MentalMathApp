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
    SURVIVAL("Survival"),
    EXAM("Exam"),
    RETRY("Retry Missed")
}

enum class Operator(val symbol: String) {
    ADDITION("+"),
    SUBTRACTION("−"),
    MULTIPLICATION("×"),
    DIVISION("÷")
}

enum class QuestionType(val label: String, val associatedTopic: Topic) {
    BASIC("Basic (e.g. 12 + 12)", Topic.BASIC),
    COMPOUND_2("2 Operations (e.g. 12 ÷ 3 + 4)", Topic.COMPOUND),
    COMPOUND_4("4 Operations (e.g. 8 × 3 + 12 ÷ 4 − 5)", Topic.COMPOUND),
    PERCENTAGE("Percentages (e.g. 20% of 50)", Topic.PERCENTAGE),
    APPLIED_PROBLEM("Word Problems", Topic.APPLIED),
    ALGEBRA("Algebra (Linear Equations)", Topic.ALGEBRA),
    EXPONENTS_ROOTS("Exponents & Roots", Topic.EXPONENTS_ROOTS),
    GEOMETRY("Geometry", Topic.GEOMETRY),
    NUMBER_THEORY("Number Theory", Topic.NUMBER_THEORY)
}

enum class Topic(val label: String) {
    BASIC("Basic"),
    COMPOUND("Compound"),
    PERCENTAGE("Percentage"),
    APPLIED("Applied"),
    ALGEBRA("Algebra"),
    EXPONENTS_ROOTS("Exponents & Roots"),
    GEOMETRY("Geometry"),
    NUMBER_THEORY("Number Theory");

}

data class DifficultyConfig(
    val basicNumbers: IntRange = 1..20,
    val compoundNumbers: IntRange = 1..20,
    val smallNumbers: IntRange = 2..12,
    val operators: List<Operator> = listOf(Operator.ADDITION, Operator.SUBTRACTION),
    val questionTypes: List<QuestionType> = listOf(QuestionType.BASIC),
    val timeLimitSeconds: Int = 90,
    val lives: Int = 5,
    val questionCount: Int = 15
)

fun getDefaultConfig(difficulty: Difficulty): DifficultyConfig = when (difficulty) {
    Difficulty.EASY -> DifficultyConfig(
        basicNumbers = 1..20,
        compoundNumbers = 1..10,
        operators = listOf(Operator.ADDITION, Operator.SUBTRACTION),
        questionTypes = listOf(QuestionType.BASIC, QuestionType.COMPOUND_2),
        timeLimitSeconds = 90,
        lives = 5,
        questionCount = 15
    )
    Difficulty.MEDIUM -> DifficultyConfig(
        basicNumbers = 1..50,
        compoundNumbers = 1..20,
        operators = Operator.entries.toList(),
        questionTypes = listOf(
            QuestionType.COMPOUND_4, QuestionType.PERCENTAGE,
            QuestionType.ALGEBRA
        ),
        timeLimitSeconds = 60,
        lives = 3,
        questionCount = 20
    )
    Difficulty.HARD -> DifficultyConfig(
        basicNumbers = 1..150,
        compoundNumbers = 1..50,
        operators = Operator.entries.toList(),
        questionTypes = listOf(
            QuestionType.COMPOUND_4, QuestionType.PERCENTAGE,
            QuestionType.APPLIED_PROBLEM, QuestionType.ALGEBRA,
            QuestionType.EXPONENTS_ROOTS, QuestionType.GEOMETRY,
            QuestionType.NUMBER_THEORY
        ),
        timeLimitSeconds = 45,
        lives = 2,
        questionCount = 25
    )
    Difficulty.CUSTOM -> getDefaultConfig(Difficulty.EASY)
}

data class Question(
    val displayText: String,
    val correctAnswer: Int,
    val topic: Topic
)

data class QuestionResult(
    val question: Question,
    val userAnswer: Int?,
    val isCorrect: Boolean,
    val timeTakenMs: Long,
    val isSkipped: Boolean = false
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
