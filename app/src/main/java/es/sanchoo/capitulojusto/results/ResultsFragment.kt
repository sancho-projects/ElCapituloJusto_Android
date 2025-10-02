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
import es.sanchoo.capitulojusto.auxiliares.Player


class ResultsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rankedPlayers = arguments?.getParcelableArrayList<Player>("rankedPlayers") ?: arrayListOf()

        val highscoreContainer = view.findViewById<LinearLayout>(R.id.highscoreContainer)

        val colors = listOf(
            R.color.gold,
            R.color.silver,
            R.color.bronze,
            R.color.gray
        )

        var currentRank = 1
        for (i in rankedPlayers.indices) {
            val player = rankedPlayers[i]

            // Si no es el primer jugador y la puntuación es igual al anterior, mantienen el mismo rango
            if (i == 0 || player.score != rankedPlayers[i - 1].score) {
                currentRank = i + 1
            }

            val playerContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16) // Margen entre posiciones
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(resources.getColor(colors.getOrElse(currentRank - 1) { R.color.gray }))
                setPadding(16, 16, 16, 16)
            }

            val positionText = TextView(context).apply {
                text = currentRank.toString()
                textSize = 18f
                setTypeface(resources.getFont(R.font.komika), Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 12
                }
            }

            val nameText = TextView(context).apply {
                text = player.name
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                typeface = resources.getFont(R.font.komika)

            }

            val scoreText = TextView(context).apply {
                text = player.score.toString()
                textSize = 18f
                setTypeface(resources.getFont(R.font.komika), Typeface.BOLD)

            }

            playerContainer.addView(positionText)
            playerContainer.addView(nameText)
            playerContainer.addView(scoreText)

            highscoreContainer.addView(playerContainer)
        }
    }
}