package com.example.mentalmath

import kotlin.random.Random

object GameManager {
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
        config = getDefaultConfig(difficulty)
        if (difficulty == Difficulty.CUSTOM) {
            config = getDefaultConfig(Difficulty.EASY)
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
        val types = config.questionTypes
        val type = types.random()
        val q = when (type) {
            QuestionType.BASIC -> generateBasic()
            QuestionType.COMPOUND_2 -> generateCompound2()
            QuestionType.COMPOUND_4 -> generateCompound4()
            QuestionType.PERCENTAGE -> generatePercentage()
        }
        currentQ = q
        questionStartTime = System.currentTimeMillis()
        return q
    }

    fun currentQuestion(): Question? = currentQ

    fun submitAnswer(userAnswer: Int): QuestionResult {
        val question = currentQ ?: return QuestionResult(
            Question("", 0), userAnswer, false, 0
        )
        val timeTaken = System.currentTimeMillis() - questionStartTime
        val isCorrect = userAnswer == question.correctAnswer

        if (isCorrect) {
            streak++
            if (streak > bestStreak) bestStreak = streak
            score += 10 + (streak - 1)
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
    }

    fun getGameResult(): GameResult {
        val correct = questions.count { it.isCorrect }
        val duration = System.currentTimeMillis() - gameStartTime
        return GameResult(difficulty, gameMode, score, questions.size, correct, bestStreak, duration)
    }

    // ---- Helpers ----

    private fun basicInt(): Int = config.basicNumbers.random()
    private fun compInt(): Int = config.compoundNumbers.random()
    private fun smallInt(): Int = (2..12).random()
    private fun pickOp(): Operator = config.operators.random()

    // ---- BASIC ----

    private fun generateBasic(): Question {
        val op = pickOp()
        return when (op) {
            Operator.ADDITION -> {
                val a = basicInt(); val b = basicInt()
                Question("$a + $b", a + b)
            }
            Operator.SUBTRACTION -> {
                val a = basicInt(); val b = (config.basicNumbers.first..a).random()
                Question("$a \u2212 $b", a - b)
            }
            Operator.MULTIPLICATION -> {
                val a = smallInt(); val b = smallInt()
                Question("$a \u00d7 $b", a * b)
            }
            Operator.DIVISION -> {
                val b = smallInt(); val q = smallInt(); val a = b * q
                Question("$a \u00f7 $b", q)
            }
        }
    }

    // ---- COMPOUND 2 OPS ----

    private fun generateCompound2(): Question {
        val patterns = listOf(
            // a + b + c
            {
                val a = compInt(); val b = compInt(); val c = compInt()
                Question("$a + $b + $c", a + b + c)
            },
            // a + b - c
            {
                val a = compInt(); val b = compInt()
                val c = (1..(a + b)).random()
                Question("$a + $b \u2212 $c", a + b - c)
            },
            // a - b + c
            {
                val a = compInt(); val b = (1..a).random(); val c = compInt()
                Question("$a \u2212 $b + $c", a - b + c)
            },
            // a × b + c  (× first)
            {
                val a = smallInt(); val b = smallInt(); val c = compInt()
                Question("$a \u00d7 $b + $c", a * b + c)
            },
            // a × b - c  (× first)
            {
                val a = smallInt(); val b = smallInt()
                val c = (1..(a * b)).random()
                Question("$a \u00d7 $b \u2212 $c", a * b - c)
            },
            // a ÷ b + c  (÷ first)
            {
                val b = smallInt(); val q = smallInt(); val a = b * q; val c = compInt()
                Question("$a \u00f7 $b + $c", q + c)
            },
            // a ÷ b - c  (÷ first)
            {
                val b = smallInt(); val q = smallInt(); val a = b * q
                val c = (1..maxOf(1, q)).random()
                Question("$a \u00f7 $b \u2212 $c", q - c)
            },
            // a + b × c  (× has precedence)
            {
                val a = compInt(); val b = smallInt(); val c = smallInt()
                Question("$a + $b \u00d7 $c", a + b * c)
            },
            // a - b × c  (× has precedence)
            {
                val b = smallInt(); val c = smallInt(); val product = b * c
                val a = (product..product + 50).random()
                Question("$a \u2212 $b \u00d7 $c", a - product)
            },
            // a ÷ b × c  (left to right)
            {
                val b = smallInt(); val q = smallInt(); val a = b * q; val c = smallInt()
                Question("$a \u00f7 $b \u00d7 $c", q * c)
            }
        )
        return patterns.random()()
    }

    // ---- COMPOUND 4 OPS ----

    private fun generateCompound4(): Question {
        val patterns = listOf(
            // a × b + c × d - e
            {
                val a = smallInt(); val b = smallInt(); val c = smallInt()
                val d = smallInt(); val ab = a * b; val cd = c * d
                val e = (1..(ab + cd)).random()
                Question("$a \u00d7 $b + $c \u00d7 $d \u2212 $e", ab + cd - e)
            },
            // a + b × c - d ÷ e  (× and ÷ first)
            {
                val b = smallInt(); val c = smallInt(); val bc = b * c
                val e = smallInt(); val q = smallInt(); val d = e * q
                val a = compInt()
                val answer = a + bc - q
                val (adjustedA, adjustedQ) = if (answer < 0) {
                    val shift = (-answer) + 10
                    Pair(a, q + shift / e)
                } else Pair(a, q)
                val ad = e * adjustedQ
                val adjAns = a + bc - adjustedQ
                Question("$a + $b \u00d7 $c \u2212 $ad \u00f7 $e", adjAns)
            },
            // a × b - c ÷ d + e
            {
                val a = smallInt(); val b = smallInt(); val prod = a * b
                val d = smallInt(); val q = smallInt(); val c = d * q
                val e = compInt()
                val answer = prod - q + e
                Question("$a \u00d7 $b \u2212 $c \u00f7 $d + $e", answer)
            },
            // a ÷ b × c + d - e
            {
                val b = smallInt(); val q1 = smallInt(); val a = b * q1
                val c = smallInt(); val prod = q1 * c
                val d = compInt()
                val e = (1..(prod + d)).random()
                Question("$a \u00f7 $b \u00d7 $c + $d \u2212 $e", prod + d - e)
            },
            // a + b - c × d ÷ e
            {
                val c = smallInt(); val d = smallInt(); val cd = c * d
                val e = smallInt(); val q = cd / e; val rem = cd % e
                val adjustedC = if (rem != 0) {
                    (cd / e + 1) * e / d
                } else c
                val finalCd = adjustedC * d
                val finalQ = finalCd / e
                val a = compInt(); val b = (1..a).random()
                val answer = a - b - finalQ
                val (adjA, adjB) = if (answer < 0) {
                    val extra = (-answer) + 10
                    Pair(a + extra, b)
                } else Pair(a, b)
                val adjAns = adjA - adjB - finalQ
                Question("$adjA \u2212 $adjB \u2212 $adjustedC \u00d7 $d \u00f7 $e", adjAns)
            },
            // a × b + c - d × e
            {
                val a = smallInt(); val b = smallInt(); val ab = a * b
                val d = smallInt(); val e = smallInt(); val de = d * e
                val c = compInt()
                val answer = ab + c - de
                val adjC = if (answer < 0) c + (-answer) + 10 else c
                val adjAns = ab + adjC - de
                Question("$a \u00d7 $b + $adjC \u2212 $d \u00d7 $e", adjAns)
            }
        )
        return patterns.random()()
    }

    // ---- PERCENTAGE ----

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun generatePercentage(): Question {
        val percentages = listOf(5, 10, 15, 20, 25, 30, 40, 50, 60, 75)
        val pct = percentages.random()
        val g = gcd(pct, 100)
        val step = 100 / g
        val k = (1..(200 / step)).random()
        val number = step * k
        val answer = (pct * number) / 100
        return Question("What is $pct% of $number?", answer)
    }
}
