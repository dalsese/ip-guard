package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.inject.AbstractModule;

public class IpGuardModule extends AbstractModule {
  @Override
  protected void configure() {
    DynamicSet.bind(binder(), UploadPackInitializer.class).to(IpGuardUploadPackInitializer.class);
    DynamicSet.bind(binder(), ReceivePackInitializer.class).to(IpGuardReceivePackInitializer.class);
    bind(IpGuardPolicy.class);
    bind(AuditLogger.class);
  }
}
