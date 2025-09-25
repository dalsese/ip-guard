package com.brody.gerrit.ipguard;

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class IpGuardAllRequestFilter extends AllRequestFilter {
  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String ip = extractClientIp(req);
    try {
      ClientIpContext.set(ip);
      chain.doFilter(req, res);
    } finally {
      ClientIpContext.clear();
    }
  }

  static String extractClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    String ip = null;
    if (xff != null && !xff.isEmpty()) {
      String first = xff.split(",")[0].trim();
      ip = normalize(first);
    }
    if (ip == null || ip.isEmpty()) {
      String xri = req.getHeader("X-Real-IP");
      if (xri != null && !xri.isEmpty()) {
        ip = normalize(xri.trim());
      }
    }
    if (ip == null || ip.isEmpty()) {
      ip = normalize(req.getRemoteAddr());
    }
    return ip;
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
