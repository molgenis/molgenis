package org.molgenis.security.audit;

public interface AuditSettings {

  /** @return <code>true</code> if system entity types are audited */
  boolean getSystemAuditEnabled();

  /** @param enabled whether system entity types should be audited */
  void setSystemAuditEnabled(boolean enabled);

  /** @return DataAuditSetting telling which non-system entity types are audited */
  DataAuditSetting getDataAuditSetting();

  /** @param setting DataAuditSetting telling which non-system entity types should be audited */
  void setDataAuditSetting(DataAuditSetting setting);
}
