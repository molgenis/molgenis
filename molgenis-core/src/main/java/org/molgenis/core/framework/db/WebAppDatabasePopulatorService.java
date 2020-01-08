package org.molgenis.core.framework.db;

/** Can be implemented by MOLGENIS apps that want to populate the database at bootstrapping. */
@SuppressWarnings("unused")
public interface WebAppDatabasePopulatorService {
  void populateDatabase();

  boolean isDatabasePopulated();
}
