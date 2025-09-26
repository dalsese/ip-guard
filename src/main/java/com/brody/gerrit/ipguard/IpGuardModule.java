package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.config.FactoryModule;  // ★ AbstractModule → FactoryModule
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.gerrit.server.git.UploadPackInitializer;

public class IpGuardModule extends FactoryModule {
  @Override
  protected void configure() {
    // 1) 플러그인 시작/종료 시점에 확실히 로그 찍기
    listener().to(IpGuardLifecycle.class);

    // 2) Git 경로 초기화기 바인딩 (clone/fetch/push 훅 주입)
    DynamicSet.bind(binder(), UploadPackInitializer.class).to(IpGuardUploadPackInitializer.class);
    DynamicSet.bind(binder(), ReceivePackInitializer.class).to(IpGuardReceivePackInitializer.class);

    // 3) 정책/감사 로거 등 내부 의존성 바인딩
    bind(IpGuardPolicy.class);
    bind(AuditLogger.class);
  }
}
