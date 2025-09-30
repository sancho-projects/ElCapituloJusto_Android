package es.sanchoo.capitulojusto

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import es.sanchoo.capitulojusto.Constants.MAX_CAP_DEFAULT
import es.sanchoo.capitulojusto.auxiliares.Player
import es.sanchoo.capitulojusto.menu.VPAdapter
import es.sanchoo.capitulojusto.results.highscoreFragment
import es.sanchoo.capitulojusto.results.registerFragment
import es.sanchoo.capitulojusto.results.resultsFragment
import kotlin.system.exitProcess

class EndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_end_screen)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val builder = AlertDialog.Builder(this@EndActivity)
                builder.setTitle(R.string.alert_close_title)
                builder.setMessage(R.string.alert_close_message)
                builder.setPositiveButton(R.string.alert_close_accept) { _, _ ->
                    finish()
                }
                builder.setNegativeButton(R.string.alert_close_dismiss) { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val results = intent.getParcelableArrayListExtra<Player>("rankedPlayers")
        val players = intent.getParcelableArrayListExtra<Player>("players")
        val panels = intent.getStringArrayListExtra("panels")
        val max_cap = intent.getIntExtra("max_cap", 1000)
        val easy = intent.getBooleanExtra("easy", true)
        val medium = intent.getBooleanExtra("medium", true)
        val hard = intent.getBooleanExtra("hard", true)
        val isManga = intent.getBooleanExtra("isManga", true)


        // MENÚS
        val viewPager: ViewPager2 = findViewById(R.id.endViewPager)
        val vpAdapter = VPAdapter(this)
        val tabLayout: TabLayout = findViewById(R.id.endTabLayout)


        val resultsFragment = resultsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("rankedPlayers", results)
            }
        }
        vpAdapter.addFragment(resultsFragment, getString(R.string.end_results_title))

        val registerFragment = registerFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("players", players)
                putStringArrayList("panels", panels)
                putInt("max_cap", max_cap)
            }
        }
        vpAdapter.addFragment(registerFragment, getString(R.string.end_register_title))

        val settingsAreCorrect = (max_cap == MAX_CAP_DEFAULT && easy && medium && hard)
        val highscoreFragment = highscoreFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("players", players)
                putBoolean("settingsAreCorrect", settingsAreCorrect)
                putBoolean("isManga", isManga)
            }
        }
        vpAdapter.addFragment(highscoreFragment, getString(R.string.end_highscore_title))

        viewPager.setAdapter(vpAdapter)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = vpAdapter.getTitle(position)
        }.attach()

    }

    fun onRestartPressed(view: View) { // TODO: hasta que descubra el error
//        finish()
        finishAffinity()  // Cierra esta Activity y todas las que estén debajo en la pila
        exitProcess(0)    // Mata el proceso para que al abrir de nuevo se arranque limpio
    }

}