package com.example.melendezt2.ui.mascota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.melendezt2.data.repository.AuthRepository
import com.example.melendezt2.data.repository.MascotaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotaAddEditViewModelFactory(
    private val mascotaRepository: MascotaRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MascotaAddEditViewModel::class.java)) {
            MascotaAddEditViewModel(
                mascotaRepository,
                authRepository,
                firestore,
                firebaseAuth
            ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}