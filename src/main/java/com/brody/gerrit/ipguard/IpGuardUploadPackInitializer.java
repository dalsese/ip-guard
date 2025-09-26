package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.gerrit.server.RemotePeer;
import com.google.gerrit.entities.Project;
import org.eclipse.jgit.transport.UploadPack;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardUploadPackInitializer implements UploadPackInitializer {

    private final IpGuardPolicy policy;

    @Inject
    public IpGuardUploadPackInitializer(IpGuardPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void init(Project.NameKey project, UploadPack uploadPack, RemotePeer peer) {
        InetAddress addr = peer.getRemoteAddress();
        if (!policy.isAllowed(addr)) {
            throw new SecurityException("IP " + addr + " is not allowed to clone repository " + project.get());
        }
    }
}
