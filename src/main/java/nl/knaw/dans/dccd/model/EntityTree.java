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

import javax.xml.transform.Source;

import nl.knaw.dans.common.lang.repo.MetadataUnit;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;
import org.tridas.schema.TridasProject;

/**
 * A tree (hierarchy) of Entities
 *
 * @see nl.knaw.dans.dccd.model.entities.Entity
 *
 * @author paulboon
 *
 */
//public class EntityTree implements Serializable
public class EntityTree implements MetadataUnit, Serializable
{
	private static final long serialVersionUID = -6437490278611107505L;
	private static Logger logger = Logger.getLogger(EntityTree.class);
	ProjectEntity projectEntity = null; // the root is always a Project


	public final static String ENTITY_ID_PREFIX = "TF"; // used to prepend to number to get id string
	public final static String ENTITYTREE_ID = "ETS";

	/**
	 * @return The project entity, root of the tree
	 */
	public ProjectEntity getProjectEntity() {
		return projectEntity;
	}

	/**
	 * @param projectEntity
	 */
	public void setProjectEntity(ProjectEntity projectEntity) {
		this.projectEntity = projectEntity;
	}

	// --- tree creation stuff ---

	//TODO: LB20090923: better to have this as a constructor

	/**
	 * Build the complete tree of entities
	 */
	public void buildTree(TridasProject projectTridas) {
		projectEntity = new ProjectEntity(projectTridas);
		projectEntity.buildEntitySubTree();
		assignEntityIds();
	}

	/**
	 * Traverse the tree and assign id's to each entity
	 * needed for persisting the tree data
	 *
	 * Note: how to give all entities within a certain tree unique id's?
	 * Now I have the EntityTree do it,
	 * but the setId of the Entity must therefore be public
	 * Java doesn't have a 'friend' concept like C++
	 * just use numbers is a-specifc, but I know Fedora wants something like "TF3"
	 *
	 */
	private void assignEntityIds() {
		List<Entity> entities = getEntities();
		int entityCount = 0;
		// Process all the entities
		for (Entity entity : entities) {
			++entityCount;
			// Note: stream ID cannot be only a number, therfore we prepend "TF" for TRiDaS Fragment
			String id = ENTITY_ID_PREFIX + Integer.toString(entityCount); // should be unique within Map of object
			entity.setId(id);
		}
	}

	/** Get all the entities in the tree as a (flat) list
	 *
	 * @return The list of entities, empty list if there is no project
	 */
	public List<Entity> getEntities() {
		//List<AbstractEntity> entities = new ArrayList<AbstractEntity>();
		List<Entity> entities = new ArrayList<Entity>();

		// don't add anything if there is no project
		if (getProjectEntity()!=null) {
			entities.add(getProjectEntity()); // The root of the tree
			entities.addAll(getSubTreeAsList(getProjectEntity()));
		}

		return entities;
	}

	/** Recursive function to get all sub entities in a tree
	 * @see getEntities
	 *
	 * Note: could be member of the abstract DendroEntity
	 * Note: Probably more efficient if we pass a 'stack like' container
	 * to avoid the recursive creation and appending of arraylists
	 *
	 * @param entity
	 * @return The list of entities
	 */
    private List<Entity> getSubTreeAsList(Entity entity) {
    	if(entity == null) {
    		logger.debug("getSubTreeAsList called with null as entity argument");
    		return null;
    	}
    	logger.debug("getting subtree of Entity : " + entity.getClass().getSimpleName());
    	List<Entity> list = new ArrayList<Entity>();

    	List<Entity> subEntities = entity.getDendroEntities();
    	// maybe assert that subEntities not is null??
    	if(subEntities == null) {
    		logger.error("getDendroEntities returned null, while getting subtree of Entity : " + entity.getClass().getSimpleName());
    	}

		Iterator<Entity> i = subEntities.iterator();
		while (i.hasNext()) {
			Entity subEntity = i.next();
			list.add(subEntity);
			list.addAll(getSubTreeAsList(subEntity)); // recursion
		}

    	return list;
    }

	// --- structmap stuff ---

	final static String ENTITYTREESTRUCT_NS = "entitytree";
	final static String ENTITYTREESTRUCT_NS_PREFIX = ENTITYTREESTRUCT_NS+":";
	final static String ENTITYTREESTRUCT_NS_URL = "http://dans.knaw.nl/dccd/entitytree/";
	final static String ENTITYTREESTRUCT_VERSION = "0.1";

	//TODO: LB20090923: put the following two methods in a EntityTreeMarshaller

	/** Get the dom4j xml document object that represents the entity tree,
	 * but then only the structure, not the Entity objects.
	 *
	 * @return The document
	 */
    public Document getTreeStructAsDocument() {
    	// note: if projectEntity == null, throw an exception ?
    	//
    	if(getProjectEntity() == null) {
    		logger.warn("No project, creating empty tree");
    	}

    	// create a XML DOM document using dom4j
    	// don't delegate to the entities this time
    	Document document = DocumentHelper.createDocument();
        Element structmap = document.addElement(ENTITYTREESTRUCT_NS_PREFIX+ENTITYTREESTRUCT_NS);//("structmap"); // root
        structmap.addAttribute("version", ENTITYTREESTRUCT_VERSION );
        structmap.addNamespace(ENTITYTREESTRUCT_NS, ENTITYTREESTRUCT_NS_URL);
        buildTreeStructElementTree(structmap, getProjectEntity());

    	return document;
    }

