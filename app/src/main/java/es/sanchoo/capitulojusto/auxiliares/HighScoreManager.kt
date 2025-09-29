package es.sanchoo.capitulojusto.auxiliares

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.sanchoo.capitulojusto.Constants.MAX_CAP_DEFAULT
import es.sanchoo.capitulojusto.menu.GameSettings
import java.io.File

// IMPLEMENTACIÓN REALITZADA POR CHAT GPT

data class HighScoreEntry(
    val name: String,
    val score: Int,
    val media: String
)

class HighScoreManager(private val context: Context) {

    private val fileName = "highscores.json"
    private val capacity = 10
    private var highscores: MutableList<HighScoreEntry> = mutableListOf()

    init {
        readFile()
    }

    private fun readFile() {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                file.createNewFile()
                highscores = mutableListOf()
                return
            }
            val text = file.readText()
            if (text.isNotEmpty()) {
                val type = object : TypeToken<MutableList<HighScoreEntry>>() {}.type
                highscores = Gson().fromJson(text, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            highscores = mutableListOf()
        }
    }

    private fun writeFile() {
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(Gson().toJson(highscores))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTop10(): List<HighScoreEntry> {
        return highscores.sortedBy { it.score }.take(capacity)
    }

    fun updateHighScore(players: ArrayList<Player>?, max_cap: Int, easy: Boolean, medium: Boolean, hard: Boolean, isManga: Boolean) {
        val media = if (isManga) "M" else "A"
        if (settingsAreCorrect(max_cap, easy, medium, hard)) {
            players?.forEach { player ->
                updateIfEligible(player, media)
            }
        }
    }

    private fun updateIfEligible(player: Player, media: String): Boolean {

        if (player.score <= getMaxScore()) {
            val entry = HighScoreEntry(player.name, player.score, media)
            highscores.add(entry)
            highscores = highscores.sortedBy { it.score }.take(capacity).toMutableList()
            writeFile()
            return true
        }
        return false
    }

    fun settingsAreCorrect(maxCap: Int = GameSettings.max_cap,
                           easy: Boolean = GameSettings.dificultad[0],
                           medium: Boolean = GameSettings.dificultad[1],
                           hard: Boolean = GameSettings.dificultad[2]): Boolean {
        Log.w("DEBUG", "max_cap=${maxCap}, easy=${easy}, medium=${medium}, hard=${hard}")
        return maxCap == MAX_CAP_DEFAULT && easy && medium && hard
    }



    private fun getMaxScore(): Int {
        if (highscores.size < capacity) return Int.MAX_VALUE
        return highscores.maxOf { it.score }
    }


}
