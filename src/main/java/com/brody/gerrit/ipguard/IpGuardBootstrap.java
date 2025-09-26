package com.brody.gerrit.ipguard;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 플러그인 로드시 즉시 생성되어 시작 로그를 남기는 eager singleton. */
@Singleton
public class IpGuardBootstrap {
  private static final Logger log = LoggerFactory.getLogger(IpGuardBootstrap.class);

  @Inject
  IpGuardBootstrap() {
    log.info("[ip-guard] Plugin bootstrap constructed (eager singleton)");
  }
}
