import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.settings.SettingsEntityMeta;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DefaultSettingsEntityMetaData extends DefaultEntityMetaData
{
	private static final String ENTITY_NAME_PREFIX = "settings_";

	public static final String ID = "id";

	@Autowired
	public SettingsEntityMeta settingsEntityMeta;

	public DefaultSettingsEntityMetaData(String id)
	{
		super(getSettingsEntityName(id));
		setExtends(settingsEntityMeta);
		setPackage(SettingsEntityMeta.PACKAGE_SETTINGS);
		addAttribute(ID).setIdAttribute(true).setDataType(STRING).setNillable(false).setLabel("Id").setVisible(false);
	}

	public static String getSettingsEntityName(String id)
	{
		return ENTITY_NAME_PREFIX + id;
	}
}
