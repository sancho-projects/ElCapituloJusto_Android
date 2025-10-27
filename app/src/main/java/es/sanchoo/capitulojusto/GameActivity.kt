package es.sanchoo.capitulojusto

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import es.sanchoo.capitulojusto.auxiliares.Panel
import es.sanchoo.capitulojusto.auxiliares.Player
import es.sanchoo.capitulojusto.auxiliares.applyValueFilter
import es.sanchoo.capitulojusto.menu.GameSettings
import com.bumptech.glide.Glide
import es.sanchoo.capitulojusto.auxiliares.Constants
import es.sanchoo.capitulojusto.auxiliares.showBackConfirmationDialog
import es.sanchoo.capitulojusto.auxiliares.showNotEnoughPlayersDialog


class GameActivity : AppCompatActivity() {
    private val db = FirebaseStorage.getInstance()
    private val viewModel: GameViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var sp: SoundPool
    private val soundIds = IntArray(7)

    private val imageURLCache = mutableMapOf<String, Uri>()
    private val uriList = ArrayList<String>()


    private val soundResIds = arrayOf(
        R.raw.answer_sound_0,
        R.raw.answer_sound_1,
        R.raw.answer_sound_2,
        R.raw.answer_sound_3,
        R.raw.answer_sound_4,
        R.raw.answer_sound_5,
        R.raw.answer_sound_6
    )

    @SuppressLint("SourceLockedOrientationActivity", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_game)

