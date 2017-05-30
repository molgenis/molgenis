package org.molgenis.data.support;

import org.molgenis.data.*;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

public class Href
{
	private final String href;
	private final String hrefCollection;

	public Href(String href, String hrefCollection)
	{
		this.href = href;
		this.hrefCollection = hrefCollection;
	}

	public String getHref()
	{
		return href;
	}

	public String getHrefCollection()
	{
		return hrefCollection;
	}

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

	public static String concatEntityHref(Entity entity)
	{
		return concatEntityHref("/api/v2", entity.getEntityType().getId(), entity.getIdValue());
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

	public static String concatMetaEntityHrefV2(String baseUri, String qualifiedEntityName)
	{
		try
		{
			return String.format(baseUri + "/%s", UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnknownEntityException(qualifiedEntityName);
		}
	}

	/**
	 * Create an encoded href for an entity collection
	 *
	 * @param qualifiedEntityName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String concatEntityCollectionHref(String baseUri, String qualifiedEntityName,
			String qualifiedIdAttributeName, List<String> entitiesIds)
	{
		try
		{
			String ids;
			ids = entitiesIds.stream().map(Href::encodeIdToRSQL).collect(Collectors.joining(","));
			return String.format(baseUri + "/%s?q=%s=in=(%s)", UriUtils.encodePathSegment(qualifiedEntityName, "UTF-8"),
					UriUtils.encodePathSegment(qualifiedIdAttributeName, "UTF-8"), ids);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new MolgenisDataException(
					"The creation of the entity collection href has failed. Entity: " + qualifiedEntityName
							+ " Attribute: " + qualifiedIdAttributeName);
		}
	}

	private static String encodeIdToRSQL(String id)
	{
		try
		{
			return '"' + UriUtils.encodePathSegment(id, "UTF-8") + '"';
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}
}
