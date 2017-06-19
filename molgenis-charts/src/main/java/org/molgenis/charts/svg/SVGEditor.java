package org.molgenis.charts.svg;

import org.molgenis.charts.charttypes.HeatMapChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.Iterator;

public class SVGEditor
{

	private static final Logger LOG = LoggerFactory.getLogger(SVGEditor.class);

	XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	static final String G = "g";
	static final String PATH = "path";
	static final QName ID = new QName("id");

	private final File inFile;
	private final File outFile;

	/**
	 * Creates a new instance of SVGEditor.
	 *
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public SVGEditor(File inFile, File outFile)
			throws FileNotFoundException, XMLStreamException, FactoryConfigurationError
	{
		this.inFile = inFile;
		this.outFile = outFile;
	}

	/**
	 * Annotates a block of values of a heatmap.plus SVG file by giving them an id and row/column attributes. A
	 * heatmap.plus can contain up to three 'blocks': row annotations, column annotations and the matrix.
	 *
	 * @param nRow    number of rows of this block
	 * @param nCol    number of columns of this block
	 * @param blockId value of the id attribute to give elements in this block
	 * @throws XMLStreamException
	 */
	private void annotateHeatMapBlock(int nRow, int nCol, String blockId, XMLEventWriter writer, XMLEventReader reader)
			throws XMLStreamException
	{
		int counter = 0;
		int nPath = nRow * nCol;
		int currentRow = nRow; // elements drawn from bottom to top, so start counting at last row
		int currentCol = 1;
		while (counter < nPath)
		{
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(PATH))
			{
				// make a new start element with the same attributes plus the extra annotations
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();

				StartElement newSe = eventFactory.createStartElement(new QName(PATH), attributes, null);
				writer.add(newSe);
				writer.add(eventFactory.createAttribute(ID, blockId));
				writer.add(eventFactory.createAttribute(new QName("row"), Integer.toString(currentRow)));
				writer.add(eventFactory.createAttribute(new QName("col"), Integer.toString(currentCol)));

				currentRow--;
				if (currentRow == 0)
				{
					// finished one column, reset currentRow and increment currentCol
					currentRow = nRow;
					currentCol++;
				}
				counter++;
			}
			else
			{
				// write the rest untouched
				writer.add(event);
			}
		}
	}

	/**
	 * Can annotate SVG heatmap.plus charts made by R. Reads and writes using StAX, adding row and col attributes to
	 * <path> elements corresponding to data points in the heatmap. All indexes can be calculated using nRow, nCol,
	 * nRowAnnotations and nColAnnotations.
	 *
	 * @param chart
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public void annotateHeatMap(HeatMapChart chart)
			throws XMLStreamException, FactoryConfigurationError, FileNotFoundException
	{
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(inFile));

		OutputStream os = new FileOutputStream(outFile);
		XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(os);

		// get values from HeatMapChart
		int nRow = chart.getData().getRowTargets().size();
		int nCol = chart.getData().getColumnTargets().size();

		// TODO get from HeatMapChart:
		int nRowAnnotations = 0;
		int nColAnnotations = 0;

		// skip the headers and <def> bit until we reach <g id="">
		while (true)
		{
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isStartElement())
			{
				StartElement se = event.asStartElement();
				if (se.getName().getLocalPart().equals(G) && se.getAttributeByName(ID) != null)
				{
					LOG.info("<g id=\"\"> reached");
					writer.add(event);
					break;
				}
			}
			writer.add(event);
		}

		// annotation begins here
		// ROW ANNOTATIONS
		if (nRowAnnotations > 0)
		{
			LOG.info("parsing " + nRowAnnotations + " row annotations");
			annotateHeatMapBlock(nRow, nRowAnnotations, "rowAnnotation", writer, reader);
		}

		// COLUMN ANNOTATIONS
		if (nColAnnotations > 0)
		{
			LOG.info("parsing " + nColAnnotations + " col annotations");
			annotateHeatMapBlock(nColAnnotations, nCol, "colAnnotatation", writer, reader);
		}

		// MATRIX ANNOTATIONS
		LOG.info("parsing " + (nRow * nCol) + " matrix values");
		annotateHeatMapBlock(nRow, nCol, "matrix", writer, reader);

		// COLUMN NAMES
		LOG.info("parsing " + nCol + " column names");
		int counter = 0;
		while (counter < nCol)
		{
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(G))
			{

				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();

				StartElement newSe = eventFactory.createStartElement(new QName(G), attributes, null);
				writer.add(newSe);
				writer.add(eventFactory.createAttribute(ID, "colName"));
				writer.add(eventFactory.createAttribute(new QName("col"), Integer.toString(counter + 1)));

				counter++;
			}
			else
			{
				writer.add(event);
			}
		}

		// ROW NAMES
		LOG.info("parsing " + nRow + " row names");
		counter = 0;
		while (counter < nRow)
		{
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(G))
			{

				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();

				StartElement newSe = eventFactory.createStartElement(new QName(G), attributes, null);
				writer.add(newSe);
				writer.add(eventFactory.createAttribute(ID, "rowName"));
				writer.add(eventFactory.createAttribute(new QName("row"), Integer.toString(nRow - counter)));
				counter++;

			}
			else
			{
				writer.add(event);
			}
		}

		// finish rest of file
		while (reader.hasNext())
		{
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isEndElement())
			{
				// close the <g id=""> tag, right before the </svg> end element
				if (event.asEndElement().getName().getLocalPart().equals("svg"))
				{
					EndElement newEe = eventFactory.createEndElement(new QName(G), null);
					writer.add(newEe);
				}
			}
			writer.add(event);
		}

		writer.close();
	}

}
