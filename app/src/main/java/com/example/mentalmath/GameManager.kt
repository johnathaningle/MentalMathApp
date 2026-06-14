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
        if (difficulty != Difficulty.CUSTOM) {
            config = getDefaultConfig(difficulty)
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
            QuestionType.APPLIED_PROBLEM -> generateAppliedProblem()
            QuestionType.ALGEBRA -> generateAlgebra()
            QuestionType.EXPONENTS_ROOTS -> generateExponentsRoots()
            QuestionType.GEOMETRY -> generateGeometry()
            QuestionType.NUMBER_THEORY -> generateNumberTheory()
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
        GameMode.EXAM -> questions.size >= config.questionCount
    }

    fun getGameResult(): GameResult {
        val correct = questions.count { it.isCorrect }
        val duration = System.currentTimeMillis() - gameStartTime

        val breakdown = mutableMapOf<Topic, TopicBreakdown>()
        for (qr in questions) {
            val topic = qr.question.topic ?: continue
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
        val topicToType: Map<Topic, QuestionType> = mapOf(
            Topic.BASIC to QuestionType.BASIC,
            Topic.COMPOUND to QuestionType.COMPOUND_2,
            Topic.PERCENTAGE to QuestionType.PERCENTAGE,
            Topic.APPLIED to QuestionType.APPLIED_PROBLEM,
            Topic.ALGEBRA to QuestionType.ALGEBRA,
            Topic.EXPONENTS_ROOTS to QuestionType.EXPONENTS_ROOTS,
            Topic.GEOMETRY to QuestionType.GEOMETRY,
            Topic.NUMBER_THEORY to QuestionType.NUMBER_THEORY
        )
        val types = missedTopics.mapNotNull { topicToType[it] }
        if (types.isNotEmpty()) {
            config = config.copy(questionTypes = types)
        }
        startGame()
    }

    fun getMissedTopics(): Set<Topic> =
        questions.filter { !it.isCorrect }.mapNotNull { it.question.topic }.toSet()

    // ---- Helpers ----

    private fun basicInt(): Int = config.basicNumbers.random()
    private fun compInt(): Int = config.compoundNumbers.random()
    private fun smallInt(): Int = config.smallNumbers.random()
    private fun pickOp(): Operator = config.operators.random()

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    // ---- BASIC ----

    private fun generateBasic(): Question {
        val op = pickOp()
        return when (op) {
            Operator.ADDITION -> {
                val a = basicInt(); val b = basicInt()
                Question("$a + $b", a + b, Topic.BASIC)
            }
            Operator.SUBTRACTION -> {
                val a = basicInt(); val b = (config.basicNumbers.first..a).random()
                Question("$a − $b", a - b, Topic.BASIC)
            }
            Operator.MULTIPLICATION -> {
                val a = smallInt(); val b = smallInt()
                Question("$a × $b", a * b, Topic.BASIC)
            }
            Operator.DIVISION -> {
                val b = smallInt(); val q = basicInt(); val a = b * q
                Question("$a ÷ $b", q, Topic.BASIC)
            }
        }
    }

    // ---- COMPOUND 2 OPS ----

    private fun generateCompound2(): Question {
        val patterns = listOf(
            {
                val a = compInt(); val b = compInt(); val c = compInt()
                Question("$a + $b + $c", a + b + c, Topic.COMPOUND)
            },
            {
                val a = compInt(); val b = compInt()
                val c = (1..(a + b)).random()
                Question("$a + $b − $c", a + b - c, Topic.COMPOUND)
            },
            {
                val a = compInt(); val b = (1..a).random(); val c = compInt()
                Question("$a − $b + $c", a - b + c, Topic.COMPOUND)
            },
            {
                val a = smallInt(); val b = smallInt(); val c = compInt()
                Question("$a × $b + $c", a * b + c, Topic.COMPOUND)
            },
            {
                val a = smallInt(); val b = smallInt()
                val c = (1..(a * b)).random()
                Question("$a × $b − $c", a * b - c, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val q = smallInt(); val a = b * q; val c = compInt()
                Question("$a ÷ $b + $c", q + c, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val q = smallInt(); val a = b * q
                val c = (1..maxOf(1, q)).random()
                Question("$a ÷ $b − $c", q - c, Topic.COMPOUND)
            },
            {
                val a = compInt(); val b = smallInt(); val c = smallInt()
                Question("$a + $b × $c", a + b * c, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val c = smallInt(); val product = b * c
                val a = (product..product + 50).random()
                Question("$a − $b × $c", a - product, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val q = smallInt(); val a = b * q; val c = smallInt()
                Question("$a ÷ $b × $c", q * c, Topic.COMPOUND)
            }
        )
        return patterns.random()()
    }

    // ---- COMPOUND 4 OPS ----

    private fun generateCompound4(): Question {
        val patterns = listOf(
            {
                val a = smallInt(); val b = smallInt(); val c = smallInt()
                val d = smallInt(); val ab = a * b; val cd = c * d
                val e = (1..(ab + cd)).random()
                Question("$a × $b + $c × $d − $e", ab + cd - e, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val c = smallInt(); val bc = b * c
                val e = smallInt()
                val a = compInt()
                val maxQ = a + bc
                val q = (2..minOf(12, maxQ.coerceAtLeast(2))).random()
                val d = e * q
                Question("$a + $b × $c − $d ÷ $e", a + bc - q, Topic.COMPOUND)
            },
            {
                val a = smallInt(); val b = smallInt(); val prod = a * b
                val d = smallInt()
                val e = compInt()
                val maxQ = prod + e
                val q = (2..minOf(12, maxQ.coerceAtLeast(2))).random()
                val c = d * q
                Question("$a × $b − $c ÷ $d + $e", prod - q + e, Topic.COMPOUND)
            },
            {
                val b = smallInt(); val q1 = smallInt(); val a = b * q1
                val c = smallInt(); val prod = q1 * c
                val d = compInt()
                val e = (1..(prod + d)).random()
                Question("$a ÷ $b × $c + $d − $e", prod + d - e, Topic.COMPOUND)
            },
            {
                val a = compInt(); val b = compInt(); val total = a + b
                val e = smallInt()
                val maxQ = minOf(12, total)
                val q = (2..maxQ.coerceAtLeast(2)).random()
                val cd = q * e
                val factors = (2..12).filter { cd % it == 0 && cd / it in 2..12 }
                val c = factors.random()
                val d = cd / c
                Question("$a + $b − $c × $d ÷ $e", total - q, Topic.COMPOUND)
            },
            {
                val a = smallInt(); val b = smallInt(); val ab = a * b
                val d = smallInt()
                val maxE = minOf(12, (ab + 30) / d)
                val e = (2..maxE.coerceAtLeast(2)).random()
                val de = d * e
                val minC = (de - ab).coerceAtLeast(1)
                val c = (minC..maxOf(minC, 30)).random()
                Question("$a × $b + $c − $d × $e", ab + c - de, Topic.COMPOUND)
            }
        )
        return patterns.random()()
    }

    // ---- PERCENTAGE ----

    private fun generatePercentage(): Question {
        val percentages = listOf(5, 10, 15, 20, 25, 30, 40, 50, 60, 75)
        val pct = percentages.random()
        val g = gcd(pct, 100)
        val step = 100 / g
        val k = (1..(200 / step)).random()
        val number = step * k
        val answer = (pct * number) / 100
        return Question("What is $pct% of $number?", answer, Topic.PERCENTAGE)
    }

    // ---- APPLIED PROBLEMS ----

    private fun generateAppliedProblem(): Question {
        val templates = listOf(
            {
                val speed = (30..70).random()
                val time = (1..5).random()
                Question(
                    "A car travels $speed miles per hour. How far does it go in $time hours?",
                    speed * time, Topic.APPLIED
                )
            },
            {
                val feet = (1..10).random()
                Question("How many inches are in $feet feet?", feet * 12, Topic.APPLIED)
            },
            {
                var avg = 0; var aa = 0; var bb = 0; var cc = 0
                while (true) {
                    aa = (10..100).random()
                    bb = (10..100).random()
                    cc = (10..100).random()
                    val sum = aa + bb + cc
                    if (sum % 3 == 0) {
                        avg = sum / 3
                        break
                    }
                }
                Question(
                    "John scored $aa, $bb, and $cc on three tests. What is his average?",
                    avg, Topic.APPLIED
                )
            },
            {
                val a = (1..5).random()
                val b = (2..6).random()
                val ratio = (2..8).random()
                val c = b * ratio
                val answer = a * ratio
                Question(
                    "A recipe needs $a cups of flour per $b cups of sugar. How much flour for $c cups of sugar?",
                    answer, Topic.APPLIED
                )
            },
            {
                val p = listOf(100, 200, 300, 400, 500, 1000).random()
                val r = (2..10).random()
                val t = (1..5).random()
                val interest = p * r * t / 100
                Question(
                    "You invest $$p at $r% annual interest. How much interest after $t years?",
                    interest, Topic.APPLIED
                )
            }
        )
        return templates.random()()
    }

    // ---- ALGEBRA ----

    private fun generateAlgebra(): Question {
        val patterns = listOf(
            {
                val a = (2..10).random()
                val x = (-10..20).random()
                val b = (1..20).random()
                val c = a * x + b
                Question("${a}x + $b = $c, x = ?", x, Topic.ALGEBRA)
            },
            {
                val a = (2..10).random()
                val x = (1..20).random()
                val b = (1..a * x).random()
                val c = a * x - b
                Question("${a}x − $b = $c, x = ?", x, Topic.ALGEBRA)
            },
            {
                val a = (2..8).random()
                val c = (1..10).filter { it != a }.random()
                val x = (0..10).random()
                val b = (1..20).random()
                val d = (a - c) * x + b
                Question("${a}x + $b = ${c}x + $d, x = ?", x, Topic.ALGEBRA)
            },
            {
                val a = (2..10).random()
                val x = (-5..15).random()
                val b = (1..10).random()
                val c = a * (x + b)
                Question("${a}(x + $b) = $c, x = ?", x, Topic.ALGEBRA)
            }
        )
        return patterns.random()()
    }

    // ---- EXPONENTS & ROOTS ----

    private fun generateExponentsRoots(): Question {
        val patterns = listOf(
            { val b = (2..15).random(); Question("$b² = ?", b * b, Topic.EXPONENTS_ROOTS) },
            { val b = (2..15).random(); Question("√${b * b} = ?", b, Topic.EXPONENTS_ROOTS) },
            { val b = (2..12).random(); Question("$b³ = ?", b * b * b, Topic.EXPONENTS_ROOTS) },
            { val b = (2..12).random(); Question("∛${b * b * b} = ?", b, Topic.EXPONENTS_ROOTS) },
            { Question("2⁴ = ?", 16, Topic.EXPONENTS_ROOTS) },
            { Question("2⁵ = ?", 32, Topic.EXPONENTS_ROOTS) },
            { Question("3³ = ?", 27, Topic.EXPONENTS_ROOTS) },
            { Question("3⁴ = ?", 81, Topic.EXPONENTS_ROOTS) },
            { Question("4³ = ?", 64, Topic.EXPONENTS_ROOTS) },
            { Question("5³ = ?", 125, Topic.EXPONENTS_ROOTS) }
        )
        return patterns.random()()
    }

    // ---- GEOMETRY ----

    private fun generateGeometry(): Question {
        val patterns = listOf(
            {
                val w = (2..20).random(); val h = (2..20).random()
                Question("What is the area of a ${w}×${h} rectangle?", w * h, Topic.GEOMETRY)
            },
            {
                val w = (2..20).random(); val h = (2..20).random()
                Question("What is the perimeter of a ${w}×${h} rectangle?", 2 * (w + h), Topic.GEOMETRY)
            },
            {
                val b = (2..20).random()
                val candidates = (2..20).filter { (b * it) % 2 == 0 }
                val h = candidates.random()
                Question("What is the area of a triangle with base $b and height $h?", b * h / 2, Topic.GEOMETRY)
            },
            {
                val r = (2..15).random()
                Question("A circle has radius $r. Using π ≈ 3, what is its area?", 3 * r * r, Topic.GEOMETRY)
            },
            {
                val r = (2..15).random()
                Question("A circle has radius $r. Using π ≈ 3, what is its circumference?", 6 * r, Topic.GEOMETRY)
            },
            {
                val triples = listOf(
                    Triple(3, 4, 5), Triple(5, 12, 13), Triple(8, 15, 17),
                    Triple(7, 24, 25), Triple(9, 40, 41), Triple(6, 8, 10),
                    Triple(9, 12, 15), Triple(12, 16, 20)
                )
                val (a, b, c) = triples.random()
                if (Random.nextBoolean()) {
                    Question("A right triangle has legs $a and $b. What is the hypotenuse?", c, Topic.GEOMETRY)
                } else {
                    val known = listOf(a, b).random()
                    val unknown = if (known == a) b else a
                    Question("A right triangle has one leg $known and hypotenuse $c. What is the other leg?", unknown, Topic.GEOMETRY)
                }
            }
        )
        return patterns.random()()
    }

    // ---- NUMBER THEORY ----

    private fun generateNumberTheory(): Question {
        val patterns = listOf(
            {
                val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)
                val idx = (0..primes.size - 2).random()
                Question("What is the next prime after ${primes[idx]}?", primes[idx + 1], Topic.NUMBER_THEORY)
            },
            {
                val pairs = listOf(
                    12 to 18, 24 to 36, 30 to 45, 48 to 60, 15 to 25,
                    18 to 27, 42 to 56, 20 to 30, 36 to 48, 54 to 72
                )
                val (a, b) = pairs.random()
                Question("What is the GCF of $a and $b?", gcd(a, b), Topic.NUMBER_THEORY)
            },
            {
                val pairs = listOf(
                    4 to 6, 6 to 8, 9 to 12, 10 to 15, 12 to 18,
                    8 to 12, 6 to 10, 9 to 15, 10 to 20, 12 to 15
                )
                val (a, b) = pairs.random()
                val lcm = a * b / gcd(a, b)
                Question("What is the LCM of $a and $b?", lcm, Topic.NUMBER_THEORY)
            }
        )
        return patterns.random()()
    }
}
