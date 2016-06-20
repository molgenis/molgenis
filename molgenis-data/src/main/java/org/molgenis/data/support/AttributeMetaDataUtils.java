package org.molgenis.data.support;

public class AttributeMetaDataUtils
{
	private AttributeMetaDataUtils(){}

	public static String getI18nAttributeName(String attrName, String languageCode) {
		return attrName + '-' + languageCode;
	}
}
