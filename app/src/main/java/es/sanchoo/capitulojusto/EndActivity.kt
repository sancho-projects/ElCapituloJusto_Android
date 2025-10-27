package es.sanchoo.capitulojusto

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import es.sanchoo.capitulojusto.auxiliares.Constants.MAX_CAP_DEFAULT
import es.sanchoo.capitulojusto.auxiliares.Player
import es.sanchoo.capitulojusto.auxiliares.VPAdapter
import es.sanchoo.capitulojusto.auxiliares.showBackConfirmationDialog
import es.sanchoo.capitulojusto.results.HighscoreFragment
import es.sanchoo.capitulojusto.results.RegisterFragment
import es.sanchoo.capitulojusto.results.ResultsFragment
import kotlin.system.exitProcess

class EndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_end_screen)


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackConfirmationDialog(this@EndActivity, true)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val results = intent.getParcelableArrayListExtra<Player>("rankedPlayers")
        val players = intent.getParcelableArrayListExtra<Player>("players")

        val max_cap = intent.getIntExtra("max_cap", 1000)
        val easy = intent.getBooleanExtra("easy", true)
        val medium = intent.getBooleanExtra("medium", true)
        val hard = intent.getBooleanExtra("hard", true)
        val isManga = intent.getBooleanExtra("isManga", true)

        val uriList = intent.getStringArrayListExtra("uriList")


        // MENÚS
        val viewPager: ViewPager2 = findViewById(R.id.endViewPager)
        val vpAdapter = VPAdapter(this)
        val tabLayout: TabLayout = findViewById(R.id.endTabLayout)


        val resultsFragment = ResultsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("rankedPlayers", results)
            }
        }
        vpAdapter.addFragment(resultsFragment, getString(R.string.end_results_title))

        val registerFragment = RegisterFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("players", players)
                putStringArrayList("uriList", uriList)
                putInt("max_cap", max_cap)
            }
        }
        vpAdapter.addFragment(registerFragment, getString(R.string.end_register_title))

        val settingsAreCorrect = (max_cap == MAX_CAP_DEFAULT && easy && medium && hard)
        val highscoreFragment = HighscoreFragment().apply {
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

    fun onRestartPressed(view: View) {
        finishAffinity()  // Cierra esta Activity y todas las que estén debajo en la pila
        exitProcess(0)    // Mata el proceso para que al abrir de nuevo se arranque limpio
    }

}