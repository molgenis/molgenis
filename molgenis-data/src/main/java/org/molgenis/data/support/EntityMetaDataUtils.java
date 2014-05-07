package org.molgenis.data.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.util.EntityUtils;

public class EntityMetaDataUtils
{
	Logger logger = Logger.getLogger(EntityMetaDataUtils.class);
	Map<String, EntityMetaData> omxEntities = new LinkedHashMap<String, EntityMetaData>();

	public EntityMetaDataUtils()
	{
		DefaultEntityMetaData characteristic = new DefaultEntityMetaData("Characteristic");
		characteristic.addAttribute("identifier");
		omxEntities.put(characteristic.getName(), characteristic);

		// TODO: categroy should be converted
		DefaultEntityMetaData category = new DefaultEntityMetaData("Category");
		category.addAttribute("identifier");
		omxEntities.put(category.getName(), category);
	}

	public Collection<EntityMetaData> loadEMD(RepositoryCollection emdFormattedRepos)
	{
		// extract entity metadata
		Map<String, DefaultEntityMetaData> entities = new LinkedHashMap<String, DefaultEntityMetaData>();
		for (Entity e : emdFormattedRepos.getRepositoryByEntityName("entities"))
		{
			DefaultEntityMetaData emd = new DefaultEntityMetaData(e.getString("name"));
			if (e.getBoolean("abstract")) emd.setAbstract(true);
		}

		// extract extends relations
		for (Entity e : emdFormattedRepos.getRepositoryByEntityName("entities"))
		{
			if (e.get("extends") != null)
			{
				DefaultEntityMetaData emd = entities.get(e.get("name"));
				emd.setExtends(entities.get(e.get("extends")));
			}
		}

		Collection<EntityMetaData> result = new ArrayList<EntityMetaData>();
		result.addAll(entities.values());
		return result;
	}

	public Collection<EntityMetaData> loadOMX(RepositoryCollection omx)
	{
		// extract attribute metadata
		Map<String, AttributeMetaData> attributes = new LinkedHashMap<String, AttributeMetaData>();
		for (Entity e : omx.getRepositoryByEntityName("observablefeature"))
		{
			logger.debug("found observablefeature: " + e);

			DefaultAttributeMetaData att = new DefaultAttributeMetaData(e.getString("name"));
			if (e.get("dataType") != null) att.setDataType(MolgenisFieldTypes.getType(e.getString("dataType")));
			attributes.put(e.getString("identifier"), att);
			if (e.get("description") != null) att.setDescription(e.getString("description"));
			// TODO unit! if(e.get("unit") != null)

			if ("xref".equals(e.get("dataType")) || "mref".equals(e.get("dataType")))
			{
				// TODO: cannot solve!!!
				att.setRefEntity(omxEntities.get("Characteristic"));
			}
			if ("categorical".equals(e.get("dataType")))
			{
				att.setRefEntity(omxEntities.get("Category"));
			}
		}
		// TODO: fix categorical!

		// extract protocols as entities(abstract=true)
		Map<String, EntityMetaData> entities = new LinkedHashMap<String, EntityMetaData>();
		for (Entity e : omx.getRepositoryByEntityName("protocol"))
		{
			// skip all null entities
			if (hasValues(e))
			{
				logger.debug("found protocol: " + e);

				DefaultEntityMetaData ent = new DefaultEntityMetaData(e.getString("identifier")); // alas
				ent.setLabel(e.getString("name"));
				ent.setAbstract(true);

				// add attributes
				if (e.get("features_identifier") != null) for (String attIdentifier : e.getList("features_identifier"))
				{
					if (attributes.get(attIdentifier) == null) throw new RuntimeException("attribute '" + attIdentifier
							+ "' unknown");
					ent.addAttributeMetaData(attributes.get(attIdentifier));
				}

				entities.put(e.getString("identifier"), ent);
			}
		}

		for (Entity e : omx.getRepositoryByEntityName("protocol"))
		{
			// add subprotocols as compound
			if (e.get("subprotocols_identifier") != null)
			{
				for (String subProtocol : e.getList("subprotocols_identifier"))
				{
					DefaultAttributeMetaData att = new DefaultAttributeMetaData(subProtocol);
					att.setDataType(MolgenisFieldTypes.COMPOUND);
					att.setRefEntity(entities.get(subProtocol));
					((DefaultEntityMetaData) entities.get(e.get("identifier"))).addAttributeMetaData(att);
				}

			}
		}

		// create dataset as entities
		for (Entity e : omx.getRepositoryByEntityName("dataset"))
		{
			logger.debug("found dataset: " + e);

			DefaultEntityMetaData ent = new DefaultEntityMetaData(e.getString("identifier"));
			ent.setLabel(e.getString("name"));
			// dataset 'extends' protocol
			ent.setExtends(entities.get(e.getString("protocolused_identifier")));
			entities.put(e.getString("identifier"), ent);
		}

		return entities.values();
	}

