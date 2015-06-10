/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.repository.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.model.InternalErrorException;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.tridas.TridasNamespacePrefixMapper;

import org.apache.log4j.Logger;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasVocabulary;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TriDaS XML is an external file format that we need to convert to and from the DCCD Project.
 *
 */
public class XMLFilesRepositoryService //implements DccdRepositoryService 
{
	public static final String TRIDAS_XML_CHARSET = "UTF-8";// maybe even on a global level?
	private static Logger logger = Logger.getLogger(XMLFilesRepositoryService.class);

	public static void validateAgainstTridasSchema(final Project project) throws TridasValidationException
	{
		// initialize validation, Schema validation in JAXB 2.0 is performed using JAXP 1.3
		Schema schema = null;
		try 
		{
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			// Note: couldn't get ClassLoader.getSystemResource working on the server
			// resource loading probably has to do with the classpath...
			URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("tridas.xsd");
			schema = sf.newSchema(schemaUrl);
		} 
		catch (IllegalArgumentException e) 
		{
			// this is an internal error, the schema should be available!
			logger.error("Could not initialize schema validation");
			throw (new InternalErrorException(e));
		} 
		catch (SAXException e) 
		{
			// this is an internal error, the schema should be available!
			logger.error("Could not initialize schema validation");
			throw (new InternalErrorException(e));
		}

		try
		{
    		JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, TRIDAS_XML_CHARSET);
			// improve readability
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			// change the namespace mapping
			// changed with Java6 ? "com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper"
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TridasNamespacePrefixMapper());
            
