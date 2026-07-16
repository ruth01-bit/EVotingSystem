package com.example.evotingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.evotingapp.databinding.ItemVoterBinding
import com.example.evotingapp.models.User

class VoterVerifyAdapter(
    private val voters: List<User>,
    private val onVerifyClick: (User) -> Unit
) : RecyclerView.Adapter<VoterVerifyAdapter.VoterViewHolder>() {

    inner class VoterViewHolder(private val binding: ItemVoterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvVoterName.text = user.fullName
            binding.tvVoterEmail.text = user.email
            binding.tvNationalId.text = "ID: ${user.nationalId}"
            binding.tvPhone.text = "Phone: ${user.phone}"
            binding.btnVerify.setOnClickListener { onVerifyClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoterViewHolder {
        val binding = ItemVoterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoterViewHolder, position: Int) {
        holder.bind(voters[position])
    }

    override fun getItemCount() = voters.size
}