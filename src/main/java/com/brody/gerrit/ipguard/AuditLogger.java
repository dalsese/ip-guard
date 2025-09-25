package com.brody.gerrit.ipguard;

import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

@Singleton
class AuditLogger {
  private final Logger logger;

  @Inject
  AuditLogger(SitePaths sitePaths) {
    logger = Logger.getLogger("ip-guard");
    logger.setUseParentHandlers(false);
    try {
      Path dir = sitePaths.logs_dir;
      if (dir != null) {
        Files.createDirectories(dir);
        FileHandler fh = new FileHandler(dir.resolve("ip-guard.log").toString(), true);
        fh.setFormatter(new Formatter() {
          @Override public String format(LogRecord r) {
            return String.format("%1$tF %1$tT %2$s%n", r.getMillis(), r.getMessage());
          }
        });
        logger.addHandler(fh);
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "ip-guard: failed to init file handler", e);
    }
  }

  void record(boolean allowed, String op, String project, String user, String ip, String reason) {
    String decision = allowed ? "ALLOW" : "DENY";
    String msg = String.format(
        "IP-GUARD %s op=%s proj=%s user=%s ip=%s %s",
        decision, op, project, user == null ? "-" : user, ip == null ? "-" : ip,
        reason == null ? "" : ("reason=" + reason));
    logger.info(msg);
  }
}