    /**
     * Recursive function, adds entity tree representation to given dom4j element
     *
     * Note: would like to get rid of the refs to DendroEntityTreeBuilder !
     * @see getTreeStructAsDocument
     *
     * @param parentElement The element to which he tree is added
     * @param entity The entity to use for getting the tree and adding it
     */
    private void buildTreeStructElementTree(Element parentElement, Entity entity) {
    	//throw an illegal argument exception ?
    	if (entity==null || parentElement==null) return;

    	String tagName = EntityTreeBuilder.entityTagsMap.get(entity.getClass());
    	// what if tagName is not found?
    	Element element = parentElement.addElement(ENTITYTREESTRUCT_NS_PREFIX+tagName);
    	element.addAttribute( EntityTreeBuilder.ID_ATTRIBUTENAME,  entity.getId());
    	element.addAttribute( EntityTreeBuilder.TITLE_ATTRIBUTENAME,  entity.getTitle());

    	 // subentities
     	List<Entity> subEntities = entity.getDendroEntities();
		for (Entity subEntity : subEntities) {
			buildTreeStructElementTree(element, subEntity);
		}
    }

	/** get all MeasurementSeriesEntity of the Project
	 *
	 * note: Maybe we could make it more general if we specify a class;
	 * and then only return the specified type of entities
	 *
	 * @return The list of MeasurementSeriesEntity's
	 */
	public List<MeasurementSeriesEntity> getMeasurementSeriesEntities() {
		List<MeasurementSeriesEntity> resultList = new ArrayList<MeasurementSeriesEntity>();

		List<Entity> entityList = getEntities();
		for(Entity entity : entityList) {
			if (entity instanceof MeasurementSeriesEntity)
				resultList.add((MeasurementSeriesEntity)entity);
		}

		return resultList;
	}


	/* MetadataUnit implementation code below */
	// what to do with ENTITYTREESTRUCT_NS_PREFIX ="entitytree:"
	String UNIT_ID = ENTITYTREE_ID;
    String UNIT_LABEL = "entitytree";
    String UNIT_FORMAT = ENTITYTREESTRUCT_NS_URL;
    URI UNIT_FORMAT_URI = URI.create(UNIT_FORMAT);
	private boolean dirty;

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
		//Document document = DocumentHelper.createDocument();
		//document.add(asElement());
		//
		//return document;

		return getTreeStructAsDocument();
	}

	public Element asElement() throws XMLSerializationException
	{
		//Element structmap = DocumentHelper.createElement(ENTITYTREESTRUCT_NS_PREFIX+ENTITYTREESTRUCT_NS);
		//buildTreeStructElementTree(structmap, getProjectEntity());
		//
		//return structmap;
		Document doc = asDocument();
		return doc.getRootElement();
	}

	public byte[] asObjectXML() throws XMLSerializationException {
		String xmlStr = asXMLString();

		try {
			return xmlStr.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new XMLSerializationException(e);
		}
	}

	public Source asSource() throws XMLSerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream asXMLInputStream() throws XMLSerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream asXMLInputStream(int indent)
			throws XMLSerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String asXMLString() throws XMLSerializationException
	{
		Element element = asElement();

		return element.asXML();
	}

	public String asXMLString(int indent) throws XMLSerializationException {
		// TODO Auto-generated method stub

		Element element = asElement();
		OutputFormat format = OutputFormat.createPrettyPrint();
		// use an XMLWriter
		StringWriter out = new StringWriter();
		XMLWriter writer = new XMLWriter(out,  format );
		writer.setIndentLevel(indent);

        try {
			writer.write(element);
		} catch (IOException e) {
			throw new XMLSerializationException(e);
		}

		return out.toString();
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void serializeTo(OutputStream outStream)
			throws XMLSerializationException
	{
		try
		{
			outStream.write(asObjectXML());
		}
		catch (IOException e)
		{
			throw new XMLSerializationException(e);
		}
	}

	public void serializeTo(OutputStream outStream, int indent)
			throws XMLSerializationException {
		// TODO Auto-generated method stub

	}

	public void serializeTo(File file) throws XMLSerializationException {
		// TODO Auto-generated method stub

	}

	public void serializeTo(File file, int indent)
			throws XMLSerializationException {
		// TODO Auto-generated method stub

	}

	public void serializeTo(String encoding, Writer out)
			throws XMLSerializationException {
		// TODO Auto-generated method stub

	}

	public void serializeTo(String encoding, Writer out, int indent)
			throws XMLSerializationException {
		// TODO Auto-generated method stub

	}

	public int computeOriginalHash() {
		return 0;
	}

	public DateTime getTimestamp() {
		return null;
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isOlderThan(Object compareDate) {
		return false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void setTimestamp(Object timestamp) {
	}

}
