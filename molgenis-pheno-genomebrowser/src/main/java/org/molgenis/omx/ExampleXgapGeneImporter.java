package org.molgenis.omx;


//import org.molgenis.framework.db.Database;
//import org.molgenis.framework.db.Database.DatabaseAction;
//import org.molgenis.framework.db.DatabaseException;
//import org.molgenis.gene.db.ProbeEntityImporter;
//import org.molgenis.io.TupleReader;
//import org.molgenis.io.csv.CsvReader;
//import org.molgenis.io.tuple.SingleTupleReader;
//import org.molgenis.util.tuple.KeyValueTuple;
//import org.molgenis.util.tuple.Tuple;

public class ExampleXgapGeneImporter
{

	public static void main(String[] args)
	{
//		try
//		{
//			new ExampleXgapGeneImporter().importXgap(null, new URL(
//					"http://molgenis38.target.rug.nl:8080/xqtl_panacea/api/find/"));
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}

//	public void importXgap(Database db, URL xgapURL) throws IOException, DatabaseException
//	{
//		File probeFile = downloadData("Probe", xgapURL);
//		File chromosomeFile = downloadData("Chromosome", xgapURL);
//		Map<String, Integer> chromosomeMap = new HashMap();
//		System.out.println("probe file: " + probeFile.getAbsolutePath());
//		System.out.println("chromosomes file: " + chromosomeFile.getAbsolutePath());
//
//		TupleReader chromosomeReader = new CsvReader(chromosomeFile);
//		try
//		{
//			ProbeEntityImporter importer = new ProbeEntityImporter();
//			importer.importEntity(chromosomeReader, db, DatabaseAction.UPDATE);
//			Iterator itr = chromosomeReader.iterator();
//			int offset = 0;
//			while (itr.hasNext())
//			{
//				Tuple tuple = (Tuple) itr.next();
//				chromosomeMap.put(tuple.getString("id"), offset);
//				if (tuple.getInt("bpLength") != null)
//				{
//					offset += tuple.getInt("bpLength");
//				}
//			}
//		}
//		finally
//		{
//			chromosomeReader.close();
//		}
//
//		TupleReader probeReader = new CsvReader(probeFile);
//		try
//		{
//			for (Tuple tuple : probeReader)
//			{
//				KeyValueTuple kvTuple = new KeyValueTuple(tuple);
//				String chromosome = tuple.getString("chromosome_id");
//				String id = tuple.getString("id");
//				if (chromosome != null)
//				{
//					Integer offset = chromosomeMap.get(chromosome);
//
//					Integer bpStart = tuple.getInt("bpStart");
//					Integer newBpStart = bpStart - offset;
//
//					Integer bpEnd = tuple.getInt("bpStart");
//					Integer newBpEnd = bpEnd - offset;
//
//					kvTuple.set("bpStart", newBpStart);
//					kvTuple.set("bpEnd", newBpEnd);
//
//					ProbeEntityImporter importer = new ProbeEntityImporter();
//					 TupleReader tupleReader = new SingleTupleReader(kvTuple);
//					importer.importEntity(tupleReader, db, DatabaseAction.UPDATE);
//				}
//			}
//		}
//		finally
//		{
//			probeReader.close();
//		}
//
//		// probe bp locations are cumulative, 'fix' by using chromosome lengths
//		// the chromosomes have an orderNr, use this to know how much to
//		// subtract!
//
//		// serve as DAS
//
//	}
//
//	public File downloadData(String what, URL xgapURL) throws IOException
//	{
//		System.out.println("starting download of " + what + " data..");
//		// URL website = new
//		// URL("http://molgenis38.target.rug.nl:8080/xqtl_panacea/api/find/" +
//		// what);
//		URL website = new URL(xgapURL.toString() + what);
//		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
//		File temp = File.createTempFile(what + ".csv", Long.toString(System.nanoTime()));
//		FileOutputStream fos = new FileOutputStream(temp);
//		try
//		{
//			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//		}
//		finally
//		{
//			fos.close();
//		}
//		System.out.println("finished download of " + what + " data");
//		return temp;
//	}
}
