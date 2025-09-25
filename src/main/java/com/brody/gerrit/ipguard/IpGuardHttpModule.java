package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.gerrit.httpd.plugins.HttpPluginModule;

public class IpGuardHttpModule extends HttpPluginModule {
  @Override
  protected void configureServlets() {
    DynamicSet.bind(binder(), AllRequestFilter.class).to(IpGuardAllRequestFilter.class);
  }
}
