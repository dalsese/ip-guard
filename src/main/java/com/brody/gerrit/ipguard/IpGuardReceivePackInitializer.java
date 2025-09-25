package com.brody.gerrit.ipguard;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.RemotePeer;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHookChain;
import java.util.Arrays;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

class IpGuardReceivePackInitializer implements ReceivePackInitializer {
  private final IpGuardPolicy policy;
  private final AuditLogger audit;
  @Inject(optional = true) @RemotePeer private Provider<SocketAddress> remotePeer;

  @Inject
  IpGuardReceivePackInitializer(IpGuardPolicy policy, AuditLogger audit) {
    this.policy = policy;
    this.audit = audit;
  }

  @Override
  public void init(Project.NameKey project, ReceivePack rp) {
    PreReceiveHook guard = (rpIgnored, commands) -> {
      // 기존 GuardHook.check() 로직과 동일 (차단 시 commands 전체 REJECT 세팅)
      String ip = /* ... 기존 코드 ... */;
      boolean allowed = policy.isAllowed(project, "push", ip);
      // ... 기존 로깅/거부 처리 ...
    };
  
    PreReceiveHook existing = rp.getPreReceiveHook();
    if (existing == null || existing == PreReceiveHook.NULL) {
      rp.setPreReceiveHook(guard);
    } else {
      // 푸시도 "차단 우선" 순서로 체인 생성
      rp.setPreReceiveHook(PreReceiveHookChain.newChain(Arrays.asList(guard, existing)));
    }
  }

  private class GuardHook implements PreReceiveHook {
    private final Project.NameKey project;
    private final ReceivePack rp;
    GuardHook(Project.NameKey p, ReceivePack rp) { this.project = p; this.rp = rp; }

    @Override
    public void onPreReceive(ReceivePack rpIgnored, Collection<ReceiveCommand> commands) {
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

      boolean allowed = policy.isAllowed(project, "push", ip);

      String userName = "-";
      try {
        PersonIdent ident = rp.getRefLogIdent();
        if (ident != null && ident.getName() != null) userName = ident.getName();
      } catch (Throwable ignore) {}

      if (!allowed) {
        for (ReceiveCommand c : commands) {
          c.setResult(ReceiveCommand.Result.REJECTED_OTHER_REASON,
              "ip-guard: push not allowed from IP " + ip);
        }
        audit.record(false, "push", project.get(), userName, ip, "blocked by ip-guard");
      } else {
        audit.record(true, "push", project.get(), userName, ip, null);
      }
    }
  }
}
