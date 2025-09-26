package com.brody.gerrit.ipguard;

import java.util.logging.Logger;

public class AuditLogger {
    private static final Logger logger = Logger.getLogger("ip-guard");

    public void log(String ip, String project) {
        logger.info("Access granted to IP " + ip + " for project " + project);
    }
}
