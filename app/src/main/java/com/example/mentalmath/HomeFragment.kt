package com.example.mentalmath

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.mentalmath.databinding.FragmentHomeBinding

class HomeFragment : BindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipEasy.isChecked = true
        binding.chipEndless.isChecked = true

        binding.chipEasy.setOnClickListener { GameManager.difficulty = Difficulty.EASY }
        binding.chipMedium.setOnClickListener { GameManager.difficulty = Difficulty.MEDIUM }
        binding.chipHard.setOnClickListener { GameManager.difficulty = Difficulty.HARD }
        binding.chipCustom.setOnClickListener {
            GameManager.difficulty = Difficulty.CUSTOM
        }

        binding.chipTimed.setOnClickListener { GameManager.gameMode = GameMode.TIMED }
        binding.chipEndless.setOnClickListener { GameManager.gameMode = GameMode.ENDLESS }
        binding.chipSurvival.setOnClickListener { GameManager.gameMode = GameMode.SURVIVAL }
        binding.chipExam.setOnClickListener { GameManager.gameMode = GameMode.EXAM }

        binding.btnStart.setOnClickListener {
            if (GameManager.difficulty == Difficulty.CUSTOM) {
                CustomDifficultyDialog().show(parentFragmentManager, "custom_difficulty")
            } else {
                GameManager.startGame()
                findNavController().navigate(R.id.action_HomeFragment_to_GameFragment)
            }
        }

        updateStats()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    private fun updateStats() {
        val gamesPlayed = StatsManager.getGamesPlayed()
        binding.statsSection.visibility = if (gamesPlayed > 0) View.VISIBLE else View.GONE
        binding.tvStatsGamesPlayed.text = getString(R.string.stats_games_played, gamesPlayed)
        binding.tvStatsTotalQuestions.text = getString(R.string.stats_total_questions, StatsManager.getTotalQuestions())
        binding.tvStatsAccuracy.text = getString(R.string.stats_accuracy, "%.1f".format(StatsManager.getAccuracy()))
        binding.tvStatsBestScore.text = getString(R.string.stats_best_score, StatsManager.getBestScore())
        binding.tvStatsBestStreak.text = getString(R.string.stats_best_streak, StatsManager.getBestStreak())

        val playedTopics = StatsManager.getPlayedTopics()
        if (playedTopics.isNotEmpty()) {
            binding.tvTopicTitle.visibility = View.VISIBLE
            binding.topicStatsContainer.visibility = View.VISIBLE
            binding.topicStatsContainer.removeAllViews()
            for (topic in playedTopics) {
                val total = StatsManager.getTopicTotalQuestions(topic)
                val accuracy = StatsManager.getTopicAccuracy(topic)
                val row = TextView(requireContext()).apply {
                    text = getString(R.string.topic_row, topic.label, total, "%.1f%%".format(accuracy))
                    textSize = 14f
                    setPadding(0, 4, 0, 4)
                }
                binding.topicStatsContainer.addView(row)
            }
        } else {
            binding.tvTopicTitle.visibility = View.GONE
            binding.topicStatsContainer.visibility = View.GONE
        }
    }
}
