package com.brody.gerrit.ipguard;

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Singleton;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class IpGuardAllRequestFilter implements AllRequestFilter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (request instanceof HttpServletRequest) ? (HttpServletRequest) request : null;

    String ip = null;
    if (req != null) {
      ip = extractClientIp(req);
    }

    try {
      ClientIpContext.set(ip);
      chain.doFilter(request, response);
    } finally {
      ClientIpContext.clear();
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // no-op
  }

  @Override
  public void destroy() {
    // no-op
  }

  private static String extractClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    String ip = null;

    if (xff != null && !xff.isEmpty()) {
      String first = xff.split(",")[0].trim();
      ip = IpMatcher.normalize(first);
    }
    if (ip == null || ip.isEmpty()) {
      String xri = req.getHeader("X-Real-IP");
      if (xri != null && !xri.isEmpty()) {
        ip = IpMatcher.normalize(xri.trim());
      }
    }
    if (ip == null || ip.isEmpty()) {
      ip = IpMatcher.normalize(req.getRemoteAddr());
    }
    return ip;
  }
}
