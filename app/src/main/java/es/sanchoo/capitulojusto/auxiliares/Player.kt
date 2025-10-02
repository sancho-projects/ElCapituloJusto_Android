package es.sanchoo.capitulojusto.auxiliares

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class Player(
    var name: String,
    var score: Int = 0,
    val register: MutableList<Int> = mutableListOf()
) : Comparable<Player>, Parcelable {

    init {
        name = name.uppercase()
    }

    @IgnoredOnParcel
    var minScore: Int = Int.MAX_VALUE
    @IgnoredOnParcel
    var maxScore: Int = Int.MIN_VALUE

    fun getTurnMinScores(): List<Int> {
        return getTurnMinOrMax(true)
    }
    fun getTurnMaxScores(): List<Int> {
        return getTurnMinOrMax(false)
    }

    fun getTurnMinOrMax(getMinimum: Boolean): List<Int> {
        val optScore = if (getMinimum) minScore else maxScore
        val list = mutableListOf<Int>()
        val firstIndex = register.indexOf(optScore)
        var lastIndex = register.lastIndexOf(optScore)
        list.add(lastIndex)
        while (firstIndex != lastIndex) {
            val subRegister = register.subList(0, lastIndex)
            lastIndex = subRegister.lastIndexOf(optScore)
            list.add(lastIndex)
        }
        return list
    }

    fun getScoreAtTurn(turn: Int) = register.getOrNull(turn) ?: -1

    fun addScore(score: Int) {
        this.score += score
        addRegister(score)
    }

    private fun addRegister(score: Int) {
        register.add(score)
        if (score < minScore) minScore = score
        if (score > maxScore) maxScore = score
    }

    fun restartGame() {
        score = 0
        register.clear()
        minScore = Int.MAX_VALUE
        maxScore = Int.MIN_VALUE
    }

    override fun compareTo(other: Player): Int {
        return this.score.compareTo(other.score)
    }
}
