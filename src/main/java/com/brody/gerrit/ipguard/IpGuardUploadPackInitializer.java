package com.brody.gerrit.ipguard;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.RemotePeer;
import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import org.eclipse.jgit.transport.PreUploadHook;
import org.eclipse.jgit.transport.PreUploadHookChain;
import java.util.Arrays;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;

class IpGuardUploadPackInitializer implements UploadPackInitializer {
  private final IpGuardPolicy policy;
  private final AuditLogger audit;
  @Inject(optional = true) @RemotePeer private Provider<SocketAddress> remotePeer;

  @Inject
  IpGuardUploadPackInitializer(IpGuardPolicy policy, AuditLogger audit) {
    this.policy = policy;
    this.audit = audit;
  }

  @Override
  public void init(Project.NameKey project, UploadPack up) {
    PreUploadHook guard = new GuardHook(project);
    PreUploadHook existing = up.getPreUploadHook();

    if (existing == null || existing == PreUploadHook.NULL) {
      up.setPreUploadHook(guard);
    } else {
      // 우리 훅이 먼저 실행되어 "차단 우선"이 되도록 순서: [guard, existing]
      up.setPreUploadHook(PreUploadHookChain.newChain(Arrays.asList(guard, existing)));
    }
  }


  private class GuardHook implements PreUploadHook {
    private final Project.NameKey project;
    GuardHook(Project.NameKey p) { this.project = p; }

    @Override
    public void onBeginNegotiateRound(UploadPack up,
        Collection<? extends org.eclipse.jgit.lib.ObjectId> wants, int cntOffered)
        throws ServiceMayNotContinueException {
      check();
    }

    @Override
    public void onEndNegotiateRound(UploadPack up,
        Collection<? extends org.eclipse.jgit.lib.ObjectId> wants, int cntCommon, int cntNotFound, boolean ready)
        throws ServiceMayNotContinueException { /* no-op */ }

    @Override
    public void onSendPack(UploadPack up,
        Collection<? extends org.eclipse.jgit.lib.ObjectId> wants,
        Collection<? extends org.eclipse.jgit.lib.ObjectId> haves)
        throws ServiceMayNotContinueException { /* no-op */ }

    private void check() throws ServiceMayNotContinueException {
      String ip = ClientIpContext.get();
      if ((ip == null || ip.isEmpty()) && remotePeer != null) {
        try {
          SocketAddress sa = remotePeer.get();
          if (sa instanceof InetSocketAddress) {
            ip = ((InetSocketAddress) sa).getAddress().getHostAddress();
          } else if (sa != null) {
            ip = sa.toString();
          }
        } catch (Throwable ignore) {}
      }
      boolean allowed = policy.isAllowed(project, "clone/fetch", ip);
      if (!allowed) {
        audit.record(false, "clone/fetch", project.get(), "-", ip, "blocked by ip-guard");
        throw new ServiceMayNotContinueException("ip-guard: clone/fetch not allowed from IP " + ip);
      } else {
        audit.record(true, "clone/fetch", project.get(), "-", ip, null);
      }
    }
  }
}
