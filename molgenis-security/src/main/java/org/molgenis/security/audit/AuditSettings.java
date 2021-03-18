package org.molgenis.security.audit;

public interface AuditSettings {

  /** @return <code>true</code> if system entity types are audited */
  boolean getSystemAuditEnabled();

  /** @return DataAuditSetting telling which non-system entity types are audited */
  DataAuditSetting getDataAuditSetting();
}
