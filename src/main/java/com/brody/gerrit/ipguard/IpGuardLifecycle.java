package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.events.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpGuardLifecycle implements LifecycleListener {
  private static final Logger log = LoggerFactory.getLogger(IpGuardLifecycle.class);
  @Override public void start() { log.info("[ip-guard] Plugin started"); }
  @Override public void stop()  { log.info("[ip-guard] Plugin stopped"); }
}
