package org.molgenis.genomebrowser.services;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MutationService
{
	private final DataService dataService;

	@Autowired
	public MutationService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	public List<Map<String, String>> getPatientMutationData(String segmentId, String mutationId) throws ParseException,
			IOException
	{
		List<Patient> patientQueryResult = null;
		List<Map<String, String>> variantArray = new ArrayList<Map<String, String>>();

		patientQueryResult = queryPatients(segmentId, mutationId);

		for (Patient patient : patientQueryResult)
		{
			createPatientFields(variantArray, patient);
		}

		return variantArray;

	}

	private List<Patient> queryPatients(String segmentId, String mutationId)
	{
		List<Patient> patientQueryResult;
		if (mutationId != null && !mutationId.isEmpty())
		{
			patientQueryResult = queryPatientsByMutation(segmentId, mutationId);
		}
		else
		{
			patientQueryResult = dataService.findAllAsList(Patient.ENTITY_NAME, new QueryImpl());
		}

		return patientQueryResult;
	}

	private void createPatientFields(List<Map<String, String>> variantArray, Patient patient)
	{
		Map<String, String> valueMap = new HashMap<String, String>();
		for (String field : patient.getAttributeNames())
		{
			if (patient.get(field) != null)
			{
				valueMap.put(field, patient.get(field).toString());
			}
			else valueMap.put(field, "");
		}
		createVarientFields(patient, valueMap);
		variantArray.add(valueMap);
	}

	private void createVarientFields(Patient patient, Map<String, String> valueMap)
	{
		createVarientFieldsForAllele(patient, valueMap, "1");
		createVarientFieldsForAllele(patient, valueMap, "2");
	}

	private void createVarientFieldsForAllele(Patient patient, Map<String, String> valueMap, String alleleId)
	{
		Variant variant = null;
		if (patient.get("Allele" + alleleId) == null)
		{
			valueMap.put("CdnaNotation" + alleleId, "");
			valueMap.put("Consequence" + alleleId, "");
			valueMap.put("Exon" + alleleId, "");
		}
		else
		{
			if ("1".equals(alleleId))
			{
				variant = patient.getAllele1();
			}
			else if ("2".equals(alleleId))
			{
				variant = patient.getAllele2();
			}
			for (String allelefield : variant.getAttributeNames())
			{
				if (variant.get(allelefield) != null)
				{
					valueMap.put(allelefield + alleleId, variant.get(allelefield).toString());
				}
				else
				{
					valueMap.put(allelefield, "");
				}
			}
		}
	}

	private List<Patient> queryPatientsByMutation(String segmentId, String mutationId)
	{
		Variant allele = dataService.findOne(Variant.ENTITY_NAME, new QueryImpl().eq(Variant.IDENTIFIER, mutationId));
		if (allele == null)
		{
			return Collections.emptyList();
		}

		Query q = new QueryImpl().eq(Patient.ALLELE1, allele).or().eq(Patient.ALLELE2, allele);
		return dataService.findAllAsList(Patient.ENTITY_NAME, q);
	}
}
