package com.brody.gerrit.ipguard;

import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.gerrit.extensions.annotations.PluginName;
import org.eclipse.jgit.lib.Config;
import com.google.gerrit.entities.Project;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class IpGuardPolicy {
  private final Map<String, Set<String>> allowMap = new ConcurrentHashMap<>();
  private volatile boolean enforceWeb = true;

  @Inject
  IpGuardPolicy(PluginConfigFactory cfgFactory, @PluginName String pluginName) {
    // 전역 플러그인 설정: $site_path/etc/ip-guard.config
    Config cfg = cfgFactory.getGlobalPluginConfig(pluginName);
    this.enforceWeb = cfg.getBoolean("settings", "enforceWeb", true);

    for (String user : cfg.getSubsections("allow")) {
      Set<String> ips = new HashSet<>(Arrays.asList(cfg.getStringList("allow", user, "ip")));
      // 공백/빈 값 제거
      ips.removeIf(s -> s == null || s.isBlank());
      allowMap.put(user, ips);
    }
  }

  public boolean isEnforceWeb() {
    return enforceWeb;
  }

  /** username이 clientIp로 접근하는 것이 허용되는지 */
  public boolean isAllowed(String username, String clientIp) {
    if (username == null || username.isBlank() || clientIp == null) return false;
    Set<String> ips = allowMap.get(username);
    return ips != null && ips.contains(clientIp);
  }
  
  public boolean isAllowed(Project.NameKey project, String username, String clientIp) {
    return isAllowed(username, clientIp);
  }
}
