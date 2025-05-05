# Beckn STUN Agent

### A peer-to-peer network architecture for Beckn transactions without centralized aggregators.

---

## 🧾 Project Description

Today, Beckn operates primarily as a server-to-server protocol, relying on aggregators and cloud providers to onboard and manage sellers. This project proposes an alternative design that enables **direct peer-to-peer communication** between participants (buyers and sellers) without any centralized infrastructure.

By leveraging **ICE**, **STUN**, and **UDP hole punching**, users' machines can directly communicate through NAT firewalls using open ports. These ports are maintained and identified by STUN servers and then published in the Beckn registry, allowing other peers to discover and connect.

---

## 🎯 Goals

- Build a **STUN Proxy Agent** that peers (buyers/sellers) can install on their machines.
- The agent should:
  - Handle STUN communications.
  - Proxy requests to a Beckn application co-hosted on the same machine.
- Modify the **reference Beckn registry** to support registering URLs that use UDP ports exposed via STUN.
- Update **reference BAP and BPP implementations** to support communication via this proxy agent.

---

## ✅ Expected Outcome

A successful end-to-end **Beckn transaction** between buyer and seller applications operating behind NAT firewalls, without using a cloud aggregator.





## 🔧 Implementation Details

- **Protocols**: ICE, STUN, TURN, UDP Hole Punching, HTTP/3
- **Infrastructure**: STUN Server (hosted or free), HTTP Proxy server
- **Languages/Tools**: Java, Docker

---

## 📦 Tech Stack

| Component         | Technology    |
|------------------|---------------|
| NAT Traversal    | ICE, STUN     |
| Proxy Server     | HTTP Proxy    |
| Communication    | UDP, HTTP/3   |
| DevOps           | Docker        |
| Backend Language | Java          |



