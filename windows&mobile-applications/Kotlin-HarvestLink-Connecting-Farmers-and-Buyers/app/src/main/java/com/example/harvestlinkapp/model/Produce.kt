package com.example.harvestlinkapp.model
//Model for Produce
data class Produce(
    val id: Int? = null,
    val farmer_id: Int? = null,
    val name: String,
    val grade: String = "A",
    val quantity: Double,
    val unit: String = "kg",
    val price: Double,
    val harvest_date: String = ""
)
