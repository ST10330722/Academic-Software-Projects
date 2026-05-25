package com.example.harvestlinkapp.ui.theme.role

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.harvestlinkapp.data.FirebaseRepository
import com.example.harvestlinkapp.databinding.ActivityRoleSelectBinding
import com.example.harvestlinkapp.ui.theme.buyer.BuyerHomeActivity
import com.example.harvestlinkapp.ui.theme.farmer.FarmerHomeActivity
import com.example.harvestlinkapp.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RoleSelectActivity : ComponentActivity() {

    private lateinit var binding: ActivityRoleSelectBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val repo: FirebaseRepository by lazy { FirebaseRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoleSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnFarmer.setOnClickListener { setRole("farmer") }
        binding.btnBuyer.setOnClickListener { setRole("buyer") }
    }

    private fun setRole(role: String) {
        if (auth.currentUser == null) {
            Toast.makeText(this, "No signed-in user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Prefs.setRole(this, role)

        lifecycleScope.launch {
            try {
                repo.upsertUser(role)

                val next = if (role == "farmer") {
                    Intent(this@RoleSelectActivity, FarmerHomeActivity::class.java)
                } else {
                    Intent(this@RoleSelectActivity, BuyerHomeActivity::class.java)
                }

                startActivity(next)
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RoleSelectActivity,
                    "Failed to save role: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
