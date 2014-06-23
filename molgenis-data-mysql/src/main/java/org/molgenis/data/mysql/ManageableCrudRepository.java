package org.molgenis.data.mysql;

import javax.sql.DataSource;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Manageable;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.validation.EntityValidator;

//TODO rename this to MysqlRepository
public interface ManageableCrudRepository extends CrudRepository, Manageable
{
	void setMetaData(EntityMetaData entityMD);

	void setRepositoryCollection(RepositoryCollection repositoryCollection);

	void setDataSource(DataSource dataSource);

	void setValidator(EntityValidator validator);

	void truncate();

	void populateWithQuery(String insertQuery);
}
