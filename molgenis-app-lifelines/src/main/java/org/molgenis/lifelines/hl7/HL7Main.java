package org.molgenis.lifelines.hl7;

import javax.persistence.Persistence;

import org.molgenis.framework.db.Database;

import app.JpaDatabase;

/**
 * 
 * @author roankanninga
 */
public class HL7Main
{
	public static void main(String[] args) throws Exception
	{
		String file1 = args[0];
		String file2 = args[1];
		HL7Data ll = new HL7LLData(file1, file2);

		Database db = new JpaDatabase(Persistence.createEntityManagerFactory(JpaDatabase.DEFAULT_PERSISTENCE_UNIT_NAME));

		HL7OmicsConnectImporter importer = new HL7OmicsConnectImporter();

		importer.start(ll, db);
	}

}
