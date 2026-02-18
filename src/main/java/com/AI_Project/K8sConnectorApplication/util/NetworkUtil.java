package com.AI_Project.K8sConnectorApplication.util;

import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkUtil {
    public static boolean isTcpPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}