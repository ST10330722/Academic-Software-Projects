package com.example.harvestlinkapp.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.data.AppDatabase
import com.example.harvestlinkapp.data.PendingActionEntity
import com.example.harvestlinkapp.model.ProduceDto
import com.google.gson.Gson
import com.example.harvestlinkapp.model.OfflineProducePayload


class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val localDb = AppDatabase.getInstance(context)
    private val gson = Gson()
    private val apiRepo = ApiRepository()

    override suspend fun doWork(): Result {
        val pendingActions = localDb.pendingActionDao().getAll()
        Log.d(TAG, "Starting sync. Pending actions count = ${pendingActions.size}")

        for (action in pendingActions) {
            val ok = try {
                processAction(action)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while processing action id=${action.id}", e)
                false
            }

            if (ok) {
                localDb.pendingActionDao().deleteById(action.id)
                Log.d(TAG, "Pending action id=${action.id} processed and removed.")
            } else {
                Log.w(TAG, "Pending action id=${action.id} failed. It will be retried later.")
            }
        }

        Log.d(TAG, "SyncWorker finished.")
        return Result.success()
    }

    private suspend fun processAction(action: PendingActionEntity): Boolean {
        return when (action.type) {
            "CREATE_PRODUCE" -> {

                val payload = try {
                    gson.fromJson(action.payloadJson, OfflineProducePayload::class.java)
                } catch (e: Exception) {

                    val legacyDto = gson.fromJson(action.payloadJson, ProduceDto::class.java)
                    OfflineProducePayload(
                        localId = -1,
                        produce = legacyDto
                    )
                }

                val dto = payload.produce

                Log.d(TAG, "Uploading produce via API: name='${dto.name}', unit='${dto.unit}', qty=${dto.quantity}")

                try {
                    apiRepo.addProduce(dto)
                    Log.d(TAG, "Upload success for produce '${dto.name}'.")


                    if (payload.localId > 0) {
                        localDb.produceDao().markSynced(payload.localId)
                    }

                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Upload FAILED for produce '${dto.name}'.", e)
                    false
                }
            }

            else -> {
                Log.w(TAG, "Unknown pending action type='${action.type}'. Marking as success so it is removed.")
                true
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}

fun enqueueSync(context: Context) {
    val req = OneTimeWorkRequestBuilder<SyncWorker>().build()
    WorkManager.getInstance(context).enqueue(req)
}
