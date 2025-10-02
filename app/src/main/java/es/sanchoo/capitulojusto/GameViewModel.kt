package es.sanchoo.capitulojusto

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import es.sanchoo.capitulojusto.Constants.MAX_CAP_DEFAULT
import es.sanchoo.capitulojusto.auxiliares.Panel
import es.sanchoo.capitulojusto.auxiliares.Player
import java.util.PriorityQueue
import java.util.Random
import kotlin.math.abs
import kotlin.math.min
import es.sanchoo.capitulojusto.Constants.MAX_TURNOS
import es.sanchoo.capitulojusto.menu.GameSettings

enum class State {
    GUESSING, CHECKING
}

const val CORRECT_ANSWER = -10

class GameViewModel: ViewModel() {
    val solutions = FirebaseFirestore.getInstance()
        .collection(
        if (GameSettings.isManga)
            "solutions_manga"
        else
            "solutions_anime"
    )

    var turn: Int = 1
    var players: MutableList<Player> = mutableListOf()

    var currentPanel: Panel? = null
    var panels: MutableList<Panel> = mutableListOf()
    private var scoresOfTurn = mutableListOf<Int>()

    private var conditionsSet: MutableSet<Int> = mutableSetOf()
    private var limitChapter: Int = MAX_CAP_DEFAULT
    private var isManga: Boolean = true

    private var state : State = State.GUESSING

    private val _finishGame = MutableLiveData<Boolean>()
    val finishGame: LiveData<Boolean> = _finishGame

    private val _showNotEnoughPanels = MutableLiveData<Boolean>()
    val showNotEnoughPanels: LiveData<Boolean> get() = _showNotEnoughPanels

    private val _tableReady = MutableLiveData<Boolean>()
    val tableReady: LiveData<Boolean> = _tableReady

    fun onNext(chapters: List<Int>): Panel?{
        when(state){
            State.GUESSING -> {
                updateScores(chapters)
                state = State.CHECKING
                turn++
                return null
            }
            State.CHECKING -> {
                state = State.GUESSING
                return nextPanel()
            }
        }
    }

    fun setConditions(){
        limitChapter = GameSettings.max_cap

        val admitEasy = GameSettings.dificultad[0]
        val admitMedium = GameSettings.dificultad[1]
        val admitHard = GameSettings.dificultad[2]

        if (admitEasy) conditionsSet.add(1)
        if (admitMedium) conditionsSet.add(2)
        if (admitHard) conditionsSet.add(3)

        isManga = GameSettings.isManga
    }

    fun setPlayers(){
        players.clear()

        val n = GameSettings.n_players
        players.add(Player(GameSettings.players_names[0]))
        if (n > 0) {
            players.add(Player(GameSettings.players_names[1]))
            if (n > 1) {
                players.add(Player(GameSettings.players_names[2]))
                if (n > 2) {
                    players.add(Player(GameSettings.players_names[3]))
                }
            }
        }
    }


    // TODO: FIREBASE
    fun setTable() {
        solutions.get().addOnSuccessListener { query ->
//            Log.d("Firestore", "Total docs leídos = ${query.documents.size}")

            val validPanels = query.documents
                .mapNotNull {
                    val rightChapter = (it.get("chapter") as? String)?.toInt()
                    val difficulty = (it.get("difficulty") as? String)?.toInt()
                    val imageURL = it.get("imageURL") as? String

                    Log.d("FirestoreDoc", "Doc: chapter=$rightChapter, difficulty=$difficulty, imageURL=$imageURL")

                    if (rightChapter != null && difficulty != null && imageURL != null &&
                        rightChapter <= limitChapter && conditionsSet.contains(difficulty)) {
                        Panel(rightChapter, imageURL)
                    } else null
                }
                .shuffled(Random(System.nanoTime()))
                .take(MAX_TURNOS)

            Log.d("Firestore", "Total panels válidos = ${validPanels.size}")

            panels.clear()
            panels.addAll(validPanels)

            if (panels.size < MAX_TURNOS) {
                _showNotEnoughPanels.value = true
            } else {
                _tableReady.value = true
            }
        }.addOnFailureListener {
            _showNotEnoughPanels.value = true
        }

    }

    fun nextPanel(): Panel? {
        currentPanel = null
        if (turn > panels.size) {
            onGameFinished()
        } else {
            currentPanel = panels[turn-1]
        }
        return currentPanel

    }

    private fun updateScores(chapters: List<Int>) {
        val rightChapter = currentPanel!!.rightChapter
        scoresOfTurn.clear()
        for (i in 0 until players.size) {
            val player = players[i]
            val guess = chapters[i]
            val score = if (guess == rightChapter) CORRECT_ANSWER
                        else abs(guess - rightChapter)
            player.addScore(score)
            scoresOfTurn.add(score)
        }
    }


    private fun onGameFinished() {
        _finishGame.value = true
//        restart()
    }

    fun restart() {
//        _finishGame.value = false
        turn = 1
        players.forEach { it.restartGame() }
        panels.clear()
        state = State.GUESSING
    }

    fun getResults(): List<Player>{
        val ranking = PriorityQueue<Player>()

        players.forEach { ranking.add(it) }

        val result = mutableListOf<Player>()
        while (ranking.isNotEmpty()) {
            result.add(ranking.poll()!!)
        }

        return result
    }

    fun getImgsFromPanels(): MutableList<String> {
        val imgPanels = mutableListOf<String>()
        panels.forEach { imgPanels.add(it.imgURL) }
        return imgPanels
    }

    fun getSoundIndex(): Int {
        return when {
            scoresOfTurn.any { it == CORRECT_ANSWER } && scoresOfTurn.count { it == CORRECT_ANSWER } > 1 -> 0 // Varios jugadores aciertan
            scoresOfTurn.any { it == CORRECT_ANSWER } -> 1
            scoresOfTurn.any { it < 10 } -> 2
            scoresOfTurn.any { it < 50 } -> 3
            scoresOfTurn.any { it > 500 } -> 6
            scoresOfTurn.any { it < 100 } -> 4
            else -> 5
        }
    }
}
