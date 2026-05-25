package com.example.harvestlinkapp.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // PRODUCE -----------------------------------------------------------------

    @GET("produce")
    suspend fun getProduce(): List<ApiProduce>

    @POST("produce")
    suspend fun addProduce(
        @Body body: ApiCreateProduceRequest
    ): ApiProduce

    @PUT("produce/{id}")
    suspend fun updateProduce(
        @Path("id") id: String,
        @Body body: ApiCreateProduceRequest
    ): ApiProduce

    @DELETE("produce/{id}")
    suspend fun deleteProduce(
        @Path("id") id: String
    )

    // ORDERS ------------------------------------------------------------------

    @GET("orders")
    suspend fun getOrders(): List<ApiOrder>

    @POST("orders")
    suspend fun addOrder(
        @Body body: ApiCreateOrderRequest
    ): ApiOrder

    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body body: ApiUpdateStatusRequest
    ): ApiOrder
}
