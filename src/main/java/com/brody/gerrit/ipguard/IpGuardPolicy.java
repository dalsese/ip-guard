package com.brody.gerrit.ipguard;

import com.google.gerrit.entities.Project;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IpGuardPolicy {
    private final Map<String, List<String>> repoIpMap = new HashMap<>();

    public IpGuardPolicy() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("conf/allowed-ips.conf"));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=");
                if (parts.length != 2) continue;
                String repo = parts[0].trim();
                List<String> ips = Arrays.asList(parts[1].trim().split(","));
                repoIpMap.put(repo, ips);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllowed(Project.NameKey project, InetAddress ip) {
        List<String> allowed = repoIpMap.getOrDefault(project.get(), Collections.emptyList());
        return allowed.contains(ip.getHostAddress());
    }
}