	public void store(Collection<EntityMetaData> model, RepositoryCollection store)
	{
		Map<String, Entity> entities = new LinkedHashMap<String, Entity>();
		Map<String, Entity> attributes = new LinkedHashMap<String, Entity>();

		for (EntityMetaData emd : model)
		{
			toEntity(emd, entities, attributes);
		}
	}

	private void toEntity(EntityMetaData emd, Map<String, Entity> entities, Map<String, Entity> attributes)
	{
		// TODO prevent duplicates
		Entity e = new MapEntity();
		e.set("name", emd.getName());
		entities.put(emd.getName(), e);

		logger.debug("entity: " + e);

		for (AttributeMetaData amd : emd.getAttributes())
		{
			Entity a = new MapEntity();
			a.set("name", amd.getName());
			a.set("entity", emd.getName());
			if (amd.getDataType() != null && amd.getDataType() != MolgenisFieldTypes.STRING) a.set("dataType",
					amd.getDataType());
			if (amd.getDefaultValue() != null) a.set("defaultValue", amd.getDefaultValue());
			if (amd.getRefEntity() != null) a.set("refEntity", amd.getRefEntity().getName());

			logger.debug("attribute: " + a);

			attributes.put(emd.getName() + "." + amd.getName(), a);

			// compound
			if (amd.getDataType() == MolgenisFieldTypes.COMPOUND && entities.get(amd.getRefEntity().getName()) == null)
			{
				this.toEntity(amd.getRefEntity(), entities, attributes);
			}
		}
	}

	public String toXml(Collection<EntityMetaData> entities)
	{
		String xml = "<molgenis>\n";

		// TODO prevent duplicates
		Map<String, EntityMetaData> allEntities = new LinkedHashMap<String, EntityMetaData>();
		getEntitiesRecursive(allEntities, entities);

		for (EntityMetaData emd : allEntities.values())
		{
			xml += "\t<entity name=\"" + emd.getName() + "\"";
			if (emd.isAbstract()) xml += " abstract=\"true\"";
			if (emd.getExtends() != null) xml += " extends=\"" + emd.getExtends().getName() + "\"";
			xml += ">\n";
			for (AttributeMetaData amd : emd.getAttributes())
			{
				xml += "\t\t<field name=\"" + amd.getName() + "\"";
				if (amd.getDataType() != MolgenisFieldTypes.STRING) xml += " dataType=\"" + amd.getDataType() + "\"";
				if (amd.getRefEntity() != null) xml += " refEntity=\"" + amd.getRefEntity().getName() + "\"";
				xml += "/>\n";
			}
			xml += "\t</entity\n";
		}

		xml += "</molgenis>\n";
		return xml;
	}

	private void getEntitiesRecursive(Map<String, EntityMetaData> allEntities, Collection<EntityMetaData> rootEntities)
	{
		for (EntityMetaData emd : rootEntities)
		{
			if (!allEntities.containsKey(emd.getName())) allEntities.put(emd.getName(), emd);
			for (AttributeMetaData amd : emd.getAttributes())
			{
				if (amd.getRefEntity() != null && !allEntities.containsKey(amd.getRefEntity().getName()))
				{
					getEntitiesRecursive(allEntities, Arrays.asList(new EntityMetaData[]
					{ amd.getRefEntity() }));
				}
			}
		}
	}

	private boolean hasValues(Entity e)
	{
		return !EntityUtils.isEmpty(e);
	}

