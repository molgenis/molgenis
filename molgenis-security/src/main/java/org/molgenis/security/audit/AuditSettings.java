package org.molgenis.security.audit;

public interface AuditSettings {

  /** @return whether system entity types should be audited */
  boolean getSystemAuditEnabled();

  /** @param signUp <code>true</code> if system entity types are audited */
  void setSystemAuditEnabled(boolean signUp);

  /** @return DataAuditSetting telling which non-system entity types are audited */
  DataAuditSetting getDataAuditSetting();

  /** @param setting DataAuditSetting telling which non-system entity types should be audited */
  void setDataAuditSetting(DataAuditSetting setting);
}
