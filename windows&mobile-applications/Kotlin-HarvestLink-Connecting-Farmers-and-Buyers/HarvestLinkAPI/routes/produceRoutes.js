const express = require("express");
const router = express.Router();
const {
  getAllProduce,
  addProduce,
  updateProduce,
  deleteProduce
} = require("../controllers/produceController");

// GET /produce
router.get("/", getAllProduce);

// POST /produce
router.post("/", addProduce);

// PUT /produce/:id
router.put("/:id", updateProduce);

// DELETE /produce/:id
router.delete("/:id", deleteProduce);

module.exports = router;
