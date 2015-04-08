package org.molgenis.data.rest;

import java.io.UnsupportedEncodingException;

import org.molgenis.data.DataConverter;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.springframework.web.util.UriUtils;

public class Href
{
	/**
	 * Create an encoded href for an attribute
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @param attributeName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatAttributeHref(String baseUri, String qualifiedEntityName, Object entityIdValue,
			String attributeName)
	{
		try
		{
			return String.format(baseUri + "/%s/%s/%s", UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"),
					UriUtils.encodePathSegment(DataConverter.toString(entityIdValue), "UTF-8"),
					UriUtils.encodePathSegment(attributeName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	/**
	 * Create an encoded href for an attribute meta
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @param attributeName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatMetaAttributeHref(String baseUri, String entityParentName, String attributeName)
	{
		try
		{
			return String.format(baseUri + "/%s/meta/%s", UriUtils.encodePathSegment(entityParentName, "UTF-8"),
					UriUtils.encodePathSegment(attributeName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	/**
	 * Create an encoded href for an entity
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatEntityHref(String baseUri, String qualifiedEntityName, Object entityIdValue)
	{
		if (null == qualifiedEntityName)
		{
			qualifiedEntityName = "";
		}

		try
		{
			return String.format(baseUri + "/%s/%s", UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"),
					UriUtils.encodePathSegment(DataConverter.toString(entityIdValue), "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownEntityException(qualifiedEntityName);
		}
	}

	/**
	 * Create an encoded href for an entity meta
	 * 
	 * @param qualifiedEntityName
	 * @param entityIdValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatMetaEntityHref(String baseUri, String qualifiedEntityName)
	{
		try
		{
			return String.format(baseUri + "/%s/meta", UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownEntityException(qualifiedEntityName);
		}
	}
}
