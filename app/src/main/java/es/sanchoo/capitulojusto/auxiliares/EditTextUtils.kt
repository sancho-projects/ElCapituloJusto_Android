package es.sanchoo.capitulojusto.auxiliares

import android.text.InputFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import es.sanchoo.capitulojusto.auxiliares.Constants.MAX_CAP_DEFAULT

private const val MIN_CAP: Int = 10

//
// Función implementada con CHAT-GPT::
//
fun applyValueFilter(editText: EditText, isGameplay: Boolean = false) {
    // 1) Filtro: bloquea solo números > MAX_CAP (permite vacíos e intermedios)
    val maxFilter = InputFilter { source, start, end, dest, dstart, dend ->
        val newText = dest.substring(0, dstart) +
                source.subSequence(start, end) +
                dest.substring(dend)

        if (newText.isEmpty()) return@InputFilter null // permitir borrar todo

        // Solo dígitos (por seguridad, aunque inputType=number ya ayuda)
        if (!newText.all { it.isDigit() }) return@InputFilter ""

        val value = newText.toIntOrNull() ?: return@InputFilter ""
        if (value > MAX_CAP_DEFAULT) "" else null
    }

    // Limitar dígitos a los del MAX para evitar desbordes innecesarios (1000 -> 4)
    val lengthFilter = InputFilter.LengthFilter(MAX_CAP_DEFAULT.toString().length)
    editText.filters = arrayOf(maxFilter, lengthFilter)

    // 2) Al finalizar (perder foco o IME Done), ajustar al rango [MIN_CAP, MAX_CAP]
    val clamp: () -> Unit = {
        val txt = editText.text?.toString().orEmpty()
        val fixedText = when {
            txt.isBlank() && isGameplay -> txt // en gameplay, si está vacío, no tocar
            txt.isBlank() -> MAX_CAP_DEFAULT.toString() // fuera de gameplay, vacío → MAX_CAP
            else -> {
                val n = txt.toIntOrNull()
                if (n == null) MAX_CAP_DEFAULT.toString()
                else if (isGameplay) minOf(n, MAX_CAP_DEFAULT).toString() // gameplay: solo limitar max
                else n.coerceIn(MIN_CAP, MAX_CAP_DEFAULT).toString()      // fuera gameplay: rango completo
            }
        }
        if (fixedText != txt) {
            editText.setText(fixedText)
            editText.setSelection(editText.text?.length ?: 0)
        }
    }

    // Conservar listener previo (si lo hubiera) y encadenarlo
    val previousFocusChange = editText.onFocusChangeListener
    editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (!hasFocus) clamp()
        previousFocusChange?.onFocusChange(v, hasFocus)
    }

    // También al pulsar "Done" en el teclado
    editText.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clamp()
        }
        false
    }
}
