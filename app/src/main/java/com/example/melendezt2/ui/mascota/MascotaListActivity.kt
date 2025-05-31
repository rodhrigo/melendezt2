package com.example.melendezt2.ui.mascota

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melendezt2.R
import com.example.melendezt2.databinding.ActivityMascotaListBinding
import com.example.melendezt2.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MascotaListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotaListBinding
    private lateinit var adapter: MascotaAdapter
    private lateinit var viewModel: MascotaListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MascotaListViewModel::class.java]
        binding = ActivityMascotaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MascotaAdapter(emptyList(),
            onEditarClick = { mascota ->
                val intent = Intent(this, MascotaAddEditActivity::class.java)
                intent.putExtra("mascota_id", mascota.id)
                startActivity(intent)
            },
            onEliminarClick = { mascota ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar mascota")
                    .setMessage("¿Estás seguro de que deseas eliminar esta mascota?")
                    .setPositiveButton("Sí") { _, _ ->
                        viewModel.eliminarMascota(mascota.id)
                        Toast.makeText(this, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        binding.recyclerMascotas.layoutManager = LinearLayoutManager(this)
        binding.recyclerMascotas.adapter = adapter

        viewModel.mascotas.observe(this) {
            adapter.actualizarLista(it)
        }

        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, MascotaAddEditActivity::class.java))
        }

        viewModel.obtenerMascotas()
    }

    override fun onResume() {
        super.onResume()
        viewModel.obtenerMascotas()
    }
}