        viewModel.showNotEnoughPanels.observe(this) { show ->
            if (show) {
                showNotEnoughPlayersDialog(this)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackConfirmationDialog(this@GameActivity)
            }
        })

        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.players.clear()

        val checkButton = findViewById<Button>(R.id.checkButton)
        checkButton.isClickable = false
        checkButton.alpha = 0.5f

        val nameJ1: TextView = findViewById(R.id.textNameJ1)
        val nameJ2: TextView = findViewById(R.id.textNameJ2)
        val nameJ3: TextView = findViewById(R.id.textNameJ3)
        val nameJ4: TextView = findViewById(R.id.textNameJ4)
        nameJ1.text = GameSettings.players_names[0]
        nameJ2.text = GameSettings.players_names[1]
        nameJ3.text = GameSettings.players_names[2]
        nameJ4.text = GameSettings.players_names[3]

        val guessJ1: EditText = findViewById(R.id.guessTextJ1)
        val guessJ2: EditText = findViewById(R.id.guessTextJ2)
        val guessJ3: EditText = findViewById(R.id.guessTextJ3)
        val guessJ4: EditText = findViewById(R.id.guessTextJ4)
        applyValueFilter(guessJ1, true)
        applyValueFilter(guessJ2, true)
        applyValueFilter(guessJ3, true)
        applyValueFilter(guessJ4, true)

        progressBar = findViewById<ProgressBar>(R.id.progressBarGame)

        setPlayersInvisibles()
        updateTurn()

        viewModel.setConditions()
        viewModel.setPlayers()

        viewModel.setTable()
        viewModel.tableReady.observe(this) { ready ->
            if (ready) {
                loadImages(viewModel.getImgsFromPanels())
                viewModel.nextPanel()?.let { setImage(it) }
            }
        }


        viewModel.finishGame.observe(this) { shouldFinish ->
            if (shouldFinish) {
                val intent = Intent(this, EndActivity::class.java)
                val results = viewModel.getResults()

                // Creo que tantos intents, y además Parcelables son malos para el rendimiento, habría que cambiarlos en el futuro
                intent.putParcelableArrayListExtra(
                    "players",
                    ArrayList<Player>(viewModel.players)
                )
                intent.putParcelableArrayListExtra(
                    "rankedPlayers",
                    ArrayList<Player>(results)
                )

                intent.putExtra("max_cap", GameSettings.max_cap)
                intent.putExtra("easy", GameSettings.dificultad[0])
                intent.putExtra("medium", GameSettings.dificultad[1])
                intent.putExtra("hard", GameSettings.dificultad[2])
                intent.putExtra("isManga", GameSettings.isManga)

                intent.putStringArrayListExtra("uriList",uriList)

                startActivity(intent)
                finish()
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        sp = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        for (i in soundResIds.indices) {
            soundIds[i] = sp.load(this, soundResIds[i], 1)
        }
    }

    fun onNextPressed(view: View){

        val checkButton = findViewById<Button>(R.id.checkButton)
        val result = viewModel.onNext(getChapters())
        if (result != null){
            checkButton.text = getString(R.string.game_checkbutton_guess)
            setImage(result)
            clearFeedback()
            clearGuesses()
            updateTurn()

        } else {
            checkButton.text = getString(R.string.game_checkbutton_next)
            updateScore()
            showFeedback()
        }


     }

    private fun showFeedback() {
        val result: TextView = findViewById(R.id.textResult)

        val rightChapter: Int = viewModel.currentPanel!!.rightChapter
        result.text = getString(R.string.game_correct_answer, rightChapter)

        val soundIndex = viewModel.getSoundIndex()
        sp.play(soundIds[soundIndex], 0.5f, 0.5f, 1, 0, 1f)

    }

    private fun updateScore() {
        val scoreJ1: TextView = findViewById(R.id.textScoreJ1)
        val scoreJ2: TextView = findViewById(R.id.textScoreJ2)
        val scoreJ3: TextView = findViewById(R.id.textScoreJ3)
        val scoreJ4: TextView = findViewById(R.id.textScoreJ4)


        scoreJ1.text = viewModel.players[0].score.toString()
        if (viewModel.players.size > 1) {
            scoreJ2.text = viewModel.players[1].score.toString()
            if (viewModel.players.size > 2) {
                scoreJ3.text = viewModel.players[2].score.toString()
                if (viewModel.players.size > 3) {
                    scoreJ4.text = viewModel.players[3].score.toString()
                }
            }
        }
    }

    private fun updateTurn() {
        val numPanel: TextView = findViewById(R.id.textNumPanel)
        val currentTurn: Int = viewModel.turn
        val totalTurns: Int = Constants.MAX_TURNOS

        numPanel.text = getString(R.string.game_vinyeta, currentTurn, totalTurns)
    }

    private fun clearGuesses() {
        val guessJ1: EditText = findViewById(R.id.guessTextJ1)
        val guessJ2: EditText = findViewById(R.id.guessTextJ2)
        val guessJ3: EditText = findViewById(R.id.guessTextJ3)
        val guessJ4: EditText = findViewById(R.id.guessTextJ4)

        guessJ1.setText("")
        guessJ2.setText("")
        guessJ3.setText("")
        guessJ4.setText("")
    }

    private fun clearFeedback() {
        val result: TextView = findViewById(R.id.textResult)
        result.text = getString(R.string.game_guess_answer)
    }

    private fun setPlayersInvisibles() {
        val n = GameSettings.n_players
        val auxSet = mutableSetOf<View>()
        if (n < 3){ // INVISIBLE JUGADOR 4
            val nameJ4: TextView = findViewById(R.id.textNameJ4)
            val guessJ4: EditText = findViewById(R.id.guessTextJ4)
            val scoreJ4: TextView = findViewById(R.id.textScoreJ4)
            auxSet.add(nameJ4)
            auxSet.add(guessJ4)
            auxSet.add(scoreJ4)

            if (n < 2){ // INVISIBLE JUGADOR 3
                val nameJ3: TextView = findViewById(R.id.textNameJ3)
                val guessJ3: EditText = findViewById(R.id.guessTextJ3)
                val scoreJ3: TextView = findViewById(R.id.textScoreJ3)
                auxSet.add(nameJ3)
                auxSet.add(guessJ3)
                auxSet.add(scoreJ3)

                if (n < 1){ // INVISIBLE JUGADOR 2
                    val nameJ2: TextView = findViewById(R.id.textNameJ2)
                    val guessJ2: EditText = findViewById(R.id.guessTextJ2)
                    val scoreJ2: TextView = findViewById(R.id.textScoreJ2)
                    auxSet.add(nameJ2)
                    auxSet.add(guessJ2)
                    auxSet.add(scoreJ2)
                }
            }
        }
        for (view in auxSet){
            view.visibility = View.INVISIBLE
        }

    }

    private fun setImage(image: Panel) {
        val imageView: ImageView = findViewById(R.id.imageView)
        val imagePath = image.imgURL
        val cachedUri = imageURLCache[imagePath]

        if (cachedUri != null) {
            // La URL ya está en caché, cargar directamente
            uriList.add(cachedUri.toString())

            Glide.with(this)
                .load(cachedUri)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_loading)
                .into(imageView)

        } else{
            val storageRef = db.getReference(imagePath)
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                imageURLCache[imagePath] = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.default_image)
                    .into(imageView)

                progressBar.visibility = View.GONE
                uriList.add(uri.toString())

            }.addOnFailureListener {
                imageView.setImageResource(R.drawable.default_image)
            }
        }


    }

    private fun loadImages(images: MutableList<String>) {
        var consultasFinalizadas = 0
        for (imagePath in images) {
            if (imagePath.isNotEmpty()) {
                val storageRef = db.getReference(imagePath)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    imageURLCache[imagePath] = uri
                    Glide.with(this)
                        .load(uri)
                        .preload()

                    consultasFinalizadas++
                    if(consultasFinalizadas == images.size){
                        val checkButton = findViewById<Button>(R.id.checkButton)
                        checkButton.isClickable = true
                        checkButton.alpha = 1.0f

                        progressBar.visibility = View.GONE
                    }

                }

            }
        }
    }
    private fun getChapters(): List<Int> {
        fun getTextOf(guess: EditText): Int{
            if (guess.text.toString() == "") {
                return 0
            }
            return guess.text.toString().toInt()
        }

        val chapters: MutableList<Int> = mutableListOf()

        val guessJ1: EditText = findViewById(R.id.guessTextJ1)
        if (guessJ1.visibility != View.INVISIBLE){
            chapters.add(getTextOf(guessJ1))
        }
        val guessJ2: EditText = findViewById(R.id.guessTextJ2)
        if (guessJ2.visibility != View.INVISIBLE){
            chapters.add(getTextOf(guessJ2))
        }
        val guessJ3: EditText = findViewById(R.id.guessTextJ3)
        if (guessJ3.visibility != View.INVISIBLE){
            chapters.add(getTextOf(guessJ3))
        }
        val guessJ4: EditText = findViewById(R.id.guessTextJ4)
        if (guessJ4.visibility != View.INVISIBLE){
            chapters.add(getTextOf(guessJ4))
        }
        return chapters
    }



    override fun onDestroy() {
        super.onDestroy()
        sp.release()
    }

}