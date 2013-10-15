package org.molgenis.genomebrowser.services;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class MutationService
{
	private Database database;

	@Autowired
	public MutationService(Database database)
	{
		this.database = database;
	}

	public JsonObject getPatientMutationData(String segmentId, String mutationId) throws ParseException,
			DatabaseException, IOException
	{
		List<Patient> patientQueryResult = null;
		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		patientQueryResult = queryPatients(segmentId, mutationId);

		for (Patient patient : patientQueryResult)
		{
			createPatientFields(jsonArray, patient);
		}
		
		jsonObject.add("variants", jsonArray);
		return jsonObject;

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

	private void createPatientFields(JsonArray jsonArray, Patient patient)
	{
		JsonObject jsonValues = new JsonObject();
		for (String field : patient.getFields())
		{
			if (patient.get(field) != null)
			{
				jsonValues.addProperty(field, patient.get(field).toString());
			}
			else jsonValues.addProperty(field, "");
		}
		createVarientFields(patient, jsonValues);
		jsonArray.add(jsonValues);
	}

	private void createVarientFields(Patient patient, JsonObject jsonValues)
	{
		createVarientFieldsForAllele(patient, jsonValues, "1");
		createVarientFieldsForAllele(patient, jsonValues, "2");
	}

	private void createVarientFieldsForAllele(Patient patient, JsonObject jsonValues, String alleleId)
	{
		Variant variant = null;
		if (patient.get("Allele" + alleleId + "_id") == null)
		{
			jsonValues.addProperty("CdnaNotation" + alleleId, "");
			jsonValues.addProperty("Consequence" + alleleId, "");
			jsonValues.addProperty("Exon" + alleleId, "");
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
					jsonValues.addProperty(allelefield + alleleId, variant.get(allelefield).toString());
				}
				else
				{
					jsonValues.addProperty(allelefield, "");
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
