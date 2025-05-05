# Beckn STUN Agent - Peer-to-Peer Communication via STUN

## 🔧 Problem Statement

Today, Beckn protocols rely on server-to-server communication where cloud-based aggregators and tech providers provision infrastructure for buyers and sellers to transact. However, this introduces centralized dependency, cost, and complexity.

**Goal:** Eliminate aggregators and hosted cloud platforms by enabling true peer-to-peer (P2P) communication between buyer and seller apps—each running on separate machines behind NAT (Network Address Translation).

## 🧠 Solution Approach

We propose using **STUN (Session Traversal Utilities for NAT)** and **UDP Hole Punching** techniques to allow NATed peers to directly communicate.

### Key Concepts

- **STUN Protocol:** Used to discover the public IP and port assigned by NAT.
- **ICE (Interactive Connectivity Establishment):** Helps in NAT traversal and session negotiation.
- **UDP Hole Punching:** Allows two NATed peers to connect directly.
- **STUN Server:** A public STUN server will be used for both buyer and seller apps to discover their public-facing IP/port.
- **Proxy Agent:** Each peer runs a local proxy agent to forward requests to its Beckn app.

### Workflow

1. Buyer and seller apps run on different NATed machines.
2. Each app connects to a STUN server to get their public IP/port.
3. This mapping is published in a Beckn-compatible registry.
4. Buyers discover seller apps via search engines or registries.
5. Direct P2P communication is established using the mapped addresses.
6. Proxy agents forward traffic locally to the Beckn applications.

---

## ✅ Expected Outcome

- Successfully send a STUN binding request.
- Print the **mapped public address (IP and port)** for the local peer.
- Demonstrate connectivity without cloud infrastructure.

---

## 🚀 Technologies Used

- Java
- ICE4J (STUN and ICE handling)
- UDP Networking
- Docker (for deployment)
- HTTP Proxy (for future work)

---



## 📂 Run the Java Sample

Ensure you have Java 8+ and Gradle installed.

1. Clone the repo
2. Run the Java file (see below)
