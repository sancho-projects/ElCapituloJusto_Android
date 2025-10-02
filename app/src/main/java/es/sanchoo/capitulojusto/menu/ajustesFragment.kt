package es.sanchoo.capitulojusto.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioGroup
import es.sanchoo.capitulojusto.Constants
import es.sanchoo.capitulojusto.R
import es.sanchoo.capitulojusto.auxiliares.applyValueFilter


class ajustesFragment : Fragment() {

    private lateinit var editTextChapter: EditText

    private lateinit var easyBox: CheckBox
    private lateinit var midBox: CheckBox
    private lateinit var hardBox: CheckBox
    private lateinit var radioGroupMedia: RadioGroup

    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true


        //LÍMITE DE CAPÍTULOS
        editTextChapter = view.findViewById(R.id.textChapter)
        editTextChapter.setText(Constants.MAX_CAP_DEFAULT.toString())
        applyValueFilter(editTextChapter)

        //CHECKBOXES DE DIFICULTAD
        easyBox = view.findViewById(R.id.boxEasy)
        midBox = view.findViewById(R.id.boxMedium)
        hardBox = view.findViewById(R.id.boxHard)

        radioGroupMedia = view.findViewById(R.id.radioGroupMangaAnime)


        val listener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                if (!easyBox.isChecked && !midBox.isChecked && !hardBox.isChecked) {
                    // Si los tres están desmarcados, volvemos a marcar el último que intentó desmarcarse.
                    buttonView.isChecked = true
                }
            }
        }

        easyBox.setOnCheckedChangeListener(listener)
        midBox.setOnCheckedChangeListener(listener)
        hardBox.setOnCheckedChangeListener(listener)


        val defaultButton = view.findViewById<Button>(R.id.buttonDefault)
        defaultButton.setOnClickListener {
            setMaximumOfChapters(Constants.MAX_CAP_DEFAULT)
            setEasyValue(true)
            setMediumValue(true)
            setHardValue(true)
        }
    }


    // GETTERS

    fun getMaximumOfChapters(): Int{
        if (!isViewCreated)
            return 1000
        return editTextChapter.text.toString().toInt()
    }
    fun getEasyValue(): Boolean{
        if (!isViewCreated)
            return true
        return easyBox.isChecked
    }

    fun getMediumValue(): Boolean{
        if (!isViewCreated)
            return true
        return midBox.isChecked
    }

    fun getHardValue(): Boolean{
        if (!isViewCreated)
            return true
        return hardBox.isChecked
    }

    fun getIsManga(): Boolean{
        if (!isViewCreated) {
            return true
        }
        val id = radioGroupMedia.checkedRadioButtonId
        return id == R.id.radioManga
    }

    fun setMaximumOfChapters(max: Int) {
        if (isViewCreated) {
            editTextChapter.setText(max.toString())
        }
    }

    fun setEasyValue(value: Boolean) {
        if (isViewCreated) {
            easyBox.isChecked = value
        }
    }

    fun setMediumValue(value: Boolean) {
        if (isViewCreated) {
            midBox.isChecked = value
        }
    }

    fun setHardValue(value: Boolean) {
        if (isViewCreated) {
            hardBox.isChecked = value
        }
    }
}