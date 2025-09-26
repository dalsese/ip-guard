package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.ReceivePack;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardReceivePackInitializer implements ReceivePackInitializer {

    private final IpGuardPolicy policy;
    private final AuditLogger audit;

    @Inject
    IpGuardReceivePackInitializer(IpGuardPolicy policy, AuditLogger audit) {
        this.policy = policy;
        this.audit = audit;
    }

    @Override
    public void init(com.google.gerrit.entities.Project.NameKey project, ReceivePack receivePack) {
        String remoteIp = receivePack.getPeer().getAddress().getAddress().getHostAddress();
        if (!policy.isAllowed(remoteIp, receivePack.getPeer().getAddress().getAddress())) {
            throw new SecurityException("IP " + remoteIp + " is not allowed to access this repository.");
        }
        audit.log(remoteIp, project.get());
    }
}
