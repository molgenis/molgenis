package uk.ac.ebi.mydas.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasEntryPointOrientation;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasType;

public class EnsemblTestManager {
	/**
	 * List of the types used in this data source
	 */
	private ArrayList<DasType> types;
	/**
	 * Types to be used in the data source: chromosome, gene, transcript and exon
	 */
	private DasType geneType,transcriptType,exonType;
	/**
	 * As this data source just have one method, it can be defined as a parameter to facilitate its use
	 */
	private DasMethod method;

	private Connection connection;
	
	private String database = "homo_sapiens_core_56_37a";
	private Collection<DasEntryPoint> entryPoints=null;

	public EnsemblTestManager() throws DataSourceException{
		//Initialize types
		geneType= new DasType("Gene", null, "SO:0000704", "Gene");
		transcriptType= new DasType("Transcript", null, "SO:0000673", "Transcript");
		exonType= new DasType("Exon", null, "SO:0000147", "Exon");
		types= new ArrayList<DasType>();
		types.add(geneType);
		types.add(transcriptType);
		types.add(exonType);
		method = new DasMethod("not_recorded","not_recorded","ECO:0000037");


		connection = null;
		String userName = "anonymous";
		String password = "";
		
		String url = "jdbc:mysql://ensembldb.ensembl.org:5306/"+database;
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			connection = DriverManager.getConnection (url, userName, password);
		} catch (InstantiationException e) {
			throw new DataSourceException("Problems loading the MySql driver",e);
		} catch (IllegalAccessException e) {
			throw new DataSourceException("Problems loading the MySql driver",e);
		} catch (ClassNotFoundException e) {
			throw new DataSourceException("Problems loading the MySql driver",e);
		} catch (SQLException e) {
			throw new DataSourceException("Problems connecting to the ensembl database",e);
		}
	}
	public void close(){
		try{
			connection.close ();
		} catch (Exception e) { /* ignore close errors */ }

	}
	public Collection<DasAnnotatedSegment> getSubmodelBySQL(String sql,int maxbins) throws DataSourceException{
		Collection<DasAnnotatedSegment> segments=null;
		try {
			Statement s = connection.createStatement ();
			s.executeQuery (sql);
			ResultSet rs = s.getResultSet ();
			DasComponentFeature previousGene=null;
			while (rs.next () && maxbins!=0) {
				if (segments==null)
					segments= new ArrayList<DasAnnotatedSegment>();
				DasAnnotatedSegment segment = this.getSegment(segments,rs.getString ("chr"));
				DasComponentFeature gene= this.getGene(rs.getString ("gene_id"),rs.getInt("gene_start"),rs.getInt("gene_end"),segment);
				if (previousGene!=gene){
					maxbins--;
					previousGene = gene;
				}

				DasComponentFeature transcript= this.getTranscript(rs.getString ("trascript_id"),rs.getInt("transcript_start"),rs.getInt("transcript_end"),gene);
				this.getExon(rs.getString ("exon_id"),rs.getInt("exon_start"),rs.getInt("exon_end"),transcript);
			}
			rs.close ();
			s.close ();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query",e);
		}
		return segments;
	}

	public Collection<DasAnnotatedSegment> getSubmodelByFeatureId(Collection<String> featureIdCollection) throws DataSourceException{
		String sql="SELECT " +
		" sr.name AS chr, " +
		" gsi.stable_id AS gene_id, " +
		" g.seq_region_start AS gene_start, " +
		" g.seq_region_end AS gene_end, " +
		" tsi.stable_id AS trascript_id, " +
		" t.seq_region_start AS transcript_start, " +
		" t.seq_region_end AS transcript_end, " +
		" esi.stable_id AS exon_id, " +
		" e.seq_region_start AS exon_start, " +
		" e.seq_region_end AS exon_end " +
		"FROM  " +
		" seq_region sr, " +
		" gene_stable_id gsi, " +
		" gene g, " +
		" transcript t, " +
		" transcript_stable_id tsi, " +
		" exon_transcript et, " +
		" exon e, " +
		" exon_stable_id esi  " +
		"WHERE  " +
		" gsi.gene_id = g.gene_id and " + 
		" g.gene_id = t.gene_id and  " +
		" t.transcript_id = tsi.transcript_id and " + 
		" t.transcript_id = et.transcript_id and  " +
		" et.exon_id = e.exon_id and  " +
		" e.exon_id = esi.exon_id and  " +
		" g.seq_region_id = sr.seq_region_id and " +
		" sr.coord_system_id = 2 and (";
		String or ="";
		for (String featureId:featureIdCollection){
			sql += 		or +" gsi.stable_id = '"+featureId+ "' or tsi.stable_id = '"+featureId+ "' or esi.stable_id = '"+featureId+ "'" ;
			or ="or";
		}
		sql += ")";
		return getSubmodelBySQL(sql,-1);
	}

	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId, int start, int stop) throws DataSourceException, BadReferenceObjectException{
		return getSubmodelBySegmentId(segmentId, start, stop,-1);
	}
	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId, int start, int stop, int maxbins) throws DataSourceException, BadReferenceObjectException{
		String sql="SELECT " +
		" sr.name AS chr, " +
		" gsi.stable_id AS gene_id, " +
		" g.seq_region_start AS gene_start, " +
		" g.seq_region_end AS gene_end, " +
		" tsi.stable_id AS trascript_id, " +
		" t.seq_region_start AS transcript_start, " +
		" t.seq_region_end AS transcript_end, " +
		" esi.stable_id AS exon_id, " +
		" e.seq_region_start AS exon_start, " +
		" e.seq_region_end AS exon_end, " +
		"(g.seq_region_end-g.seq_region_start) AS size " +
		"FROM  " +
		" seq_region sr, " +
		" gene_stable_id gsi, " +
		" gene g, " +
		" transcript t, " +
		" transcript_stable_id tsi, " +
		" exon_transcript et, " +
		" exon e, " +
		" exon_stable_id esi  " +
		"WHERE  " +
		" gsi.gene_id = g.gene_id and " + 
		" g.gene_id = t.gene_id and  " +
		" t.transcript_id = tsi.transcript_id and " + 
		" t.transcript_id = et.transcript_id and  " +
		" et.exon_id = e.exon_id and  " +
		" e.exon_id = esi.exon_id and  " +
		" g.seq_region_id = sr.seq_region_id and " +
		" sr.coord_system_id = 2 and " +
		" sr.name ='"+segmentId+"' ";
		if (start!=-1 && stop!=-1)
			sql += " and g.seq_region_start>"+start+" and g.seq_region_end<"+stop;
		sql += " ORDER BY size DESC";
		Collection<DasAnnotatedSegment>segments= getSubmodelBySQL(sql,maxbins);
		if (segments!=null && segments.size()>0)
			return segments.iterator().next();
		else
			throw new BadReferenceObjectException("Unknown Chromosome", segmentId);
	}

	private DasComponentFeature getExon(String exonID, int startI, int stopI, DasComponentFeature transcript) throws DataSourceException {
		for (DasComponentFeature feature:transcript.getReportableSubComponents())
			if(feature.getFeatureId().equals(exonID))
				return feature;
		return transcript.addSubComponent(exonID, startI, stopI, startI, stopI, exonID, this.exonType, exonID, exonID, method, null, null, null, null, null);
	}

	private DasComponentFeature getTranscript(String transcriptID, int startI, int stopI, DasComponentFeature gene) throws DataSourceException {
		for (DasComponentFeature feature:gene.getReportableSubComponents())
			if(feature.getFeatureId().equals(transcriptID))
				return feature;
		return gene.addSubComponent(transcriptID, startI, stopI, startI, stopI, transcriptID, this.transcriptType, transcriptID, transcriptID, method, null, null, null, null, null);
	}

	private DasComponentFeature getGene(String geneID, int startI, int stopI, DasAnnotatedSegment segment) throws DataSourceException {
		for (DasFeature feature:segment.getFeatures())
			if(feature.getFeatureId().equals(geneID))
				return (DasComponentFeature)feature;
		return segment.getSelfComponentFeature().addSubComponent(geneID, startI, stopI, startI, stopI, geneID, geneType, geneID, geneID, method, null, null, null, null, null);
	}

	private DasAnnotatedSegment getSegment(
			Collection<DasAnnotatedSegment> segments, String segmentId) throws DataSourceException {
		for (DasAnnotatedSegment segment:segments)
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		Integer length = this.getSegmentLength(segmentId);
		DasAnnotatedSegment newSegment = new DasAnnotatedSegment(segmentId,1,length,"FROM_DATABASE",segmentId, new ArrayList<DasFeature>());
		segments.add(newSegment);
		return newSegment;
	}
	private Integer getSegmentLength(String segmentId){
		String sql="SELECT length FROM seq_region WHERE name='"+segmentId+"' and coord_system_id=2";
		try{
			Statement s = connection.createStatement ();
			s.executeQuery (sql);
			ResultSet rs = s.getResultSet ();
			while (rs.next ()) 
				return rs.getInt ("length");
		} catch (SQLException e) {
			return 1;
		}
		return 1;
	}
	public ArrayList<DasType> getTypes() {
		return types;
	}
	public Integer getTotalCountForType(String typeId)
	throws DataSourceException {
		String sql="";
		Integer count =0;
		if (typeId.equalsIgnoreCase("Gene"))
			sql="SELECT count(stable_id) as num FROM gene_stable_id;";
		else if (typeId.equalsIgnoreCase("Transcript"))
			sql="SELECT count(stable_id) as num FROM transcript_stable_id;";
		else if (typeId.equalsIgnoreCase("Exon"))
			sql="SELECT count(stable_id) as num FROM exon_stable_id;";
		try {
			Statement s = connection.createStatement ();
			s.executeQuery (sql);
			ResultSet rs = s.getResultSet ();
			if (rs.next ()) {
				count=rs.getInt("num");
			}
			rs.close ();
			s.close ();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query",e);
		}
		return count;
	}
	public DasSequence getSequence(String segmentId) throws DataSourceException, BadReferenceObjectException {
		DasSequence seq=null;
		String sql="";
		sql="SELECT * FROM chromosome WHERE name="+segmentId;
		try {
			Statement s = connection.createStatement ();
			s.executeQuery (sql);
			ResultSet rs = s.getResultSet ();
			if (rs.next ()) {
				seq= new DasSequence(segmentId,rs.getString("SEQUENCE"),1,"homo_sapiens_core_56_37a", "Chromosome "+segmentId);
			}else{
				throw new BadReferenceObjectException("The segment ["+segmentId+"] was not found in this reference server", segmentId);
			}
			rs.close ();
			s.close ();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query",e);
		}
		return seq;
	}
	public String getDatabase(){
		return database;
	}
	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException{
		if (entryPoints!=null){
			if ((start != null) && (stop != null)) 
				return ((ArrayList<DasEntryPoint>)entryPoints).subList(start, stop);
			return entryPoints;
		}
		String sql="";
		sql="SELECT * FROM seq_region WHERE seq_region.coord_system_id =2;";
		try {
			Statement s = connection.createStatement ();
			s.executeQuery (sql);
			ResultSet rs = s.getResultSet ();
			entryPoints = new ArrayList<DasEntryPoint>();
			while (rs.next ()) {
				entryPoints.add(new DasEntryPoint(rs.getString("name"), 1, rs.getInt("length"), "Chromosome", getDatabase(), DasEntryPointOrientation.POSITIVE_ORIENTATION, "Chromosome", true));
			}
			rs.close ();
			s.close ();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query",e);
		}
		if ((start != null) && (stop != null)) 
			return ((ArrayList<DasEntryPoint>)entryPoints).subList(start, stop);

		return entryPoints;
	}

}
