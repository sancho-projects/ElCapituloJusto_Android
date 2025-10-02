package es.sanchoo.capitulojusto.results

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import es.sanchoo.capitulojusto.R
import es.sanchoo.capitulojusto.auxiliares.Player


data class HighScoreEntry(
    val name: String,
    val score: Int,
    val medio: String
)

class HighscoreFragment : Fragment() {
    private val tamHighScore: Long = 10
    private val db = Firebase.firestore
    private val collection = "highscores"

    private var players: ArrayList<Player>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players = arguments?.getParcelableArrayList("players")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_highscore, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarHighScore)
        progressBar.visibility = View.VISIBLE

        val actualizable = arguments?.getBoolean("settingsAreCorrect") ?: true
        val isManga = arguments?.getBoolean("isManga") ?: true
        val medio = if (isManga) "M" else "A"

        if (actualizable) {
            updateHighScore(players, medio)
        }

        val containerLayout = view.findViewById<LinearLayout>(R.id.highscoreContainer)
        val currentPlayers = players ?: listOf()

        var currentRank = 1
        var prevScore: Int? = null

        db.collection(collection)
            .orderBy("score", Query.Direction.ASCENDING)
            .limit(tamHighScore)
            .get()
            .addOnSuccessListener { result ->
                for ((index, document) in result.withIndex()) {
                    val name = document.getString("name") ?: "Desconocido"
                    val score = document.getLong("score")?.toInt() ?: -51
                    val medio = document.getString("medio") ?: "?"

                    val isCurrentPlayer = currentPlayers.any { it.name == name && it.score == score }

                    // Crear contenedor dinámico
                    val playerContainer = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 4) // Margen entre posiciones
                        }
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        if (isCurrentPlayer) {
                            setBackgroundColor(resources.getColor(R.color.gold))
                        } else {
                            setBackgroundColor(resources.getColor(R.color.gray))
                        }
                        setPadding(8, 8, 8, 8)
                    }

                    // Posicion
                    if (prevScore != score) {
                        currentRank = index + 1
                        prevScore = score
                    }
                    val positionText = TextView(context).apply {
                        text = currentRank.toString()
                        textSize = 18f
                        setTypeface(resources.getFont(R.font.caslon_antique_bold), Typeface.BOLD)
                        setTextColor(resources.getColor(R.color.wanted_text))

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 12
                        }
                    }

                    // Nombre
                    val nameText = TextView(context).apply {
                        text = name.uppercase()
                        textSize = 24F
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        typeface = resources.getFont(R.font.caslon_antique_bold)
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(resources.getColor(R.color.wanted_text))
                    }

                    // Puntuación
                    val scoreTextView = TextView(context).apply {
                        val scoreText = "Pts. " + "%,d".format(score) + " — (" + medio + ")"
                        text = scoreText
                        textSize = 18f
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        letterSpacing = 0.2f
                        setTypeface(resources.getFont(R.font.caslon_antique_regular), Typeface.BOLD)
                        setTextColor(resources.getColor(R.color.wanted_text))
                    }

                    playerContainer.addView(positionText)
                    playerContainer.addView(nameText)

                    containerLayout.addView(playerContainer)
                    containerLayout.addView(scoreTextView)
                }
                progressBar.visibility = View.GONE

            }
            .addOnFailureListener { exception ->
                Log.w("HighScoreFragment", "Error getting documents: ", exception)
                progressBar.visibility = View.VISIBLE

            }


    }

    fun updateHighScore(players: ArrayList<Player>?, medio: String) {
        players?.forEach { player ->
            val entry = HighScoreEntry(player.name, player.score, medio)

            db.collection(collection)
                .add(entry)
                .addOnSuccessListener {
                    Toast.makeText(context, "Se han registrado los resultados", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al editar el histórico", Toast.LENGTH_SHORT)
                        .show()
                }

        }
    }
}
