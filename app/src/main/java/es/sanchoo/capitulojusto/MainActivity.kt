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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import es.sanchoo.capitulojusto.Constants.MAX_CAP_DEFAULT
import es.sanchoo.capitulojusto.menu.GameSettings
import es.sanchoo.capitulojusto.menu.VPAdapter
import es.sanchoo.capitulojusto.menu.ajustesFragment
import es.sanchoo.capitulojusto.menu.jugadoresFragment
import es.sanchoo.capitulojusto.menu.reglasFragment
import es.sanchoo.capitulojusto.views.MenuView

class MainActivity : AppCompatActivity(), MenuView {
    lateinit var vpAdapter: VPAdapter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (!isNetworkAvailable()) {
            AlertDialog.Builder(this)
                .setTitle("Sin conexión a Internet")
                .setMessage("Esta aplicación necesita conexión a Internet para funcionar correctamente.")
                .setPositiveButton("Aceptar") { _, _ ->
                    finishAffinity() // Cierra la app completamente
                }
                .setCancelable(false) // Opcional: evita que se cierre tocando fuera
                .show()
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

        vpAdapter.addFragment(jugadoresFragment(), "Jugadores")
        vpAdapter.addFragment(reglasFragment(), "Cómo jugar")
        vpAdapter.addFragment(ajustesFragment(), "Ajustes")
        viewPager.setAdapter(vpAdapter)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = vpAdapter.getTitle(position)
        }.attach()


        // COMENZAR A JUGAR
        val buttonStartGame: Button = findViewById(R.id.startButton)
        buttonStartGame.setOnClickListener {
            val playerFragment = vpAdapter.getFragment(0) as? jugadoresFragment
            val customFragment = vpAdapter.getFragment(2) as? ajustesFragment

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

//                Log.w("DEBUG", "max_cap=${GameSettings.max_cap}, easy=${GameSettings.dificultad[0]}, medium=${GameSettings.dificultad[1]}, hard=${GameSettings.dificultad[2]}")

                if (!(GameSettings.max_cap == MAX_CAP_DEFAULT
                            && GameSettings.dificultad[0]
                            && GameSettings.dificultad[1]
                            && GameSettings.dificultad[2])) // Settings por defecto
                {
                    AlertDialog.Builder(this)
                        .setTitle("Aviso")
                        .setMessage("Con estos ajustes no entrarás en el top histórico.")
                        .setPositiveButton("Continuar") { _, _ ->
                            startActivity(Intent(this, GameActivity::class.java))
                        }
                        .setNegativeButton("Cambiar ajustes") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
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