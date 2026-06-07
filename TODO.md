# TODO — Content & Feature Expansion

## New Question Types

These expand the app beyond pure arithmetic into broader math topics, making it useful for standardized test preparation.

### 1. Applied Problems (Word Problems)

Generate real-world scenario questions that require reading comprehension and selecting the correct operation(s).

| Topic | Example | Notes |
|---|---|---|
| Distance / Rate / Time | "A car travels 60 miles per hour. How far does it go in 3 hours?" | `d = r × t` |
| Unit conversion | "How many inches are in 2.5 feet?" | Consistent unit systems |
| Averages | "John scored 80, 90, and 85 on three tests. What is his average?" | Mean calculation |
| Ratios & proportions | "A recipe needs 2 cups of flour per 3 cups of sugar. How much flour for 9 cups of sugar?" | Cross-multiplication |
| Simple interest | "You invest $500 at 4% annual interest. How much do you have after 2 years?" | `I = P × r × t` |

**Implementation notes:**
- Use templates with placeholders for randomized numbers (e.g., `"A car travels {speed} miles per hour. How far does it go in {time} hours?"`)
- Ensure all numbers and answers are integers (no decimals unless explicitly intended).
- Each template maps to a known answer formula — verified during generation.

### 2. Algebra (Linear Equations)

Generate single-variable linear equations to solve for `x`.

| Pattern | Example |
|---|---|
| `ax + b = c` | `3x + 5 = 20` |
| `ax − b = c` | `4x − 7 = 13` |
| `ax + b = cx + d` | `2x + 5 = 3x − 1` |
| `a(x + b) = c` | `2(x + 4) = 18` |

**Implementation notes:**
- Coefficient `a` should be small (2–10), solution `x` always an integer within a reasonable range (−20 to 20).
- Generate the equation from the solution (pick `x`, then pick coefficients, compute RHS) to guarantee an integer answer.
- Display format: `"3x + 5 = 20, x = ?"`

### 3. Exponents & Roots

| Topic | Example |
|---|---|
| Squares | `11² = ?` |
| Square roots | `√144 = ?` |
| Cubes | `5³ = ?` |
| Cube roots | `∛27 = ?` |
| Small powers | `2⁵ = ?`, `3⁴ = ?` |

**Implementation notes:**
- Base values should stay small (2–15 for squares, 2–12 for cubes, 2–5 for higher powers).
- Only perfect squares and perfect cubes to keep answers integer.

### 4. Geometry (Area, Perimeter, Pythagorean Theorem)

| Shape | Formula | Example |
|---|---|---|
| Rectangle area | `A = w × h` | "What is the area of a 5×3 rectangle?" |
| Rectangle perimeter | `P = 2(w + h)` | "What is the perimeter of a 5×3 rectangle?" |
| Triangle area | `A = ½ × b × h` | "What is the area of a triangle with base 6 and height 4?" (ensure even product so answer is integer) |
| Circle area | `A = πr²` | Use `π = 3.14` with integer results where possible |
| Circle circumference | `C = 2πr` | Same as above |
| Pythagorean theorem | `a² + b² = c²` | "A right triangle has legs of 3 and 4. What is the hypotenuse?" Use known triples (3-4-5, 5-12-13, 8-15-17, 7-24-25, 9-40-41) |

### 5. Number Theory

| Topic | Example |
|---|---|
| Prime numbers | "Is 17 prime?" (yes/no) or "What is the next prime after 17?" |
| Greatest common factor | "What is the GCF of 12 and 18?" |
| Least common multiple | "What is the LCM of 6 and 8?" |

### Question Type Configuration

Add each new type to the `QuestionType` enum so they can be toggled in Custom Difficulty alongside Basic, Compound, and Percentage.

---

## New Game Modes

### Exam Mode (Fixed-Length Session)

Similar to Timed mode but with a fixed number of questions instead of a countdown timer. The session ends when all questions are answered (or the time limit expires, whichever comes first).

- User-configured question count (default 15 or 30).
- Optional per-question time limit (configurable).
- Score displayed as `X/Y` during the game.
- No lives — wrong answers are just marked wrong.
- Post-game summary shows accuracy, time per question, and a topic breakdown.

This mode mirrors standardized test formats where the number of questions is known upfront.

---

## Answer Review Screen

After a game ends (any mode), show a scrollable list of every question with:

- The question text and the user's answer.
- Whether it was correct or incorrect.
- The correct answer (if wrong).
- (Optional) Topic tag (e.g., "Algebra", "Geometry", "Percentage").

Controls:
- "Retry missed questions" — starts a new game containing only the questions the user got wrong (regenerating similar variants).
- "Share results" — export as text or image.

---

## Topic-Based Performance Tracking

Replace the flat per-game stats with per-topic tracking:

| Topic | Games Played | Total Questions | Correct | Accuracy | Best Score |
|---|---|---|---|---|---|
| Basic | 5 | 48 | 42 | 87.5% | 520 |
| Algebra | 3 | 27 | 20 | 74.1% | 290 |
| Geometry | 2 | 18 | 12 | 66.7% | 180 |
| ... | ... | ... | ... | ... | ... |

- Display as a table or list on the Home screen, below or alongside the overall stats.
- Each game records topic-level breakdowns (number of questions per topic, correct per topic).
- This helps users identify weak areas and focus their practice.

**Storage options:**
- Extend `SharedPreferences` schema (more complex but avoids adding Room).
- Migrate to **Room database** for proper relational storage (recommended for this feature).

---

## Implementation Priority

| Priority | Feature | Rationale |
|---|---|---|
| P0 | Applied Problems (word problems) | Biggest impact; closest to real test content |
| P0 | Algebra, Exponents, Geometry, Number Theory | Broadens content coverage significantly |
| P1 | Topic-based performance tracking | Lets users identify weak areas from day one |
| P1 | Answer Review screen | Direct learning feedback loop |
| P2 | Exam Mode | Polishes the test-prep experience |

---

This file is aspirational — a living wishlist. Items should be promoted into ARCHITECTURE.md's Roadmap section once scoped and prioritized for implementation.
