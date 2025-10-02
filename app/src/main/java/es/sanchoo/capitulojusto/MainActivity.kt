package es.sanchoo.capitulojusto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import es.sanchoo.capitulojusto.auxiliares.showNoHighScoreDialog
import es.sanchoo.capitulojusto.auxiliares.showNoInternetDialog
import es.sanchoo.capitulojusto.menu.GameSettings
import es.sanchoo.capitulojusto.menu.GameSettings.isDefaultSettings
import es.sanchoo.capitulojusto.auxiliares.VPAdapter
import es.sanchoo.capitulojusto.menu.AjustesFragment
import es.sanchoo.capitulojusto.menu.JugadoresFragment
import es.sanchoo.capitulojusto.menu.ReglasFragment

class MainActivity : AppCompatActivity() {
    lateinit var vpAdapter: VPAdapter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (!isNetworkAvailable()) {
            showNoInternetDialog(this)
        }
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        // MENÚS
        val viewPager: ViewPager2 = findViewById(R.id.viewpager)
        vpAdapter = VPAdapter(this)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        vpAdapter.addFragment(JugadoresFragment(), "Jugadores")
        vpAdapter.addFragment(ReglasFragment(), "Cómo jugar")
        vpAdapter.addFragment(AjustesFragment(), "Ajustes")
        viewPager.setAdapter(vpAdapter)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = vpAdapter.getTitle(position)
        }.attach()


        // COMENZAR A JUGAR
        val buttonStartGame: Button = findViewById(R.id.startButton)
        buttonStartGame.setOnClickListener {
            val playerFragment = vpAdapter.getFragment(0) as? JugadoresFragment
            val customFragment = vpAdapter.getFragment(2) as? AjustesFragment

            if (playerFragment != null && customFragment != null) {
                GameSettings.players_names[0] = playerFragment.getPlayerName(1)
                GameSettings.players_names[1] = playerFragment.getPlayerName(2)
                GameSettings.players_names[2] = playerFragment.getPlayerName(3)
                GameSettings.players_names[3] = playerFragment.getPlayerName(4)
                GameSettings.n_players = playerFragment.getNumberOfPlayers()
                GameSettings.max_cap = customFragment.getMaximumOfChapters()
                GameSettings.dificultad[0] = customFragment.getEasyValue()
                GameSettings.dificultad[1] = customFragment.getMediumValue()
                GameSettings.dificultad[2] = customFragment.getHardValue()
                GameSettings.isManga = customFragment.getIsManga()

                if (!isDefaultSettings()){
                    showNoHighScoreDialog(this){
                        startActivity(Intent(this, GameActivity::class.java))
                    }
                } else {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}