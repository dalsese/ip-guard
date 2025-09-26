package com.brody.gerrit.ipguard;

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class IpGuardAllRequestFilter extends AllRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(IpGuardAllRequestFilter.class);

  private final Provider<CurrentUser> currentUser;
  private final IpGuardPolicy policy;

  @Inject
  IpGuardAllRequestFilter(Provider<CurrentUser> currentUser, IpGuardPolicy policy) {
    this.currentUser = currentUser;
    this.policy = policy;
  }

  // ★ 반드시 HTTP 시그니처를 오버라이드해야 함 (AllRequestFilter의 추상 메서드)
  @Override
  public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
      throws IOException, ServletException {

    if (!policy.isEnforceWeb()) {
      chain.doFilter(req, rsp);
      return;
    }

    // 로그인 전(익명) 요청은 패스
    CurrentUser cu = currentUser.get();
    if (cu == null || !cu.isIdentifiedUser()) {
      chain.doFilter(req, rsp);
      return;
    }

    String username = cu.getUserName().orElse(null);
    String ip = extractClientIp(req);

    boolean ok = policy.isAllowed(username, ip);
    if (!ok) {
      log.warn("[ip-guard] deny web request: user={} ip={} uri={}", username, ip, req.getRequestURI());
      rsp.sendError(HttpServletResponse.SC_FORBIDDEN,
          "Access denied by ip-guard: user '" + username + "' is not allowed from IP " + ip);
      return;
    }

    chain.doFilter(req, rsp);
  }

  private static String extractClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      int comma = xff.indexOf(',');
      return (comma > 0 ? xff.substring(0, comma) : xff).trim();
    }
    return req.getRemoteAddr();
  }
}
