package com.example.mentalmath

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.mentalmath.databinding.DialogCustomDifficultyBinding

class CustomDifficultyDialog : DialogFragment() {

    private var _binding: DialogCustomDifficultyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCustomDifficultyBinding.inflate(layoutInflater)

        val config = GameManager.config
        binding.etMinNumber.setText(config.basicNumbers.first.toString())
        binding.etMaxNumber.setText(config.basicNumbers.last.toString())
        binding.etCompoundMin.setText(config.compoundNumbers.first.toString())
        binding.etCompoundMax.setText(config.compoundNumbers.last.toString())
        binding.etSmallMin.setText(config.smallNumbers.first.toString())
        binding.etSmallMax.setText(config.smallNumbers.last.toString())
        binding.etTimeLimit.setText(config.timeLimitSeconds.toString())
        binding.etLives.setText(config.lives.toString())

        binding.cbAddition.isChecked = config.operators.contains(Operator.ADDITION)
        binding.cbSubtraction.isChecked = config.operators.contains(Operator.SUBTRACTION)
        binding.cbMultiplication.isChecked = config.operators.contains(Operator.MULTIPLICATION)
        binding.cbDivision.isChecked = config.operators.contains(Operator.DIVISION)

        binding.cbBasic.isChecked = config.questionTypes.contains(QuestionType.BASIC)
        binding.cbCompound2.isChecked = config.questionTypes.contains(QuestionType.COMPOUND_2)
        binding.cbCompound4.isChecked = config.questionTypes.contains(QuestionType.COMPOUND_4)
        binding.cbPercentage.isChecked = config.questionTypes.contains(QuestionType.PERCENTAGE)

        return AlertDialog.Builder(requireContext())
            .setTitle("Custom Difficulty")
            .setView(binding.root)
            .setPositiveButton("Start") { _, _ ->
                val basicMin = binding.etMinNumber.text.toString().toIntOrNull()
                val basicMax = binding.etMaxNumber.text.toString().toIntOrNull()
                val compMin = binding.etCompoundMin.text.toString().toIntOrNull()
                val compMax = binding.etCompoundMax.text.toString().toIntOrNull()
                val smallMin = binding.etSmallMin.text.toString().toIntOrNull()
                val smallMax = binding.etSmallMax.text.toString().toIntOrNull()
                val timeLimit = binding.etTimeLimit.text.toString().toIntOrNull()
                val lives = binding.etLives.text.toString().toIntOrNull()

                if (basicMin == null || basicMax == null || compMin == null ||
                    compMax == null || smallMin == null || smallMax == null ||
                    timeLimit == null || lives == null
                ) {
                    Toast.makeText(requireContext(), "Please fill in all fields with valid numbers", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (basicMin < 1 || compMin < 1 || smallMin < 2) {
                    Toast.makeText(requireContext(), "Minimum values must be at least 1 (2 for small numbers)", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (basicMax < basicMin || compMax < compMin || smallMax < smallMin) {
                    Toast.makeText(requireContext(), "Max must be greater than or equal to Min", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val operators = mutableListOf<Operator>()
                if (binding.cbAddition.isChecked) operators.add(Operator.ADDITION)
                if (binding.cbSubtraction.isChecked) operators.add(Operator.SUBTRACTION)
                if (binding.cbMultiplication.isChecked) operators.add(Operator.MULTIPLICATION)
                if (binding.cbDivision.isChecked) operators.add(Operator.DIVISION)
                if (operators.isEmpty()) operators.add(Operator.ADDITION)

                val questionTypes = mutableListOf<QuestionType>()
                if (binding.cbBasic.isChecked) questionTypes.add(QuestionType.BASIC)
                if (binding.cbCompound2.isChecked) questionTypes.add(QuestionType.COMPOUND_2)
                if (binding.cbCompound4.isChecked) questionTypes.add(QuestionType.COMPOUND_4)
                if (binding.cbPercentage.isChecked) questionTypes.add(QuestionType.PERCENTAGE)
                if (questionTypes.isEmpty()) questionTypes.add(QuestionType.BASIC)

                GameManager.config = DifficultyConfig(
                    basicNumbers = basicMin..basicMax,
                    compoundNumbers = compMin..compMax,
                    smallNumbers = smallMin..smallMax,
                    operators = operators,
                    questionTypes = questionTypes,
                    timeLimitSeconds = timeLimit.coerceAtLeast(10),
                    lives = lives.coerceAtLeast(1)
                )
                GameManager.startGame()
                findNavController().navigate(R.id.action_HomeFragment_to_GameFragment)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
