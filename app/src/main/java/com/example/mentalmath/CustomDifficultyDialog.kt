package com.example.mentalmath

import android.app.Dialog
import android.os.Bundle
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
                val basicMin = binding.etMinNumber.text.toString().toIntOrNull() ?: 1
                val basicMax = binding.etMaxNumber.text.toString().toIntOrNull() ?: 20
                val compMin = binding.etCompoundMin.text.toString().toIntOrNull() ?: 1
                val compMax = binding.etCompoundMax.text.toString().toIntOrNull() ?: 20
                val timeLimit = binding.etTimeLimit.text.toString().toIntOrNull() ?: 60
                val lives = binding.etLives.text.toString().toIntOrNull() ?: 3

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
                    basicNumbers = basicMin.coerceAtLeast(1)..basicMax.coerceAtLeast(basicMin),
                    compoundNumbers = compMin.coerceAtLeast(1)..compMax.coerceAtLeast(compMin),
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
