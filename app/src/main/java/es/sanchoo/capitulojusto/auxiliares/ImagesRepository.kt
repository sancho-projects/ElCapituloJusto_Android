package es.sanchoo.capitulojusto.auxiliares

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedReader
import java.io.InputStreamReader

class ImagesRepository {
companion object{
    const val MAX_IMAGE = 45

    fun subirImgstoFirestorage(context: Context, resources: Resources, packageName: String, isManga: Boolean = true) {
        val media = if (isManga) "manga" else "anime"
        val storage = FirebaseStorage.getInstance()

        for (i in 1..MAX_IMAGE) {
            val resourceName = "img$i"
            val resId = resources.getIdentifier(resourceName, "raw", packageName)
            if (resId != 0) {
                val inputStream = resources.openRawResource(resId)
                val storageRef = storage.reference.child("paneles/$media/$resourceName.jpg")
                val uploadTask = storageRef.putStream(inputStream)
                uploadTask.addOnFailureListener {
                    Log.e("FIRESTORAGE", "Error al subir $resourceName")
                }.addOnSuccessListener {
                    Log.e("FIRESTORAGE", "$resourceName subido correctamente")
                }
            } else {
                Log.w("FIRESTORAGE", "No se encontró el recurso: $resourceName")
            }
        }
    }


    fun subirCSVtoFirestore(context: Context, csvResId: Int, isManga: Boolean = true) {
        val media = if (isManga) "manga" else "anime"
        val db = FirebaseFirestore.getInstance()

        val inputStream = context.resources.openRawResource(csvResId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return

        val header = lines[0].split(",")
        val idIndex = 0
        val field1 = header[1]
        val field2 = header[2]

        for (i in 1 until lines.size) {
            val cols = lines[i].split(",")
            if (cols.size < 3) continue
            val docId = cols[idIndex]
            val data = hashMapOf(
                field1 to cols[1],
                field2 to cols[2],
                "imageURL" to "paneles/$media/img$docId.jpg"
            )
            db.collection("solutions_$media")
                .document(docId)
                .set(data)
                .addOnSuccessListener {
                    Log.d("FIRESTORE", "Documento $docId agregado en solutions_$media")
                }.addOnFailureListener {
                    Log.d("FIRESTORE", "Ha ocurrido un error con $docId")
                }

        }
    }
}
}