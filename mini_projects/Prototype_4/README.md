# 🔄 Setting Up an HTTP/3 Server with Netty-QUIC

## 📌 Problem Statement

As the web continues to evolve, performance becomes increasingly important, especially for high-traffic applications. **HTTP/3**, built on top of the **QUIC** protocol, promises to reduce latency and improve connection performance, particularly on unreliable networks. This project aims to set up a simple **HTTP/3 server** using **Netty-QUIC** to leverage the benefits of QUIC's multiplexing and low-latency features while supporting encryption for secure communication.

The goal is to:

- Set up a basic HTTP/3 server.
- Enable **QUIC** support to provide faster and more reliable connections.
- Handle basic HTTP/3 features such as multiplexing and encryption.

---

## 🎯 Objectives

- Build an **HTTP/3 server** using **Netty** and **QUIC**.
- Implement **QUIC encryption** using **TLS**.
- Handle both **HTTP/3 requests** and **QUIC connections** efficiently.
- Provide an easily extendable foundation for future **HTTP/3**-based services.

---


## ⚙️ Technologies Used

- **Java 11+** (Required for QUIC and HTTP/3 support)
- **Netty 4.x** (for HTTP/3 and QUIC support)
- **Gradle** (for dependency management)
- **QUIC Protocol** (for low-latency, multiplexed connections)
- **TLS** (for encrypted communication)

---

## 🛠️ How It Works

### Startup
- The **server** binds to a local port (e.g., `4433`) and starts listening for QUIC connections.
- It then **sets up SSL/TLS certificates** to secure the communication.
- The server sends and receives **QUIC**-based HTTP/3 requests, handling each connection with **Netty** and **QUIC SSL**.

### Packet Handling
- **QUIC connection setup** happens when a client sends a **QUIC handshake**.
- Once a connection is established, the server uses **Netty's HTTP/3 codec** to handle HTTP requests over the QUIC transport.
- The server can **process requests** (e.g., static content or simple JSON responses) and send them back over the same QUIC connection.


