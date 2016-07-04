package org.molgenis.app.promise.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.promise.mapper.RadboudMapper.*;
import static org.molgenis.app.promise.model.BbmriNlCheatSheet.REF_DISEASE_TYPES;

class RadboudDiseaseMap
{
	private static final Logger LOG = LoggerFactory.getLogger(RadboudDiseaseMap.class);

	static final String URN_MIRIAM_ICD_PREFIX = "urn:miriam:icd:";
	static final String XML_IDAABB = "IDAABB";
	static final String XML_CODENAME = "CODENAME";
	static final String XML_CODEVERSION = "CODEVERSION";
	static final String XML_CODEDESCEN = "CODEDESCEN";
	static final String XML_CODEINDEX = "CODEINDEX";

	private Map<String, List<Entity>> diseases = newHashMap();
	private DataService dataService;

	RadboudDiseaseMap(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	void addDisease(Entity radboudDiseaseEntity)
	{
		String diseaseId = radboudDiseaseEntity.getString(XML_IDAA);
		diseases.putIfAbsent(diseaseId, newArrayList());
		diseases.get(diseaseId).add(radboudDiseaseEntity);
	}

	Iterable<Entity> getDiseaseTypes(String biobankIdaa)
	{
		List<Entity> diseaseTypes = newArrayList();
		Iterable<Entity> diseaseEntities = diseases.get(biobankIdaa);

		if (diseaseEntities != null)
		{
			diseaseEntities.forEach(disease -> {
				String icd10urn = URN_MIRIAM_ICD_PREFIX + disease.getString(XML_CODEINDEX);
				Entity diseaseType = dataService.findOne(REF_DISEASE_TYPES, icd10urn);
				if (diseaseType != null)
				{
					diseaseTypes.add(diseaseType);
				}
				else
				{
					LOG.info("Disease type with id [" + icd10urn + "] not found");
				}
			});
		}

		if (diseaseTypes.isEmpty())
		{
			diseaseTypes.add(dataService.findOne(REF_DISEASE_TYPES, "NAV"));
		}
		return diseaseTypes;
	}
}


