package com.brody.gerrit.ipguard;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.git.ReceivePackInitializer;
import com.google.gerrit.server.git.UploadPackInitializer;
import com.google.inject.AbstractModule;

public class IpGuardModule extends AbstractModule {
  @Override
  protected void configure() {
    // [A] Git 경로 초기화기 바인딩 (원래 코드 그대로)
    DynamicSet.bind(binder(), UploadPackInitializer.class).to(IpGuardUploadPackInitializer.class);
    DynamicSet.bind(binder(), ReceivePackInitializer.class).to(IpGuardReceivePackInitializer.class);

    // [B] 내부 의존성
    bind(IpGuardPolicy.class);
    bind(AuditLogger.class);

    // [C] 플러그인 시작 시 확실히 로그를 찍는 eager singleton
    bind(IpGuardBootstrap.class).asEagerSingleton();
  }
}
