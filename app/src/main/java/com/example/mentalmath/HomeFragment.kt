package com.example.mentalmath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mentalmath.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        binding.btnStart.setOnClickListener {
            if (GameManager.difficulty == Difficulty.CUSTOM) {
                CustomDifficultyDialog().show(parentFragmentManager, "custom_difficulty")
            } else {
                GameManager.config = getDefaultConfig(GameManager.difficulty)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