			marshaller.marshal(project.getTridasTridas(), new DefaultHandler()); // no output!
		}
		catch (JAXBException e)
		{
			//logger.error("JAXB could not marshal the project, Not conform the TRiDaS Standard");//, e);
			//
			//e.printStackTrace();
			Throwable linkedException = e.getLinkedException();
			
			// Note: throwing an exception here is not 'good practice', 
			// because when validating we can expect invalidness. 
			// But this was the simples way to handle it.
			throw new TridasValidationException("Not conform the TRiDaS Standard", 
					linkedException!=null?linkedException:e);
		}
	}
	
	// Maybe add to the common file utils, getSafeName?
	public static String constructTridasFilename(Project project)
	{
		final String TRIDAS_FILENAME_END = "_tridas.xml";// extension
		
		return nl.knaw.dans.dccd.util.FileUtil.getSaveFilename(project.getTitle() + TRIDAS_FILENAME_END);
	}
	
	public static void saveToTridasXML(File file, Project project) throws TridasSaveException 
	{
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(file);

			JAXBContext jaxbContext = null;
			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, TRIDAS_XML_CHARSET);
			// improve readability
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			// change the namespace mapping
			// changed with Java6 ? "com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper"
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TridasNamespacePrefixMapper());

			//marshaller.marshal(project.getTridas(), fos);
			marshaller.marshal(project.getTridasTridas(), fos); // always the Tridas as root element
			
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			throw new TridasSaveException("Could not save to file: " + file.getName(), e);
		}
		catch (JAXBException e)
		{
			throw new TridasSaveException("Could not save to file: " + file.getName(), e);
		}
		catch (IOException e)
		{
			throw new TridasSaveException("Could not save to file: " + file.getName(), e);
		}
		
	}
	
	// class for filtering xml files from folder listing
	class XMLFilter implements FilenameFilter {
	    public boolean accept(File dir, String name) {
	    	// assume xml files have (lowercase) extension xml
	        return (name.endsWith(".xml"));
	    }
	}
	
	// get the dendroProject from a given stream of TRiDaS XML data
	// Note: maybe make this part of a DCCDXMLFile utility class?
	//
	// Note: maybe have an non-validating version for better performance?
	// Note: throw exceptions instead of returning null when something goes wrong?
	public static Project getDendroProjectFromTridasXML(InputStream is) throws TridasLoadException {
		Project project = null;

		// initialize validation, Schema validation in JAXB 2.0 is performed using JAXP 1.3
		Schema schema = null;
		try {
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			//schema = sf.newSchema(ClassLoader.getSystemResource("tridas.xsd"));
			// Note: couldn't get this working on the server
			// resource loading probably has to do with the classpath...
			//
			// ? http://www.tridas.org/1.2/tridas.xsd
			//URL schemaUrl = new URL("http://www.tridas.org/1.2/tridas.xsd");
			// however, now our code fails if the tridas server is down ;-)
			//URL schemaUrl = ClassLoader.getSystemResource("tridas.xsd");
			//URL schemaUrl = XMLFilesRepositoryService.class.getClassLoader().getResource("tridas.xsd");
			URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("tridas.xsd");

			schema = sf.newSchema(schemaUrl);
		} catch (IllegalArgumentException e) {
			logger.error("Could not initialize schema validation");//, e);
			throw (new InternalErrorException(e));
		} catch (SAXException e) {
			// this is an internal error, the schema should be available!
			logger.error("Could not initialize schema validation");//, e);
			throw (new InternalErrorException(e));
//		} catch (MalformedURLException e) {
//			logger.error("Could not initialize schema validation");//, e);
//			throw (new DccdInternalErrorException(e));
		}

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			// Note: with our own event handler we could give more detailed information!
			unmarshaller.setEventHandler(new ValidationEventHandler() {
			      public boolean handleEvent(ValidationEvent ve) {
			        if (ve.getSeverity()==ValidationEvent.FATAL_ERROR ||
			            ve.getSeverity()==ValidationEvent.ERROR){
			            ValidationEventLocator  locator = ve.getLocator();
			            //print message from valdation event, output line and column number
			            logger.error("Validation error" +
			            			" at line number " + locator.getLineNumber() + " column " +
			            			locator.getColumnNumber() +
			            			", Message is [" + ve.getMessage() + "]");
			         }
			         return false; // terminate operation;
			       }
			   });

			Object o = unmarshaller.unmarshal(is);

			TridasProject tridasProject = null;

			logger.info("Unmarshalled object of class: " + o.getClass().getName());

			if ( o.getClass() == org.tridas.schema.TridasProject.class) {
				tridasProject = (TridasProject)o; // assume it's a XmlRootElement
			} else {
				logger.info("Wrapping tridas root element!");
				// try a wrapping Tridas root element
				TridasTridas tridasTridas = (TridasTridas)o;
				List<TridasProject> tridasProjectList = tridasTridas.getProjects();
				// Only one project per TRiDaS file allowed by DCCD
				if (tridasProjectList.size() == 1) {
					// OK, we have a single project
					tridasProject = tridasProjectList.get(0);
					logger.info("Using single project inside tridas root element");
				} else {
					// Wrong, more projects are not allowed in DCCD
					logger.error("More than one project in TRiDaS file, ignoring all!");
					throw new TridasLoadException("More than one project in TRiDaS file");
				}
			}

			if (tridasProject != null) {
				// Yes we can!
				logger.info("tridas project title: " + tridasProject.getTitle());
				//project = new Project();
				project = DccdDataService.getService().createProject();

				project.setTridas(tridasProject);
				project.setTitle(tridasProject.getTitle());
			}
		} catch (JAXBException e) {
			// Maybe it wasn't valid XML or not valid TRiDaS?
			// Would like to get more specific error information
			// If we could get the linked SAXParseException...
			//
			//Throwable le = e.getLinkedException();
			//
			// Maybe we need to use a custom ValidationEventHandler?

			logger.error("JAXB could not unmarshal input stream, Not conform the TRiDaS Standard");//, e);
			throw new TridasLoadException("Not conform the TRiDaS Standard", e);
		}
		catch (DataServiceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return project;
	}


	/**
	 * Create a proper Project with creation metadata because we have the User id
	 *
	 * @param is
	 * @param userId
	 * @return
	 * @throws TridasLoadException
	 */
	public static Project createDendroProjectFromTridasXML(InputStream is, String userId) throws TridasLoadException
	{
//		TridasProject tridasProject = getTridasProjectFromXML(is);
		Object object = getTridasAsObjectFromXML(is);
		TridasProject tridasProject = getTridasProjectFromXMLObject(object);
		
		// also get the vocabulary if it has one
		TridasVocabulary vocabulary = getTridasVocabularyFromXMLObject(object);
		
		Project project = null;
		try
		{
			project = DccdDataService.getService().createProject(userId);

			project.setTridas(tridasProject);
			project.setTitle(tridasProject.getTitle());
			
			// add vocabulary
			if (vocabulary != null)
			{
				//logger.debug(">>>>>>>>>>>>>Vocabulary>>>>>>>>>>>");
				project.setTridasVocabulary(vocabulary);
			}
		}
		catch (DataServiceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return project;
	}

	private static Object getTridasAsObjectFromXML(InputStream is) throws TridasLoadException
	{
		Object object = null;
		
		// initialize validation, Schema validation in JAXB 2.0 is performed using JAXP 1.3
		Schema schema = null;
		try
		{
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			//schema = sf.newSchema(ClassLoader.getSystemResource("tridas.xsd"));
			// Note: couldn't get this working on the server
			// resource loading probably has to do with the classpath...
		    //
			//Changed to version 1.2.1
			//URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("tridas.xsd");
			//URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("tridas-1.2.1.xsd");
			//Changed to version 1.2.2
			URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("tridas-1.2.2.xsd");

			schema = sf.newSchema(schemaUrl);
		}
		catch (IllegalArgumentException e)
		{
			logger.error("Could not initialize schema validation");//, e);
			throw (new InternalErrorException(e));
		}
		catch (SAXException e)
		{
			// this is an internal error, the schema should be available!
			logger.error("Could not initialize schema validation");//, e);
			throw (new InternalErrorException(e));
		}

		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			// Note: with our own event handler we could give more detailed information!
			unmarshaller.setEventHandler(new ValidationEventHandler()
			{
			      public boolean handleEvent(ValidationEvent ve)
			      {
			        if (ve.getSeverity()==ValidationEvent.FATAL_ERROR ||
			            ve.getSeverity()==ValidationEvent.ERROR)
			        {
			            ValidationEventLocator  locator = ve.getLocator();
			            
			            //print message from valdation event, output line and column number
			            logger.error("Validation error" +
			            			" at line number " + locator.getLineNumber() + " column " +
			            			locator.getColumnNumber() +
			            			", Message is [" + ve.getMessage() + "]");
			         }
			         return false; // terminate operation;
			       }
			});

			object = unmarshaller.unmarshal(is);
			logger.info("Unmarshalled object of class: " + object.getClass().getName());
		}
		catch (JAXBException e)
		{
			// Maybe it wasn't valid XML or not valid TRiDaS?
			// Would like to get more specific error information
			// If we could get the linked SAXParseException...
			//
			Throwable le = e.getLinkedException();
			logger.error("Related: " + le.getMessage());
			//
			// Maybe we need to use a custom ValidationEventHandler?

			logger.error("JAXB could not unmarshal input stream, Not conform the TRiDaS Standard");//, e);
			throw new TridasLoadException("Not conform the TRiDaS Standard", e);
		}
		
		return object;
	}

	private static TridasVocabulary getTridasVocabularyFromXMLObject(Object object)// throws TridasLoadException
	{
		TridasVocabulary tridasVocabulary = null;

		if ( object.getClass() == org.tridas.schema.TridasTridas.class)
		{
			logger.info("Wrapping tridas root element!");
			// try a wrapping Tridas root element
			TridasTridas tridasTridas = (TridasTridas)object;

			if (tridasTridas.isSetVocabulary())
			{
				tridasVocabulary = tridasTridas.getVocabulary();
				logger.info("found a vocabulary inside tridas root element");
			}
		}
	
		return tridasVocabulary;
	}

	private static TridasProject getTridasProjectFromXMLObject(Object object) throws TridasLoadException
	{
		TridasProject tridasProject = null;

		if ( object.getClass() == org.tridas.schema.TridasProject.class)
		{
			tridasProject = (TridasProject)object; // assume it's a XmlRootElement
		}
		else
		{
			logger.info("Wrapping tridas root element!");
			// try a wrapping Tridas root element
			TridasTridas tridasTridas = (TridasTridas)object;
			List<TridasProject> tridasProjectList = tridasTridas.getProjects();
			// Only one project per TRiDaS file allowed by DCCD
			if (tridasProjectList.size() == 1)
			{
				// OK, we have a single project
				tridasProject = tridasProjectList.get(0);
				logger.info("Using single project inside tridas root element");
			}
			else
			{
				// Wrong, more projects are not allowed in DCCD
				logger.error("More than one project in TRiDaS file, ignoring all!");
				throw new TridasLoadException("More than one project in TRiDaS file");
			}
		}
		return tridasProject;		
	}
	
	/**
	 *
	 * @param is
	 * @return
	 * @throws TridasLoadException
	 */
	private static TridasProject getTridasProjectFromXML(InputStream is) throws TridasLoadException
	{
		TridasProject tridasProject = null;

		Object o = getTridasAsObjectFromXML(is);
			

		if ( o.getClass() == org.tridas.schema.TridasProject.class)
		{
			tridasProject = (TridasProject)o; // assume it's a XmlRootElement
		}
		else
		{
			logger.info("Wrapping tridas root element!");
			// try a wrapping Tridas root element
			TridasTridas tridasTridas = (TridasTridas)o;
			List<TridasProject> tridasProjectList = tridasTridas.getProjects();
			// Only one project per TRiDaS file allowed by DCCD
			if (tridasProjectList.size() == 1)
			{
				// OK, we have a single project
				tridasProject = tridasProjectList.get(0);
				logger.info("Using single project inside tridas root element");
			}
			else
			{
				// Wrong, more projects are not allowed in DCCD
				logger.error("More than one project in TRiDaS file, ignoring all!");
				throw new TridasLoadException("More than one project in TRiDaS file");
			}
		}
		return tridasProject;
	}


}
