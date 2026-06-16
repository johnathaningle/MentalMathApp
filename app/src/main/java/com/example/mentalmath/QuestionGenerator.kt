package com.example.mentalmath

import kotlin.random.Random

interface QuestionGenerator {
    fun generate(config: DifficultyConfig): Question
}

private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

// ---- BASIC ----

class BasicGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
        val op = config.operators.random()
        return when (op) {
            Operator.ADDITION -> {
                val a = config.basicNumbers.random()
                val b = config.basicNumbers.random()
                Question("$a + $b = ?", a + b, Topic.BASIC)
            }
            Operator.SUBTRACTION -> {
                val a = config.basicNumbers.random()
                val b = (config.basicNumbers.first..a).random()
                Question("$a − $b = ?", a - b, Topic.BASIC)
            }
            Operator.MULTIPLICATION -> {
                val a = config.smallNumbers.random()
                val b = config.smallNumbers.random()
                Question("$a × $b = ?", a * b, Topic.BASIC)
            }
            Operator.DIVISION -> {
                val b = config.smallNumbers.random()
                val q = config.basicNumbers.random()
                val a = b * q
                Question("$a ÷ $b = ?", q, Topic.BASIC)
            }
        }
    }
}

// ---- COMPOUND 2 OPS ----

class Compound2Generator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
        val comp = { config.compoundNumbers.random() }
        val small = { config.smallNumbers.random() }

        val patterns = listOf(
            {
                val a = comp(); val b = comp(); val c = comp()
                Question("$a + $b + $c = ?", a + b + c, Topic.COMPOUND)
            },
            {
                val a = comp(); val b = comp()
                val c = (1..(a + b)).random()
                Question("$a + $b − $c = ?", a + b - c, Topic.COMPOUND)
            },
            {
                val a = comp(); val b = (1..a).random(); val c = comp()
                Question("$a − $b + $c = ?", a - b + c, Topic.COMPOUND)
            },
            {
                val a = small(); val b = small(); val c = comp()
                Question("$a × $b + $c = ?", a * b + c, Topic.COMPOUND)
            },
            {
                val a = small(); val b = small()
                val c = (1..(a * b)).random()
                Question("$a × $b − $c = ?", a * b - c, Topic.COMPOUND)
            },
            {
                val b = small(); val q = small(); val a = b * q; val c = comp()
                Question("$a ÷ $b + $c = ?", q + c, Topic.COMPOUND)
            },
            {
                val b = small(); val q = small(); val a = b * q
                val c = (1..maxOf(1, q)).random()
                Question("$a ÷ $b − $c = ?", q - c, Topic.COMPOUND)
            },
            {
                val a = comp(); val b = small(); val c = small()
                Question("$a + $b × $c = ?", a + b * c, Topic.COMPOUND)
            },
            {
                val b = small(); val c = small(); val product = b * c
                val a = (product..product + 50).random()
                Question("$a − $b × $c = ?", a - product, Topic.COMPOUND)
            },
            {
                val b = small(); val q = small(); val a = b * q; val c = small()
                Question("$a ÷ $b × $c = ?", q * c, Topic.COMPOUND)
            }
        )
        return patterns.random()()
    }
}

// ---- COMPOUND 4 OPS ----

class Compound4Generator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
        val small = { config.smallNumbers.random() }
        val comp = { config.compoundNumbers.random() }

        val patterns = listOf(
            {
                val a = small(); val b = small(); val c = small()
                val d = small(); val ab = a * b; val cd = c * d
                val e = (1..(ab + cd)).random()
                Question("$a × $b + $c × $d − $e = ?", ab + cd - e, Topic.COMPOUND)
            },
            {
                val b = small(); val c = small(); val bc = b * c
                val e = small()
                val a = comp()
                val maxQ = a + bc
                val q = (2..minOf(12, maxQ.coerceAtLeast(2))).random()
                val d = e * q
                Question("$a + $b × $c − $d ÷ $e = ?", a + bc - q, Topic.COMPOUND)
            },
            {
                val a = small(); val b = small(); val prod = a * b
                val d = small()
                val e = comp()
                val maxQ = prod + e
                val q = (2..minOf(12, maxQ.coerceAtLeast(2))).random()
                val c = d * q
                Question("$a × $b − $c ÷ $d + $e = ?", prod - q + e, Topic.COMPOUND)
            },
            {
                val b = small(); val q1 = small(); val a = b * q1
                val c = small(); val prod = q1 * c
                val d = comp()
                val e = (1..(prod + d)).random()
                Question("$a ÷ $b × $c + $d − $e = ?", prod + d - e, Topic.COMPOUND)
            },
            {
                val a = comp(); val b = comp(); val total = a + b
                val e = small()
                val maxQ = minOf(12, total)
                val q = (2..maxQ.coerceAtLeast(2)).random()
                val cd = q * e
                val factors = (2..12).filter { cd % it == 0 && cd / it in 2..12 }
                val c = factors.random()
                val d = cd / c
                Question("$a + $b − $c × $d ÷ $e = ?", total - q, Topic.COMPOUND)
            },
            {
                val a = small(); val b = small(); val ab = a * b
                val d = small()
                val maxE = minOf(12, (ab + 30) / d)
                val e = (2..maxE.coerceAtLeast(2)).random()
                val de = d * e
                val minC = (de - ab).coerceAtLeast(1)
                val c = (minC..maxOf(minC, 30)).random()
                Question("$a × $b + $c − $d × $e = ?", ab + c - de, Topic.COMPOUND)
            }
        )
        return patterns.random()()
    }
}

// ---- PERCENTAGE ----

class PercentageGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
        val percentages = listOf(5, 10, 15, 20, 25, 30, 40, 50, 60, 75)
        val pct = percentages.random()
        val g = gcd(pct, 100)
        val step = 100 / g
        val k = (1..(200 / step)).random()
        val number = step * k
        val answer = (pct * number) / 100
        return Question("What is $pct% of $number?", answer, Topic.PERCENTAGE)
    }
}

// ---- APPLIED PROBLEMS ----

class AppliedProblemGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
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
}

// ---- ALGEBRA ----

class AlgebraGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
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
}

// ---- EXPONENTS & ROOTS ----

class ExponentsRootsGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
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
}

// ---- GEOMETRY ----

class GeometryGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
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
}

// ---- NUMBER THEORY ----

class NumberTheoryGenerator : QuestionGenerator {
    override fun generate(config: DifficultyConfig): Question {
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

// ---- REGISTRY ----

object QuestionGenerators {
    private val generators: Map<QuestionType, QuestionGenerator> = mapOf(
        QuestionType.BASIC to BasicGenerator(),
        QuestionType.COMPOUND_2 to Compound2Generator(),
        QuestionType.COMPOUND_4 to Compound4Generator(),
        QuestionType.PERCENTAGE to PercentageGenerator(),
        QuestionType.APPLIED_PROBLEM to AppliedProblemGenerator(),
        QuestionType.ALGEBRA to AlgebraGenerator(),
        QuestionType.EXPONENTS_ROOTS to ExponentsRootsGenerator(),
        QuestionType.GEOMETRY to GeometryGenerator(),
        QuestionType.NUMBER_THEORY to NumberTheoryGenerator()
    )

    fun generate(type: QuestionType, config: DifficultyConfig): Question {
        val generator = generators[type]
            ?: error("Unknown question type: $type")
        return generator.generate(config)
    }
}
