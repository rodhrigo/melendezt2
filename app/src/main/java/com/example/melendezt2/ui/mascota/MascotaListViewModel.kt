package com.example.melendezt2.ui.mascota

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.melendezt2.data.model.Mascota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotaListViewModel : ViewModel() {

    private val _mascotas = MutableLiveData<List<Mascota>>()
    val mascotas: LiveData<List<Mascota>> = _mascotas

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun obtenerMascotas() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("mascotas")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { result ->
                val listaMascotas = result.map { document ->
                    Mascota(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        price = (document.getDouble("price") ?: 0.0).toInt(),
                        imageUrl = document.getString("imageUrl") ?: "",
                        ownerId = document.getString("ownerId") ?: "",
                        tipo = document.getString("tipo") ?: ""
                    )
                }
                _mascotas.value = listaMascotas
            }
            .addOnFailureListener {
                _mascotas.value = emptyList()
            }
    }


    fun eliminarMascota(mascotaId: String) {
        firestore.collection("mascotas")
            .document(mascotaId)
            .delete()
            .addOnSuccessListener {
                obtenerMascotas() //  Refrescar después de borrar
            }
            .addOnFailureListener {
                // Manejo de errores opcional
            }
    }
}