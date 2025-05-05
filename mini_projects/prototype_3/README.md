# 🔄 Netty UDP Echo Server with STUN Integration

## 📌 Problem Statement

Traditional UDP servers face limitations when deployed on devices behind NAT (Network Address Translation). In peer-to-peer networks or decentralized systems, it's crucial to enable communication between clients across NATs without relying on central servers or cloud platforms.

This project aims to enhance a simple UDP Echo Server by integrating **STUN (Session Traversal Utilities for NAT)** to allow NAT-traversal. This enables the server to:

- Determine its public IP address and port using a STUN server.
- Maintain connectivity across firewalled or NAT-ed networks.
- Handle and differentiate **STUN responses** and **application-layer UDP packets**.

The broader goal aligns with building a decentralized open-network communication protocol like **Beckn** using peer-to-peer architectures and UDP hole punching.

---

## 🎯 Objectives

- Build a Netty-based UDP Echo Server.
- Integrate STUN client logic to discover external IP/Port.
- Distinguish between STUN packets and regular messages.
- Echo back regular messages while responding appropriately to STUN exchanges.

---

## 🧩 Solution Architecture

1. **STUN Discovery**:  
   On startup, the server uses a public STUN server (like `stun.l.google.com:19302`) to discover its public-facing IP and UDP port.

2. **UDP Echo Logic**:  
   Netty listens for incoming `DatagramPacket`s. It inspects the payload to determine if the packet is:
   - A STUN response (identified by the first 2 bytes of the message)
   - A regular UDP application message (text, JSON, etc.)

3. **Packet Routing**:
   - If it's a STUN response, parse and log public IP and port.
   - If it's a regular message, echo the data back to the sender.

4. **Proxy Potential**:  
   This forms the foundational block to build a **UDP proxy agent** for Beckn, facilitating communication without a traditional cloud aggregator.

---

## ⚙️ Technologies Used

- Java 11+
- Netty 4.x
- Gradle
- STUN Protocol (RFC 5389)

---

## 🛠️ How It Works

### Startup
- Server binds to a local UDP port (e.g., 9999).
- Sends a STUN binding request to a public STUN server.
- Receives STUN response → extracts public IP and port.
- Logs the NAT-exposed address (can be registered to a registry like Beckn Registry).

### Runtime Behavior
- Receives incoming packets.
- Parses headers to detect whether the packet is a STUN message.
- Echoes messages or handles STUN accordingly.

