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
package nl.knaw.dans.dccd.model.entities;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;

import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.dccd.model.InternalErrorException;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
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

import org.tridas.schema.TridasFile;


/**
 * The DCCD information (metadata) which is also stored in TRiDaS xml files, but split into (entity)levels.
 * 
 * @see nl.knaw.dans.dccd.model.entities.Entity
 * @author paulboon
 */
public abstract class AbstractEntity implements Serializable, Entity
{
	private static Logger logger = Logger.getLogger(AbstractEntity.class);
	private static final long	serialVersionUID	= -2262058266429622995L;
	private String				title;											// short descriptive name, as in the TridasEntity
	private String				id;											// identification must be unique within an entity tree
	private List<Entity>		entities			= null;					// sub-entities, or children

	private static JAXBContext jaxbContext = null;
	private static Marshaller marshaller = null;

	// keeps the result of the (archiving) validation
	private boolean validForArchiving = true;

	@Override
	public boolean isValidForArchiving()
	{
		return this.validForArchiving;
	}

	@Override
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
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// testing
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

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public String getLabel()
	{
		return getClass().getSimpleName();// .toLowerCase();
	}

	public List<Entity> getDendroEntities()
	{
		if (entities == null)
		{
			/* lazy construction */
			entities = new ArrayList<Entity>();
		}
		return entities;
	}

	public AbstractEntity()
	{
		// empty
	}

	public boolean hasTridas()
	{
		return (null != getTridasAsObject());
	}

	public boolean isPermittedBy(ProjectPermissionLevel level)
	{
		// System.out.println("Level: " + getPermissionLevel() + " was asked with permission: " + level);
		return getPermissionLevel().isPermittedBy(level);
	}

	public abstract ProjectPermissionLevel getPermissionLevel();

	public abstract void buildEntitySubTree();

	public abstract Object getTridasAsObject();

	public abstract void setTridasObject(Object tridas);

	public abstract void connectTridasObjectTree();

	@SuppressWarnings("unchecked")
	public abstract Class getTridasClass();

	public abstract String getTridasTitle();

	/**
	 * Remove all sub-trees from the given tridas object, disconnecting it Only specific Entities know how to prune their Tridas Implementations should support
	 * the unprune to reverse (undo) the pruning Note: be carefull with what you prune, using a clone is safest! Note: maybe using generics possible?
	 * 
	 * @param o
	 *        The tridas object to prune
	 */
	protected abstract void pruneTridas();//Object o);

	/**
	 * Undo the pruning
	 * @see pruneTridas()
	 * 
	 * @param o
	 *        The tridas object to unprune
	 */
	protected abstract void unpruneTridas();//Object o);

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

	/**
	 * Needed for getting readable namespaces like xlink and gml instead of ns1 and ns2 used by getXMLString
	 * 
	 * @author paulboon
	 */
/*	
	private static class TridasNamespacePrefixMapper extends NamespacePrefixMapper
	{
		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
		{
			if ("http://www.opengis.net/gml".equals(namespaceUri))
				return "gml";
			else if ("http://www.w3.org/1999/xlink".equals(namespaceUri))
				return "xlink";
			// Changed with version 1.2.1
			// else if ( "http://www.tridas.org/1.2".equals(namespaceUri) )
			else if ("http://www.tridas.org/1.2.1".equals(namespaceUri))
				return "tridas"; // now prefixes every element with tridas, except gml and xlink

			return suggestion;
		}
	}
*/
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

	/*
	 * (non-Javadoc)
	 * @see nl.knaw.dans.dccd.model.entities.Entity#toTreeString(java.lang.String)
	 */
	public List<String> toTreeString(String indent)
	{
		List<String> list = new ArrayList<String>();
		list.add(indent + "+-" + getTitle() + " [" + getId() + "]");

		// then the sun entities
		Iterator<Entity> i = getDendroEntities().iterator();
		while (i.hasNext())
		{
			Entity entity = i.next();
			list.addAll(entity.toTreeString(indent + "  ")); // recursion
		}

		return list;
	}

	public List<Entity> getSubTreeAsList()
	{
		List<Entity> list = new ArrayList<Entity>();

		List<Entity> subEntities = getDendroEntities();
		// maybe assert that subEntities not is null??

		Iterator<Entity> i = subEntities.iterator();
		while (i.hasNext())
		{
			Entity subEntity = i.next();
			list.add(subEntity);
			list.addAll(subEntity.getSubTreeAsList()); // recursion
		}

		return list;
	}
	
	/* === MetadataUnit implementation code below === */

	// ENTITY_ID_PREFIX, did I have numbers after that and the TF12
	// for some nested entity; those ID are also used in the EntityTree !
	String			UNIT_ID			= "TF";
	String			UNIT_LABEL		= "tridasentity";
	String			UNIT_FORMAT		= "http://dans.knaw.nl/dccd/tridasentity";
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
		return getId(); // Note: UNIT_ID + number;
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

	// We need XML Fragments; no child entities (Tridas sub entities)
	// There are several options:
	// A) so use clone() and then prune
	// by having the JAXB class generator add clonable to the classes
	// B) also clone but, do this by marshall your object to XML
	// and unmarshall it back to another object,
	// this is not efficient but easy to implement
	// C) Move object children to another list before marshalling
	// and place back afterwards, do this inside synchronised block
	//
	// Now using method C !
	// 1) prune
	// 2) marshall
	// 3) unprune
	//
	// When using B the process would be divided into the following steps:
	// 1) clone
	// 2) prune the clone (this is done by the subclass)
	// 3) marshall it to an xml fragment
	//
	public String asXMLString() throws XMLSerializationException
	{
		String result = "";
		Object tridasObject = getTridasAsObject();
		if (tridasObject == null)
			return result; // nothing to do, warn?

		try
		{
			Object objectPruned = tridasObject;
			pruneTridas();
			
			java.io.StringWriter sw = new StringWriter();
			getJAXBmarshaller().marshal(objectPruned, sw);
			
			unpruneTridas();

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
			// prune before calculation and unprune aftewards
			// we don't want to use tridas from the other entitylevels
			this.pruneTridas();
			hash = getTridasAsObject().hashCode();
			this.unpruneTridas();
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
			//logger.debug("Original hash=" + originalHash + ", current hash=" + computeOriginalHash());	
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

	public List<String> getFileNames(final List<TridasFile> files)
	{
		List<String> fileNames = new ArrayList<String>();

		for (TridasFile file : files)
		{
			if (file.isSetHref() && !file.getHref().trim().isEmpty())
				fileNames.add(file.getHref());
		}
		
		return fileNames;		
	}		
}
