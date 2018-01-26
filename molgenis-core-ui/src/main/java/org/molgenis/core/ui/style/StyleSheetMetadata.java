package org.molgenis.core.ui.style;

import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.settings.SettingsPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class StyleSheetMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "StyleSheet";
	public static final String STYLE_SHEET = SettingsPackage.PACKAGE_SETTINGS + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String BOOTSTRAP_3_THEME = "bootstrap3Theme";
	public static final String BOOTSTRAP_4_THEME = "bootstrap4Theme";
	private final SettingsPackage settingsPackage;

	private final FileMetaMetaData fileMetaMetaData;

	public StyleSheetMetadata(SettingsPackage settingsPackage, FileMetaMetaData fileMetaMetaData)
	{
		super(SIMPLE_NAME, SettingsPackage.PACKAGE_SETTINGS);
		this.settingsPackage = requireNonNull(settingsPackage);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("StyleSheet");
		setPackage(settingsPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);

		addAttribute(NAME, ROLE_LABEL).setNillable(false).setDataType(STRING);

		addAttribute(BOOTSTRAP_3_THEME).setNillable(false).setDataType(FILE).setRefEntity(fileMetaMetaData);

		addAttribute(BOOTSTRAP_4_THEME).setNillable(true).setDataType(FILE).setRefEntity(fileMetaMetaData);
	}
}
