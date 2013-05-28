package org.molgenis.lifelines.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.hl7.ANY;
import org.molgenis.hl7.BL;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.INT;
import org.molgenis.hl7.PQ;
import org.molgenis.hl7.REAL;
import org.molgenis.hl7.REPCMT000100UV01Component3;
import org.molgenis.hl7.REPCMT000100UV01Observation;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.hl7.REPCMT000400UV01ActCategory;
import org.molgenis.hl7.REPCMT000400UV01Component4;
import org.molgenis.hl7.ST;
import org.molgenis.hl7.TS;
import org.molgenis.lifelines.catalogue.CatalogIdConverter;
import org.molgenis.lifelines.hl7.jaxb.QualityMeasureDocument;
import org.molgenis.lifelines.resourcemanager.ResourceManagerService;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataQueryService
{
	private static final Logger logger = Logger.getLogger(ResourceManagerService.class);
	private final Database database;
	private final String dataQueryServiceUrl;

	@Autowired
	public DataQueryService(Database database, @Value("${lifelines.data.query.service.url}")
	String dataQueryServiceUrl)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		if (dataQueryServiceUrl == null) throw new IllegalArgumentException("DataQueryServiceUrl is null");
		this.database = database;
		this.dataQueryServiceUrl = dataQueryServiceUrl;
	}

	public void loadStudyDefinitionData(QualityMeasureDocument studyDefinition)
	{
		HttpURLConnection urlConnection = null;
		OutputStream outStream = null;
		InputStream inStream = null;
		try
		{
			database.beginTx();

			JAXBContext jaxbContext = JAXBContext.newInstance(REPCMT000100UV01Organizer.class,
					QualityMeasureDocument.class);

			Marshaller eenMarshaller = jaxbContext.createMarshaller();
			eenMarshaller.marshal(studyDefinition, System.out);

			URL url = new URL(dataQueryServiceUrl + "/data");
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/xml");

			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);

			outStream = urlConnection.getOutputStream();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(studyDefinition, outStream);
			outStream.flush();

			// convert to HL7 organizer
			Unmarshaller um = jaxbContext.createUnmarshaller();
			REPCMT000400UV01ActCategory actCategory = um.unmarshal(new StreamSource(urlConnection.getInputStream()),
					REPCMT000400UV01ActCategory.class).getValue();

			String id = studyDefinition.getId().getExtension();
			DataSet dataSet = database.query(DataSet.class)
					.eq(DataSet.IDENTIFIER, CatalogIdConverter.catalogIdToOmxIdentifier(id)).find().get(0);

			for (REPCMT000400UV01Component4 rootComponent : actCategory.getComponent())
			{
				// create observation set
				ObservationSet observationSet = new ObservationSet();
				observationSet.setPartOfDataSet(dataSet);

				REPCMT000100UV01Organizer organizer = rootComponent.getOrganizer().getValue();
				// COCTMT050000UV01Patient patient = organizer.getRecordTarget().getValue().getPatient().getValue();
				// JAXBElement<?> postalCodeSerializable = (JAXBElement<?>)
				// patient.getAddr().get(0).getContent().get(0);
				// if (postalCodeSerializable.getDeclaredType().equals(AdxpPostalCode.class))
				// {
				// AdxpPostalCode postalCode = (AdxpPostalCode) postalCodeSerializable;
				// postalCodez
				// }

				// create other features and values
				for (REPCMT000100UV01Component3 organizerComponent : organizer.getComponent())
				{
					REPCMT000100UV01Observation observation = organizerComponent.getObservation().getValue();
					String featureId = observation.getId().get(0).getRoot();
					ObservableFeature observableFeature = ObservableFeature.findByIdentifier(database, featureId);
					if (observableFeature == null) throw new RuntimeException(
							"missing ObservableFeature with identifier " + featureId);

					ObservedValue observedValue = new ObservedValue();
					observedValue.setObservationSet(observationSet);
					observedValue.setFeature(observableFeature);
					observedValue.setValue(toValue(observation.getValue()));

					database.add(observedValue);
				}
				database.add(observationSet);
			}
			database.commitTx();
		}
		catch (IOException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
			}
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (JAXBException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
			}
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (DatabaseException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
			}
			logger.error(e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (inStream != null) IOUtils.closeQuietly(inStream);
			if (outStream != null) IOUtils.closeQuietly(outStream);
			if (urlConnection != null) urlConnection.disconnect();
		}
	}

	private String toValue(ANY anyValue) throws DatabaseException
	{
		if (anyValue instanceof INT)
		{
			// integer
			INT value = (INT) anyValue;
			return value.getValue().toString();
		}
		else if (anyValue instanceof ST)
		{
			// string
			ST value = (ST) anyValue;
			return value.getRepresentation().value();
		}
		else if (anyValue instanceof PQ)
		{
			// physical quantity
			PQ value = (PQ) anyValue;
			return value.getValue();
		}
		else if (anyValue instanceof TS)
		{
			// time
			TS value = (TS) anyValue;
			return value.getValue();
		}
		else if (anyValue instanceof REAL)
		{
			// fractional number
			REAL value = (REAL) anyValue;
			return value.getValue();
		}
		else if (anyValue instanceof BL)
		{
			// boolean
			BL value = (BL) anyValue;
			return value.isValue().toString();
		}
		else if (anyValue instanceof CD) // for CD and CO values
		{
			// boolean
			CD value = (CD) anyValue;
			// TODO to common class
			Category category = Category.findByIdentifier(database, value.getCodeSystem() + '.' + value.getCode());
			return category != null ? category.getName() : "NEED TO FIX THIS"; // FIXME grote hackaton!
		}

		throw new UnsupportedOperationException("ANY instance not supported: " + anyValue.getClass());
	}
}