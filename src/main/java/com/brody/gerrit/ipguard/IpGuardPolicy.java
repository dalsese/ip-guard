package com.brody.gerrit.ipguard;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IpGuardPolicy {

    private final List<String> allowedIps;

    public IpGuardPolicy(Path confFile) throws IOException {
        allowedIps = Files.readAllLines(confFile);
    }

    public boolean isAllowed(InetAddress addr) {
        String ip = addr.getHostAddress();
        return allowedIps.contains(ip);
    }
}
