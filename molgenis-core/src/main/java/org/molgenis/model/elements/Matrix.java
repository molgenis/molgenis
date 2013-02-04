package org.molgenis.model.elements;

import org.apache.log4j.Logger;
import org.molgenis.model.MolgenisModelException;

/*
 * <entity name="TextData"> <field name="assay" type="xref"
 * xref_entity="Assay"/> <field name="row" type="xref" xref_entity="Item"/>
 * <field name="col" type="xref" xref_entity="Item"/> <field name="value"
 * type="string" nillable="true"/> </entity>
 * 
 * <matrix name="Genotypes" content_entity="TextData" container="assay"
 * content="value" row="row" col="col" row_entity="Marker" col_entity="Subject">
 * 
 * 
 */

public class Matrix extends DBSchema
{
	private static final long serialVersionUID = 1L;
	/** the entity that is wrapped by this matrix */
	String content_entity;
	/** field of the entity that points to the container */
	String container;
	/** field of the entity that contains the matrix cell values */
	String content;
	/** field that points to the labels of the row */
	String row;
	/** field that points to the labels of the column */
	String col;
	/** override the default xref type of row to use a subclass */
	String row_entity;
	/** override the default xref type of col to use a subclass */
	String col_entity;

	public Matrix(String name, DBSchema parent)
	{
		super(name, parent, parent.getModel());
	}

	public String getContentEntity()
	{
		return content_entity;
	}

	public void setContentEntity(String entity)
	{
		this.content_entity = entity;
	}

	public String getContainer()
	{
		return container;
	}

	/**
	 * @param container
	 */
	public void setContainer(String container)
	{
		this.container = container;
	}

	public String getRow()
	{
		return row;
	}

	public void setRow(String row)
	{
		this.row = row;
	}

	public String getCol()
	{
		return col;
	}

	public void setCol(String col)
	{
		this.col = col;
	}

	/**
	 * Return the Entity that contains the labels for the rows.
	 * 
	 * @return entity that is refered to from the rows
	 * @throws MolgenisModelException
	 */
	public Entity getRowEntity() throws MolgenisModelException
	{
		if (this.row_entity != null && !this.row_entity.equals(""))
		{
			return (Entity) get(this.row_entity);
		}
		else
		{
			Entity e = (Entity) get(this.content_entity);
			Field row_field = e.getField(this.row);
			return row_field.getXrefEntity();
			// return model.getEntity(row_field.getXRefEntity());
		}
	}

	/**
	 * Return the Entity that contains the labels for the columns.
	 * 
	 * @return entity that is referred to by the columns
	 * @throws MolgenisModelException
	 */
	public Entity getColEntity() throws MolgenisModelException
	{
		if (this.col_entity != null && !this.col_entity.equals(""))
		{
			return (Entity) get(col_entity);
		}
		else
		{
			Entity ce = (Entity) get(content_entity);
			// get the field describing the row
			Field col_field = ce.getField(col);
			// get from the field the entity that is 'xref-ed' by the row
			return col_field.getXrefEntity();
		}
	}

	/**
	 * Return the Entity that contains the container
	 * 
	 * @return entity that provides the name of this matrix
	 * @throws MolgenisModelException
	 */
	public Entity getContainerEntity() throws MolgenisModelException
	{
		// get the column of the content table that poinst to the container
		Entity ce = (Entity) get(content_entity); // has a field with name
		// equals container
		// e.g. textdata.assay
		Field container_field = ce.getField(container);

		Logger.getLogger("TEST").info("container_field: " + container_field);
		// Logger.getLogger("TEST").debug("container_entity:
		// "+container_field.getXRefEntity());
		// get from the field the entity that is 'xref-ed' by the row
		return container_field.getXrefEntity();
	}

	public String getRowEntityName()
	{
		return row_entity;
	}

	public void setRowEntityName(String row_entity)
	{
		this.row_entity = row_entity;
	}

	public String getColEntityName()
	{
		return col_entity;
	}

	public void setColEntityName(String col_entity)
	{
		this.col_entity = col_entity;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return "Matrix(name=" + getName() + " content_entity=" + getContentEntity() + " container=" + container
				+ " content=" + content + " row=" + row + " row_entity=" + getRowEntityName() + " col=" + col
				+ " col_entity=" + getColEntityName() + ")";
	}
}
