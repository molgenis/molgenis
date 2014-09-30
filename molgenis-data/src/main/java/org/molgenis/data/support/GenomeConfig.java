package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.stereotype.Component;

import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

/**
 * Created by charbonb on 13/05/14.
 */
@Component
public class GenomeConfig
{
	public static final String GENOMEBROWSER_POS = "genomebrowser.data.start";
	public static final String GENOMEBROWSER_CHROM = "genomebrowser.data.chromosome";
    public static final String GENOMEBROWSER_REF = "genomebrowser.data.ref";
    public static final String GENOMEBROWSER_ALT = "genomebrowser.data.alt";
    public static final String GENOMEBROWSER_ID = "genomebrowser.data.id";
    public static final String GENOMEBROWSER_STOP = "genomebrowser.data.stop";
    public static final String GENOMEBROWSER_DESCRIPTION = "genomebrowser.data.desc";
    public static final String GENOMEBROWSER_LINK = "genomebrowser.data.linkout";
    public static final String GENOMEBROWSER_NAME = "genomebrowser.data.name";
    public static final String GENOMEBROWSER_PATIENT_ID = "genomebrowser.data.patient";

    private MolgenisSettings settings;

	public AttributeMetaData getAttributeMetadataForAttributeNameArray(String propertyName, EntityMetaData metadata)
	{
        if(settings == null){
            settings = getApplicationContext().getBean(MolgenisSettings.class);
        }
		AttributeMetaData attribute;
		String[] attributeNames = settings.getProperty(propertyName, "").split(",");
		for (String attributeName : attributeNames)
		{
			attribute = metadata.getAttribute(attributeName);
			if (attribute != null)
			{
				return attribute;
			}
		}
		return null;
	}

	public String getAttributeNameForAttributeNameArray(String propertyName, EntityMetaData metadata)
	{
		AttributeMetaData attribute = getAttributeMetadataForAttributeNameArray(propertyName, metadata);
		if (attribute != null)
		{
			return attribute.getName();
		}
		return "";
	}
}
