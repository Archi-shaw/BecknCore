# 📄 Netty UDP Echo Server

## 🧠 Problem Statement

In modern network communication, lightweight protocols like UDP are often preferred for low-latency and connectionless applications such as VoIP, online gaming, and real-time systems. However, implementing a reliable UDP server that handles packet transmission and echoes messages for testing and diagnostics is essential.

The challenge is to build a UDP server using **Netty**, a high-performance asynchronous event-driven network application framework, that can:

- Receive UDP packets  
- Echo them back to the sender  
- Handle asynchronous I/O and efficient event handling

---

## ✅ Proposed Solution

We implement a **UDP Echo Server** using **Netty 4.x**, which:

1. Listens for UDP packets on a configurable port.
2. Receives incoming datagram packets.
3. Extracts the sender's address and message.
4. Sends the same message back to the sender (echo).

By using Netty, we get benefits like:

- High throughput via non-blocking I/O (NIO)
- Event-driven architecture
- Clean separation of concerns (handler-based processing)
- Easy scalability for production-level workloads

---

## ⚙️ Technologies Used

- Java 8+
- Netty 4.1+
- Gradle (for dependency management)

---

## 🛠️ How It Works

1. The `EchoServer.java` sets up the Netty `Bootstrap` for UDP using `NioDatagramChannel`.
2. The `EchoServerHandler.java` handles incoming `DatagramPacket` messages.
3. Upon receiving a packet, the handler sends the message back to the sender using the same UDP port.

---


