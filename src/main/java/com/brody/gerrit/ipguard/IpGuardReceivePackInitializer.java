package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.gerrit.server.RemotePeer;
import com.google.gerrit.entities.Project;
import org.eclipse.jgit.transport.ReceivePack;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardReceivePackInitializer implements ReceivePackInitializer {

    private final IpGuardPolicy policy;

    @Inject
    public IpGuardReceivePackInitializer(IpGuardPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void init(Project.NameKey project, ReceivePack receivePack, RemotePeer peer) {
        InetAddress addr = peer.getRemoteAddress();
        if (!policy.isAllowed(addr)) {
            throw new SecurityException("IP " + addr + " is not allowed to push to repository " + project.get());
        }
    }
}
