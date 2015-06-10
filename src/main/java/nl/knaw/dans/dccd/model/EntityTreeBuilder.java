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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ElementEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ObjectEntity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.model.entities.RadiusEntity;
import nl.knaw.dans.dccd.model.entities.SampleEntity;
import nl.knaw.dans.dccd.model.entities.ValuesEntity;

import org.apache.log4j.Logger;
import org.dom4j.Element;

//TODO: LB20090923: use singleton pattern instead of having everything static

//TODO: LB20090923: This is an unmarshaller, call it so, otherwise it becomes
// very unclear what it is
// Pboon: No, it is not an unmarshaller, the xml from fedora is unmarshalled to dom4j elements.
// Those are used by this builder class to build the tree of entities

/** Can build the complete entity tree from dom4j elements
 * Note that there elmenents are unmarshalled from the xml
 *
 * @author paulboon
 */
public class EntityTreeBuilder {
	private static Logger logger = Logger.getLogger(EntityTreeBuilder.class);

	/** Mapping from class to the tagname
	 * Maybe place this somewhere else?
	 *
	 * Note; getSimpleName() and to lowercase would produce a similar mapping
	 * this explicit mapping allows for changing the tagnames independently
	 */
	@SuppressWarnings("unchecked")
	public static final Map<Class, String> entityTagsMap = new HashMap<Class, String>() {
		private static final long serialVersionUID = 1L;
	{
	   put(ProjectEntity.class			, "project");
	   put(ObjectEntity.class				, "object");
	   put(ElementEntity.class			, "element");
	   put(SampleEntity.class				, "sample");
	   put(RadiusEntity.class				, "radius");
	   put(MeasurementSeriesEntity.class	, "measurementseries");
	   put(DerivedSeriesEntity.class		, "derivedseries");
	   put(ValuesEntity.class				, "values");
	}};
	public final static String ID_ATTRIBUTENAME = "datastreamId";
	public final static String TITLE_ATTRIBUTENAME = "title";

	/** build entity tree from given tree structure (dom4j element)
     * there will be entities but they don't have data
     * (=TridasEntity implementation from xml fragments)
	 *
     * @param treestructElement
     */
    public static ProjectEntity buildTree(Element treestructElement) {
    	// note: if treestructElement == null, throw an illegal argument exception ?
    	if(treestructElement == null) {
    		logger.warn("No Element given, not creating tree");
    		throw new IllegalArgumentException("No Element given");//return null;
    	}

    	// must have a single project!
    	Element element = treestructElement.element(EntityTreeBuilder.entityTagsMap.get(ProjectEntity.class));
    	if(element == null) {
    		logger.warn("Element has no project, not creating tree");
    		throw new IllegalArgumentException("Element has no project");//return null;
    	}

    	ProjectEntity projectEntity = new ProjectEntity();
    	buildProjectEntityTree(projectEntity, element);
    	return projectEntity;
    }

	//TODO: LB20090923: return a DendroProjectEntity instead of getting one
	// via the parameters and filling it
	// suggestion: choose between build, create, marshal and unmarshal, but then stick
	// to it

    /** Construct the tree of entities for the project level (root of the tree)
     * For each of the lower levels (sub entities) there is a build function
     * Note: would be nice if we could do some more function templating.
     * Maybe it would help if I have a mapping from each class to it's possible sub entity classes
     *
     * @param entity The entity being build
     * @param element The element with the information used for building
     */
    @SuppressWarnings("unchecked")
	public static void buildProjectEntityTree(ProjectEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> iObj = element.elementIterator( entityTagsMap.get(ObjectEntity.class) ); iObj.hasNext(); ) {
            Element subElement = (Element)iObj.next();
            ObjectEntity subEntity = new ObjectEntity();
            subentities.add(subEntity);
            buildObjectEntityTree(subEntity, subElement);// go deeper
        }
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(DerivedSeriesEntity.class)); i.hasNext(); ) {
            Element subElement = (Element)i.next();
            DerivedSeriesEntity subEntity = new DerivedSeriesEntity();
            subentities.add(subEntity);
            buildDerivedSeriesEntityTree(subEntity, subElement);// go deeper
        }
    }

    @SuppressWarnings("unchecked")
	private static void buildObjectEntityTree(ObjectEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> iObj = element.elementIterator(entityTagsMap.get(ObjectEntity.class)); iObj.hasNext(); ) {
            Element subElement = iObj.next();
            ObjectEntity subEntity = new ObjectEntity();
            subentities.add(subEntity);
            buildObjectEntityTree(subEntity, subElement);// go deeper, recursive!
        }
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(ElementEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            ElementEntity subEntity = new ElementEntity();
            subentities.add(subEntity);
            buildElementEntityTree(subEntity, subElement);// go deeper
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildElementEntityTree(ElementEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(SampleEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            SampleEntity subEntity = new SampleEntity();
            subentities.add(subEntity);
            buildSampleEntityTree(subEntity, subElement);// go deeper
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildSampleEntityTree(SampleEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(RadiusEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            RadiusEntity subEntity = new RadiusEntity();
            subentities.add(subEntity);
            buildRadiusEntityTree(subEntity, subElement);// go deeper
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildRadiusEntityTree(RadiusEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(MeasurementSeriesEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            MeasurementSeriesEntity subEntity = new MeasurementSeriesEntity();
            subentities.add(subEntity);
            buildMeasurementSeriesEntityTree(subEntity, subElement);// go deeper
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildMeasurementSeriesEntityTree(MeasurementSeriesEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(ValuesEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            ValuesEntity subEntity = new ValuesEntity();
            subentities.add(subEntity);
            buildValuesEntityTree(subEntity, subElement);// go deeper
        }
    }

    private static void buildValuesEntityTree(ValuesEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// no subentities
    }

    @SuppressWarnings("unchecked")
    private static void buildDerivedSeriesEntityTree(DerivedSeriesEntity entity, Element element) {
    	// note: throw an illegal argument exception ?
    	if (entity==null || element==null) return;

    	entity.setId(element.attributeValue(ID_ATTRIBUTENAME));
    	entity.setTitle(element.attributeValue(TITLE_ATTRIBUTENAME));
    	// subentities
    	List<Entity> subentities = entity.getDendroEntities();
    	// iterate through child elements
        for ( Iterator<Element> i = element.elementIterator(entityTagsMap.get(ValuesEntity.class)); i.hasNext(); ) {
            Element subElement = i.next();
            ValuesEntity subEntity = new ValuesEntity();
            subentities.add(subEntity);
            buildValuesEntityTree(subEntity, subElement);// go deeper
        }
    }
}
