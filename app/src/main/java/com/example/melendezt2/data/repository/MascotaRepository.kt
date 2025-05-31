package com.example.melendezt2.data.repository

import com.example.melendezt2.data.model.Mascota
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotaRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    private val petsCollection = firestore.collection("pets")

    // Agrega una mascota al Firestore con un nuevo ID y el ID del usuario actual como ownerId
    suspend fun addPet(pet: Mascota): Result<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val newDoc = petsCollection.document()
            val petWithIdAndOwner = pet.copy(id = newDoc.id, ownerId = currentUserId)
            newDoc.set(petWithIdAndOwner).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualiza la mascota existente (el documento se sobrescribe con el nuevo contenido)
    suspend fun updatePet(pet: Mascota): Result<Unit> {
        return try {
            if (pet.id.isBlank()) {
                return Result.failure(Exception("ID de mascota inválido"))
            }
            petsCollection.document(pet.id).set(pet).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Elimina la mascota por ID
    suspend fun deletePet(petId: String): Result<Unit> {
        return try {
            if (petId.isBlank()) {
                return Result.failure(Exception("ID de mascota inválido"))
            }
            petsCollection.document(petId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtiene la lista de mascotas del usuario autenticado
    suspend fun getUserPets(): Result<List<Mascota>> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val snapshot = petsCollection
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .await()

            val pets = snapshot.toObjects(Mascota::class.java)
            Result.success(pets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}