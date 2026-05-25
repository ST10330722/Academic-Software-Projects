package com.example.harvestlinkapp.model
//Model for Order
data class Order(
    val id: Int? = null,
    val buyer_id: Int,
    val produce_id: Int,
    val quantity: Double,
    val status: String = "pending",
    val note: String = ""
)
