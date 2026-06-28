package com.example.mentalmath

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mentalmath.databinding.FragmentEndGameBinding

class EndGameFragment : BindingFragment<FragmentEndGameBinding>(FragmentEndGameBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = GameManager.getGameResult()
        val total = result.totalQuestions
        val correct = result.correctAnswers
        val accuracy = if (total > 0) correct.toFloat() / total * 100f else 0f
        val avgTime = if (total > 0) result.durationMs / total else 0L

        binding.tvScoreValue.text = result.score.toString()
        binding.tvCorrectValue.text = getString(R.string.stats_correct_total, correct, total)
        binding.tvAccuracyValue.text = "%.1f%%".format(accuracy)
        binding.tvAvgTimeValue.text = getString(R.string.avg_time_format, avgTime / 1000.0)
        binding.tvBestStreakValue.text = result.bestStreak.toString()
        binding.tvDifficultyValue.text = result.difficulty.label
        binding.tvModeValue.text = result.gameMode.label

        val questions = GameManager.questions.toList()
        if (questions.isNotEmpty()) {
            binding.rvAnswerReview.layoutManager = LinearLayoutManager(requireContext())
            binding.rvAnswerReview.adapter = QuestionReviewAdapter(questions)
        }

        val missedCount = questions.count { !it.isCorrect }
        if (missedCount == 0) {
            binding.btnRetryMissed.visibility = View.GONE
        }

        binding.btnRetryMissed.setOnClickListener {
            val missedQuestions = GameManager.questions
                .filter { !it.isCorrect }
                .map { it.question }
            GameManager.startRetryGame(missedQuestions)
            findNavController().navigate(R.id.action_EndGameFragment_to_GameFragment)
        }

        binding.btnShareResults.setOnClickListener {
            shareResults(result, questions)
        }

        binding.btnPlayAgain.setOnClickListener {
            if (GameManager.gameMode == GameMode.RETRY) {
                GameManager.gameMode = GameManager.previousGameMode
            }
            GameManager.startGame()
            findNavController().navigate(R.id.action_EndGameFragment_to_GameFragment)
        }

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_EndGameFragment_to_HomeFragment)
        }
    }

    private fun shareResults(result: GameResult, questions: List<QuestionResult>) {
        val sb = StringBuilder()
        sb.appendLine("MentalMath — Game Results")
        sb.appendLine("Difficulty: ${result.difficulty.label} | Mode: ${result.gameMode.label}")
        sb.appendLine("Score: ${result.score}")
        sb.appendLine("Correct: ${result.correctAnswers} / ${result.totalQuestions}")
        val accuracy = if (result.totalQuestions > 0)
            "%.1f%%".format(result.correctAnswers.toFloat() / result.totalQuestions * 100f) else "—"
        sb.appendLine("Accuracy: $accuracy")
        sb.appendLine("Best Streak: ${result.bestStreak}")
        sb.appendLine()

        for ((i, qr) in questions.withIndex()) {
            val mark = if (qr.isCorrect) "\u2713" else "\u2717"
            sb.appendLine("${i + 1}. ${qr.question.displayText} $mark")
            sb.appendLine("   Your answer: ${qr.userAnswer ?: "—"}")
            if (!qr.isCorrect) {
                sb.appendLine("   Correct: ${qr.question.correctAnswer}")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Share Results"))
    }
}
