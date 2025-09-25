package com.brody.gerrit.ipguard;

import java.net.InetAddress;
import java.net.UnknownHostException;

class IpMatcher {
  static boolean match(String candidateIp, String rule) {
    if (candidateIp == null || candidateIp.isEmpty() || rule == null || rule.isEmpty()) return false;
    String ip = normalize(candidateIp);
    String r  = normalize(rule);

    int slash = r.indexOf('/');
    if (slash > 0) {
      String base = r.substring(0, slash);
      int prefix = Integer.parseInt(r.substring(slash + 1));
      try {
        InetAddress baseAddr = InetAddress.getByName(base);
        InetAddress ipAddr   = InetAddress.getByName(ip);
        if (baseAddr.getClass() != ipAddr.getClass()) return false;
        byte[] b = baseAddr.getAddress();
        byte[] i = ipAddr.getAddress();
        int bits = prefix;
        for (int idx = 0; idx < b.length && bits > 0; idx++) {
          int maskBits = Math.min(8, bits);
          int mask = 0xFF << (8 - maskBits) & 0xFF;
          if ((b[idx] & mask) != (i[idx] & mask)) return false;
          bits -= maskBits;
        }
        return true;
      } catch (UnknownHostException e) {
        return false;
      }
    }
    return ip.equals(r);
  }

  static String normalize(String in) {
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
    int pct = s.indexOf('%'); // IPv6 zone id
    if (pct > 0) s = s.substring(0, pct);
    return s;
  }
}
