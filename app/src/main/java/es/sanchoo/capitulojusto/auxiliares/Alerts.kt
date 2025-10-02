package es.sanchoo.capitulojusto.auxiliares

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import es.sanchoo.capitulojusto.R
import kotlin.system.exitProcess

fun showNoInternetDialog(activity: Activity) {
    AlertDialog.Builder(activity)
        .setTitle("Sin conexión a Internet")
        .setMessage("Esta aplicación necesita conexión a Internet para funcionar correctamente.")
        .setPositiveButton("Aceptar") { _, _ ->
            activity.finishAffinity()
        }
        .setCancelable(false)
        .show()
}

fun showNoHighScoreDialog(activity: Activity, onContinue: () -> Unit) {
    AlertDialog.Builder(activity)
        .setTitle("Aviso")
        .setMessage("Con estos ajustes no entrarás en el top histórico.")
        .setPositiveButton("Continuar") { _, _ ->
            onContinue()
        }
        .setNegativeButton("Cambiar ajustes") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun showBackConfirmationDialog(activity: Activity, isEndActivity: Boolean = false) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle(R.string.alert_close_title)
    builder.setMessage(R.string.alert_close_message)
    builder.setPositiveButton(R.string.alert_close_accept) { _, _ ->
        if (isEndActivity){
            activity.finishAffinity()  // Cierra esta Activity y todas las que estén debajo en la pila
            exitProcess(0)    // Mata el proceso para que al abrir de nuevo se arranque limpio
        } else {
            activity.finish()
        }
    }
    builder.setNegativeButton(R.string.alert_close_dismiss) { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()
}

fun showNotEnoughPlayersDialog(activity: Activity) {
    AlertDialog.Builder(activity)
        .setTitle(R.string.alert_not_panels_title)
        .setMessage(R.string.alert_not_panels_message)
        .setPositiveButton(R.string.alert_not_panels_accept) { dialog, _ ->
            dialog.dismiss()
            activity.finish()
        }
        .show()
}