package org.molgenis.lifelines.utils;

import org.molgenis.util.Entity;

public class OmxIdentifierGenerator
{
	public static String from(Class<? extends Entity> clazz, String codeSystem)
	{
		return clazz.getSimpleName() + '_' + codeSystem;
	}

	public static String from(Class<? extends Entity> clazz, String codeSystem, String code)
	{
		return clazz.getSimpleName() + '_' + codeSystem + '.' + code;
	}
}
