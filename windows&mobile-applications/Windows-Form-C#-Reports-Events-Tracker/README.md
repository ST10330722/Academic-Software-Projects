## 📝 README: POE Municipal Services Application for South Africa

### 🏙️ Project Overview

The Municipal Services Application is a Windows Forms (WinForms) desktop application developed in C\# that serves as a central hub for citizen-municipality interaction. It integrates issue reporting, local event announcements, and, most importantly, advanced **Service Request Tracking** using complex data structures and graph algorithms for efficient resource management. The application aims to enhance transparency and responsiveness in local governance.

### ✨ Key Features

  * **Issue Reporting:** Allows citizens to log new service issues (e.g., potholes, water leaks).
  * **Local Events & Announcements:** Displays important municipal notices and upcoming events.
  * **Service Request Status Tracking:** Enables citizens to search for and view the real-time status and timeline of their submitted requests using a unique tracking ID.
  * **Optimised Resource Allocation (Internal):** Uses advanced algorithms to internally prioritize and suggest efficient routing for service resolution, minimizing operational costs.

### ⚙️ Technical Core: Data Structures & Algorithms

This application is powered by several key data structures and algorithms, which ensure performance, especially under load :

| Component | Data Structure/Algorithm | Purpose in Application |
| :--- | :--- | :--- |
| **Lookup & Retrieval** | `Dictionary` / `HashSet` | Provides **O(1)** average-case time complexity for instantly retrieving a service request using its Tracking ID. Also ensures unique list of service locations for the graph structure. |
| **Prioritization** | **Max-Heap (Priority Queue)** | Dynamically manages all outstanding service requests. It prioritizes issues with the **highest numerical urgency** (e.g., critical water issues) to ensure they are handled first, guaranteeing effective emergency response. |
| **Location Modeling** | **Weighted Graph** | Models the geographic relationship between service locations (nodes) and the logistical cost (weight) of travel between them (edges). |
| **Field Traversal** | **Depth-First Search (DFS)** | Used for graph traversal to systematically explore all connected service locations, simulating a comprehensive, pre-planned route for field technicians starting from a central depot. |
| **Route Optimization** | **Minimum Spanning Tree (MST)** | Identifies the most cost-effective way to connect all outstanding service locations, minimizing the total distance or time traveled to service multiple related issues in a single outing. |

-----

### 🚀 Getting Started

Follow these steps to quickly clone the repository and set up the project in Visual Studio.

#### 1\. Prerequisites

Make sure you have the following installed:

  * **Git** (for cloning the repository).
  * **Visual Studio** (2019 or newer, with the **.NET desktop development** workload installed).
  * **.NET Framework 4.7.2** (or compatible .NET version for WinForms).

#### 2\. Cloning the Repository 

Open your terminal or command prompt (like Git Bash, PowerShell, or Command Prompt) and run the following command.

```bash
# Clone the repository to your local machine
git clone https://github.com/VCPTA/bca3-prog7312-poe-submission-ST10330722.git

# Navigate into the project directory
cd municipal-services-application 
```

#### 3\. Setup in Visual Studio

1.  Open **Visual Studio**.
2.  Select **File \> Open \> Project/Solution**.
3.  Navigate to the folder you just cloned and select the main solution file (it should end in `.sln`).
4.  Once the solution loads, confirm the primary Windows Forms project is set as the **Startup Project** (right-click the project name in the Solution Explorer and choose **Set as Startup Project**).

#### 4\. Build and Run

Press **F5** or click the **Start** button to compile and run the application. The application will build the initial data store and open the main menu.

### 📜 Class Structure Overview

The core functionality is distributed across these essential classes:

  * `IssueDataStore.cs`: Manages the main collection of service requests, utilizing the `MaxHeap` and `ServiceGraph`.
  * `MaxHeap.cs`: Implements the custom priority queue logic.
  * `ServiceGraph.cs`: Contains the graph structure, `DepthFirstTraversal()`, and `FindMinimumSpanningTree()` logic.
  * `StatusForm.cs`: The main UI form displaying search results and the resource allocation output.

### ✍️ Author

  * **ST10330722** 

-----

