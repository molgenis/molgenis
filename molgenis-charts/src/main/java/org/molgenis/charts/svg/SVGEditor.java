package org.molgenis.charts.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.r.RChartService;

public class SVGEditor {

	private static final Logger logger = Logger.getLogger(SVGEditor.class);
	
	XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
	
	static final String G = "g";
	static final String PATH = "path";
	static final QName ID = new QName("id");
	
	private XMLEventReader reader;
	private OutputStream os;
	private XMLEventWriter writer;
	
	/** Creates a new instance of SVGEditor 
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException */
	public SVGEditor(File inFile, File outFile) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError{
		reader = XMLInputFactory.newInstance().createXMLEventReader(
                new java.io.FileInputStream(inFile));
		
	    os = new FileOutputStream(outFile);
	    writer = XMLOutputFactory.newInstance().createXMLEventWriter(os);
	}
	
	/**
	 * Annotates a block of values of a heatmap.plus SVG.
	 * A heatmap.plus can contain up to three 'blocks': row annotations, column annotations and the matrix.
	 * These blocks are drawn the same way.
	 * @throws XMLStreamException 
	 */
	private void annotateHeatMapBlock(int nRow, int nCol, String blockType) throws XMLStreamException{
		int counter = 0;
		int nPath = nRow * nCol;
		int currentRow = nRow;
		int currentCol = 1;
		while (counter < nPath){
			XMLEvent event = (XMLEvent) reader.next();
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(PATH)){
				// change element 
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();
				
				StartElement newSe = m_eventFactory.createStartElement(new QName(PATH), attributes, null);
				writer.add(newSe);
				writer.add(m_eventFactory.createAttribute(ID, blockType));
				writer.add(m_eventFactory.createAttribute(new QName("row"), Integer.toString(currentRow)));
				writer.add(m_eventFactory.createAttribute(new QName("col"), Integer.toString(currentCol)));
			
				currentRow--;
				if (currentRow == 0){
					currentRow = nRow;
					currentCol++;
				}
				counter++;
			}else{
				writer.add(event);
			}
		}
	}
	
	
	/**
	 * Can annotate SVG heatmap.plus charts made by R. Reads and writes using StAX, adding 
	 * row and col attributes to <path> elements corresponding to data points in the heatmap. 
	 * All indexes can be calculated using nRow, nCol, nRowAnnotations and nColAnnotations.
	 * @param chart 
	 */
	public void annotateHeatMap(HeatMapChart chart){
 
        // get values from HeatMapChart
		int nRow = chart.getData().getRowTargets().size();
		int nCol = chart.getData().getColumnTargets().size();
		
		//TODO get from HeatMapChart:
		int nRowAnnotations = 0;
		int nColAnnotations = 0;
		
		try {
        	// skip the headers and <def> bit until we reach <g id="">
            while (true){
            	XMLEvent event = (XMLEvent) reader.next();
            	if (event.isStartElement()){
            		StartElement se = event.asStartElement();
	            	if (se.getName().getLocalPart().equals(G) && se.getAttributeByName(ID) != null){
            			System.out.println("INFO: <g id=\"\"> reached");
            			writer.add(event);
            			break;
	            	}
            	}
            	writer.add(event);
            }
            
            // annotation begins here 
            
            // ROW ANNOTATIONS
    		if (nRowAnnotations > 0){
    			System.out.println("parsing row annotations");
    			annotateHeatMapBlock(nRow, nRowAnnotations, "rowAnnotation");
    		}
    		
    		// COLUMN ANNOTATIONS
    		if (nColAnnotations > 0){
    			System.out.println("parsing col annotations");
    			annotateHeatMapBlock(nColAnnotations, nCol, "colAnnotatation");
    		}
    		
    		// MATRIX ANNOTATIONS
    		System.out.println("parsing matrix");
    		annotateHeatMapBlock(nRow, nCol, "matrix");
    		
    		// COLUMN NAMES
    		System.out.println("parsing column names");
    		int counter = 0;
    		while (counter < nCol){
    			XMLEvent event = (XMLEvent) reader.next();
    			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(G)){
    				
    				@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = event.asStartElement().getAttributes();
    				
    				StartElement newSe = m_eventFactory.createStartElement(new QName(G), attributes, null);
    				writer.add(newSe);
    				writer.add(m_eventFactory.createAttribute(ID, "colName"));
    				writer.add(m_eventFactory.createAttribute(new QName("col"), Integer.toString(counter+1)));
    				
    				
    				counter++;
    			}else{
    				writer.add(event);
    			}
    		}
    		
    		// ROW NAMES
    		System.out.println("parsing row names");
    		counter = 0;
    		while (counter < nRow){
    			XMLEvent event = (XMLEvent) reader.next();
    			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(G)){
    				
    				@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = event.asStartElement().getAttributes();
    				
    				StartElement newSe = m_eventFactory.createStartElement(new QName(G), attributes, null);
    				writer.add(newSe);
    				writer.add(m_eventFactory.createAttribute(ID, "rowName"));
    				writer.add(m_eventFactory.createAttribute(new QName("row"), Integer.toString(nRow-counter)));
    				counter++;
    			}else{
    				writer.add(event);
    			}
    		}
    		
    		// finish rest of file
            while (reader.hasNext()){	
            	XMLEvent event = (XMLEvent) reader.next();
            	if (event.isEndElement()){
            		// close the <g id=""> tag, right before the </svg> end element
            		if (event.asEndElement().getName().getLocalPart().equals(new QName("svg"))){
            			EndElement newEe = m_eventFactory.createEndElement(new QName(G), null);
                		writer.add(newEe);
            		}
            	}
            	writer.add(event);
            }

            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

	}

}
