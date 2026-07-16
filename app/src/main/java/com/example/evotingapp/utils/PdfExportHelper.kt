package com.example.evotingapp.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.evotingapp.models.Candidate
import com.example.evotingapp.models.Election
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun exportResults(
        context: Context,
        election: Election,
        candidates: List<Candidate>
    ): Boolean {
        return try {
            val fileName = "Election_Results_${System.currentTimeMillis()}.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val outputStream = FileOutputStream(file)
            val writer = outputStream.bufferedWriter()

            // Write simple text-based PDF content
            val totalVotes = candidates.sumOf { it.voteCount }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = dateFormat.format(Date())

            writer.write("E-VOTING SYSTEM - ELECTION RESULTS\n")
            writer.write("=====================================\n\n")
            writer.write("Election: ${election.title}\n")
            writer.write("Category: ${election.category}\n")
            if (election.county.isNotEmpty()) writer.write("County: ${election.county}\n")
            writer.write("Status: ${election.status}\n")
            writer.write("Generated: $date\n\n")
            writer.write("RESULTS\n")
            writer.write("-------\n\n")

            val sorted = candidates.sortedByDescending { it.voteCount }
            sorted.forEachIndexed { index, candidate ->
                val percentage = if (totalVotes > 0)
                    (candidate.voteCount * 100.0 / totalVotes).toInt() else 0
                writer.write("${index + 1}. ${candidate.name}\n")
                writer.write("   Party: ${candidate.party}\n")
                writer.write("   Votes: ${candidate.voteCount} ($percentage%)\n\n")
            }

            writer.write("Total Votes Cast: $totalVotes\n")
            writer.write("=====================================\n")
            writer.write("E-Voting System - Secure & Transparent\n")

            writer.close()
            outputStream.close()

            // Share the file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Election Results - ${election.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Results"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}