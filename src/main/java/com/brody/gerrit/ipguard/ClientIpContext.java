package com.brody.gerrit.ipguard;

final class ClientIpContext {
  private static final ThreadLocal<String> TL = new ThreadLocal<>();
  private ClientIpContext() {}
  static void set(String ip) { TL.set(ip); }
  static String get() { return TL.get(); }
  static void clear() { TL.remove(); }
}
