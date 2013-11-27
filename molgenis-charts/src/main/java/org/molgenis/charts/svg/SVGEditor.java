package org.molgenis.charts.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import javax.xml.parsers.*;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.r.RChartService;

public class SVGEditor {

	private static final Logger logger = Logger.getLogger(SVGEditor.class);
	
	public SVGEditor(){
		
	}

	public static void annotateHeatMap(HeatMapChart chart, File f){
		try{
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
		    XMLEventReader eventReader = inFactory.createXMLEventReader(new FileInputStream(f));
		    XMLOutputFactory factory = XMLOutputFactory.newInstance();
		    XMLEventWriter writer = factory.createXMLEventWriter(new FileWriter(f));
		    XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		    while (eventReader.hasNext()) {
		        XMLEvent event = eventReader.nextEvent();
		        writer.add(event);
		        if (event.getEventType() == XMLEvent.START_ELEMENT) {
		            if (event.asStartElement().getName().toString().equalsIgnoreCase("book")) {
		                writer.add(eventFactory.createStartElement("", null, "index"));
		                writer.add(eventFactory.createEndElement("", null, "index"));
		            }
		        }
		    }
		    writer.close();
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
