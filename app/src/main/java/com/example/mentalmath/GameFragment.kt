package com.example.mentalmath

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.findNavController
import com.example.mentalmath.databinding.FragmentGameBinding

class GameFragment : BindingFragment<FragmentGameBinding>(FragmentGameBinding::inflate) {

    private var countDownTimer: CountDownTimer? = null
    private var isShowingFeedback = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        showNextQuestion()
    }

    private fun setupUI() {
        binding.etAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSubmit()
                true
            } else false
        }

        binding.btnSubmit.setOnClickListener { handleSubmit() }
        binding.btnEndSession.setOnClickListener { endSession() }
    }

    private fun showNextQuestion() {
        if (GameManager.isGameOver()) {
            navigateToEnd()
            return
        }

        if (GameManager.gameMode == GameMode.TIMED) {
            startTimer()
        }
        updateHUD()

        val question = GameManager.generateQuestion()
        binding.tvQuestion.text = question.displayText
        binding.etAnswer.text.clear()
        binding.etAnswer.isEnabled = true
        binding.btnSubmit.isEnabled = true
        binding.etAnswer.requestFocus()
        isShowingFeedback = false

        binding.ivFeedback.setImageDrawable(null)
        binding.etAnswer.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.tvFeedback.visibility = View.GONE
    }

    private fun handleSubmit() {
        if (isShowingFeedback) {
            showNextQuestion()
            return
        }

        val text = binding.etAnswer.text.toString()
        if (text.isEmpty()) return

        val userAnswer = text.toIntOrNull() ?: run {
            binding.etAnswer.error = "Enter a number"
            return
        }

        binding.etAnswer.isEnabled = false
        binding.btnSubmit.isEnabled = false

        val result = GameManager.submitAnswer(userAnswer)
        showFeedback(result)
    }

    private fun showFeedback(result: QuestionResult) {
        isShowingFeedback = true

        if (result.isCorrect) {
            binding.ivFeedback.setImageResource(android.R.drawable.ic_menu_edit)
            binding.tvFeedback.text = getString(R.string.feedback_correct)
            binding.tvFeedback.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            binding.ivFeedback.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.tvFeedback.text = getString(
                R.string.feedback_wrong,
                result.question.correctAnswer
            )
            binding.tvFeedback.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
        }

        binding.etAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.tvFeedback.visibility = View.VISIBLE

        binding.btnSubmit.text = getString(R.string.next_question)

        updateHUD()

        if (GameManager.isGameOver()) {
            binding.btnSubmit.postDelayed({ navigateToEnd() }, 1000)
        } else {
            binding.btnSubmit.isEnabled = true
            binding.etAnswer.isEnabled = true
            binding.btnSubmit.postDelayed({
                if (isAdded) showNextQuestion()
            }, 1200)
        }
    }

    private fun updateHUD() {
        binding.tvScore.text = getString(R.string.score_display, GameManager.score)
        binding.tvStreak.text = getString(R.string.streak_display, GameManager.streak)

        when (GameManager.gameMode) {
            GameMode.TIMED -> {
                binding.timerGroup.visibility = View.VISIBLE
                binding.examProgressGroup.visibility = View.GONE
                binding.livesGroup.visibility = View.GONE
                updateTimerDisplay()
            }
            GameMode.SURVIVAL -> {
                binding.timerGroup.visibility = View.GONE
                binding.examProgressGroup.visibility = View.GONE
                binding.livesGroup.visibility = View.VISIBLE
                binding.tvLives.text = "❤ ".repeat(GameManager.lives.coerceAtLeast(0)).trim()
            }
            GameMode.ENDLESS -> {
                binding.timerGroup.visibility = View.GONE
                binding.examProgressGroup.visibility = View.GONE
                binding.livesGroup.visibility = View.GONE
            }
            GameMode.EXAM -> {
                binding.timerGroup.visibility = View.GONE
                binding.examProgressGroup.visibility = View.VISIBLE
                binding.livesGroup.visibility = View.GONE
                binding.tvExamProgress.text = getString(
                    R.string.exam_progress,
                    GameManager.questions.size,
                    GameManager.config.questionCount
                )
            }
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(GameManager.remainingTimeMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                GameManager.remainingTimeMs = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                GameManager.remainingTimeMs = 0
                updateTimerDisplay()
                if (isAdded) navigateToEnd()
            }
        }.start()
    }

    private fun updateTimerDisplay() {
        val seconds = (GameManager.remainingTimeMs / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        binding.tvTimer.text = getString(R.string.timer_display, minutes, secs)
    }

    private fun endSession() {
        countDownTimer?.cancel()
        navigateToEnd()
    }

    private fun navigateToEnd() {
        countDownTimer?.cancel()
        StatsManager.saveGameResult(GameManager.getGameResult())
        findNavController().navigate(R.id.action_GameFragment_to_EndGameFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
