# P2PBruteforcePassword Project

Final project for the **Design Patterns** course, **5th semester of Computer Science and Intelligent Systems**, AGH 2025.

The core part of the code was developed in a single night—the night before the final project submission—after an exhausting **Electrician's Ball**. As a result, there are still many areas where optimizations, simplifications, and refactorings could improve readability. Some of these areas are marked with **TODO** comments.

The designed and implemented system is a **Peer-to-Peer (P2P) network** with a built-in business application. The P2P network itself is a **generic, standalone library** that can support various applications.

Each generic module (P2P Network, Password Cracker) has its own **set of communication messages (DTO objects)**. This ensures that the modules remain independent and their logic does not mix.

---

## **System Architecture**

### **P2P Network**

The `p2pnetwork` module is responsible for the structure and management of the **P2P network**.

The P2P system enables:

- Creating a network from a **single node** (network initialization).
- **Adding new nodes** to the network.
- **Updating active nodes** in the network (before sending a message to the entire network, we update the network state using lazy updates).
- **Detecting disconnected nodes** in the network.

Before a node sends a message to the entire network, it first updates its knowledge about the current network state. The same happens before a node shares its knowledge with another node (e.g., one that is joining the network). This is a form of **lazy update**—we update only when necessary.

#### **New Node Connection Process**

One of the more complex processes is adding a new node to the network:

1. The new node sends a **request** to an existing node in the network (this node must be provided at startup).
2. The existing node:
  - Starts processing the request in `NetworkManagerImpl.P2PTCPListener.handleAddNewNodeToNetwork`.
  - **Updates its network state** (removes disconnected nodes from its list).
  - **Adds the new node** to the network.
  - **Broadcasts a network update** to all nodes (including itself in the list).
  - Executes an additional **external action**, which in our case is sending a dump of business application data to the new node (previously solved passwords, chunks of ongoing tasks).

#### **Communication in the Network**

Messages in the network are exchanged as **JSON objects**. Every message inherits from a base class containing information about the message type and sender.

**Example Messages:**

```json
{
  "type": "JoinToNetworkRequest",
  "node": {
    "id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d",
    "ip":"localhost",
    "port":5001
  }
}
```

- **JoinToNetworkRequest:**

```json
{
  "newNode": {
    "id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d",
    "ip": "localhost",
    "port": 5001
  }
}
```

- **UpdateNetworkMessage:**

```json
{
  "nodes": [
    {
      "id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d",
      "ip": "localhost",
      "port": 5001
    },
    {
      "id": "6ad3b2b5-fa29-4688-8eba-19f089ef692e",
      "ip": "localhost",
      "port": 5002}
  ]
}
```

- **Ping:**

```json
{
  "ping":"ping"
}
```

The code follows **good programming practices**, including **SOLID principles** and **design patterns**. The project is designed for scalability, making it easy to integrate and reuse in other systems.

One notable feature is the **external interface**, which triggers a custom action when a new node joins the network. This allows for **business process integration**, such as updating knowledge or starting new operations.

---

### **Business Application**

The `task` module is responsible for **password-cracking task management**.

A simple **brute-force password cracking** application was built on top of the P2P network. By **splitting large tasks into smaller batches**, the system supports parallel execution while exposing APIs for integration with other instances and systems.

Each task runs in a **separate thread**, allowing the controller to handle multiple tasks simultaneously with minimal performance loss.

NewTaskRequest
```json
{
  "type": "NewTaskRequest",
  "passwordHash": "216a4438875df831967fc4c6c2b15469a4c6f62dc4d28a2b5cddebddf4cfe5ad",
  "alphabet": "abcdoijklms",
  "maxLength": 7,
  "maxBatchSize": 500
}
```

#### **Key Features**

- **Creating new tasks** (both fresh tasks and those already being processed by other instances).
- **Updating task knowledge** (e.g., when a password is found).
- **Tracking batch states** (important in distributed environments where multiple nodes work on the same task).
- **Retrieving system knowledge** (useful for monitoring progress and parallel execution).

#### **Workflow Overview**

1. Given a **password hash**, **character set**, and **max password length**, the system calculates the total number of possible combinations.
2. The system **splits the search space** into batches (storing only numerical ranges for lightweight communication).
3. The system **processes a random unfinished batch**, sending and receiving updates about task progress.

#### **External Ports**

To enable **customization and integration**, the following external interfaces are available:

- **Repository** – Stores batch and task data, with implementation-dependent persistence.
- **TaskMessageSender** – Sends task progress updates, customizable based on external system needs.
- **DoneTaskProcessor** – Defines **post-task actions**, executed multiple times per task (e.g., result storage).

---

### **Middleware**

Middleware is responsible for integrating **two independent modules**:

- **Business application** (password cracking).
- **P2P network**.

Connected computers in the **P2P network** run instances of the **business application**. The middleware handles **message passing and interface implementation** for both modules.

A key aspect is using **custom message types** that are distinct from the basic P2P network messages. This separation ensures **clear module responsibilities**.

---

## **Usage**

1. **Create a network of nodes**.
2. **Send requests** containing password hashes to any active node in the network. The entire network will attempt to crack the passwords.
3. **Once a password is found**, all nodes store the result.

*(Originally, the system was intended to support callbacks for sending results via POST requests—this can be a future extension.)*

---

## **Example**

Run scripts are available in **run.sh**. Task request scripts are available in **request.sh**.

### **Use Case Flow:**

1. **Start a standalone node** on port **5000**.
2. **Send a password cracking request**.
3. **Add a new node** on port **5001**, connecting it to **5000**.
4. **Wait a few seconds**.
5. **Stop the node** on **5000**.
6. **Add a new node** on port **5002**, connecting it to **5001**.
7. **Nodes 5001 and 5002 complete the task**.
8. **Add a new node** on port **5003** and verify whether it receives information about the completed task.

---
