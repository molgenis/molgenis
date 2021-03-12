package org.molgenis.settings.entity;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.settings.entity.TableSettingsPackage.PACKAGE_ENTITY_SETTINGS;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class DataExplorerEntitySettings extends SystemEntityType {

  private static final String SIMPLE_NAME = "DataExplorerEntitySettings";
  private static final String TABLE = "table";
  private static final String CARD_TEMPLATE = "card_template";
  private static final String DETAIL_TEMPLATE = "detail_template";
  private static final String SHOP = "shop";
  private static final String TEMPLATE_ATTRS = "template_attrs";
  private static final String COLLAPSE_LIMIT = "collapse_limit";
  private static final String DEFAULT_FILTERS = "default_filters";
  private final TableSettingsPackage tableSettingsPackage;
  public static final String DATA_EXPLORER_ENTITY_SETTINGS =
      PACKAGE_ENTITY_SETTINGS + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  private final EntityTypeMetadata entityTypeMetaData;

  public DataExplorerEntitySettings(
      TableSettingsPackage tableSettingsPackage, EntityTypeMetadata entityTypeMetaData) {
    super(SIMPLE_NAME, PACKAGE_ENTITY_SETTINGS);
    this.tableSettingsPackage = requireNonNull(tableSettingsPackage);
    this.entityTypeMetaData = requireNonNull(entityTypeMetaData);
  }

  @Override
  protected void init() {
    setLabel("Data Explorer Settings");
    setPackage(tableSettingsPackage);
    setDescription("Settings entities for per entity config");
    addAttribute(ID, ROLE_ID)
        .setDataType(STRING)
        .setNillable(false)
        .setAuto(true)
        .setUnique(true)
        .setReadOnly(true)
        .setLabel("id");
    addAttribute(TABLE)
        .setDataType(XREF)
        .setRefEntity(entityTypeMetaData)
        .setNillable(false)
        .setAuto(false)
        .setUnique(true)
        .setReadOnly(false)
        .setLabel("Table")
        .setDescription("The table to apply the settings to");
    addAttribute(CARD_TEMPLATE)
        .setDataType(SCRIPT)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Card template")
        .setDescription(
            "Specify a Vue template that should be rendered in the card layout of data explorer v2");
    addAttribute(DETAIL_TEMPLATE)
        .setDataType(SCRIPT)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Detail template")
        .setDescription(
            "Specify a Vue template that should be rendered as the entity detail layout of data explorer v2");
    addAttribute(SHOP)
        .setDataType(BOOL)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Is shop")
        .setDescription("Can users shop items from this table?");
    addAttribute(TEMPLATE_ATTRS)
        .setDataType(STRING)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Template attributes");
    addAttribute(COLLAPSE_LIMIT)
        .setDataType(STRING)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Collapse limit");
    addAttribute(DEFAULT_FILTERS)
        .setDataType(STRING)
        .setNillable(true)
        .setAuto(false)
        .setUnique(false)
        .setReadOnly(false)
        .setLabel("Default filters")
        .setDescription("Comma separated list of filter names that should be active by default ");
  }
}
