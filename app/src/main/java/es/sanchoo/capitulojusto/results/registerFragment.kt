package es.sanchoo.capitulojusto.results

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import es.sanchoo.capitulojusto.Constants
import es.sanchoo.capitulojusto.R
import es.sanchoo.capitulojusto.auxiliares.Player


class registerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val players = arguments?.getParcelableArrayList<Player>("players") ?: arrayListOf()
        val panels = arguments?.getStringArrayList("panels") ?: arrayListOf()

        val containerRecords = view.findViewById<LinearLayout>(R.id.containerRecords)
        containerRecords.removeAllViews()

        for (i in 0 until minOf(Constants.MAX_TURNOS, panels.size)) {
            // TURNO X
            val turnTitle = TextView(requireContext()).apply {
                text = getString(R.string.end_register_turn, i + 1)
                textSize = 20f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 8) // margen superior e inferior
                typeface = resources.getFont(R.font.caslon_antique_bold)
            }
            containerRecords.addView(turnTitle)


            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            // VIÑETA
            val bullet = View(requireContext()).apply {
                val sizeInPx = (48 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                    marginEnd = (16 * resources.displayMetrics.density).toInt()
                }
                val resourceId = resources.getIdentifier(panels.get(i), "drawable", requireContext().packageName)
                background = if (resourceId != 0) {
                    ContextCompat.getDrawable(requireContext(), resourceId)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.default_image)
                }
            }

            // REGISTRO DE LOS JUGADORES
            val registerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val minScore = players.minOfOrNull { it.getScoreAtTurn(i) } ?: 0

            players.forEach { player ->
                val score = player.getScoreAtTurn(i)
                val playerText = TextView(requireContext()).apply {
                    text = getString(R.string.end_register_player_scored, player.name, score)
                    textSize = 18f
                    if (score == minScore) {
                        setTextColor(Color.WHITE)
                        typeface = resources.getFont(R.font.caslon_antique_bold)
                    } else {
                        setTextColor(Color.BLACK)
                        typeface = resources.getFont(R.font.caslon_antique_regular)
                    }
                }
                registerLayout.addView(playerText)
            }
            row.addView(bullet)
            row.addView(registerLayout)
            containerRecords.addView(row)
        }
    }
}
