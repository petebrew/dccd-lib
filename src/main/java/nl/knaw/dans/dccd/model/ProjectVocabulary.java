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
package nl.knaw.dans.dccd.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;

import nl.knaw.dans.common.lang.repo.MetadataUnit;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.dccd.model.InternalErrorException;
import nl.knaw.dans.dccd.search.DccdSB;
import nl.knaw.dans.dccd.tridas.TridasNamespacePrefixMapper;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;

import org.tridas.schema.TridasVocabulary;

/**
 * The Vocabulary stored with this Project TRiDaS
 * TridasTridas has a list of project (DCCD only supports one) and a Vocabulary.
 * It must be stored and retrieved from the repository
 * This is similar to the entities, but does not have sub-entities and is also non-searchable 
 * , no permissions and also no title and id etc.
 * 
 * @see nl.knaw.dans.dccd.model.entities.Entity
 * @author paulboon
 */
public class ProjectVocabulary implements MetadataUnit, Serializable
{
	private static final long	serialVersionUID	= 6029999409897728869L;
	private static Logger logger = Logger.getLogger(ProjectVocabulary.class);
	private TridasVocabulary vocabulary = null;
	public final static String VOCABULARY_ID = "VOCTF";
	
	/* JAXB related */
	private static JAXBContext jaxbContext = null;
	private static Marshaller marshaller = null;

	// keeps the result of the (archiving) validation
	private boolean validForArchiving = true;

	public boolean isValidForArchiving()
	{
		return this.validForArchiving;
	}

	public void setValidForArchiving(boolean valid)
	{
		this.validForArchiving = valid;
	}

	/**
	 * For producing TRiDaS xml fragments
	 * 
	 * @return The Marshaller
	 * @throws JAXBException
	 */
	private static Marshaller getJAXBmarshaller() throws JAXBException
	{
		if (marshaller == null)
		{
			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// improve readability
			// change the namespace mapping
			// changed with Java6 ? "com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper"
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TridasNamespacePrefixMapper());
		}
		
