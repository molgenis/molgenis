package org.molgenis.data.annotation.impl.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;

public class Report
{

	public Report(boolean dummyValues)
	{
		super();
		if (dummyValues)
		{

			// patient
			this.patientName = "Doe, John";
			this.patientDOB = "12/34/5678";
			this.patientSex = "Male";
			this.patientRace = "Caucasian";
			this.patientIndication = "5GPM";
			this.patientTest = "ClinicalWES";
			this.patientMRN = "123456789";
			this.patientSpecimenType = "Blood, peripheral";
			this.patientSpecimenRecieved = "12/34/5678";
			this.patientAccessionID = "12345";
			this.patientFamilyNr = "34567";
			this.patientReferringPhysician = "Doe, Jane";
			this.patientReferringFacility = "NICU";

			// sequencing
			this.coveredPositions = 95.6;
			this.coverageDepth = 20;
			this.totalVariants = 125000;

			// variants
			this.monogenicDiseaseRiskVariants = new HashMap<String, ArrayList<Entity>>();
			this.monogenicDiseaseRiskGeneRanking = new LinkedHashMap<String, Integer>();
			this.carrierRiskVariants = new HashMap<String, Entity>();
			this.riskAlleles = new HashMap<String, Entity>();
		}

	}

	/**
	 * Patient info
	 */
	private String patientName;
	private String patientDOB;
	private String patientSex;
	private String patientRace;
	private String patientIndication;
	private String patientTest;
	private String patientMRN;
	private String patientSpecimenType;
	private String patientSpecimenRecieved;
	private String patientAccessionID;
	private String patientFamilyNr;
	private String patientReferringPhysician;
	private String patientReferringFacility;

	/**
	 * Sequencing info
	 */
	private Double coveredPositions;
	private Integer coverageDepth;
	private Integer totalVariants;

	/**
	 * Variant info
	 *
	 */
	private Map<String, ArrayList<Entity>> monogenicDiseaseRiskVariants;
	private LinkedHashMap<String, Integer> monogenicDiseaseRiskGeneRanking;
	private Map<String, Entity> carrierRiskVariants;
	private Map<String, Entity> riskAlleles;

	/**
	 * 
	 * Getters and setters! woohoo..
	 * 
	 */

	public String getPatientName()
	{
		return patientName;
	}

	public void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	public String getPatientDOB()
	{
		return patientDOB;
	}

	public void setPatientDOB(String patientDOB)
	{
		this.patientDOB = patientDOB;
	}

	public String getPatientSex()
	{
		return patientSex;
	}

	public void setPatientSex(String patientSex)
	{
		this.patientSex = patientSex;
	}

	public String getPatientRace()
	{
		return patientRace;
	}

	public void setPatientRace(String patientRace)
	{
		this.patientRace = patientRace;
	}

	public String getPatientIndication()
	{
		return patientIndication;
	}

	public void setPatientIndication(String patientIndication)
	{
		this.patientIndication = patientIndication;
	}

	public String getPatientTest()
	{
		return patientTest;
	}

	public void setPatientTest(String patientTest)
	{
		this.patientTest = patientTest;
	}

	public String getPatientMRN()
	{
		return patientMRN;
	}

	public void setPatientMRN(String patientMRN)
	{
		this.patientMRN = patientMRN;
	}

	public String getPatientSpecimenType()
	{
		return patientSpecimenType;
	}

	public void setPatientSpecimenType(String patientSpecimenType)
	{
		this.patientSpecimenType = patientSpecimenType;
	}

	public String getPatientSpecimenRecieved()
	{
		return patientSpecimenRecieved;
	}

	public void setPatientSpecimenRecieved(String patientSpecimenRecieved)
	{
		this.patientSpecimenRecieved = patientSpecimenRecieved;
	}

	public String getPatientAccessionID()
	{
		return patientAccessionID;
	}

	public void setPatientAccessionID(String patientAccessionID)
	{
		this.patientAccessionID = patientAccessionID;
	}

	public String getPatientFamilyNr()
	{
		return patientFamilyNr;
	}

	public void setPatientFamilyNr(String patientFamilyNr)
	{
		this.patientFamilyNr = patientFamilyNr;
	}

	public String getPatientReferringPhysician()
	{
		return patientReferringPhysician;
	}

	public void setPatientReferringPhysician(String patientReferringPhysician)
	{
		this.patientReferringPhysician = patientReferringPhysician;
	}

	public String getPatientReferringFacility()
	{
		return patientReferringFacility;
	}

	public void setPatientReferringFacility(String patientReferringFacility)
	{
		this.patientReferringFacility = patientReferringFacility;
	}

	public Double getCoveredPositions()
	{
		return coveredPositions;
	}

	public void setCoveredPositions(Double coveredPositions)
	{
		this.coveredPositions = coveredPositions;
	}

	public Integer getCoverageDepth()
	{
		return coverageDepth;
	}

	public void setCoverageDepth(Integer coverageDepth)
	{
		this.coverageDepth = coverageDepth;
	}

	public Integer getTotalVariants()
	{
		return totalVariants;
	}

	public void setTotalVariants(Integer totalVariants)
	{
		this.totalVariants = totalVariants;
	}

	public Map<String, ArrayList<Entity>> getMonogenicDiseaseRiskVariants()
	{
		return monogenicDiseaseRiskVariants;
	}

	public void setMonogenicDiseaseRiskVariants(Map<String, ArrayList<Entity>> monogenicDiseaseRiskVariants)
	{
		this.monogenicDiseaseRiskVariants = monogenicDiseaseRiskVariants;
	}

	public LinkedHashMap<String, Integer> getMonogenicDiseaseRiskGeneRanking()
	{
		return monogenicDiseaseRiskGeneRanking;
	}

	public void setMonogenicDiseaseRiskGeneRanking(LinkedHashMap<String, Integer> monogenicDiseaseRiskGeneRanking)
	{
		this.monogenicDiseaseRiskGeneRanking = monogenicDiseaseRiskGeneRanking;
	}

	public Map<String, Entity> getCarrierRiskVariants()
	{
		return carrierRiskVariants;
	}

	public void setCarrierRiskVariants(Map<String, Entity> carrierRiskVariants)
	{
		this.carrierRiskVariants = carrierRiskVariants;
	}

	public Map<String, Entity> getRiskAlleles()
	{
		return riskAlleles;
	}

	public void setRiskAlleles(Map<String, Entity> riskAlleles)
	{
		this.riskAlleles = riskAlleles;
	}

}