	public Collection<EntityMetaData> extractMetaData(DataSource ds) throws ClassNotFoundException
	{
		Map<String, DefaultEntityMetaData> result = new LinkedHashMap<String, DefaultEntityMetaData>();

		Connection conn = null;
		try
		{
			conn = ds.getConnection();

			DatabaseMetaData md = conn.getMetaData();

			ResultSet tableInfo = md.getTables(null, null, "%", new String[]
			{ "TABLE" });

			// first add all entities
			while (tableInfo.next())
			{
				DefaultEntityMetaData entity = new DefaultEntityMetaData(tableInfo.getString("TABLE_NAME"));
				result.put(entity.getName(), entity);
			}

			// again, then add columns
			tableInfo = md.getTables(null, null, "%", new String[]
			{ "TABLE" });
			while (tableInfo.next())
			{
				DefaultEntityMetaData entity = result.get(tableInfo.getString("TABLE_NAME"));

				// ADD THE COLUMNS
				ResultSet fieldInfo = md.getColumns(null, null, tableInfo.getString("TABLE_NAME"), null);
				while (fieldInfo.next())
				{
					DefaultAttributeMetaData f = new DefaultAttributeMetaData(fieldInfo.getString("COLUMN_NAME"));
					// FIXME refactor out
					f.setDataType(MolgenisFieldTypes.getType(org.molgenis.model.jaxb.Field.Type.getType(
							fieldInfo.getInt("DATA_TYPE")).toString()));
					f.setDefaultValue(fieldInfo.getString("COLUMN_DEF"));
					entity.addAttributeMetaData(f);

					if (md.getDatabaseProductName().toLowerCase().contains("mysql"))
					{
						// accomodate mysql CURRENT_TIMESTAMP
						if ("CURRENT_TIMESTAMP".equals(f.getDefaultValue())
								&& (f.getDataType().equals(MolgenisFieldTypes.DATETIME) || f.getDataType().equals(
										MolgenisFieldTypes.DATE)))
						{
							f.setDefaultValue(null);
							f.setAuto(true);
						}
					}

					// accomodate mysql text/string fields +
					// nillable="false" -> mysql ignore not null and so
					// should we!

					if (fieldInfo.getString("REMARKS") != null && !"".equals(fieldInfo.getString("REMARKS").trim()))
					{
						f.setDescription(fieldInfo.getString("REMARKS"));
					}

					if (fieldInfo.getBoolean("NULLABLE"))
					{
						f.setNillable(true);
					}

					// auto increment?
					if (f.getDataType().equals(MolgenisFieldTypes.INT)
							&& fieldInfo.getObject("IS_AUTOINCREMENT") != null)
					{
						f.setAuto(fieldInfo.getBoolean("IS_AUTOINCREMENT"));
					}

					if (f.getDataType().equals(MolgenisFieldTypes.STRING))
					{
						if (fieldInfo.getInt("COLUMN_SIZE") > 255)
						{
							f.setDataType(MolgenisFieldTypes.TEXT);
							// f.setLength(fieldInfo.getInt("COLUMN_SIZE"));
						}
						else
						{
							// if (fieldInfo.getInt("COLUMN_SIZE") != 255)
							// {f.setLength(fieldInfo.getInt("COLUMN_SIZE"));}
							f.setDataType(null); // defaults to string
						}
					}

					// xrefs
					ResultSet xrefInfo = md.getImportedKeys(null, null, tableInfo.getString("TABLE_NAME"));
					while (xrefInfo.next())
					{
						if (xrefInfo.getString("FKCOLUMN_NAME").equals(fieldInfo.getString("COLUMN_NAME")))
						{
							f.setDataType(MolgenisFieldTypes.XREF);
							// problem: PKTABLE_NAME is lowercase, need to be
							// corrected later?

							f.setRefEntity(result.get(xrefInfo.getString("PKTABLE_NAME")));
							// + "."
							// + xrefInfo.getString("PKCOLUMN_NAME"));
						}
					}
				}

				// // GET AUTO INCREMENT
				//
				// // mysql workaround
				// Statement stmt = null;
				// try
				// {
				// String sql = "select * from " + e.getName() + " where 1=0";
				// stmt = conn.createStatement();
				//
				// ResultSet autoincRs = stmt.executeQuery(sql);
				// ResultSetMetaData rowMeta = autoincRs.getMetaData();
				// for (int i = 1; i <= rowMeta.getColumnCount(); i++)
				// {
				// if (rowMeta.isAutoIncrement(i))
				// {
				// e.getFields().get(i - 1).setAuto(true);
				// }
				// }
				// }
				// catch (Exception exc)
				// {
				// logger.error("didn't retrieve autoinc/sequence: " + exc.getMessage());
				// // e.printStackTrace();
				// }
				// finally
				// {
				// stmt.close();
				// }
				//
				// ADD UNIQUE CONTRAINTS
				ResultSet rsIndex = md.getIndexInfo(null, null, tableInfo.getString("TABLE_NAME"), true, false);
				// indexed list of uniques
				Map<String, List<String>> uniques = new LinkedHashMap<String, List<String>>();
				while (rsIndex.next())
				{
					logger.debug("UNIQUE: " + rsIndex);

					// TABLE_CAT='molgenistest' TABLE_SCHEM='null'
					// TABLE_NAME='boolentity' NON_UNIQUE='false'
					// INDEX_QUALIFIER='' INDEX_NAME='PRIMARY' TYPE='3'
					// ORDINAL_POSITION='1' COLUMN_NAME='id' ASC_OR_DESC='A'
					// CARDINALITY='0' PAGES='0' FILTER_CONDITION='null'
					if (uniques.get(rsIndex.getString("INDEX_NAME")) == null) uniques.put(
							rsIndex.getString("INDEX_NAME"), new ArrayList<String>());
					uniques.get(rsIndex.getString("INDEX_NAME")).add(rsIndex.getString("COLUMN_NAME"));
				}
				for (List<String> index : uniques.values())
				{
					if (index.size() == 1)
					{
						((DefaultAttributeMetaData) entity.getAttribute(index.get(0))).setUnique(true);
					}
					else
					{
						// TODO: composite keys!
						// StringBuilder fieldsBuilder = new StringBuilder();
						// for (String field_name : index)
						// {
						// fieldsBuilder.append(',').append(field_name);
						// }
						// Unique u = new Unique();
						// u.setFields(fieldsBuilder.substring(1));
						// e.getUniques().add(u);
					}

				}
				//
				// // FIND type="autoid"
				// for (Field f : e.getFields())
				// {
				// if (f.getAuto() != null && f.getAuto() && f.getType().equals(Type.INT) && f.getUnique() != null
				// && f.getUnique())
				// {
				// f.setType(Field.Type.AUTOID);
				// f.setAuto(null);
				// f.setUnique(null);
				// }
				// }
				// }
				//
				// // GUESS type="xref"
				// // normally they should be defined as foreign key but sometimes
				// // designers leave this out
				// // rule: if the field name is the same and one is autoid,
				// // then other fields having the same name are likely to be xref to
				// // the autoid
				// for (Entity e : m.getEntities())
				// {
				// for (Field f : e.getFields())
				// {
				// if (Field.Type.AUTOID.equals(f.getType()))
				// {
				// for (Entity otherE : m.getEntities())
				// {
				// for (Field otherF : otherE.getFields())
				// {
				// // assume xref if
				// // name == name
				// // otherF.type == int
				// if (otherF.getName().equals(f.getName()) && otherF.getType().equals(Field.Type.INT))
				// {
				// logger.debug("Guessed that " + otherE.getName() + "." + otherF.getName()
				// + " references " + e.getName() + "." + f.getName());
				// otherF.setType(Field.Type.XREF_SINGLE);
				// // otherF.setXrefEntity(;
				// otherF.setXrefField(e.getName() + "." + f.getName());
				// }
				// }
				// }
				// }
				//
				// }
				// }
				//
				// // GUESS the xref labels
				// // guess the xreflabel as being the non-autoid field that is unique
				// // and not null
				// // rule: if there is another unique field in the referenced table
				// // then that probably is usable as label
				// for (Entity e : m.getEntities())
				// {
				// for (Field f : e.getFields())
				// {
				// if (Field.Type.XREF_SINGLE.equals(f.getType()))
				// {
				// String xrefEntityName = f.getXrefField().substring(0, f.getXrefField().indexOf("."));
				// String xrefFieldName = f.getXrefField().substring(f.getXrefField().indexOf(".") + 1);
				// // reset the xref entity to the uppercase version
				// f.setXrefField(m.getEntity(xrefEntityName).getName() + "." + xrefFieldName);
				//
				// for (Field labelField : m.getEntity(xrefEntityName).getFields())
				// {
				// // find the other unique, nillable="false" field, if
				// // any
				// if (!labelField.getName().equals(xrefFieldName)
				// && Boolean.TRUE.equals(labelField.getUnique())
				// && Boolean.FALSE.equals(labelField.getNillable()))
				// {
				// logger.debug("guessed label " + e.getName() + "." + labelField.getName());
				// f.setXrefLabel(labelField.getName());
				// }
				// }
				// }
				// }
				// }

				// GUESS the inheritance relationship
				// rule: if there is a foreign key that is unique itself it is
				// probably inheriting...
				// action: change to inheritance and remove the xref field
				for (DefaultEntityMetaData e : result.values())
				{
					List<AttributeMetaData> toBeRemoved = new ArrayList<AttributeMetaData>();
					for (AttributeMetaData f : e.getAttributes())
					{
						if (MolgenisFieldTypes.XREF.equals(f.getDataType()) && Boolean.TRUE.equals(f.isUnique()))
						{
							e.setExtends(f.getRefEntity());
							toBeRemoved.add(f);
						}
					}
//					for (DefaultEntityMetaData f : toBeRemoved)
//					{
//						//e.removeAttributeMetaData(f);
//					}
				}
				//
				// // TODO GUESS the type="mref"
				// // rule: any entity that is not a subclass and that has maximum two
				// // xref fields and autoid field
				// // should be a mref
				// List<Entity> toBeRemoved = new ArrayList<Entity>();
				// for (Entity e : m.getEntities())
				// if ("".equals(e.getExtends()))
				// {
				//
				// if (e.getFields().size() <= 3)
				// {
				// int xrefs = 0;
				// String idField = null;
				// // the column refering to 'localEntity'
				// String localIdField = null;
				// // the localEntiy
				// String localEntity = null;
				// // the column referring to 'remoteEntity'
				// String localEntityField = null;
				// // the column the localIdField is referning to
				// String remoteIdField = null;
				// // the column remoteEntity
				// String remoteEntity = null;
				// // the column the remoteIdField is referring to
				// String remoteEntityField = null;
				//
				// for (Field f : e.getFields())
				// {
				// if (Field.Type.AUTOID.equals(f.getType()))
				// {
				// idField = f.getName();
				// }
				// else if (Field.Type.XREF_SINGLE.equals(f.getType()))
				// {
				// xrefs++;
				// if (xrefs == 1)
				// {
				// localIdField = f.getName();
				// // localEntityField is just the idField of
				// // the
				// // localEntity
				// localEntity = f.getXrefField().substring(0, f.getXrefField().indexOf("."));
				// localEntityField = f.getXrefField()
				// .substring(f.getXrefField().indexOf(".") + 1);
				// }
				// else
				// {
				// remoteIdField = f.getName();
				// // should be the id field of the remote
				// // entity
				// remoteEntity = f.getXrefField().substring(0, f.getXrefField().indexOf("."));
				// remoteEntityField = f.getXrefField().substring(
				// f.getXrefField().indexOf(".") + 1);
				// }
				// }
				// }
				//
				// // if valid mref, drop this entity and add mref fields
				// // to
				// // the other entities.
				// if (xrefs == 2 && (e.getFields().size() == 2 || idField != null))
				// {
				// // add mref on 'local' end
				// Entity localContainer = m.getEntity(localEntity);
				// Field localField = new Field();
				// if (localContainer.getField(e.getName()) == null)
				// {
				// localField.setName(e.getName());
				// }
				//
				// localField.setType(Field.Type.XREF_MULTIPLE);
				// localField.setXrefField(remoteEntity + "." + remoteEntityField);
				// localField.setMrefName(e.getName());
				// localField.setMrefLocalid(localIdField);
				// localField.setMrefRemoteid(remoteIdField);
				// localContainer.getFields().add(localField);
				//
				// // add mref to remote end
				// Entity remoteContainer = m.getEntity(remoteEntity);
				// Field remoteField = new Field();
				// remoteField.setType(Field.Type.XREF_MULTIPLE);
				// remoteField.setXrefField(localEntity + "." + localEntityField);
				// remoteField.setMrefName(e.getName());
				// // don't need to add local id as it is refering back
				// remoteField.setMrefLocalid(remoteIdField);
				// remoteField.setMrefRemoteid(localIdField);
				//
				// if (remoteContainer.getField(e.getName()) == null)
				// {
				// remoteField.setName(e.getName());
				// }
				// else
				// {
				// throw new RuntimeException("MREF creation failed: there is already a field "
				// + remoteContainer.getName() + "." + e.getName());
				// }
				//
				// remoteContainer.getFields().add(remoteField);
				//
				// // remove the link table as separate entity
				// toBeRemoved.add(e);
				// logger.debug("guessed mref " + e.getName());
				// }
				// }
				// }
				// m.getEntities().removeAll(toBeRemoved);
				//
				// // logger.info(MolgenisLanguage.summarize(m));
				// logger.info(toString(m));
				// return m;

				logger.debug(entity);
			}
		}
		catch (Exception ex)
		{
			logger.error(ex);
			ex.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception ex2)
			{
				throw new RuntimeException(ex2);
			}
		}
		return null;
	}
}
