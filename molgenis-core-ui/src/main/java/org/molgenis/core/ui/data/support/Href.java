package org.molgenis.core.ui.data.support;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.springframework.web.util.UriUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

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
	 */
	public static String concatAttributeHref(String baseUri, String qualifiedEntityName, Object entityIdValue,
			String attributeName)
	{
		return String.format(baseUri + "/%s/%s/%s", encodePathSegment(qualifiedEntityName),
				encodePathSegment(DataConverter.toString(entityIdValue)), encodePathSegment(attributeName));
	}

	/**
	 * Create an encoded href for an attribute meta
	 */
	public static String concatMetaAttributeHref(String baseUri, String entityParentName, String attributeName)
	{
		return String.format(baseUri + "/%s/meta/%s", encodePathSegment(entityParentName),
				encodePathSegment(attributeName));
	}

	public static String concatEntityHref(Entity entity)
	{
		return concatEntityHref("/api/v2", entity.getEntityType().getId(), entity.getIdValue());
	}

	/**
	 * Create an encoded href for an entity
	 */
	public static String concatEntityHref(String baseUri, String qualifiedEntityName, Object entityIdValue)
	{
		if (null == qualifiedEntityName)
		{
			qualifiedEntityName = "";
		}

		return String.format(baseUri + "/%s/%s", encodePathSegment(qualifiedEntityName),
				encodePathSegment(DataConverter.toString(entityIdValue)));
	}

	/**
	 * Create an encoded href for an entity meta
	 */
	public static String concatMetaEntityHref(String baseUri, String qualifiedEntityName)
	{
		return String.format(baseUri + "/%s/meta", encodePathSegment(qualifiedEntityName));
	}

	public static String concatMetaEntityHrefV2(String baseUri, String qualifiedEntityName)
	{
		return String.format(baseUri + "/%s", encodePathSegment(qualifiedEntityName));
	}

	/**
	 * Create an encoded href for an entity collection
	 */
	public static String concatEntityCollectionHref(String baseUri, String qualifiedEntityName,
			String qualifiedIdAttributeName, List<String> entitiesIds)
	{
		String ids;
		ids = entitiesIds.stream().map(Href::encodeIdToRSQL).collect(Collectors.joining(","));
		return String.format(baseUri + "/%s?q=%s=in=(%s)", encodePathSegment(qualifiedEntityName),
				encodePathSegment(qualifiedIdAttributeName), ids);
	}

	private static String encodeIdToRSQL(String id)
	{
		return '"' + encodePathSegment(id) + '"';
	}

	private static String encodePathSegment(String pathSegment)
	{
		return UriUtils.encodePathSegment(pathSegment, UTF_8.name());
	}
}
