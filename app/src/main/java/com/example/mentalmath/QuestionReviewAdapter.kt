package com.example.mentalmath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mentalmath.databinding.ItemQuestionReviewBinding

class QuestionReviewAdapter(
    private val results: List<QuestionResult>
) : RecyclerView.Adapter<QuestionReviewAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemQuestionReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: QuestionResult) {
            binding.tvReviewQuestion.text = "${result.question.displayText} = ?"
            binding.tvReviewYourAnswer.text = "Your answer: ${result.userAnswer ?: "—"}"

            if (result.isCorrect) {
                binding.tvReviewStatus.text = "\u2713"
                binding.tvReviewStatus.setTextColor(
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                )
                binding.tvReviewCorrectAnswer.visibility = View.GONE
            } else {
                binding.tvReviewStatus.text = "\u2717"
                binding.tvReviewStatus.setTextColor(
                    binding.root.context.getColor(android.R.color.holo_red_dark)
                )
                binding.tvReviewCorrectAnswer.text = "Correct: ${result.question.correctAnswer}"
                binding.tvReviewCorrectAnswer.visibility = View.VISIBLE
            }

            val topic = result.question.topic
            if (topic != null) {
                binding.tvReviewTopic.visibility = View.VISIBLE
                binding.tvReviewTopic.text = topic.label
            } else {
                binding.tvReviewTopic.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size
}
