package com.brody.gerrit.ipguard;

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Singleton;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Singleton
public class IpGuardAllRequestFilter extends AllRequestFilter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String ip = null;
    if (request instanceof HttpServletRequest) {
      ip = extractClientIp((HttpServletRequest) request);
    }
    try {
      ClientIpContext.set(ip);
      chain.doFilter(request, response);
    } finally {
      ClientIpContext.clear();
    }
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
