const express = require("express");
const router = express.Router();
const {
  getAllOrders,
  addOrder,
  updateOrderStatus,
  deleteOrder
} = require("../controllers/orderController");

// GET /orders
router.get("/", getAllOrders);

// POST /orders
router.post("/", addOrder);

// PUT /orders/:id/status
router.put("/:id/status", updateOrderStatus);

// DELETE /orders/:id
router.delete("/:id", deleteOrder);

module.exports = router;
