package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.ReceivePack;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.RemotePeer;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardReceivePackInitializer implements ReceivePackInitializer {
    private final IpGuardPolicy policy;

    @Inject
    IpGuardReceivePackInitializer(IpGuardPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void init(Project.NameKey project, ReceivePack rp) {
        RemotePeer peer = rp.getPeer();
        InetAddress remoteIp = peer.getRemoteAddress();
        if (!policy.isAllowed(project, remoteIp)) {
            throw new SecurityException("IP not allowed: " + remoteIp.getHostAddress());
        }
    }
}
