package com.brody.gerrit.ipguard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class IpGuardPolicy {

    private final Set<String> allowedIps = new HashSet<>();

    public IpGuardPolicy() {
        try (BufferedReader br = new BufferedReader(new FileReader("conf/allowed-ips.conf"))) {
            String line;
            while ((line = br.readLine()) != null) {
                allowedIps.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllowed(String ip, InetAddress addr) {
        return allowedIps.contains(ip);
    }
}
