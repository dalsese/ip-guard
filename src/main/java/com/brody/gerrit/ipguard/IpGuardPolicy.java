package com.brody.gerrit.ipguard;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class IpGuardPolicy {
  private static final String PLUGIN_NAME = "ip-guard";
  private final PluginConfigFactory cfgFactory;

  @Inject
  IpGuardPolicy(PluginConfigFactory cfgFactory) {
    this.cfgFactory = cfgFactory;
  }

  boolean isAllowed(Project.NameKey project, String op, String clientIp) {
    if (Boolean.getBoolean("ipguard.smoke")) return false; // deny-all for smoke test
    String ip = IpMatcher.normalize(clientIp);

    PluginConfig pc = cfgFactory.getFromProjectConfig(project, PLUGIN_NAME);
    String[] rules;
    switch (op) {
      case "push":
        rules = pc.getStringList("allowPush");
        break;
      case "clone/fetch":
      default:
        rules = pc.getStringList("allowClone");
        if (rules == null || rules.length == 0) {
          rules = pc.getStringList("allowRead");
        }
        break;
    }
    if (rules == null || rules.length == 0) return false; // fail-closed
    for (String r : rules) if (IpMatcher.match(ip, r)) return true;
    return false;
  }
