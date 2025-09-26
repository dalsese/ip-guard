package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.UploadPack;
import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.RemotePeer;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardUploadPackInitializer implements UploadPackInitializer {
    private final IpGuardPolicy policy;

    @Inject
    IpGuardUploadPackInitializer(IpGuardPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void init(Project.NameKey project, UploadPack up) {
        RemotePeer peer = up.getPeer();
        InetAddress remoteIp = peer.getRemoteAddress();
        if (!policy.isAllowed(project, remoteIp)) {
            throw new SecurityException("IP not allowed: " + remoteIp.getHostAddress());
        }
    }
}
