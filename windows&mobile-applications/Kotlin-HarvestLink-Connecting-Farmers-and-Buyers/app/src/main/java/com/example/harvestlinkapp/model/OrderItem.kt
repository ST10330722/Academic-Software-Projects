package com.example.harvestlinkapp.model
//Model for OrderItem
data class OrderItem(
    val id: Int,
    val buyer_id: Int,
    val produce_id: Int,
    val produce_name: String?,
    val quantity: Double,
    val status: String?,
    val note: String?
)
