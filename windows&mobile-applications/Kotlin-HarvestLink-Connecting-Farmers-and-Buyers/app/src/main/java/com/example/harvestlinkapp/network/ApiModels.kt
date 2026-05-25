package com.example.harvestlinkapp.network


data class ApiProduce(
    val id: String,
    val farmerUid: String? = null,
    val name: String = "",
    val variety:String ="",
    val grade: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val pricePerUnit: Double = 0.0,
    val harvestDate: String = "",
    val location:String="",
    val imageUrl: String? = null
)


data class ApiCreateProduceRequest(
    val farmerUid: String?,
    val name: String,
    val variety: String,
    val grade: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val harvestDate: String,
    val location: String,
    val imageUrl: String?
)


data class ApiOrder(
    val id: String,
    val buyerUid: String,
    val farmerUid: String,
    val produceDocId: String?,
    val quantity: Double,
    val status: String? = null,
    val note: String? = null
)


data class ApiCreateOrderRequest(
    val buyerUid: String,
    val farmerUid: String,
    val produceDocId: String,
    val quantity: Double,
    val note: String? = null
)



data class ApiUpdateStatusRequest(
    val status: String
)