		return marshaller;
	}
	
	/**
	 * default implementation adds nothing to the bean
	 */
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		return searchBean;
	}

	public ProjectVocabulary()
	{
		// empty
	}

	public boolean hasTridas()
	{
		return (null != getTridasAsObject());
	}

	public Object getTridasAsObject()
	{
		return vocabulary;
	}

	public void setTridasObject(Object tridas)
	{
		vocabulary = (TridasVocabulary)tridas;
	}

	public Class getTridasClass() 
	{
		return TridasVocabulary.class;
	}

	/**
	 * Create a clone of the given tridas object Useful when creating pruned versions of a tridas object Note: using JAXB to clone is a trick that might be
	 * suboptimal! Note: could also implement a version that uses a given instantiated JAXBContext or use a static JAXBContext?
	 * 
	 * @see nl.knaw.dans.dccd.model.entities.AbstractEntity#pruneTridas(Object)
	 * @param o
	 *        The tridas object to clone
	 * @return The clone
	 */
	protected Object cloneTridas(Object o)
	{
		JAXBContext jaxbContext = null;
		Object result = null;
		try
		{
			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			result = unmarshaller.unmarshal(new JAXBSource(jaxbContext, o));
		}
		catch (JAXBException e)
		{
			// e.printStackTrace();
			// coding error?
			throw new InternalErrorException(e);
		}
		return result;
	}

	public String getXMLString()
	{		
		try
		{
			return asXMLString();
		}
		catch (XMLSerializationException e)
		{
			throw new InternalErrorException(e);
		}
	}

	/* === MetadataUnit implementation code below === */

	// ENTITY_ID_PREFIX
	String			UNIT_ID			= VOCABULARY_ID;
	String			UNIT_LABEL		= "tridasvocabulary";
	String			UNIT_FORMAT		= "http://dans.knaw.nl/dccd/tridasvocabulary";
	URI				UNIT_FORMAT_URI	= URI.create(UNIT_FORMAT);
	private boolean	dirty;//=true;
	private int     originalHash;

	public String getUnitFormat()
	{
		return UNIT_FORMAT;
	}

	public URI getUnitFormatURI()
	{
		return UNIT_FORMAT_URI;
	}

	public String getUnitId()
	{
		return UNIT_ID;
	}

	public String getUnitLabel()
	{
		return UNIT_LABEL;
	}

	public boolean isVersionable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setVersionable(boolean versionable)
	{
		// TODO Auto-generated method stub
	}

	public Document asDocument() throws XMLSerializationException
	{
		String xmlString = asXMLString();
		Document xmlDocument;
		try
		{
			xmlDocument = DocumentHelper.parseText(xmlString);
		}
		catch (DocumentException e)
		{
			throw new XMLSerializationException(e);
		}

		return xmlDocument;
	}

	// Would be nice if we can go from JAXB to dom4j using the JAXB marshaller
	//
	public Element asElement() throws XMLSerializationException
	{
		String xmlString = asXMLString();
		Document xmlDocument;
		Element xmlElement;
		try
		{
			xmlDocument = DocumentHelper.parseText(xmlString);
			xmlElement = xmlDocument.getRootElement();
		}
		catch (DocumentException e)
		{
			throw new XMLSerializationException(e);
		}

		return xmlElement;
	}

	public byte[] asObjectXML() throws XMLSerializationException
	{
		String xmlStr = asXMLString();

		try
		{
			return xmlStr.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new XMLSerializationException(e);
		}
	}

	public Source asSource() throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream asXMLInputStream() throws XMLSerializationException
	{
		InputStream is = new ByteArrayInputStream(asObjectXML());// asXMLString().getBytes("UTF-8");

		return is;
	}

	public InputStream asXMLInputStream(int indent) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	// Marshalls it to an xml fragment
	public String asXMLString() throws XMLSerializationException
	{
		String result = "";
		Object tridasObject = getTridasAsObject();
		if (tridasObject == null)
			return result; // nothing to do, warn?

		try
		{
			java.io.StringWriter sw = new StringWriter();
			getJAXBmarshaller().marshal(tridasObject, sw);

			// System.out.print(sw.toString());
			result = sw.toString();
		}
		catch (JAXBException e)
		{
			throw new XMLSerializationException(e);
		}
		return result;
	}

	public String asXMLString(int indent) throws XMLSerializationException
	{
		Element element = asElement();
		OutputFormat format = OutputFormat.createPrettyPrint();
		// use an XMLWriter
		StringWriter out = new StringWriter();
		XMLWriter writer = new XMLWriter(out, format);
		writer.setIndentLevel(indent);

		try
		{
			writer.write(element);
		}
		catch (IOException e)
		{
			throw new XMLSerializationException(e);
		}

		return out.toString();
	}

	public String getVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void serializeTo(OutputStream outStream)
	{
		try
		{
			outStream.write(asObjectXML());
		}
		catch (XMLSerializationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void serializeTo(OutputStream outStream, int indent) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
	}

	public void serializeTo(File file) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
	}

	public void serializeTo(File file, int indent) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
	}

	public void serializeTo(String encoding, Writer out) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
	}

	public void serializeTo(String encoding, Writer out, int indent) throws XMLSerializationException
	{
		// TODO Auto-generated method stub
	}

	// Note: just return the value , don't set the member value
	public int computeOriginalHash()
	{		
		int hash = 0;
		if (this.hasTridas()) {
			hash = getTridasAsObject().hashCode();
		}
		return hash;
	}

	public DateTime getTimestamp()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDirty()
	{
		if (dirty)
		{
			logger.debug("Dirty");	
			return dirty; // it was set to dirty
		}
		else
		{
			logger.debug("Original hash=" + originalHash + ", current hash=" + computeOriginalHash());	
		   // check if it was made dirty
	       return (originalHash != computeOriginalHash());
		}
	}

	public boolean isOlderThan(Object compareDate)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setDirty(boolean dirty)
	{
		logger.debug("Dirty set to: " + dirty);
		
		this.dirty = dirty;
        if (!dirty)
        {
        	originalHash = computeOriginalHash();
        }		
	}

	public void setTimestamp(Object timestamp)
	{
		// TODO Auto-generated method stub
	}

}
