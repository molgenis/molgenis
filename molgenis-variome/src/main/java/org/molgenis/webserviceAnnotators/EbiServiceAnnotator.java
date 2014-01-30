package org.molgenis.webserviceAnnotators;

import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryAnnotator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import uk.ac.ebi.chemblws.domain.*;
import uk.ac.ebi.chemblws.exception.*;
import uk.ac.ebi.chemblws.restclient.ChemblRestClient;

@Component
public class EbiServiceAnnotator implements RepositoryAnnotator {

	@Override
	public Repository annotate(Repository source) {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(	"applicationContext.xml" );
		ChemblRestClient chemblClient = applicationContext.getBean( "chemblRestClient", ChemblRestClient.class );
		
		/* ========================= API Health Status Querying Example ========================= */
		try{
			chemblClient.checkStatus();
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown, service is down");
			//Do something
		}
		
		/* ========================= API Target Querying Examples ========================= */
		
		/* Fetch a ChEMBL target. If target is not found an HTTP 404 status is returned from the web service server and translated into a TargetNotFoundException by the web service client */
		/* If an invalid target identifier is supplied then an HTTP 400 status (Bad Request) is returned from the web service server and translated into a InvalidTargetIdentifierException by the web service client */
		
		try{
			Target target = chemblClient.getTarget("CHEMBL1");
		}catch(TargetNotFoundException e){
			System.out.println( "TargetNotFoundException thrown");
			//Do something
		}catch(InvalidTargetIdentifierException e){
			System.out.println( "InvalidTargetIdentifierException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		/* Try to fetch an existing ChEMBL target. If target found an HTTP 200 status is returned and program flow proceeds as normal */
	
		try{
			Target t1 = chemblClient.getTarget("CHEMBL240");
			System.out.println( "Target: " + t1.getDescription());
		}catch(TargetNotFoundException e){
			System.out.println( "TargetNotFoundException thrown");
			//Do something
		}catch(InvalidTargetIdentifierException e){
			System.out.println( "InvalidTargetIdentifierException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		

		/* Fetch a ChEMBL target by UniProt accession identifier, catch TargetNotFoundException (corresponds to a HTTP 404 Not Found status) and InvalidUniProtAccessionException (corresponds to HTTP 400 Bad Request Status) */
		
		try{
			Target t1 = chemblClient.getTargetByUniProtAccession("Q13936");
		}catch(TargetNotFoundException e){
			System.out.println( "TargetNotFoundException thrown");
			//Do something
		}catch(InvalidUniProtAccessionException e){
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Fetch an individual ChEMBL target by RefSeq accession identifier*/
		/*
		target = chemblClient.getTargetByRefSeqAccession("NP_001128722");
		System.out.println( "Target Preferred Name: " + target.getPreferredName());
		*/
		
		/* Fetch all ChEMBL targets */
		
		/*
		List<Target> targets = chemblClient.getTargets();
		for(Target target : targets )
		{
			System.out.println( "Target ChEMBLID: " + target.getChemblId());
			System.out.println( "Target Organism: " + target.getOrganism());
		}
		*/
		
		/* Fetch the bioactivities for an individual ChEMBL target */
		/*
		List<Bioactivity> target_bioactivities = chemblClient.getTargetBioactivities("CHEMBL240");
		
		
		for(Bioactivity bioactivity : target_bioactivities )
		{
			System.out.println( "Bioactivity Assay Description: " + bioactivity.getAssay_description());
		}
		*/
		
		
		
		
		/* ========================= API Compound Querying Examples ========================= */
		
		/* Fetch an individiual ChEMBL compound. If compound is not found an HTTP 404 status is returned from the web service server and translated into a CompoundNotFoundException by the web service client */
		
		try{
			Compound compound = chemblClient.getCompound("CHEMBL1");
			System.out.println( "Compound ChEMBLID: " + compound.getChemblId());
		}catch(CompoundNotFoundException e){
			System.out.println( "CompoundNotFoundException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Fetch an individual ChEMBL compound using stdInchiKey.  Catch CompoundNotFoundException (corresponds to a HTTP 404 Not Found status) and InvalidInChiKeyException (corresponds to HTTP 400 Bad Request Status) */
		
		try{
			Compound compound = chemblClient.getCompoundByStdInChiKey("GHBOEFUAGSHXPO-XZOTUCIWSA-N");
			System.out.println( "Compound ChEMBLID: " + compound.getChemblId());
			
			/* Properties are not calculated for all compounds, therefore we must check if the property has been set before attempting to use it*/
			
			if(compound.getAlogp()!=null){
			System.out.println( "Compound Alogp: " + compound.getAlogp());
			}
			
			if(compound.getNumRo5Violations()!=null){
				System.out.println( "Compound Ro5 violations: " + compound.getNumRo5Violations());
			}
			
		}catch(CompoundNotFoundException e){
			System.out.println( "CompoundNotFoundException thrown");
			//Do something
		}catch(InvalidInChiKeyException e){
			System.out.println( "InvalidInChiKeyException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Fetch a list of ChEMBL compounds corresponding to a given SMILES string.  Catch InvalidSmilesException (corresponds to HTTP 400 Bad Request Status) */
		
		try{
			 List<Compound> matchingCompounds = chemblClient.getCompoundBySmiles("COc1ccc2[C@@H]3[C@H](COc2c1)C(C)(C)OC4=C3C(=O)C(=O)C5=C4OC(C)(C)[C@@H]6COc7cc(OC)ccc7[C@H]56");
			 System.out.println( "Matching compounds list size: " + matchingCompounds.size());
		}catch(InvalidSmilesException e){
			System.out.println( "InvalidSmilesException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Issue HTTP POST request to Fetch a list of ChEMBL compounds corresponding to a given SMILES string.  Catch InvalidSmilesException (corresponds to HTTP 400 Bad Request Status) */
		try{
			 List<Compound> matchingCompounds = chemblClient.postForCompoundBySmiles("COc1ccc2[C@@H]3[C@H](COc2c1)C(C)(C)OC4=C3C(=O)C(=O)C5=C4OC(C)(C)[C@@H]6COc7cc(OC)ccc7[C@H]56");
			 System.out.println( "Matching compounds list size: " + matchingCompounds.size());
		}catch(InvalidSmilesException e){
			System.out.println( "InvalidSmilesException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		

		/* Fetch list of ChEMBL compounds containing a particular substructure. Catch InvalidSmilesException */
		
		try{
			
			List<Compound> substructureList = chemblClient.getCompoundBySubstructureSmiles("COc1ccc2[C@@H]3[C@H](COc2c1)C(C)(C)OC4=C3C(=O)C(=O)C5=C4OC(C)(C)[C@@H]6COc7cc(OC)ccc7[C@H]56");
			
			System.out.println( "Matching substructure list size: " + substructureList.size());
		}catch(InvalidSmilesException e){
			System.out.println( "InvalidSmilesException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Issue HTTP POST request to Fetch a list of ChEMBL compounds compounds containing a particular substructure. Catch InvalidSmilesException */
		try{

			List<Compound> substructureList = chemblClient.postForCompoundSubstructureBySmiles("N#CCc2ccc1ccccc1c2");

				System.out.println( "HTTP POST -- Matching substructure list size: " + substructureList.size());
		}catch(InvalidSmilesException e){
				System.out.println( "InvalidSmilesException thrown");
				//Do something
		}catch(ChemblServiceException e){
				System.out.println( "ChemblServiceException thrown");
				//Do something
		}
			
		/* Fetch list of ChEMBL compounds similar to the one represented by the given SMILES string and above a given similarity cutoff score (75%). Minimum value for cutoff score is 70%. Catch InvalidSmilesException, InvalidSimilarityScoreException */
		
		try{
			List<Compound> similarCompounds = chemblClient.getSimilarCompoundBySmiles("COc1ccc2[C@@H]3[C@H](COc2c1)C(C)(C)OC4=C3C(=O)C(=O)C5=C4OC(C)(C)[C@@H]6COc7cc(OC)ccc7[C@H]56", 75);
			
			for(Compound comp : similarCompounds )
			{
				System.out.println( "Compound Similarity Score: " + comp.getSimilarity());
			}
		}catch(InvalidSmilesException e){
			System.out.println( "InvalidSmilesException thrown");
			//Do something
		}catch(InvalidSimilarityScoreException e){
			System.out.println( "InvalidSimilarityScoreException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		/* Issue HTTP POST request to Fetch a list of ChEMBL compounds similar to the one represented by the given SMILES string and above a given similarity cutoff score (75%). Minimum value for cutoff score is 70%. Catch InvalidSmilesException, InvalidSimilarityScoreException */
		try{
			List<Compound> similarCompounds = chemblClient.postForSimilarCompoundBySmiles("O=C(C=CC#Cc2cccc(NS(=O)(=O)c1ccc(N(=O)=O)cc1)c2)NO",70);  
			
			for(Compound comp : similarCompounds )
			{
				System.out.println( "HTTP POST -- Compound Similarity Score: " + comp.getSimilarity());
			}
		}catch(InvalidSmilesException e){
			System.out.println( "InvalidSmilesException thrown");
			//Do something
		}catch(InvalidSimilarityScoreException e){
			System.out.println( "InvalidSimilarityScoreException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		/* Fetch the image of a given ChEMBL compound. Catch CompoundNotFoundException */
		
		try{
			byte[] cmpdImage = chemblClient.getCompoundImage("CHEMBL1");
			
			//Do something with the image data
		}catch(CompoundNotFoundException e){
			System.out.println( "CompoundNotFoundException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
		
		/* Fetch the image of a given ChEMBL compound, providing the dimensions (minimum value 1, maximum value 500), convert the raw byte data to a Java image object */
		
		try{
			byte[] cmpdImage = chemblClient.getCompoundImage("CHEMBL1", 350);
			BufferedImage chembl1image = ImageIO.read(new ByteArrayInputStream(cmpdImage));
			
			//Do something with the image data
		}catch(CompoundNotFoundException e){
			System.out.println( "CompoundNotFoundException thrown");
			//Do something
		}catch(InvalidCompoundImageRequestException e){
			System.out.println( "InvalidCompoundImageRequestException thrown");
			//Do something
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		
	

		/* Fetch the bioactivities for an individual ChEMBL compound */
		
		/*
		System.out.println("***** Fetching compound bioactivities *****");
		
		List cmpd_bioactivities = chemblClient.getCompoundBioactivities("CHEMBL1");
		System.out.println( "cmpd bioactivity list size: " + cmpd_bioactivities.size());
		
		
		for(Bioactivity bioactivity : cmpd_bioactivities )
		{
			System.out.println( "Bioactivity Assay Description: " + bioactivity.getAssay_description());
		}
	
		
		System.out.println("***** End fetching compound bioactivities *****");
		
		*/
		
		
		/* ========================= API Assay Querying Examples ========================= */
		
		/* Fetch an individiual ChEMBL assay. If assay is not found an HTTP 404 status is returned from the web service server and translated into a AssayNotFoundException by the web service client */
		
		try{
			Assay assay = chemblClient.getAssay("CHEMBL1000635");
			System.out.println( "Assay ChEMBLID: " + assay.getChemblId());
		}catch(AssayNotFoundException e){
			System.out.println( "AssayNotFoundException thrown");
			//Do something
		}catch(ChemblServiceException e){
			System.out.println( "ChemblServiceException thrown");
			//Do something
		}
		return null;
			
		
		/* Fetch the bioactivities for an individual ChEMBL assay */
		/*
		List assay_bioactivities = chemblClient.getAssayBioactivities("CHEMBL1014194");
		
	
		for(Bioactivity bioactivity : assay_bioactivities )
		{
			System.out.println( "Bioactivity Assay Description: " + bioactivity.getAssay_description());
		}
		*/
		 
		}
	}

