package es.sanchoo.capitulojusto.results

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import es.sanchoo.capitulojusto.Constants
import es.sanchoo.capitulojusto.R
import es.sanchoo.capitulojusto.auxiliares.Player
import kotlin.collections.set
import androidx.core.net.toUri


class registerFragment : Fragment() {
    private val db = FirebaseStorage.getInstance()

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
        val uriList = arguments?.getStringArrayList("uriList")


        val containerRecords = view.findViewById<LinearLayout>(R.id.containerRecords)
        containerRecords.removeAllViews()

        for (i in 0 until minOf(Constants.MAX_TURNOS, uriList?.size!!)) {
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
            val imageView = ImageView(requireContext()).apply {
                val sizeInPx = (48 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                    marginEnd = (16 * resources.displayMetrics.density).toInt()
                }
            }

            val imagePath = uriList[i].toUri()
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_loading)
                .into(imageView)

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
            row.addView(imageView)
            row.addView(registerLayout)
            containerRecords.addView(row)
        }
    }
}
