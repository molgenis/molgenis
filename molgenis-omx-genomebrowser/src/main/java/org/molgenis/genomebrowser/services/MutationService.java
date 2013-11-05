package org.molgenis.genomebrowser.services;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MutationService
{
	private Database database;

	@Autowired
	public MutationService(Database database)
	{
		this.database = database;
	}

	public List<Map<String,String>> getPatientMutationData(String segmentId, String mutationId) throws ParseException,
			DatabaseException, IOException
	{
		List<Patient> patientQueryResult = null;
		List<Map<String,String>> variantArray = new ArrayList<Map<String,String>>();

		patientQueryResult = queryPatients(segmentId, mutationId);

		for (Patient patient : patientQueryResult)
		{
			createPatientFields(variantArray, patient);
		}
		
		return variantArray;

	}

	private List<Patient> queryPatients(String segmentId, String mutationId) throws DatabaseException
	{
		List<Patient> patientQueryResult;
		if (mutationId != null && !"".equals(mutationId))
		{
			patientQueryResult = queryPatientsByMutation(segmentId, mutationId);
		}
		else
		{
			patientQueryResult = database.query(Patient.class).find();
		}
		return patientQueryResult;
	}

	private void createPatientFields(List<Map<String,String>> variantArray, Patient patient)
	{
		Map<String,String> valueMap = new HashMap<String,String>();
		for (String field : patient.getFields())
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

	private void createVarientFields(Patient patient, Map<String,String> valueMap)
	{
		createVarientFieldsForAllele(patient, valueMap, "1");
		createVarientFieldsForAllele(patient, valueMap, "2");
	}

	private void createVarientFieldsForAllele(Patient patient, Map<String,String> valueMap, String alleleId)
	{
		Variant variant = null;
		if (patient.get("Allele" + alleleId + "_id") == null)
		{
			valueMap.put("CdnaNotation" + alleleId, "");
			valueMap.put("Consequence" + alleleId, "");
			valueMap.put("Exon" + alleleId, "");
		}
		else
		{
			if ("1".equals(alleleId))
			{
				variant = (Variant) patient.getAllele1();
			}
			else if ("2".equals(alleleId))
			{
				variant = (Variant) patient.getAllele2();
			}
			for (String allelefield : variant.getFields())
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

	private List<Patient> queryPatientsByMutation(String segmentId, String mutationId) throws DatabaseException
	{
		Query<Patient> variantQuery = database.query(Patient.class);
		variantQuery.equals(Patient.ALLELE1_IDENTIFIER, mutationId)
		.or()
		.equals(Patient.ALLELE2_IDENTIFIER, mutationId);
		List<Patient> patients = variantQuery.find();
		return patients;
	}
}
