package com.brody.gerrit.ipguard;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.NoSuchProjectException; // ✅ 추가
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
    if (Boolean.getBoolean("ipguard.smoke")) {
      return false; // deny-all in smoke mode
    }

    String ip = IpMatcher.normalize(clientIp);

    // ✅ getFromProjectConfig()에서 프로젝트를 못 찾으면 NoSuchProjectException 발생 가능 → fail-closed
    final PluginConfig pc;
    try {
      pc = cfgFactory.getFromProjectConfig(project, PLUGIN_NAME);
    } catch (NoSuchProjectException e) {
      return false;
    }

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

    if (rules == null || rules.length == 0) {
      return false; // fail-closed when no policy
    }

    for (String r : rules) {
      if (IpMatcher.match(ip, r)) {
        return true;
      }
    }
    return false;
  }
}
