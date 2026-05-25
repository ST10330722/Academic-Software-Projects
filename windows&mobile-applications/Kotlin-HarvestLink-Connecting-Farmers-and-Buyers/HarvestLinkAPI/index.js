require("dotenv").config();
const express = require("express");
const cors = require("cors");

const produceRoutes = require("./routes/produceRoutes");
const orderRoutes = require("./routes/orderRoutes");

const app = express();
app.use(cors());
app.use(express.json());

app.use("/produce", produceRoutes);
app.use("/orders", orderRoutes);

const PORT = process.env.PORT || 3000;
const HOST = "0.0.0.0";
app.listen(PORT, HOST, () => {
  console.log(`HarvestLink API running on http://${HOST}:${PORT}`);
});
