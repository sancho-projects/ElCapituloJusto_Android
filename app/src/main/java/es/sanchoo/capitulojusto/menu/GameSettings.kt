package es.sanchoo.capitulojusto.menu

object GameSettings {
    var n_players: Int = 0
    var players_names: MutableList<String> = MutableList(4) { "Jugador" }
    var dificultad: MutableList<Boolean> = MutableList(3) { true }
    var max_cap: Int = 1000

    var isManga: Boolean = true
}