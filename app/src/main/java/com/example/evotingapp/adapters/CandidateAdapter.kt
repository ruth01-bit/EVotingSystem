package com.example.evotingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.evotingapp.databinding.ItemCandidateBinding
import com.example.evotingapp.models.Candidate

class CandidateAdapter(
    private val candidates: List<Candidate>,
    private val onItemClick: (Candidate) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    private var selectedPosition = -1

    private val avatarColors = listOf(
        "#1A237E", "#880E4F", "#1B5E20",
        "#E65100", "#4A148C", "#006064",
        "#BF360C", "#37474F"
    )

    inner class CandidateViewHolder(private val binding: ItemCandidateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(candidate: Candidate, isSelected: Boolean) {
            binding.tvCandidateName.text = candidate.name
            binding.tvCandidateParty.text = candidate.party

            // Initials
            val initials = candidate.name
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("")
                .uppercase()
            binding.tvInitials.text = initials

            // Avatar color
            val colorHex = avatarColors[bindingAdapterPosition % avatarColors.size]
            (binding.tvInitials.parent as? androidx.cardview.widget.CardView)
                ?.setCardBackgroundColor(android.graphics.Color.parseColor(colorHex))

            // Selected state
            if (isSelected) {
                binding.root.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#E8EAF6")
                )
                binding.root.cardElevation = 6f
            } else {
                binding.root.setCardBackgroundColor(
                    android.graphics.Color.WHITE
                )
                binding.root.cardElevation = 2f
            }

            binding.rbSelect.isChecked = isSelected

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onItemClick(candidate)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val binding = ItemCandidateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CandidateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(candidates[position], position == selectedPosition)
    }

    override fun getItemCount() = candidates.size
}

