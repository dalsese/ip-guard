package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.restapi.Url;
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

  @Override
  public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
      throws IOException, ServletException {

    if (!policy.isEnforceWeb()) {
      chain.doFilter(req, rsp);
      return;
    }

    // 인증 전 요청은 패스 (로그인 페이지/정적 리소스 등)
    CurrentUser cu = currentUser.get();
    if (cu == null || !cu.isIdentifiedUser()) {
      chain.doFilter(req, rsp);
      return;
    }

    String username = cu.getUserName().orElse(null);
    // 클라이언트 IP 추출 (프록시 환경이면 XFF 우선)
    String ip = extractClientIp(req);

    boolean ok = policy.isAllowed(username, ip);
    if (!ok) {
      log.warn("[ip-guard] deny web request: user={} ip={} uri={}", username, ip, Url.decode(req.getRequestURI()));
      // 세션까지 끊고 싶다면 WebSession.logout()을 주입해 호출하면 되지만,
      // 플러그인 API 종속성 문제를 피하려고 여기서는 403만 반환한다.
      rsp.sendError(HttpServletResponse.SC_FORBIDDEN,
          "Access denied by ip-guard: user '" + username + "' is not allowed from IP " + ip);
      return;
    }

    chain.doFilter(req, rsp);
  }

  private static String extractClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // "client, proxy1, proxy2" 형태 → 첫 항목
      int comma = xff.indexOf(',');
      return (comma > 0 ? xff.substring(0, comma) : xff).trim();
    }
    return req.getRemoteAddr();
  }
}
