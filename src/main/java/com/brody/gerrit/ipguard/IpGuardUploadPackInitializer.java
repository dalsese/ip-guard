package com.brody.gerrit.ipguard;

import com.google.gerrit.server.git.UploadPack;
import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.inject.Inject;
import java.net.InetAddress;

public class IpGuardUploadPackInitializer implements UploadPackInitializer {

    private final IpGuardPolicy policy;
    private final AuditLogger audit;

    @Inject
    IpGuardUploadPackInitializer(IpGuardPolicy policy, AuditLogger audit) {
        this.policy = policy;
        this.audit = audit;
    }

    @Override
    public void init(com.google.gerrit.entities.Project.NameKey project, UploadPack uploadPack) {
        String remoteIp = uploadPack.getPeer().getAddress().getAddress().getHostAddress();
        if (!policy.isAllowed(remoteIp, uploadPack.getPeer().getAddress().getAddress())) {
            throw new SecurityException("IP " + remoteIp + " is not allowed to access this repository.");
        }
        audit.log(remoteIp, project.get());
    }
}
