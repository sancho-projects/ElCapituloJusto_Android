package es.sanchoo.capitulojusto.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import es.sanchoo.capitulojusto.R

class jugadoresFragment : Fragment() {
    private lateinit var seekBar: SeekBar
    private val playersList = mutableListOf<TextView>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jugadores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BARRA DEL LÍMITE DE JUGADORES
        seekBar = view.findViewById(R.id.seekBarPlayers)
        playersList.add(view.findViewById(R.id.textJ1))
        playersList.add(view.findViewById(R.id.textJ2))
        playersList.add(view.findViewById(R.id.textJ3))
        playersList.add(view.findViewById(R.id.textJ4))

        // Configura el listener para la SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePlayersVisibility(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // VACÍO
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // O PASAR EL VALOR AHORA U OLVIDARME Y PEDIRLO CUANDO LE DÉ AL BOTÓN.
            }

        })

        updatePlayersVisibility(seekBar.progress)
    }

    private fun updatePlayersVisibility(progress: Int) {
        for (i in playersList.indices) {
            if (i <= progress) {
                playersList[i].visibility = View.VISIBLE
            } else {
                playersList[i].visibility = View.GONE
            }
        }
    }


    // GETTERS y SETTERS (CONSULTAS A VISTA)
    fun getPlayerName(i: Int): String {
        return playersList[i-1].text.toString()
    }

    fun getNumberOfPlayers(): Int {
        return seekBar.progress
    }


    fun setPlayerName(i: Int, name: String) {
        if (i in 1..playersList.size) {
            playersList[i - 1].setText(name)
        }
    }

    fun setNumberOfPlayers(n: Int) {
        seekBar.progress = n
        playersList.forEachIndexed { index, editText ->
            editText.visibility = if (index < n) View.VISIBLE else View.GONE
        }
    }

}