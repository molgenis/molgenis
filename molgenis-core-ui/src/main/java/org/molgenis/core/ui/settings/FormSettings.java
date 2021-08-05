package org.molgenis.core.ui.settings;

import static org.molgenis.data.meta.AttributeType.BOOL;

import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class FormSettings extends DefaultSettingsEntity {
  public static final String FORM_SETTINGS = "sys_set_forms";
  public static final String ADD_CATEGORICAL_NULL_OPTION = "addCategoricalNullOption";
  public static final String ADD_ENUM_NULL_OPTION = "addEnumNullOption";
  public static final String ADD_BOOLEAN_NULL_OPTION = "addBooleanNullOption";

  static final String ID = "forms";

  public FormSettings() {
    super(ID);
  }

  public boolean isAddCategoricalNullOption() {
    return getBoolean(ADD_CATEGORICAL_NULL_OPTION);
  }

  public boolean isAddEnumNullOption() {
    return getBoolean(ADD_ENUM_NULL_OPTION);
  }

  public boolean isAddBooleanNullOption() {
    return getBoolean(ADD_BOOLEAN_NULL_OPTION);
  }

  @Component
  public static class Meta extends DefaultSettingsEntityType {

    public Meta() {
      super(ID);
    }

    @Override
    public void init() {
      super.init();
      setLabel("Form settings");

      addAttribute(ADD_ENUM_NULL_OPTION)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(true))
          .setLabel("Add null option to nullable enums")
          .setDescription(
              "Add an extra option for the radio button list of nullable enum attributes, to allow deselection.");
      addAttribute(ADD_BOOLEAN_NULL_OPTION)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(true))
          .setLabel("Add null option to nullable booleans")
          .setDescription(
              "Add an extra option for the radio button list of nullable boolean attributes, to allow deselection.");
      addAttribute(ADD_CATEGORICAL_NULL_OPTION)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(true))
          .setLabel("Add null option to nullable categoricals")
          .setDescription(
              "Add an an extra option for the radio button list of nullable categorical attributes, to allow deselection.");
    }
  }
}
