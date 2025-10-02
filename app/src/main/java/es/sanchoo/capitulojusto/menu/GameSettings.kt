package es.sanchoo.capitulojusto.menu

import es.sanchoo.capitulojusto.auxiliares.Constants.MAX_CAP_DEFAULT

object GameSettings {
    var n_players: Int = 0
    var players_names: MutableList<String> = MutableList(4) { "Jugador" }
    var dificultad: MutableList<Boolean> = MutableList(3) { true }
    var max_cap: Int = 1000

    var isManga: Boolean = true

    fun isDefaultSettings(): Boolean {
        return max_cap == MAX_CAP_DEFAULT
                && dificultad[0]
                && dificultad[1]
                && dificultad[2]
    }
}