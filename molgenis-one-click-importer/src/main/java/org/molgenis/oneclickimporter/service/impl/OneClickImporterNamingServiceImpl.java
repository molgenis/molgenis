package org.molgenis.oneclickimporter.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.oneclickimporter.service.OneClickImporterNamingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.util.FileExtensionUtils.getFileNameWithoutExtension;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;

@Component
public class OneClickImporterNamingServiceImpl implements OneClickImporterNamingService
{
	private DataService dataService;

	public OneClickImporterNamingServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public String getLabelWithPostFix(String label)
	{
		List<String> entityTypeLabels = dataService.findAll(ENTITY_TYPE_META_DATA,
				new QueryImpl<EntityType>().like(LABEL, label), EntityType.class)
												   .map(EntityType::getLabel)
												   .collect(Collectors.toList());

		if (entityTypeLabels.isEmpty() || !entityTypeLabels.contains(label))
		{
			return label;
		}
		else
		{
			boolean found = true;
			int index = 0;
			while (found)
			{
				index++;
				found = entityTypeLabels.contains(label + " (" + index + ")");
			}
			return label + " (" + index + ")";
		}
	}

	@Override
	public String createValidIdFromFileName(String filename)
	{
		String packageName = getFileNameWithoutExtension(filename);
		return packageName.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
	}

	@Override
	public String asValidColumnName(String column)
	{
		return column.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
	}
}
