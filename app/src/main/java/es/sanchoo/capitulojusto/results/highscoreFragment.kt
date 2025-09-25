package es.sanchoo.capitulojusto.results

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import es.sanchoo.capitulojusto.R
import es.sanchoo.capitulojusto.auxiliares.HighScoreManager
import es.sanchoo.capitulojusto.auxiliares.Player

class highscoreFragment : Fragment() {

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

        val containerLayout = view.findViewById<LinearLayout>(R.id.highscoreContainer)

        val manager = HighScoreManager(requireContext())
        val top10 = manager.getTop10()

        val currentPlayers = players ?: listOf()

        var currentRank = 1
        var prev_score: Int? = null

        top10.forEachIndexed { index, player ->
            val isCurrentPlayer = currentPlayers.any { it.name == player.name && it.score == player.score }

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


            // Posición
            if (player.score != prev_score) {
                currentRank = index + 1
                prev_score = player.score
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
                text = player.name.uppercase()
                textSize = 24F
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                typeface = resources.getFont(R.font.caslon_antique_bold)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(resources.getColor(R.color.wanted_text))


            }

            // Puntuación
            val scoreText = TextView(context).apply {
                text = "Pts. " + "%,d".format(player.score) + " —"
                textSize = 18f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                letterSpacing = 0.2f
                setTypeface(resources.getFont(R.font.caslon_antique_regular), Typeface.BOLD)
                setTextColor(resources.getColor(R.color.wanted_text))
            }

            playerContainer.addView(positionText)
            playerContainer.addView(nameText)

            containerLayout.addView(playerContainer)
            containerLayout.addView(scoreText)


        }


    }
}
