package org.molgenis.integrationtest.config;

import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.security.audit.AuditSettings;
import org.molgenis.security.audit.DataAuditSetting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AuditEventPublisher.class})
public class AuditTestConfig {

  @Bean
  public AuditSettings auditSettings() {
    return new AuditSettings() {
      @Override
      public boolean getSystemAuditEnabled() {
        return true;
      }

      @Override
      public DataAuditSetting getDataAuditSetting() {
        return DataAuditSetting.ALL;
      }
    };
  }
}
