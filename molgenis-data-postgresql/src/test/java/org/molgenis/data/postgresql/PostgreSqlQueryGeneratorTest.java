package org.molgenis.data.postgresql;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

public class PostgreSqlQueryGeneratorTest
{
	@Test
	public void testGetSqlSelectMref() throws Exception
	{
		PackageImpl pack = new PackageImpl("org_molgenis");

		DefaultEntityMetaData ref1Meta = new DefaultEntityMetaData("Ref1", pack);
		ref1Meta.addAttributeMetaData(new DefaultAttributeMetaData("ref1Id"), ROLE_ID);

		DefaultEntityMetaData ref2Meta = new DefaultEntityMetaData("Ref2", pack);
		ref2Meta.addAttributeMetaData(new DefaultAttributeMetaData("ref2Id"), ROLE_ID);

		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("MasterEntity", pack);
		entityMeta.addAttributeMetaData(new DefaultAttributeMetaData("masterId"), ROLE_ID);
		entityMeta.addAttributeMetaData(new DefaultAttributeMetaData("mref1", MREF).setRefEntity(ref1Meta));
		entityMeta.addAttributeMetaData(new DefaultAttributeMetaData("mref2", MREF).setRefEntity(ref2Meta));

		QueryImpl<Entity> q = new QueryImpl<>();

		List<Object> parameters = Lists.newArrayList();

		String sqlSelect = PostgreSqlQueryGenerator.getSqlSelect(entityMeta, q, parameters, true);
		Assert.assertEquals(sqlSelect,
				"SELECT this.\"masterId\", "
						+ "(SELECT array_agg(DISTINCT ARRAY[\"mref1\".\"order\"::TEXT,\"mref1\".\"mref1\"::TEXT]) "
						+ "FROM \"org_molgenis_MasterEntity_mref1\" AS \"mref1\" "
						+ "WHERE this.\"masterId\" = \"mref1\".\"masterId\") AS \"mref1\", "
						+ "(SELECT array_agg(DISTINCT ARRAY[\"mref2\".\"order\"::TEXT,\"mref2\".\"mref2\"::TEXT]) "
						+ "FROM \"org_molgenis_MasterEntity_mref2\" AS \"mref2\" "
						+ "WHERE this.\"masterId\" = \"mref2\".\"masterId\") AS \"mref2\" "
						+ "FROM \"org_molgenis_MasterEntity\" AS this");
	}

}