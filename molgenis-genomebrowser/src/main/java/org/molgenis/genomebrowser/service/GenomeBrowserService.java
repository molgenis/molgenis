package org.molgenis.genomebrowser.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettingsMetadata;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettingsMetadata.GENOMEBROWSERSETTINGS;

/**
 * Service implements genomeBrowser specific business logic.
 */
@Component
public class GenomeBrowserService
{
	private final DataService dataService;

	public GenomeBrowserService(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public Map<String, GenomeBrowserTrack> getGenomeBrowserTracks(EntityType entityType)
	{
		return getGenomeBrowserTracks(entityType, getDefaultGenomeBrowserAttributes().collect(Collectors.toList()));
	}

	public Map<String, GenomeBrowserTrack> getGenomeBrowserTracks(EntityType entityType,
			List<GenomeBrowserAttributes> defaultGenomeBrowserAttributes)
	{
		Map<String, GenomeBrowserTrack> settings = new HashMap<>();
		dataService.findAll(GENOMEBROWSERSETTINGS,
				new QueryImpl<GenomeBrowserSettings>().eq(GenomeBrowserSettingsMetadata.ENTITY,
						entityType.getIdValue()), GenomeBrowserSettings.class)
				   .forEach(referenceSettings -> settings.put(referenceSettings.getIdentifier(),
						   GenomeBrowserTrack.create(referenceSettings)));

		if (settings.isEmpty())
		{
			//if not check if attrs match any default config
			Collections.sort(defaultGenomeBrowserAttributes);
			for (GenomeBrowserAttributes genomeBrowserAttributes : defaultGenomeBrowserAttributes)
			{
				List<String> attributeNames = Lists.newArrayList(entityType.getAttributeNames());
				if (areAllAttributeAvailable(genomeBrowserAttributes, attributeNames))
				{
					GenomeBrowserTrack genomeBrowserTrack = getDefaultGenomeBrowserSettingsEntity(entityType,
							genomeBrowserAttributes);
					settings.put(genomeBrowserTrack.getId(), genomeBrowserTrack);
					break;
				}
			}
		}
		return settings;
	}

	public Map<String, GenomeBrowserTrack> getReferenceTracks(GenomeBrowserTrack settings)
	{
		Map<String, GenomeBrowserTrack> result = new HashMap<>();
		if (settings.getMolgenisReferenceMode() != GenomeBrowserSettings.MolgenisReferenceMode.NONE)
		{
			if (settings.getMolgenisReferenceMode() == GenomeBrowserSettings.MolgenisReferenceMode.CONFIGURED)
			{
				settings.getMolgenisReferenceTracks()
						.forEach(referenceTrack -> result.put(referenceTrack.getId(), referenceTrack));
			}
			else
			{//Mode == ALL
				//TODO Improve performance by rewriting to query that returns all genomic entities instead of retrieving all entities and determining which one is genomic
				List<GenomeBrowserAttributes> defaultGenomeBrowserAttributes = getDefaultGenomeBrowserAttributes().collect(
						Collectors.toList());
				for (EntityType entityType : dataService.getMeta().getEntityTypes().collect(Collectors.toList()))
				{
					if (!entityType.isAbstract() && !entityType.equals(settings.getEntity()))
					{
						getGenomeBrowserTracks(entityType, defaultGenomeBrowserAttributes).values()
																						  .forEach(
																								  referenceSettings -> result
																										  .put(referenceSettings
																														  .getId(),
																												  referenceSettings));
					}
				}
			}
		}
		return result;
	}

	private Stream<GenomeBrowserAttributes> getDefaultGenomeBrowserAttributes()
	{
		return dataService.findAll(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				new QueryImpl<GenomeBrowserAttributes>().eq(GenomeBrowserAttributesMetadata.DEFAULT, true),
				GenomeBrowserAttributes.class);
	}

	private boolean isAttributeAvailable(String attributeName, Iterable<String> attributeNames)
	{
		return (attributeName == null || Iterables.contains(attributeNames, attributeName));
	}

	private boolean areAllAttributeAvailable(GenomeBrowserAttributes genomeBrowserAttributes,
			Iterable<String> attributeNames)
	{
		return isAttributeAvailable(genomeBrowserAttributes.getChrom(), attributeNames) && isAttributeAvailable(
				genomeBrowserAttributes.getPos(), attributeNames) && isAttributeAvailable(
				genomeBrowserAttributes.getAlt(), attributeNames) && isAttributeAvailable(
				genomeBrowserAttributes.getRef(), attributeNames) && isAttributeAvailable(
				genomeBrowserAttributes.getStop(), attributeNames);
	}

	private GenomeBrowserTrack getDefaultGenomeBrowserSettingsEntity(EntityType entityType,
			GenomeBrowserAttributes attrs)
	{
		return GenomeBrowserTrack.create(entityType.getIdValue().toString(), entityType.getLabelAttribute().getName(),
				entityType, GenomeBrowserSettings.TrackType.VARIANT, Collections.emptyList(),
				GenomeBrowserSettings.MolgenisReferenceMode.ALL, attrs, null, null, null, null);
	}
}
