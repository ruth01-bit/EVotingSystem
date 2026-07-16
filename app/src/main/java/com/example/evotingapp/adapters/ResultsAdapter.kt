package com.example.evotingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evotingapp.R
import com.example.evotingapp.activities.voter.ResultItem

class ResultsAdapter(
    private val items: List<ResultItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CANDIDATE = 1
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvElectionTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvElectionStatus)
    }

    inner class CandidateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvResultCandidateName)
        val tvParty: TextView = view.findViewById(R.id.tvResultParty)
        val tvVotes: TextView = view.findViewById(R.id.tvVoteCount)
        val tvPercentage: TextView = view.findViewById(R.id.tvVotePercentage)
        val progressBar: ProgressBar = view.findViewById(R.id.voteProgressBar)
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ResultItem.ElectionHeader -> TYPE_HEADER
        is ResultItem.CandidateResult -> TYPE_CANDIDATE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_result_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_result_candidate, parent, false)
            CandidateViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ResultItem.ElectionHeader -> {
                (holder as HeaderViewHolder).tvTitle.text = item.title
                holder.tvStatus.text = item.status
            }
            is ResultItem.CandidateResult -> {
                (holder as CandidateViewHolder).apply {
                    tvName.text = item.candidate.name
                    tvParty.text = item.candidate.party
                    tvVotes.text = "${item.candidate.voteCount} votes"
                    val percentage = if (item.totalVotes > 0)
                        (item.candidate.voteCount * 100 / item.totalVotes)
                    else 0
                    tvPercentage.text = "$percentage%"
                    progressBar.progress = percentage
                }
            }
        }
    }

    override fun getItemCount() = items.size
}