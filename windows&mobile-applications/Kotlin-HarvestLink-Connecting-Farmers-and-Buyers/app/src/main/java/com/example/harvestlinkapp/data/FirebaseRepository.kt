package com.example.harvestlinkapp.data

import com.example.harvestlinkapp.model.OrderDto
import com.example.harvestlinkapp.model.OrderItemDto
import com.example.harvestlinkapp.model.ProduceDto
import com.example.harvestlinkapp.model.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ---------- Users ----------

    suspend fun upsertUser(role: String): UserDto {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val uid = user.uid
        val name = user.displayName ?: ""
        val email = user.email ?: ""

        val data = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to role
        )

        db.collection("users").document(uid).set(data).await()

        return UserDto(
            uid = uid,
            name = name,
            email = email,
            role = role
        )
    }

    suspend fun getCurrentUserRole(): String? {
        val user = auth.currentUser ?: return null
        val snap = db.collection("users").document(user.uid).get().await()
        return snap.getString("role")
    }

    // ---------- Produce ----------

    suspend fun getAllProduce(): List<ProduceDto> {
        val snap = db.collection("produce").get().await()
        return snap.documents.map { doc ->
            ProduceDto(
                docId = doc.id,
                farmerUid = doc.getString("farmerUid"),
                name = doc.getString("name") ?: "",
                grade = doc.getString("grade") ?: "",
                quantity = doc.getDouble("quantity") ?: 0.0,
                unit = doc.getString("unit") ?: "",
                pricePerUnit = doc.getDouble("pricePerUnit") ?: 0.0,
                harvestDate = doc.getString("harvestDate") ?: "",
                imageUrl = doc.getString("imageUrl")
            )
        }
    }

    suspend fun addProduce(produce: ProduceDto) {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val data = mapOf(
            "farmerUid" to user.uid,
            "name" to produce.name,
            "grade" to produce.grade,
            "quantity" to produce.quantity,
            "unit" to produce.unit,
            "pricePerUnit" to produce.pricePerUnit,
            "harvestDate" to produce.harvestDate,
            "imageUrl" to produce.imageUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("produce").add(data).await()
    }

    // ---------- Orders ----------

    suspend fun createOrder(order: OrderDto) {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val data = mapOf(
            "buyerUid" to user.uid,
            "farmerUid" to order.farmerUid,
            "produceDocId" to order.produceDocId,
            "quantity" to order.quantity,
            "note" to (order.note ?: ""),
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("orders").add(data).await()
    }

    suspend fun getOrdersForRole(role: String): List<OrderItemDto> {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val field = if (role == "farmer") "farmerUid" else "buyerUid"

        val orderSnap = db.collection("orders")
            .whereEqualTo(field, user.uid)
            .get()
            .await()

        val items = mutableListOf<OrderItemDto>()

        for (doc in orderSnap.documents) {
            val produceDocId = doc.getString("produceDocId")
            val farmerUid = doc.getString("farmerUid")

            var produceName = "Produce"
            var farmerName = ""

            if (!produceDocId.isNullOrEmpty()) {
                val pSnap = db.collection("produce").document(produceDocId).get().await()
                produceName = pSnap.getString("name") ?: produceName
            }

            if (!farmerUid.isNullOrEmpty()) {
                val uSnap = db.collection("users").document(farmerUid).get().await()
                farmerName = uSnap.getString("name") ?: ""
            }

            items.add(
                OrderItemDto(
                    docId = doc.id,
                    produceName = produceName,
                    quantity = doc.getDouble("quantity") ?: 0.0,
                    status = doc.getString("status") ?: "pending",
                    farmerName = farmerName
                )
            )
        }

        return items
    }

    suspend fun updateOrderStatus(orderDocId: String, status: String) {
        db.collection("orders").document(orderDocId).update("status", status).await()
    }
}
