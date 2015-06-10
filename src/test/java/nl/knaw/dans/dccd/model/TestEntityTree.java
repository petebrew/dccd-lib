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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ObjectEntity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class TestEntityTree extends TestCase {

	/**
	 * - DendroEntityTree()
	 */
	public void testConstructor()
    {
		// constructor
		EntityTree entityTree = new EntityTree();
		// project entity should be null
		ProjectEntity projectEntity = entityTree.getProjectEntity();
		assertNull(projectEntity);
		// entities should be an empty list
		List<Entity> entities =  entityTree.getEntities();
		assertNotNull(entities);
		assertEquals(0, entities.size());
		// check that the tree is empty
		Document documentDom = entityTree.getTreeStructAsDocument();
    	Element elementDom = documentDom.getRootElement();
    	assertEquals(0, elementDom.elements().size());
    }

	/**
	 * - createTree(TridasProject projectTridas)
	 */
	public void testCreateTreeFromTridasProject()
    {
		// constructor
		EntityTree entityTree = new EntityTree();

		// use the most simple Tridas project object to create a tree
		// with only one element, the project
		TridasProject projectTridas = new TridasProject();
		entityTree.buildTree(projectTridas);
		// entity should be non-null
		ProjectEntity projectEntity = entityTree.getProjectEntity();
		assertNotNull(projectEntity);
		// list should have one entity
		List<Entity> entities =  entityTree.getEntities();
		assertNotNull(entities);
		assertEquals(1, entities.size());
		// that entity should be the project entity
		assertSame(projectEntity, entities.get(0));
		// check that the treestruct has one element
		Document documentDom = entityTree.getTreeStructAsDocument();
		Element elementDom = documentDom.getRootElement();
    	assertEquals(1, elementDom.elements().size());

    	// Note test adding an object to the project
    	// and then recreate the tree
    	TridasObject objectTridas = new TridasObject();
    	projectTridas.getObjects().add(objectTridas);
    	// note, we don't test the Tridas classes, but assume they work correctly
		entityTree.buildTree(projectTridas);
		// entity should be non-null
		projectEntity = entityTree.getProjectEntity();
		assertNotNull(projectEntity);
		// list should have two entitis
		entities =  entityTree.getEntities();
		assertNotNull(entities);
		assertEquals(2, entities.size());
		// check that the treestruct has two elements along the tree
		documentDom = entityTree.getTreeStructAsDocument();
		elementDom = documentDom.getRootElement();
    	assertEquals("project",1, elementDom.elements().size());
    	Element subElementDom = (Element)elementDom.elements().get(0);
    	assertEquals("object",1, subElementDom.elements().size());

// test Tridas equals, shows if JAXB compares values
// see:   	 https://jaxb2-commons.dev.java.net/commons-lang-plugin/
//TridasProject projectTridas2 = new TridasProject();
//assertSame(projectTridas2, projectTridas);
    }

	/**
	 * - createTree(Element treestructElement)
	 */
	public void testCreateTreeFromElement()
    {
		// constructor
		EntityTree entityTree = new EntityTree();

		// mockup a dom4j element tree,
		// is not possible without looking into private methods
		//
		// Also note that if the dom is wrong, an exception should be thrown
		// but that should be done while parsing the xml (from the repsitory).
		// the entitytree xml should be conform it's schema?
		//
		// Use some xml string that we know to be correct, ALWAY CHECK IF TEST FAILS!
		String xmlStr = ""+
		"<entitytree:entitytree xmlns:entitytree=\"http://dans.knaw.nl/dccd/entitytree/\" version=\"0.1\">"+
		"  <entitytree:project datastreamId=\"TF1\" title=\"TridasProject-tridas-title\">"+
		"    <entitytree:object datastreamId=\"TF2\" title=\"TridasObject-tridas-title\">"+
		"    </entitytree:object>"+
		"  </entitytree:project>"+
		"</entitytree:entitytree>";
		Document documentDom = null;
		try {
			documentDom = DocumentHelper.parseText( xmlStr );
		} catch (DocumentException e) {
		     e.printStackTrace();
		     fail("dom4j exception");
		}
    	Element elementDom = documentDom.getRootElement();

    	//entityTree.buildTree(elementDom);
		entityTree.setProjectEntity(EntityTreeBuilder.buildTree(elementDom));

		// entity should be non-null
    	ProjectEntity projectEntity = entityTree.getProjectEntity();
		assertNotNull(projectEntity);
		// list should have two entities
		List<Entity> entities =  entityTree.getEntities();
		assertNotNull(entities);
		assertEquals(2, entities.size());

		assertEquals("TF1",projectEntity.getId());
		assertEquals("TridasProject-tridas-title",projectEntity.getTitle());

		List<Entity> projectEntities = projectEntity.getDendroEntities();
		assertNotNull(projectEntities);
		assertEquals(1, projectEntities.size());
		ObjectEntity objectEntity = (ObjectEntity) projectEntities.get(0);
		// one and only one
		assertNotNull(objectEntity);
		assertEquals("TF2",objectEntity.getId());
		assertEquals("TridasObject-tridas-title",objectEntity.getTitle());
    }

    public void testConvertToAndFromXML()
    {
    	// shallow tree/branch only two levels
    	// otherwise it takes to long
    	ConvertToAndFromXML(2);
    }

	/**
     * Test the entity tree for conversion to/from a dom4j XML representation of the tree:
     * Sort of identity transform
     * - construct a mockup object
     * - construct entity tree from tridas with createTree()
     * - create the dom4j (XML) element tree with getTreeStructAsDocument()
     * - reconstruct the entity tree from this element using createTree()
     * - compare with original mockup object
     *
     * note:
     * We could also use tridas test files
     * where we don't care about the content only the structure!
     * Then also use JAXB to read them and construct the tree etc. etc.
     *
     * note: this is a long test, should be decomposed
     *
     */
    public void ConvertToAndFromXML(int depth)
    {
    	// create the tree structure, used for testing
    	// construct a TriDaS tree instead of reading a tridas file
    	//TridasXXX xxxTridas = new TridasXXX();
    	TridasProject projectTridas = new TridasProject();
//    	TridasObject objectTridas = new TridasObject();
TridasObject objectTridas = new ObjectFactory().createTridasObject();//new TridasObjectEx();
    	TridasElement elementTridas = new TridasElement();
    	TridasSample sampleTridas = new TridasSample();
    	TridasRadius radiusTridas = new TridasRadius();
    	TridasMeasurementSeries measurementserieTridas = new TridasMeasurementSeries();
    	TridasValues valuesTridas = new TridasValues();

    	// set titles, where applicable
    	//xxxTridas.setTitle(xxxTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	projectTridas.setTitle(projectTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	objectTridas.setTitle(objectTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	elementTridas.setTitle(elementTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	sampleTridas.setTitle(sampleTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	radiusTridas.setTitle(radiusTridas.getClass().getSimpleName()+"-"+"tridas-title");
    	measurementserieTridas.setTitle(measurementserieTridas.getClass().getSimpleName()+"-"+"tridas-title");

    	// construct path from root to leave
    	// use given depth to limit the tree size
    	int level = 1;
    	if(depth > level++) projectTridas.getObjects().add(objectTridas);
    	if(depth > level++) objectTridas.getElements().add(elementTridas);
    	if(depth > level++) elementTridas.getSamples().add(sampleTridas);
    	if(depth > level++) sampleTridas.getRadiuses().add(radiusTridas);
    	if(depth > level++) radiusTridas.getMeasurementSeries().add(measurementserieTridas);
    	if(depth > level++) measurementserieTridas.getValues().add(valuesTridas);

    	// show tridas xml
    	// note: we are now also testing JAXB
		JAXBContext jaxbContext = null;
		try {
			//System.out.println("\n TRiDaS XML, non valid, but with the structure");
			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			// now marshall the pruned clone
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//testing
			java.io.StringWriter sw = new StringWriter();
			marshaller.marshal(projectTridas, sw);

			//System.out.print(sw.toString());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail("JAXB exception");
		}

    	// construct entity tree from tridas, allthough it isn't valid schema wise
		EntityTree entityTree = new EntityTree();
		entityTree.buildTree(projectTridas);

		// check the projectEntity constructed by the tree
		// - projectEntity should not be null
		ProjectEntity projectEntity = entityTree.getProjectEntity();
		assertNotNull(projectEntity);

		// - each entitie's tridas object should be the one we have here
    	List<Entity> entities =  entityTree.getEntities();
    	// should have all actualDepth(1-7) entities
    	assertEquals(depth, entities.size());
    	//System.out.println("Number of entities in tree1: " + entities.size());
    	for (Entity entity : entities) {
    		Object tridas = entity.getTridasAsObject();
    		// should be non null
    		assertNotNull(tridas);
    		// compare?
    	}

		// give some descriptive output = show tree
		//System.out.println("entity tree1:");
    	List<String> lines = projectEntity.toTreeString("");
    	for (String line : lines)
    	{
    		//System.out.println(line);
    	}
    	
		// EXTRA
		// Store the tridas as xml fragments (like what we do for the fedora repository
    	// note: Serves as a fake store!
		Map<String, String> tridasMap = new HashMap<String, String>();
		// adding to the map makes sure ID's are unique!
		for (Entity entity : entities) {
			tridasMap.put(entity.getId(), entity.getXMLString());
		}

    	// create a structure
    	Document documentDom = entityTree.getTreeStructAsDocument();
    	Element elementDom = documentDom.getRootElement();
    	// should not be null, and a project...
		assertNotNull(elementDom);
    	// maybe count the number of elements in the tree, 7?

    	// give some descriptive output = show xml
    	// note: you can always use treeDoc.asXML();
    	// note: we are not testing dom4j
		/*
    	OutputFormat format = OutputFormat.createPrettyPrint();
    	XMLWriter writer;
		try {
			System.out.println("tree structure xml: ");
			writer = new XMLWriter( System.out, format );
	        writer.write( elementDom );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("dom4j exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO exception");
		}
		 */
		
		// Note: tree struct is now not converted to/from xml
		// dom4j marshall/unmarshall for the struct is not tested now!
		// ingest uses:
		// version.setXmlContent(entityTree.getTreeStructAsDocument().getRootElement());
		// just passes the dom4j element
		// not sure if we can get that back when retrieving?
		// Yep we can, so no XML needed here, it is handled by others

    	// recreate the entity tree, without any changes
		EntityTree entityTree2 = new EntityTree();
		//entityTree2.buildTree(elementDom);
		entityTree2.setProjectEntity(EntityTreeBuilder.buildTree(elementDom));
		ProjectEntity projectEntity2 = entityTree2.getProjectEntity();
		assertNotNull(projectEntity2);

		// give some descriptive output = show tree
		//System.out.println("\nentity tree2:");
    	List<String> lines2 = projectEntity2.toTreeString("");
    	for (String line : lines2)
    	{
    		//System.out.println(line);
    	}
    	
    	// except for the Tridas members the entities should be equal,
		// title, Id and equal sub entities
		//
		// Assume that the same (non-random) algorithm is being used to produce a list
		// and if we put in the same entity hierarchy we get the same order in the list
    	// - should have same amount of entities
		List<Entity> entities2 =  entityTree2.getEntities();
		assertNotNull(entities2);
    	//System.out.println("Number of entities in tree2: " + entities2.size());
    	// should have all actualDepth(1-7) entities
    	assertEquals(depth, entities2.size());
    	//for (DendroEntity entity : entities2) {
    	//	Object tridas = entity.getTridasAsObject();
     	//}

    	// EXTRA
    	// NOTE: SHOULD TEST FOR EQUALITY OF THE TRIDAS PROJECT
    	// but then I need the fragments as well,
    	// but not using the DigitalObjexct or Fedora...
    	// make use of a Map of the fragments with an ID.
    	// note: we are not testing JAXB
    	for (Entity entity : entities2) {
    		// get xml from map
    		String xmlStr = tridasMap.get(entity.getId());
    		// use JAXB to get the Tridas object
    		jaxbContext = null;
    		Object tridas = null;
    		try {
    			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
    			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    			ByteArrayInputStream input = new ByteArrayInputStream (xmlStr.getBytes());
    			tridas = unmarshaller.unmarshal(input);
    			entity.setTridasObject(tridas);
    		} catch (JAXBException e) {
    			e.printStackTrace();
    			fail("JAXB exception");
    		}
     	}

    	// But now the tridas objects are not connected the way they should be
    	// let entity tree do that by delegating to the entities?
    	//
    	// Note: maybe have this in EntityTree?
    	projectEntity2.connectTridasObjectTree();

    	// now check if we have a tridas tree that JAXB can handle
    	// note: we are not testing JAXB
    	TridasProject projectTridas2 = (TridasProject)projectEntity2.getTridasAsObject();
		jaxbContext = null;
		try {
			//System.out.println("\n TRiDaS XML, non valid, but with the structure");
			jaxbContext = JAXBContext.newInstance("org.tridas.schema");
			// now marshall the pruned clone
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//testing
			java.io.StringWriter sw = new StringWriter();
			marshaller.marshal(projectTridas2, sw);
			//System.out.print(sw.toString());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail("JAXB exception");
		}

		// test if trees are equal, java wise?
		// only works if the order of adding to a List container is also the
		// order we get when iterating the List
		//if (projectTridas2.equals(projectTridas)) {
		//	System.out.println("Identity transform succeded");
		//} else {
		//	System.out.println("Identity transform FAILED!");// always get here?
		//}
		//
		//assertSame(projectTridas, projectTridas2);
    }

    /* This stuff is inside the DendroEntityTree...

    	// Build a fake tree of entities and try to get structure from it
    	//DendroXXXEntity xxxEntity = new DendroXXXEntity();
    	//projectEntity
    	//objectEntity
    	//elementEntity
    	//sampleEntity
    	//radiusEntity
    	//measurementseriesEntity
    	DendroProjectEntity projectEntity = new DendroProjectEntity();
    	DendroObjectEntity objectEntity = new DendroObjectEntity();
    	DendroElementEntity elementEntity = new DendroElementEntity();
    	DendroSampleEntity sampleEntity = new DendroSampleEntity();
    	DendroRadiusEntity radiusEntity = new DendroRadiusEntity();
    	DendroMeasurementSeriesEntity measurementseriesEntity = new DendroMeasurementSeriesEntity();
    	DendroValuesEntity valuesEntity = new DendroValuesEntity();
    	// construct path from root to leave
    	projectEntity.getDendroEntities().add(objectEntity);
    	objectEntity.getDendroEntities().add(elementEntity);
    	elementEntity.getDendroEntities().add(sampleEntity);
    	sampleEntity.getDendroEntities().add(radiusEntity);
    	radiusEntity.getDendroEntities().add(measurementseriesEntity);
    	measurementseriesEntity.getDendroEntities().add(valuesEntity);
    	// Note: It is possible to construct trees that do not correspond to valid TRiDaS

    	// Set titles
    	//xxxEntity.setTitle(xxxEntity.getClass().getSimpleName()+"-"+"title");
    	projectEntity.setTitle(projectEntity.getClass().getSimpleName()+"-"+"title");
    	objectEntity.setTitle(objectEntity.getClass().getSimpleName()+"-"+"title");
    	elementEntity.setTitle(elementEntity.getClass().getSimpleName()+"-"+"title");
    	sampleEntity.setTitle(sampleEntity.getClass().getSimpleName()+"-"+"title");
    	radiusEntity.setTitle(radiusEntity.getClass().getSimpleName()+"-"+"title");
    	measurementseriesEntity.setTitle(measurementseriesEntity.getClass().getSimpleName()+"-"+"title");
    	valuesEntity.setTitle(valuesEntity.getClass().getSimpleName()+"-"+"title");

    	// Set id's
    	// Note: I am not sure if those should be numeric or maybe allready Fedora complient
    	// or at least unique within a tree...
    	// testing makes you thing about these things...
    	// xxxEntity.setId("ID"+Integer.toString(count++));
    	int count = 1;
    	projectEntity.setId("ID"+Integer.toString(count++));
    	objectEntity.setId("ID"+Integer.toString(count++));
    	elementEntity.setId("ID"+Integer.toString(count++));
    	sampleEntity.setId("ID"+Integer.toString(count++));
    	radiusEntity.setId("ID"+Integer.toString(count++));
    	measurementseriesEntity.setId("ID"+Integer.toString(count++));
    	valuesEntity.setId("ID"+Integer.toString(count++));

    	// give some descriptive output
    	List<String> lines = projectEntity.toTreeString("");
    	for (String line : lines)
    		System.out.println(line);

     */
}
