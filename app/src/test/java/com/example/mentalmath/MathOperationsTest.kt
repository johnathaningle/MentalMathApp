package com.example.mentalmath

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MathOperationsTest {

    @Before
    fun setUp() {
        GameManager.difficulty = Difficulty.EASY
        GameManager.gameMode = GameMode.ENDLESS
        GameManager.config = getDefaultConfig(Difficulty.EASY)
        GameManager.score = 0
        GameManager.streak = 0
        GameManager.bestStreak = 0
        GameManager.questions = mutableListOf()
        GameManager.lives = 3
        GameManager.remainingTimeMs = 90000L
        GameManager.gameStartTime = System.currentTimeMillis()
    }

    // ======================== ENUMS ========================

    @Test
    fun `operator symbols are correct`() {
        assertEquals("+", Operator.ADDITION.symbol)
        assertEquals("−", Operator.SUBTRACTION.symbol)
        assertEquals("×", Operator.MULTIPLICATION.symbol)
        assertEquals("÷", Operator.DIVISION.symbol)
    }

    @Test
    fun `difficulty labels are correct`() {
        assertEquals("Easy", Difficulty.EASY.label)
        assertEquals("Medium", Difficulty.MEDIUM.label)
        assertEquals("Hard", Difficulty.HARD.label)
        assertEquals("Custom", Difficulty.CUSTOM.label)
    }

    @Test
    fun `game mode labels are correct`() {
        assertEquals("Timed", GameMode.TIMED.label)
        assertEquals("Endless", GameMode.ENDLESS.label)
        assertEquals("Survival", GameMode.SURVIVAL.label)
        assertEquals("Exam", GameMode.EXAM.label)
    }

    // ======================== DEFAULT CONFIGS ========================

    @Test
    fun `easy default config values`() {
        val c = getDefaultConfig(Difficulty.EASY)
        assertEquals(1..20, c.basicNumbers)
        assertEquals(1..10, c.compoundNumbers)
        assertEquals(2..12, c.smallNumbers)
        assertEquals(listOf(Operator.ADDITION, Operator.SUBTRACTION), c.operators)
        assertEquals(
            listOf(QuestionType.BASIC, QuestionType.APPLIED_PROBLEM),
            c.questionTypes
        )
        assertEquals(90, c.timeLimitSeconds)
        assertEquals(5, c.lives)
    }

    @Test
    fun `medium default config values`() {
        val c = getDefaultConfig(Difficulty.MEDIUM)
        assertEquals(1..50, c.basicNumbers)
        assertEquals(1..20, c.compoundNumbers)
        assertEquals(2..12, c.smallNumbers)
        assertEquals(Operator.entries.toList(), c.operators)
        assertTrue(c.questionTypes.containsAll(listOf(QuestionType.COMPOUND_2, QuestionType.PERCENTAGE)))
        assertEquals(60, c.timeLimitSeconds)
        assertEquals(3, c.lives)
    }

    @Test
    fun `hard default config values`() {
        val c = getDefaultConfig(Difficulty.HARD)
        assertEquals(1..100, c.basicNumbers)
        assertEquals(1..30, c.compoundNumbers)
        assertEquals(2..12, c.smallNumbers)
        assertEquals(Operator.entries.toList(), c.operators)
        assertTrue(c.questionTypes.containsAll(listOf(QuestionType.COMPOUND_4, QuestionType.PERCENTAGE)))
        assertEquals(45, c.timeLimitSeconds)
        assertEquals(2, c.lives)
    }

    @Test
    fun `custom default config falls back to easy`() {
        val c = getDefaultConfig(Difficulty.CUSTOM)
        val easy = getDefaultConfig(Difficulty.EASY)
        assertEquals(easy, c)
    }

    // ======================== BASIC — ADDITION ========================

    @Test
    fun `basic addition generates correct arithmetic`() {
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 1..100,
            smallNumbers = 2..12
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            assertTrue(q.displayText.contains("+"))
            val (a, b) = extractTwoNumbers(q.displayText, "+")
            assertEquals(a + b, q.correctAnswer)
        }
    }

    // ======================== BASIC — SUBTRACTION ========================

    @Test
    fun `basic subtraction never produces negative result`() {
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.SUBTRACTION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 1..100,
            smallNumbers = 2..12
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            assertTrue(q.displayText.contains("−"))
            val (a, b) = extractTwoNumbers(q.displayText, "−")
            assertTrue("a ($a) must be >= b ($b) for non-negative result", a >= b)
            assertEquals(a - b, q.correctAnswer)
        }
    }

    // ======================== BASIC — MULTIPLICATION ========================

    @Test
    fun `basic multiplication generates correct arithmetic`() {
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.MULTIPLICATION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 1..100,
            smallNumbers = 2..12
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            assertTrue(q.displayText.contains("×"))
            val (a, b) = extractTwoNumbers(q.displayText, "×")
            assertTrue("a ($a) should be in small range 2..12", a in 2..12)
            assertTrue("b ($b) should be in small range 2..12", b in 2..12)
            assertEquals(a * b, q.correctAnswer)
        }
    }

    // ======================== BASIC — DIVISION ========================

    @Test
    fun `basic division always yields integer result`() {
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.DIVISION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 1..100,
            smallNumbers = 2..12
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            assertTrue(q.displayText.contains("÷"))
            val (a, b) = extractTwoNumbers(q.displayText, "÷")
            assertTrue("b ($b) must be in small range 2..12", b in 2..12)
            assertEquals("Division must be exact (no remainder)", 0, a % b)
            assertEquals(a / b, q.correctAnswer)
        }
    }

    // ======================== COMPOUND_2 ========================

    @Test
    fun `compound 2 questions compute correct answer`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.COMPOUND_2),
            compoundNumbers = 1..30,
            smallNumbers = 2..12
        )
        repeat(500) {
            val q = GameManager.generateQuestion()
            assertEquals(
                "Incorrect evaluation for: ${q.displayText}",
                evaluateExpression(q.displayText),
                q.correctAnswer
            )
        }
    }

    @Test
    fun `compound 2 answer is never negative`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.COMPOUND_2),
            compoundNumbers = 1..30,
            smallNumbers = 2..12
        )
        repeat(500) {
            val q = GameManager.generateQuestion()
            assertTrue(
                "Compound 2 answer was negative: ${q.correctAnswer} for '${q.displayText}'",
                q.correctAnswer >= 0
            )
        }
    }

    // ======================== COMPOUND_4 ========================

    @Test
    fun `compound 4 questions compute correct answer`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.COMPOUND_4),
            compoundNumbers = 1..30,
            smallNumbers = 2..12
        )
        repeat(500) {
            val q = GameManager.generateQuestion()
            assertEquals(
                "Incorrect evaluation for: ${q.displayText}",
                evaluateExpression(q.displayText),
                q.correctAnswer
            )
        }
    }

    @Test
    fun `compound 4 answer is never negative`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.COMPOUND_4),
            compoundNumbers = 1..30,
            smallNumbers = 2..12
        )
        repeat(500) {
            val q = GameManager.generateQuestion()
            assertTrue(
                "Compound 4 answer was negative: ${q.correctAnswer} for '${q.displayText}'",
                q.correctAnswer >= 0
            )
        }
    }

    // ======================== PERCENTAGE ========================

    @Test
    fun `percentage questions compute correct answer`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.PERCENTAGE)
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            assertTrue("Missing % in: ${q.displayText}", q.displayText.contains("%"))
            assertTrue("Answer must be non-negative", q.correctAnswer >= 0)

            val match = Regex("""What is (\d+)% of (\d+)\?""").find(q.displayText)!!
            val pct = match.groupValues[1].toInt()
            val number = match.groupValues[2].toInt()
            assertEquals((pct * number) / 100, q.correctAnswer)
        }
    }

    @Test
    fun `percentage answer divides evenly`() {
        GameManager.config = DifficultyConfig(
            operators = Operator.entries.toList(),
            questionTypes = listOf(QuestionType.PERCENTAGE)
        )
        repeat(200) {
            val q = GameManager.generateQuestion()
            val match = Regex("""What is (\d+)% of (\d+)\?""").find(q.displayText)!!
            val pct = match.groupValues[1].toInt()
            val number = match.groupValues[2].toInt()
            assertEquals(
                "pct * number must be divisible by 100",
                0, (pct * number) % 100
            )
        }
    }

    // ======================== GAME STATE ========================

    @Test
    fun `startGame resets all mutable state`() {
        GameManager.score = 99
        GameManager.lives = 1
        GameManager.streak = 10
        GameManager.bestStreak = 10
        GameManager.remainingTimeMs = 0
        GameManager.questions = mutableListOf(QuestionResult(Question("x", 0), 0, true, 0))

        GameManager.startGame()

        assertEquals(0, GameManager.score)
        assertEquals(0, GameManager.streak)
        assertEquals(0, GameManager.bestStreak)
        assertEquals(5, GameManager.lives)
        assertEquals(90000L, GameManager.remainingTimeMs)
        assertTrue(GameManager.questions.isEmpty())
    }

    @Test
    fun `correct answer increases score based on streak`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 10..10
        )

        var q = GameManager.generateQuestion()
        GameManager.submitAnswer(q.correctAnswer)
        assertEquals(10, GameManager.score)
        assertEquals(1, GameManager.streak)
        assertEquals(1, GameManager.bestStreak)

        q = GameManager.generateQuestion()
        GameManager.submitAnswer(q.correctAnswer)
        assertEquals(10 + 11, GameManager.score)
        assertEquals(2, GameManager.streak)
        assertEquals(2, GameManager.bestStreak)
    }

    @Test
    fun `wrong answer resets streak and costs a life`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 10..10
        )

        val q = GameManager.generateQuestion()
        GameManager.submitAnswer(q.correctAnswer)
        assertEquals(1, GameManager.streak)

        val q2 = GameManager.generateQuestion()
        GameManager.submitAnswer(q2.correctAnswer + 1)
        assertEquals(0, GameManager.streak)
        assertEquals(4, GameManager.lives)
    }

    @Test
    fun `submitAnswer returns correct QuestionResult`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 10..10
        )

        val q = GameManager.generateQuestion()
        val wrong = GameManager.submitAnswer(q.correctAnswer + 1)
        assertFalse(wrong.isCorrect)
        assertEquals(q.correctAnswer + 1, wrong.userAnswer)
        assertEquals(q, wrong.question)

        val q2 = GameManager.generateQuestion()
        val right = GameManager.submitAnswer(q2.correctAnswer)
        assertTrue(right.isCorrect)
        assertEquals(q2.correctAnswer, right.userAnswer)
    }

    @Test
    fun `currentQuestion returns null after submission`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 10..10
        )

        val q = GameManager.generateQuestion()
        assertNotNull(GameManager.currentQuestion())
        GameManager.submitAnswer(q.correctAnswer)
        assertNull(GameManager.currentQuestion())
    }

    @Test
    fun `submitAnswer with no current question returns default result`() {
        val result = GameManager.submitAnswer(42)
        assertFalse(result.isCorrect)
        assertEquals(42, result.userAnswer)
    }

    // ======================== GAME OVER ========================

    @Test
    fun `endless mode never triggers game over`() {
        GameManager.gameMode = GameMode.ENDLESS
        assertFalse(GameManager.isGameOver())
    }

    @Test
    fun `timed mode game over when time expires`() {
        GameManager.gameMode = GameMode.TIMED
        GameManager.remainingTimeMs = 0
        assertTrue(GameManager.isGameOver())
    }

    @Test
    fun `timed mode not game over with remaining time`() {
        GameManager.gameMode = GameMode.TIMED
        GameManager.remainingTimeMs = 1
        assertFalse(GameManager.isGameOver())
    }

    @Test
    fun `survival mode game over when lives reach zero`() {
        GameManager.gameMode = GameMode.SURVIVAL
        GameManager.lives = 0
        assertTrue(GameManager.isGameOver())
    }

    @Test
    fun `survival mode not game over with lives remaining`() {
        GameManager.gameMode = GameMode.SURVIVAL
        GameManager.lives = 1
        assertFalse(GameManager.isGameOver())
    }

    @Test
    fun `survival mode game over when lives go negative`() {
        GameManager.gameMode = GameMode.SURVIVAL
        GameManager.lives = -1
        assertTrue(GameManager.isGameOver())
    }

    // ======================== GAME RESULT ========================

    @Test
    fun `getGameResult computes summary correctly`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 5..5,
            lives = 5
        )

        val q1 = GameManager.generateQuestion()
        GameManager.submitAnswer(q1.correctAnswer)

        val q2 = GameManager.generateQuestion()
        GameManager.submitAnswer(q2.correctAnswer + 1)

        val q3 = GameManager.generateQuestion()
        GameManager.submitAnswer(q3.correctAnswer)

        val result = GameManager.getGameResult()
        assertEquals(Difficulty.EASY, result.difficulty)
        assertEquals(GameMode.ENDLESS, result.gameMode)
        assertEquals(20, result.score)
        assertEquals(3, result.totalQuestions)
        assertEquals(2, result.correctAnswers)
        assertEquals(1, result.bestStreak)
    }

    @Test
    fun `getGameResult tracks best streak separately from current streak`() {
        GameManager.startGame()
        GameManager.config = DifficultyConfig(
            operators = listOf(Operator.ADDITION),
            questionTypes = listOf(QuestionType.BASIC),
            basicNumbers = 5..5
        )

        repeat(3) {
            val q = GameManager.generateQuestion()
            GameManager.submitAnswer(q.correctAnswer)
        }

        val q = GameManager.generateQuestion()
        GameManager.submitAnswer(q.correctAnswer + 1)

        val q2 = GameManager.generateQuestion()
        GameManager.submitAnswer(q2.correctAnswer)

        val result = GameManager.getGameResult()
        assertEquals(3, result.bestStreak)
    }

    // ======================== DATA CLASSES ========================

    @Test
    fun `Question data class stores values correctly`() {
        val q = Question("1 + 2 = ?", 3)
        assertEquals("1 + 2 = ?", q.displayText)
        assertEquals(3, q.correctAnswer)
    }

    @Test
    fun `QuestionResult tracks correctness`() {
        val q = Question("1 + 2 = ?", 3)
        val correct = QuestionResult(q, 3, true, 100L)
        assertTrue(correct.isCorrect)
        assertEquals(3, correct.userAnswer)

        val wrong = QuestionResult(q, 1, false, 50L)
        assertFalse(wrong.isCorrect)
        assertEquals(1, wrong.userAnswer)
    }

    @Test
    fun `GameResult stores all fields`() {
        val now = 1000L
        val r = GameResult(
            difficulty = Difficulty.HARD,
            gameMode = GameMode.TIMED,
            score = 50,
            totalQuestions = 10,
            correctAnswers = 7,
            bestStreak = 5,
            durationMs = 60000L,
            timestamp = now
        )
        assertEquals(Difficulty.HARD, r.difficulty)
        assertEquals(GameMode.TIMED, r.gameMode)
        assertEquals(50, r.score)
        assertEquals(10, r.totalQuestions)
        assertEquals(7, r.correctAnswers)
        assertEquals(5, r.bestStreak)
        assertEquals(60000L, r.durationMs)
        assertEquals(now, r.timestamp)
    }

    @Test
    fun `DifficultyConfig has sensible defaults`() {
        val c = DifficultyConfig()
        assertEquals(1..20, c.basicNumbers)
        assertEquals(1..20, c.compoundNumbers)
        assertEquals(2..12, c.smallNumbers)
        assertEquals(listOf(Operator.ADDITION, Operator.SUBTRACTION), c.operators)
        assertEquals(listOf(QuestionType.BASIC), c.questionTypes)
        assertEquals(90, c.timeLimitSeconds)
        assertEquals(5, c.lives)
    }

    // ======================== HELPERS ========================

    private fun extractTwoNumbers(text: String, operator: String): Pair<Int, Int> {
        val clean = text.removeSuffix(" = ?")
        val parts = clean.split(" $operator ")
        assertEquals("Expected 2 operands in '$text' with operator '$operator'", 2, parts.size)
        return Pair(parts[0].trim().toInt(), parts[1].trim().toInt())
    }

    private fun evaluateExpression(text: String): Int {
        val clean = text.removeSuffix(" = ?")
        val tokens = clean.split(" ")
        val postMultDiv = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            when (tokens[i]) {
                "×" -> {
                    val left = postMultDiv.removeAt(postMultDiv.lastIndex).toInt()
                    val right = tokens[++i].toInt()
                    postMultDiv.add((left * right).toString())
                }
                "÷" -> {
                    val left = postMultDiv.removeAt(postMultDiv.lastIndex).toInt()
                    val right = tokens[++i].toInt()
                    postMultDiv.add((left / right).toString())
                }
                else -> postMultDiv.add(tokens[i])
            }
            i++
        }

        var result = postMultDiv[0].toInt()
        i = 1
        while (i < postMultDiv.size) {
            when (postMultDiv[i]) {
                "+" -> result += postMultDiv[++i].toInt()
                "−" -> result -= postMultDiv[++i].toInt()
            }
            i++
        }
        return result
    }
}
