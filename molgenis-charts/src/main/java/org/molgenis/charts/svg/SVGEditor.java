package org.molgenis.charts.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
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
	
	/** Creates a new instance of SVGEditor */
	public SVGEditor(){
	}

	
	public static void annotateHeatMap(HeatMapChart chart, File input, File output){
		
	}
	
	public static void main(String[] args) {

		try {
            SVGEditor ms = new SVGEditor();

            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(
                        new java.io.FileInputStream("/Users/tommydeboer/test3.svg"));
            OutputStream os = new FileOutputStream("/Users/tommydeboer/test2.svg");
            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(os);

               
        	// skip the headers and <def> bit until we reach <g id="">
            while (true){
            	XMLEvent event = (XMLEvent) reader.next();
            	if (event.getEventType() == event.START_ELEMENT){
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
            // get values from HeatMapChart
    		int nRow = 50;
    		int nCol = 50;
    		
    		int nRowAnnotations = 2;
    		int nColAnnotations = 2;
            
            // ROW ANNOTATIONS
    		if (nRowAnnotations > 0){
    			System.out.println("parsing row annotations");
    			int nPath = nRowAnnotations * nRow;
    			int counter = 0;
    			while(counter < nPath){
    				XMLEvent event = (XMLEvent) reader.next();
    				if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(PATH)){
    					// annotate path elements
    					writer.add(event);
    					counter++;
    				}else{    				
    					writer.add(event);
    				}
    			}
    		}
    		
    		// COL ANNOTATIONS
    		if (nColAnnotations > 0){
    			System.out.println("parsing col annotations");
    			int nPath = nColAnnotations * nCol;
    			int counter = 0;
    			while(counter < nPath){
    				XMLEvent event = (XMLEvent) reader.next();
    				if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(PATH)){
    					// annotate path elements
    					writer.add(event);
    					counter++;
    				}else{
    					writer.add(event);
    				}
    			}
    		}
    		
    		// matrix annotations
    		int counter = 0;
    		int nPath = nRow * nCol;
    		while (counter < nPath){
    			XMLEvent event = (XMLEvent) reader.next();
    			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(PATH)){
    				// change element 
    				Iterator<Attribute> attributes = event.asStartElement().getAttributes();
    				
    				StartElement newSe = ms.m_eventFactory.createStartElement(new QName(PATH), attributes, null);
    				writer.add(newSe);
					writer.add(ms.m_eventFactory.createAttribute(ID, "matrix"));
					counter++;
				}else{
					writer.add(event);
				}
    		}
    		
    		// finish rest of file
            while (reader.hasNext()){	
            	XMLEvent event = (XMLEvent) reader.next();
            	if (event.isEndElement()){
            		// close the <g id="surface1"> tag.
            		if (event.asEndElement().getName().getLocalPart().equals(new QName("svg"))){
            			EndElement newEe = ms.m_eventFactory.createEndElement(new QName(G), null);
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
	
	/** New Character event (with text containing current time) is created using XMLEventFactory in case the
     *  Characters event passed matches the criteria.
     *
     *  @param Characters Current character event.
     *  return Characters New Characters event.
     */
    private Characters getNewCharactersEvent(Characters event) {
        if (event.getData()
                     .equalsIgnoreCase("Yogasana Vijnana: the Science of Yoga")) {
            return m_eventFactory.createCharacters(
                    Calendar.getInstance().getTime().toString());
        }
        //else return the same event
        else {
            return event;
        }
    }

}
