package com.example.harvestlinkapp.data

import android.util.Log
import com.example.harvestlinkapp.model.OrderDto
import com.example.harvestlinkapp.model.OrderItemDto
import com.example.harvestlinkapp.model.ProduceDto
import com.example.harvestlinkapp.network.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository(
    private val api: ApiService = ApiClient.apiService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ------------------------ PRODUCE ---------------------------------------

    suspend fun getAllProduce(): List<ProduceDto> = withContext(Dispatchers.IO) {
        val list = api.getProduce()
        list.map { apiProd ->
            ProduceDto(
                docId = apiProd.id,
                farmerUid = apiProd.farmerUid,
                name = apiProd.name,
                variety = apiProd.variety ?: "",
                grade = apiProd.grade,
                quantity = apiProd.quantity,
                unit = apiProd.unit,
                pricePerUnit = apiProd.pricePerUnit,
                harvestDate = apiProd.harvestDate,
                location = apiProd.location ?: "",
                imageUrl = apiProd.imageUrl
            )
        }
    }

    suspend fun addProduce(dto: ProduceDto): ProduceDto = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val req = ApiCreateProduceRequest(
            farmerUid = user.uid,
            name = dto.name,
            variety = dto.variety,
            grade = dto.grade,
            quantity = dto.quantity,
            unit = dto.unit,
            pricePerUnit = dto.pricePerUnit,
            harvestDate = dto.harvestDate,
            location = dto.location,
            imageUrl = dto.imageUrl
        )
        val created = api.addProduce(req)

        ProduceDto(
            docId = created.id,
            farmerUid = created.farmerUid,
            name = created.name,
            variety = created.variety ?: "",
            grade = created.grade,
            quantity = created.quantity,
            unit = created.unit,
            pricePerUnit = created.pricePerUnit,
            harvestDate = created.harvestDate,
            location = created.location ?: "",
            imageUrl = created.imageUrl
        )
    }

    suspend fun updateProduce(docId: String, dto: ProduceDto) = withContext(Dispatchers.IO) {
        val req = ApiCreateProduceRequest(
            farmerUid = dto.farmerUid,
            name = dto.name,
            variety = dto.variety,
            grade = dto.grade,
            quantity = dto.quantity,
            unit = dto.unit,
            pricePerUnit = dto.pricePerUnit,
            harvestDate = dto.harvestDate,
            location = dto.location,
            imageUrl = dto.imageUrl
        )
        api.updateProduce(docId, req)
    }

    suspend fun deleteProduce(docId: String) = withContext(Dispatchers.IO) {
        api.deleteProduce(docId)
    }

    // ------------------------ ORDERS ----------------------------------------

    suspend fun createOrder(order: OrderDto) = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: throw IllegalStateException("No auth user")
        val req = ApiCreateOrderRequest(
            buyerUid = user.uid,
            farmerUid = order.farmerUid,
            produceDocId = order.produceDocId,
            quantity = order.quantity,
            note = order.note
        )
        api.addOrder(req)
    }


    suspend fun getOrdersForRole(role: String): List<OrderItemDto> =
        withContext(Dispatchers.IO) {
            val user = auth.currentUser ?: throw IllegalStateException("No auth user")
            val uid = user.uid

            val allOrders = api.getOrders()

            Log.d("ApiRepository", "Fetched ${allOrders.size} orders from API")

            val filteredByRole = allOrders.filter { apiOrder ->
                if (role == "farmer") {
                    apiOrder.farmerUid == uid
                } else {
                    apiOrder.buyerUid == uid
                }
            }


            val activeOnly = filteredByRole.filter { apiOrder ->
                !(apiOrder.status ?: "pending").equals("fulfilled", ignoreCase = true)
            }

            Log.d(
                "ApiRepository",
                "After filtering ACTIVE orders for role=$role and uid=$uid, we have ${activeOnly.size} orders"
            )

            val allProduce = api.getProduce()
            val produceMap = allProduce.associateBy { it.id }

            activeOnly.map { apiOrder ->
                val produce = apiOrder.produceDocId?.let { produceMap[it] }

                OrderItemDto(
                    docId = apiOrder.id,
                    produceName = produce?.name ?: "Produce",
                    quantity = apiOrder.quantity,
                    status = apiOrder.status ?: "pending",
                    farmerName = ""
                )
            }
        }


    suspend fun getPastOrdersForRole(role: String): List<OrderItemDto> =
        withContext(Dispatchers.IO) {
            val user = auth.currentUser ?: throw IllegalStateException("No auth user")
            val uid = user.uid

            val allOrders = api.getOrders()

            Log.d("ApiRepository", "Fetched ${allOrders.size} orders from API (for past orders)")

            val filteredByRole = allOrders.filter { apiOrder ->
                if (role == "farmer") {
                    apiOrder.farmerUid == uid
                } else {
                    apiOrder.buyerUid == uid
                }
            }


            val fulfilledOnly = filteredByRole.filter { apiOrder ->
                (apiOrder.status ?: "pending").equals("fulfilled", ignoreCase = true)
            }

            Log.d(
                "ApiRepository",
                "After filtering FULFILLED orders for role=$role and uid=$uid, we have ${fulfilledOnly.size} orders"
            )

            val allProduce = api.getProduce()
            val produceMap = allProduce.associateBy { it.id }

            fulfilledOnly.map { apiOrder ->
                val produce = apiOrder.produceDocId?.let { produceMap[it] }

                OrderItemDto(
                    docId = apiOrder.id,
                    produceName = produce?.name ?: "Produce",
                    quantity = apiOrder.quantity,
                    status = apiOrder.status ?: "fulfilled",
                    farmerName = ""
                )
            }
        }

    suspend fun updateOrderStatus(orderDocId: String, status: String) =
        withContext(Dispatchers.IO) {
            api.updateOrderStatus(orderDocId, ApiUpdateStatusRequest(status))
        }
}
