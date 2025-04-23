package prototype_1;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

public class StunClient {
    private static final Logger logger = Logger.getLogger(StunClient.class.getName());
    
    // STUN Message Types
    private static final char BINDING_REQUEST = 0x0001;
    private static final char BINDING_RESPONSE = 0x0101;
    
    // STUN Attributes
    private static final char MAPPED_ADDRESS = 0x0001;
    private static final char XOR_MAPPED_ADDRESS = 0x0020;
    
    // Magic Cookie constant (fixed value as per the RFC)
    private static final int MAGIC_COOKIE = 0x2112A442;
    
    // Default STUN server
    private static final String DEFAULT_STUN_SERVER = "stun.l.google.com";
    private static final int DEFAULT_STUN_PORT = 19302;
    
    public static void main(String[] args) {
        try {
            TransportAddress serverAddress = new TransportAddress(DEFAULT_STUN_SERVER, DEFAULT_STUN_PORT);
            StunClient client = new StunClient();
            TransportAddress publicAddress = client.discoverPublicAddress(serverAddress);
            
            if (publicAddress != null) {
                System.out.println("Your public address is: " + publicAddress);
            } else {
                System.out.println("Could not discover public address");
            }
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Discovers the public address by sending a STUN Binding Request
     * 
     * @param stunServer the STUN server address
     * @return the discovered public address or null if discovery failed
     * @throws IOException if there is a network error
     * @throws StunException if there is a STUN protocol error
     */
    public TransportAddress discoverPublicAddress(TransportAddress stunServer) 
            throws IOException, StunException {
        // Create a datagram socket
        DatagramSocket socket = new DatagramSocket();
        try {
            // Create a STUN Binding Request
            byte[] bindingRequest = createBindingRequest();
            
            // Send the request to the STUN server
            DatagramPacket request = new DatagramPacket(
                    bindingRequest, 
                    bindingRequest.length,
                    stunServer.getInetAddress(),
                    stunServer.getPort());
            
            socket.send(request);
            
            // Receive the response
            byte[] responseBuffer = new byte[512];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            
            socket.setSoTimeout(5000); // 5 second timeout
            socket.receive(response);
            
            // Parse the response
            Response stunResponse = parseStunResponse(responseBuffer, response.getLength());
            
            // Extract the mapped address
            return handleStunResponse(stunResponse);
        } finally {
            socket.close();
        }
    }
    
    /**
     * Creates a STUN Binding Request
     * 
     * @return the STUN Binding Request as byte array
     */
    private byte[] createBindingRequest() {
        byte[] request = new byte[20]; // STUN header is 20 bytes
        
        // Set message type (Binding Request)
        request[0] = 0x00;
        request[1] = 0x01;
        
        // Set message length (no attributes, so 0)
        request[2] = 0x00;
        request[3] = 0x00;
        
        // Set magic cookie (in network byte order)
        request[4] = (byte) (MAGIC_COOKIE >> 24);
        request[5] = (byte) ((MAGIC_COOKIE >> 16) & 0xFF);
        request[6] = (byte) ((MAGIC_COOKIE >> 8) & 0xFF);
        request[7] = (byte) (MAGIC_COOKIE & 0xFF);
        
        // Set transaction ID (random 12 bytes)
        for (int i = 0; i < 12; i++) {
            request[i + 8] = (byte) (Math.random() * 256);
        }
        
        return request;
    }
    
    /**
     * Parses a STUN response
     * 
     * @param data the response data
     * @param length the response length
     * @return the parsed STUN response
     * @throws StunException if the response is invalid
     */
    private Response parseStunResponse(byte[] data, int length) throws StunException {
        if (length < 20) {
            throw new StunException("Response too short");
        }
        
        // Check message type (should be Binding Response)
        int messageType = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        if (messageType != BINDING_RESPONSE) {
            throw new StunException("Unexpected message type: " + messageType);
        }
        
        // Get message length
        int messageLength = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        
        // Parse attributes
        Response response = new Response();
        
        int pos = 20; // Start after the header
        while (pos + 4 <= length && pos + 4 <= 20 + messageLength) {
            // Get attribute type and length
            int attributeType = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            int attributeLength = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
            
            pos += 4; // Move past the attribute header
            
            // Process the attribute
            if (attributeType == MAPPED_ADDRESS && attributeLength >= 8) {
                response.mappedAddress = parseMappedAddress(data, pos);
            } else if (attributeType == XOR_MAPPED_ADDRESS && attributeLength >= 8) {
                response.xorMappedAddress = parseXorMappedAddress(data, pos);
            }
            
            // Move to the next attribute (with padding to 4-byte boundary)
            pos += attributeLength;
            if (attributeLength % 4 != 0) {
                pos += 4 - (attributeLength % 4);
            }
        }
        
        return response;
    }
    
    /**
     * Parses a MAPPED-ADDRESS attribute
     * 
     * @param data the response data
     * @param offset the offset to the attribute value
     * @return the mapped address
     * @throws StunException if the attribute is invalid
     */
    private TransportAddress parseMappedAddress(byte[] data, int offset) throws StunException {
        // Skip the first byte (reserved)
        // Get address family (IPv4 = 1, IPv6 = 2)
        int family = data[offset + 1] & 0xFF;
        if (family != 1) {
            throw new StunException("Only IPv4 is supported");
        }
        
        // Get port (in network byte order)
        int port = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
        
        // Get IP address (IPv4 = 4 bytes)
        byte[] addressBytes = new byte[4];
        System.arraycopy(data, offset + 4, addressBytes, 0, 4);
        
        try {
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            return new TransportAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new StunException("Invalid IP address");
        }
    }
    
    /**
     * Parses an XOR-MAPPED-ADDRESS attribute
     * 
     * @param data the response data
     * @param offset the offset to the attribute value
     * @return the mapped address
     * @throws StunException if the attribute is invalid
     */
    private TransportAddress parseXorMappedAddress(byte[] data, int offset) throws StunException {
        // Skip the first byte (reserved)
        // Get address family (IPv4 = 1, IPv6 = 2)
        int family = data[offset + 1] & 0xFF;
        if (family != 1) {
            throw new StunException("Only IPv4 is supported");
        }
        
        // Get port (XORed with the first 16 bits of the magic cookie)
        int xorPort = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
        int port = xorPort ^ (MAGIC_COOKIE >> 16);
        
        // Get IP address (XORed with the magic cookie)
        byte[] xorAddressBytes = new byte[4];
        System.arraycopy(data, offset + 4, xorAddressBytes, 0, 4);
        
        byte[] addressBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            addressBytes[i] = (byte) (xorAddressBytes[i] ^ ((MAGIC_COOKIE >> (24 - 8 * i)) & 0xFF));
        }
        
        try {
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            return new TransportAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new StunException("Invalid IP address");
        }
    }
    
    /**
     * Handles the STUN response to extract the mapped address
     * 
     * @param response the STUN response
     * @return the mapped address
     * @throws StunException if there is no mapped address
     */
    private static TransportAddress handleStunResponse(Response response) throws StunException {
        if (response.xorMappedAddress != null) {
            return response.xorMappedAddress;
        } else if (response.mappedAddress != null) {
            return response.mappedAddress;
        } else {
            logger.warning("No mapped address found in response");
            return null;
        }
    }
    
    /**
     * Represents a STUN response
     */
    private static class Response {
        TransportAddress mappedAddress;
        TransportAddress xorMappedAddress;
    }
    
    /**
     * Represents a transport address (IP address and port)
     */
    public static class TransportAddress {
        private final InetAddress address;
        private final int port;
        
        public TransportAddress(String host, int port) throws UnknownHostException {
            this.address = InetAddress.getByName(host);
            this.port = port;
        }
        
        public TransportAddress(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
        
        public InetAddress getInetAddress() {
            return address;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getAddress() {
            return address.getHostAddress() + ":" + port;
        }
        
        @Override
        public String toString() {
            return getAddress();
        }
    }
}

/**
 * Exception thrown by the STUN client
 */
class StunException extends Exception {
    public StunException(String message) {
        super(message);
    }
}