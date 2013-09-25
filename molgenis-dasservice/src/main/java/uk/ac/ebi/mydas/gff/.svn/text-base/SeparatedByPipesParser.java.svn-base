package uk.ac.ebi.mydas.examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasType;

public class SeparatedByPipesParser {
	/**
	 * Object used to process the file line by line.
	 */
	private Scanner scanner;
	/**
	 * List of the parsed segments.
	 */
	private ArrayList<DasAnnotatedSegment> segments;
	/**
	 * List of the types used in this data source
	 */
	private ArrayList<DasType> types;
	/**
	 * Types to be used in the data source:  gene, transcript and exon
	 */
	private DasType geneType,transcriptType,exonType;
	/**
	 * As this data source just have one method, it can be defined as a parameter to facilitate its use
	 */
	private DasMethod method;

	/**
	 * Instantiate the scanner with the stream, creates the empty lists for types and segments and creates the types and method to use through the source.
	 * @param gffdoc Stream with the content of the file to process 
	 * @throws DataSourceException In case of any error creating objects of the MyDas model
	 */
	public SeparatedByPipesParser(InputStream gffdoc) throws DataSourceException{
		scanner= new Scanner(gffdoc);
		segments= new ArrayList<DasAnnotatedSegment>();
		types= new ArrayList<DasType>();
		geneType= new DasType("Gene", null, "SO:0000704", "Gene");
		transcriptType= new DasType("Transcript", null, "SO:0000673", "Transcript");
		exonType= new DasType("Exon", null, "SO:0000147", "Exon");
		types.add(geneType);
		types.add(transcriptType);
		types.add(exonType);
		method = new DasMethod("not_recorded","not_recorded","ECO:0000037");
	}
	
	/**
	 * Go through the whole file line by line to process its content  
	 * @return a set of the segments with its features in the file
	 * @throws Exception in cases where the parsing has errors
	 */
	public Collection<DasAnnotatedSegment> parse() throws Exception{
		try {
			//first use a Scanner to get each line
			while ( scanner.hasNextLine() ){
				processLine( scanner.nextLine() );
			}
		} finally {
			//ensure the underlying stream is always closed
			scanner.close();
		}
		return this.segments;
	}

	/**
	 * Processes a line by splitting it by pipes. 
	 * The acquired data is used then, to call the methods to get/create the segment(chromosome) and features(gene, transcript or exon). 
	 * @param aLine
	 * @throws Exception
	 */
	private void processLine(String aLine) throws Exception{
		String[] parts = aLine.split("\\|");
		if (parts.length<11)
			throw new Exception("Parsing Error: A line doesn't have the right number of fields ["+aLine+"]");
		DasAnnotatedSegment segment = this.getSegment(parts[1]);
		DasComponentFeature gene= this.getGene(parts[2],parts[3],parts[4],segment);
		DasComponentFeature transcript= this.getTranscript(parts[5],parts[6],parts[7],gene);
		this.getExon(parts[8],parts[9],parts[10],transcript);
	}


	private DasComponentFeature getExon(String exonID, String start, String stop, DasComponentFeature transcript) throws DataSourceException {
		for (DasComponentFeature feature:transcript.getReportableSubComponents())
			if(feature.getFeatureId().equals(exonID))
				return feature;
		int startI=0,stopI=0;
		try {
			startI=Integer.parseInt(start);
			stopI=Integer.parseInt(stop);
		}catch (NumberFormatException nfe){
			throw new DataSourceException("PARSE ERROR: The coordinates for the exon "+exonID+" should be numeric",nfe);
		}
		return transcript.addSubComponent(exonID, startI, stopI, startI, stopI, exonID, this.exonType, exonID, exonID, method, null, null, null, null, null);
	}
	private DasComponentFeature getTranscript(String transcriptID, String start, String stop, DasComponentFeature gene) throws DataSourceException {
		for (DasComponentFeature feature:gene.getReportableSubComponents())
			if(feature.getFeatureId().equals(transcriptID))
				return feature;
		int startI=0,stopI=0;
		try {
			startI=Integer.parseInt(start);
			stopI=Integer.parseInt(stop);
		}catch (NumberFormatException nfe){
			throw new DataSourceException("PARSE ERROR: The coordinates for the transcript "+transcriptID+" should be numeric",nfe);
		}
		return gene.addSubComponent(transcriptID, startI, stopI, startI, stopI, transcriptID, this.transcriptType, transcriptID, transcriptID, method, null, null, null, null, null);
	}
	private DasComponentFeature getGene(String geneID,String start,String stop, DasAnnotatedSegment segment) throws DataSourceException {
		for (DasFeature feature:segment.getFeatures())
			if(feature.getFeatureId().equals(geneID))
				return (DasComponentFeature)feature;
		int startI=0,stopI=0;
		try {
			startI=Integer.parseInt(start);
			stopI=Integer.parseInt(stop);
		}catch (NumberFormatException nfe){
			throw new DataSourceException("PARSE ERROR: The coordinates for the gene "+geneID+" should be numeric",nfe);
		}
		return segment.getSelfComponentFeature().addSubComponent(geneID, startI, stopI, startI, stopI, geneID, geneType, geneID, geneID, method, null, null, null, null, null);
	}
	/**
	 * To get a segment we start by looking in the current list of segments in case this segment has been created already. 
	 * If is found is returned. If not a new segment is created using the id of the chromosome. 
	 * And then, added to the list and returned. the file is not giving us too much information about the chromosome, 
	 * so we are using default values in most of its fields.
	 * @param segmentId the id to recover/create a segment
	 * @return The segment with that ID
	 * @throws DataSourceException in case there is a problem creating a DAS object.
	 */
	private DasAnnotatedSegment getSegment(String segmentId) throws DataSourceException {
		for (DasAnnotatedSegment segment:segments)
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		DasAnnotatedSegment newSegment = new DasAnnotatedSegment(segmentId,1,1,"FROM_PIPE_FILE",segmentId, new ArrayList<DasFeature>());
		segments.add(newSegment);
		return newSegment;
	}
	public static void main(String[] a){
		try {
			SeparatedByPipesParser parser = new SeparatedByPipesParser((new FileInputStream("/Users/4ndr01d3/Downloads/test16genes.txt")));
			parser.parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<DasType> getTypes() {
		return types;
	}

}
