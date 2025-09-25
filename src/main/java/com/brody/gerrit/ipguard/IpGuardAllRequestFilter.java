package com.brody.gerrit.ipguard;

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class IpGuardAllRequestFilter implements AllRequestFilter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (request instanceof HttpServletRequest) ? (HttpServletRequest) request : null;
    HttpServletResponse res = (response instanceof HttpServletResponse) ? (HttpServletResponse) response : null;

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

  @Override public void init(FilterConfig filterConfig) throws ServletException { /* no-op */ }
  @Override public void destroy() { /* no-op */ }

  static String extractClientIp(HttpServletRequest req) {
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

  private static String normalize(String in) {
    if (in == null) return null;
    String s = in.trim();
    if (s.startsWith("[")) {
      int close = s.indexOf(']');
      if (close > 0) s = s.substring(1, close);
      return s;
    }
    int lastColon = s.lastIndexOf(':');
    if (lastColon > -1 && s.indexOf(':') == lastColon && s.contains(".")) {
      return s.substring(0, lastColon);
    }
    return s;
  }
}
