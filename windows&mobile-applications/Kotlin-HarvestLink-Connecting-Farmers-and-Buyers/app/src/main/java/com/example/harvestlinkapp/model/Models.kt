package com.example.harvestlinkapp.model


data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ""
)


data class ProduceDto(
    val docId: String? = null,
    val farmerUid: String? = null,
    val name: String = "",
    val variety: String = "",
    val grade: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val pricePerUnit: Double = 0.0,
    val harvestDate: String = "",
    val location: String = "",
    val imageUrl: String? = null
)

data class OrderDto(
    val docId: String? = null,
    val produceDocId: String,
    val farmerUid: String,
    val quantity: Double,
    val note: String? = null
)


data class OrderItemDto(
    val docId: String,
    val produceName: String,
    val quantity: Double,
    val status: String,
    val farmerName: String
)
