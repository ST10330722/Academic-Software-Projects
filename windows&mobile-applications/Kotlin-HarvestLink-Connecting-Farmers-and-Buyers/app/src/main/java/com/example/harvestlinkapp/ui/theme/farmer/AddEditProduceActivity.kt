package com.example.harvestlinkapp.ui.theme.farmer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.data.AppDatabase
import com.example.harvestlinkapp.data.PendingActionEntity
import com.example.harvestlinkapp.data.ProduceEntity
import com.example.harvestlinkapp.databinding.ActivityAddEditProduceBinding
import com.example.harvestlinkapp.model.OfflineProducePayload
import com.example.harvestlinkapp.model.ProduceDto
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class AddEditProduceActivity : LocalizedActivity() {

    companion object {
        const val EXTRA_LOCAL_ID = "extra_local_id"
        const val EXTRA_SERVER_ID = "extra_server_id"
    }

    private lateinit var binding: ActivityAddEditProduceBinding

    private val localDb by lazy { AppDatabase.getInstance(this) }
    private val gson = Gson()
    private val apiRepo = ApiRepository()

    private val cloudStorage: FirebaseStorage? by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private var selectedImageUri: Uri? = null
    private var editingLocalId: Long? = null
    private var editingServerId: String? = null
    private var existingImageUrl: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    binding.imgPreview.setImageURI(uri)
                }
            }
        }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditProduceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingLocalId = intent.getLongExtra(EXTRA_LOCAL_ID, -1L).takeIf { it > 0 }
        editingServerId = intent.getStringExtra(EXTRA_SERVER_ID)

        if (editingLocalId != null) {
            actionBar?.title = getString(R.string.edit_produce_title)
            loadExistingProduce(editingLocalId!!)
        } else {
            actionBar?.title = getString(R.string.add_produce_title)
        }

        binding.btnAddPhoto.setOnClickListener { openImagePicker() }
        binding.btnSave.setOnClickListener { saveProduce() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadExistingProduce(localId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val entity = localDb.produceDao().getAll().find { it.localId == localId }
            entity?.let {
                existingImageUrl = it.imageUrl
                withContext(Dispatchers.Main) {
                    binding.etName.setText(it.name)
                    binding.etVariety.setText(it.variety)
                    binding.etGrade.setText(it.grade)
                    binding.etQuantity.setText(it.quantity.toString())
                    binding.etUnit.setText(it.unit)
                    binding.etPrice.setText(it.pricePerUnit.toString())
                    binding.etHarvestDate.setText(it.harvestDate)
                    binding.etLocation.setText(it.location)

                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun saveProduce() {
        val name = binding.etName.text.toString().trim()
        val variety = binding.etVariety.text.toString().trim()
        val grade = binding.etGrade.text.toString().trim()
        val qty = binding.etQuantity.text.toString().toDoubleOrNull()
        val unit = binding.etUnit.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val harvestDate = binding.etHarvestDate.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (name.isEmpty() || grade.isEmpty() || qty == null || price == null ||
            unit.isEmpty() || harvestDate.isEmpty() || location.isEmpty()
        ) {
            Toast.makeText(
                this,
                getString(R.string.error_fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dtoBase = ProduceDto(
            docId = editingServerId,
            farmerUid = null,
            name = name,
            variety = variety,
            grade = grade,
            quantity = qty,
            unit = unit,
            pricePerUnit = price,
            harvestDate = harvestDate,
            location = location,
            imageUrl = existingImageUrl
        )

        val online = isOnline()
        val uri = selectedImageUri
        val storageInstance = cloudStorage

        if (editingLocalId == null) {

            lifecycleScope.launch {
                if (!online) {
                    queueOfflineCreate(dtoBase)
                    return@launch
                }


                if (uri != null && storageInstance != null) {
                    uploadImageThenCreate(storageInstance, uri, dtoBase)
                } else {
                    attemptOnlineCreate(dtoBase)
                }
            }
        } else {

            lifecycleScope.launch {
                if (!online) {

                    updateLocalOnly(dtoBase)
                    Toast.makeText(
                        this@AddEditProduceActivity,
                        getString(R.string.saved_offline_edit),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                if (uri != null && storageInstance != null) {
                    uploadImageThenUpdate(storageInstance, uri, dtoBase)
                } else {
                    attemptOnlineUpdate(dtoBase)
                }
            }
        }
    }

    // ---------- CREATE flow ----------

    private suspend fun uploadImageThenCreate(
        storage: FirebaseStorage,
        uri: Uri,
        baseDto: ProduceDto
    ) {
        try {
            val ref = storage.reference
                .child("produce_photos/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await()
            val dtoWithImage = baseDto.copy(imageUrl = url.toString())
            attemptOnlineCreate(dtoWithImage)
        } catch (e: Exception) {
            attemptOnlineCreate(baseDto)
        }
    }

    private suspend fun attemptOnlineCreate(dto: ProduceDto) {
        try {
            val created = apiRepo.addProduce(dto)
            withContext(Dispatchers.IO) {
                val entity = ProduceEntity(

                    name = created.name,
                    variety = created.variety,
                    grade = created.grade,
                    quantity = created.quantity,
                    unit = created.unit,
                    pricePerUnit = created.pricePerUnit,
                    harvestDate = created.harvestDate,
                    location = created.location,
                    imageUrl = created.imageUrl,
                    serverId = created.docId,
                    isSynced = true
                )
                localDb.produceDao().insert(entity)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddEditProduceActivity,
                    getString(R.string.saved_and_synced),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        } catch (e: Exception) {
            queueOfflineCreate(dto)
        }
    }

    private fun queueOfflineCreate(dto: ProduceDto) {
        lifecycleScope.launch(Dispatchers.IO) {

            val localId = localDb.produceDao().insert(
                ProduceEntity(

                    serverId = null,
                    name = dto.name,
                    variety = dto.variety,
                    grade = dto.grade,
                    quantity = dto.quantity,
                    unit = dto.unit,
                    pricePerUnit = dto.pricePerUnit,
                    harvestDate = dto.harvestDate,
                    location = dto.location,
                    imageUrl = dto.imageUrl,
                    isSynced = false
                )
            )


            val payload = OfflineProducePayload(
                localId = localId,
                produce = dto
            )


            localDb.pendingActionDao().insert(
                PendingActionEntity(
                    type = "CREATE_PRODUCE",
                    payloadJson = gson.toJson(payload),
                    targetEndpoint = "api/produce"
                )
            )


            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddEditProduceActivity,
                    getString(R.string.saved_offline_will_sync),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    // ---------- UPDATE flow ----------

    private suspend fun uploadImageThenUpdate(
        storage: FirebaseStorage,
        uri: Uri,
        baseDto: ProduceDto
    ) {
        try {
            val ref = storage.reference
                .child("produce_photos/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await()
            val dtoWithImage = baseDto.copy(imageUrl = url.toString())
            attemptOnlineUpdate(dtoWithImage)
        } catch (e: Exception) {
            attemptOnlineUpdate(baseDto)
        }
    }

    private suspend fun attemptOnlineUpdate(dto: ProduceDto) {
        val localId = editingLocalId ?: return
        val serverId = editingServerId

        try {
            if (serverId != null) {
                apiRepo.updateProduce(serverId, dto)
            }
            withContext(Dispatchers.IO) {
                val entity = ProduceEntity(
                    localId = localId,
                    serverId = serverId,
                    name = dto.name,
                    variety = dto.variety,
                    grade = dto.grade,
                    quantity = dto.quantity,
                    unit = dto.unit,
                    pricePerUnit = dto.pricePerUnit,
                    harvestDate = dto.harvestDate,
                    location = dto.location,
                    imageUrl = dto.imageUrl,
                    isSynced = true
                )
                localDb.produceDao().update(entity)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddEditProduceActivity,
                    getString(R.string.updated_and_synced),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddEditProduceActivity,
                    getString(R.string.error_generic, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateLocalOnly(dto: ProduceDto) {
        val localId = editingLocalId ?: return
        val serverId = editingServerId
        lifecycleScope.launch(Dispatchers.IO) {
            val entity = ProduceEntity(
                localId = localId,
                serverId = serverId,
                name = dto.name,
                variety = dto.variety,
                grade = dto.grade,
                quantity = dto.quantity,
                unit = dto.unit,
                pricePerUnit = dto.pricePerUnit,
                harvestDate = dto.harvestDate,
                location = dto.location,
                imageUrl = dto.imageUrl,
                isSynced = false
            )
            localDb.produceDao().update(entity)
        }
    }
}
