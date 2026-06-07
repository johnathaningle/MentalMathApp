package com.example.mentalmath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mentalmath.databinding.FragmentEndGameBinding

class EndGameFragment : Fragment() {

    private var _binding: FragmentEndGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEndGameBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        binding.btnPlayAgain.setOnClickListener {
            GameManager.startGame()
            findNavController().navigate(R.id.action_EndGameFragment_to_GameFragment)
        }

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_EndGameFragment_to_HomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
