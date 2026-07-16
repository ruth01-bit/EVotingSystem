package com.example.evotingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.evotingapp.databinding.ItemElectionBinding
import com.example.evotingapp.models.Election

class ElectionAdapter(
    private val elections: List<Election>,
    private val onItemClick: (Election) -> Unit
) : RecyclerView.Adapter<ElectionAdapter.ElectionViewHolder>() {

    inner class ElectionViewHolder(private val binding: ItemElectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(election: Election) {
            binding.tvElectionTitle.text = election.title
            binding.tvElectionDescription.text = election.description
            binding.tvElectionStatus.text = election.status
            binding.tvCategory.text = election.category
            binding.tvCounty.text = if (election.county.isNotEmpty())
                "📍 ${election.county}" else ""

            val statusColor = when (election.status) {
                "OPEN" -> android.graphics.Color.parseColor("#4CAF50")
                "CLOSED" -> android.graphics.Color.parseColor("#F44336")
                else -> android.graphics.Color.parseColor("#FF9800")
            }
            binding.tvElectionStatus.setTextColor(statusColor)

            val categoryColor = when (election.category) {
                "PRESIDENTIAL" -> android.graphics.Color.parseColor("#1A237E")
                "GOVERNOR" -> android.graphics.Color.parseColor("#880E4F")
                "SENATOR" -> android.graphics.Color.parseColor("#1B5E20")
                "MP" -> android.graphics.Color.parseColor("#E65100")
                "WOMEN REP" -> android.graphics.Color.parseColor("#4A148C")
                "MCA" -> android.graphics.Color.parseColor("#006064")
                else -> android.graphics.Color.parseColor("#1A237E")
            }
            binding.tvCategory.setBackgroundColor(categoryColor)

            binding.root.setOnClickListener { onItemClick(election) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        val binding = ItemElectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ElectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        holder.bind(elections[position])
    }

    override fun getItemCount() = elections.size
}
