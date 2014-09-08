package org.molgenis.data.elasticsearch;

import org.elasticsearch.client.Client;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.elasticsearch.index.MappingsBuilder;

import java.io.IOException;

/**
 * Created by charbonb on 05/09/14.
 */
public interface MappingManager {
    public boolean create(Client client, EntityMetaData entityMetaData, String indexName);
}
