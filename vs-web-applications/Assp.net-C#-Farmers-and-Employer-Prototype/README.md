# AgriEnergyConnect Prototype

## Overview

AgriEnergyConnect is a prototype web application designed to manage farmers and their products, supporting two user roles: **Farmer** and **Employee**. The system demonstrates secure authentication, role-based access, robust data management, and a user-friendly interface.

---

## Table of Contents

- [Features](#features)
- [System Functionalities](#system-functionalities)
- [User Roles](#user-roles)
- [Setup Instructions](#setup-instructions)
- [How to Build and Run](#how-to-build-and-run)
- [Sample Data](#sample-data)
- [Testing and Validation](#testing-and-validation)
- [Notes](#notes)
- [Contact](#contact)

---

## Features

- Relational database integration for farmers and products.
- Secure authentication and role-based authorization.
- Farmers can add/view their own products.
- Employees can add new farmers, view/filter all products, and search by criteria.
- Responsive, user-friendly interface.
- Data validation and error handling throughout.

---

## System Functionalities

### For Farmers

- **Add Products:** Farmers can add new products with details (name, category, production date).
- **View Products:** Farmers can view a list of their own products.

### For Employees

- **Add Farmers:** Employees can create new farmer profiles with essential details.
- **View & Filter Products:** Employees can view all products and filter by farmer, date range, and product type.

---

## User Roles

- **Farmer:**  
  - Can log in, add products, and view their own product listings.
- **Employee:**  
  - Can log in, add new farmers, view all products, and use filters for searching.

---

## Setup Instructions

### Prerequisites

- [.NET 9 SDK](https://dotnet.microsoft.com/download)
- [SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) (or compatible)
- Visual Studio 2022 or later

### Step 1: Clone the Repository


### Step 2: Database Setup

1. **Configure Connection String:**  
   Edit `appsettings.json` to set your SQL Server connection string.

2. **Apply Migrations and Seed Data:**  
   Open a terminal in the project directory and run:
      This will create the database schema and seed it with sample data.

### Step 3: User Roles and Accounts

- The system seeds at least one Farmer and one Employee user for demonstration.
- Default credentials (change as needed in the seed logic):
  - **Employee:**  
    - Username: `employee@example.com`  
    - Password: `Employee123!`
  - **Farmer:**  
    - Username: `farmer@example.com`  
    - Password: `Farmer123!`

---

## How to Build and Run

1. Open the solution in Visual Studio 2022.
2. Set `AgriEnergyConnect` as the startup project.
3. Press `F5` or click "Run" to start the application.
4. Navigate to `https://localhost:<port>/` in your browser.

---

## Sample Data

- The database is pre-populated with sample farmers and products to simulate real-world scenarios.
- You can log in as a Farmer or Employee to test all features.

---

## Testing and Validation

- All forms include validation to ensure data accuracy and consistency.
- Error messages are displayed for invalid input.
- The application has been tested for usability and responsiveness on desktop and mobile devices.

---

## Notes

- If you encounter issues with database creation, ensure your SQL Server is running and the connection string is correct.
- To add more sample data, update the seed logic in `ApplicationDbContext` or use the UI as an Employee.

---

## Contact

For questions or support, please contact the project maintainer.

---

References:
https://www.geeksforgeeks.org/csharp-programming-language/

https://www.w3schools.com/cs/index.php
