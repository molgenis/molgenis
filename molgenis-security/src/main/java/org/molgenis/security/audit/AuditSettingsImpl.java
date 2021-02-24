package org.molgenis.security.audit;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;

import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class AuditSettingsImpl extends DefaultSettingsEntity implements AuditSettings {
  private static final String ID = "aud";
  public static final String AUDIT_SETTINGS = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + ID;

  public AuditSettingsImpl() {
    super(ID);
  }

  @Component
  public static class Meta extends DefaultSettingsEntityType {

    private static final String AUDIT_SYSTEM = "audit_system";
    private static final String AUDIT_DATA = "audit_data";

    public Meta() {
      super(ID);
    }

    @Override
    public void init() {
      super.init();
      setLabel("Audit settings");
      setDescription("Settings for auditing.");

      addAttribute(AUDIT_SYSTEM)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(false))
          .setLabel("Audit system entity types")
          .setDescription("If enabled, users' changes to system entity types will be logged");
      addAttribute(AUDIT_DATA)
          .setDataType(ENUM)
          .setNillable(false)
          .setEnumOptions(
              asList(
                  DataAuditSetting.NONE.getLabel(),
                  DataAuditSetting.TAGGED.getLabel(),
                  DataAuditSetting.ALL.getLabel()))
          .setDefaultValue(DataAuditSetting.NONE.getLabel())
          .setLabel("Audit non-system entity types")
          .setDescription(
              "If enabled, users' interactions with non-system entity types will be "
                  + "logged. If 'Tagged' is chosen, only entity types tagged with 'audit-audited' "
                  + "will be audited.");
    }
  }

  @Override
  public boolean getSystemAuditEnabled() {
    Boolean value = getBoolean(Meta.AUDIT_SYSTEM);
    return TRUE.equals(value);
  }

  @Override
  public void setSystemAuditEnabled(boolean auditSystem) {
    set(Meta.AUDIT_SYSTEM, auditSystem);
  }

  @Override
  public DataAuditSetting getDataAuditSetting() {
    return DataAuditSetting.fromLabel(getString(Meta.AUDIT_DATA));
  }

  @Override
  public void setDataAuditSetting(DataAuditSetting setting) {
    set(Meta.AUDIT_DATA, setting.getLabel());
  }
}
