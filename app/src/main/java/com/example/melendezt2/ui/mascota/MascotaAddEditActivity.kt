package com.example.melendezt2.ui.mascota

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.melendezt2.R
import com.example.melendezt2.data.model.Mascota
import com.example.melendezt2.data.repository.AuthRepository
import com.example.melendezt2.data.repository.MascotaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MascotaAddEditActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null
    private var mascotaExistente: Mascota? = null

    private lateinit var tipoSpinner: Spinner

    private val mascotaAddEditViewModel: MascotaAddEditViewModel by viewModels {
        MascotaAddEditViewModelFactory(
            mascotaRepository = MascotaRepository(firestore, firebaseAuth),
            authRepository = AuthRepository(firebaseAuth, firestore),
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    private lateinit var imageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var imageUrlEditText: EditText

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imageView.setImageURI(uri)
        } else {
            showToast("No se seleccionó ninguna imagen.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascota_add_edit)

        imageView = findViewById(R.id.imageMascota)
        nameEditText = findViewById(R.id.etNombre)
        descriptionEditText = findViewById(R.id.etDescripcion)
        priceEditText = findViewById(R.id.etPrecio)
        imageUrlEditText = findViewById(R.id.etImagenUrl)
        tipoSpinner = findViewById(R.id.spinnerTipo)

        val seleccionarImagenBtn = findViewById<Button>(R.id.btnSeleccionarImagen)
        val saveButton = findViewById<Button>(R.id.btnGuardar)

        // Configurar Spinner con opciones de tipo mascota
        val tiposMascotas = resources.getStringArray(R.array.tipos_mascota)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposMascotas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoSpinner.adapter = adapter

        val mascotaId = intent.getStringExtra("mascota_id")
        if (mascotaId != null) {
            mascotaAddEditViewModel.cargarMascotaPorId(mascotaId)
        }

        mascotaAddEditViewModel.mascota.observe(this) { mascota ->
            mascota?.let {
                mascotaExistente = it
                nameEditText.setText(it.name)
                descriptionEditText.setText(it.description)
                priceEditText.setText(it.price.toString())
                imageUrlEditText.setText(it.imageUrl)
                Glide.with(this).load(it.imageUrl).into(imageView)

                // Buscar posición ignorando mayúsculas
                val pos = tiposMascotas.indexOfFirst { tipo -> tipo.equals(it.tipo, ignoreCase = true) }
                if (pos >= 0) {
                    tipoSpinner.setSelection(pos)
                } else {
                    Log.d("MascotaAddEdit", "Tipo no encontrado en el spinner")
                }
            }
        }


        seleccionarImagenBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val price = priceEditText.text.toString().toIntOrNull() ?: 0
            val imageUrlFromInput = imageUrlEditText.text.toString().trim()
            val tipo = tipoSpinner.selectedItem.toString()

            if (name.isEmpty() || description.isEmpty()) {
                showToast("Por favor completa todos los campos obligatorios")
                return@setOnClickListener
            }

            if (imageUri != null) {
                uploadImageToFirebase(imageUri!!, name, description, price, tipo)
            } else if (imageUrlFromInput.isNotEmpty()) {
                guardarMascota(name, description, price, imageUrlFromInput, tipo)
            } else if (mascotaExistente != null) {
                guardarMascota(name, description, price, mascotaExistente!!.imageUrl, tipo)
            } else {
                showToast("Selecciona una imagen o ingresa una URL")
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri, name: String, description: String, price: Int, tipo: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("mascotas/${UUID.randomUUID()}.jpg")

        imageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Error al subir la imagen")
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                guardarMascota(name, description, price, downloadUri.toString(), tipo)
            }
            .addOnFailureListener { e ->
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun guardarMascota(name: String, description: String, price: Int, imageUrl: String, tipo: String) {
        val mascota = Mascota(
            id = mascotaExistente?.id ?: "",
            ownerId = firebaseAuth.currentUser?.uid.orEmpty(),
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            tipo = tipo
        )

        mascotaAddEditViewModel.savePet(mascota)
        showToast("Mascota ${if (mascotaExistente == null) "registrada" else "actualizada"} correctamente")
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}