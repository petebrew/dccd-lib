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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.search.DccdSB;

import org.apache.log4j.Logger;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasObject;
import org.tridas.spatial.GMLPointSRSHandler;
import org.tridas.util.TridasObjectEx;

import com.jhlabs.map.proj.ProjectionException;

/**
 * Wraps the TridasObject
 * 
 * @author paulboon
 */
public class ObjectEntity extends AbstractEntity
{
	private static Logger					logger	= Logger.getLogger(ObjectEntity.class);
	private static final long	serialVersionUID	= 1247433031777899223L;
	private TridasObject		tridasObject		= null;
	private List<TridasElement>	elementsPruned		= null;
	private List<TridasObject>	objectsPruned		= null;

	public ObjectEntity()
	{
		// empty
	}

	public ObjectEntity(TridasObject tridasObject)
	{
		this.tridasObject = tridasObject;
		setTitle(tridasObject.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.OBJECT;
	}

	@Override
	public String getUnitLabel()
	{
		return "ObjectEntity";
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// child-objects, or parent-objects info might already be there
			// just add our own information

			// tridas.object.title
			List<String> tridasObjectTitle = searchBean.getTridasObjectTitle();
			if (tridasObjectTitle == null)
				tridasObjectTitle = new ArrayList<String>();
			if (tridasObject.isSetTitle())
			{
				tridasObjectTitle.add(tridasObject.getTitle());
			}
			searchBean.setTridasObjectTitle(tridasObjectTitle);

			// tridas.object.identifier
			List<String> tridasObjectIdentifier = searchBean.getTridasObjectIdentifier();
			if (tridasObjectIdentifier == null)
				tridasObjectIdentifier = new ArrayList<String>();
			if (tridasObject.isSetIdentifier() && tridasObject.getIdentifier().isSetValue())
			{
				// Note: ignore domain
				tridasObjectIdentifier.add(tridasObject.getIdentifier().getValue());
			}
			searchBean.setTridasObjectIdentifier(tridasObjectIdentifier);

			// tridas.object.type
			// tridas.object.type.normal
			List<String> tridasObjectType = searchBean.getTridasObjectType();
			if (tridasObjectType == null)
				tridasObjectType = new ArrayList<String>();
			List<String> tridasObjectTypeNormal = searchBean.getTridasObjectTypeNormal();
			if (tridasObjectTypeNormal == null)
				tridasObjectTypeNormal = new ArrayList<String>();
			if (tridasObject.isSetType())
			{
				ControlledVoc type = tridasObject.getType();

				if (type.isSetValue())
					tridasObjectType.add(type.getValue());
				else
					tridasObjectType.add("");

				if (type.isSetNormal())
					tridasObjectTypeNormal.add(type.getNormal());
				else
					tridasObjectTypeNormal.add("");
			}
			searchBean.setTridasObjectType(tridasObjectType);
			searchBean.setTridasObjectTypeNormal(tridasObjectTypeNormal);

			// tridas.object.creator
			List<String> tridasObjectCreator = searchBean.getTridasObjectCreator();
			if (tridasObjectCreator == null)
				tridasObjectCreator = new ArrayList<String>();
			if (tridasObject.isSetCreator())
			{
				tridasObjectCreator.add(tridasObject.getCreator());
			}
			searchBean.setTridasObjectCreator(tridasObjectCreator);

			// tridas.object.coverage.coverageTemporalFoundation
			List<String> tridasObjectCoverageCoveragetemporalfoundation = searchBean.getTridasObjectCoverageCoveragetemporalfoundation();
			if (tridasObjectCoverageCoveragetemporalfoundation == null)
				tridasObjectCoverageCoveragetemporalfoundation = new ArrayList<String>();
			if (tridasObject.isSetCoverage() && tridasObject.getCoverage().isSetCoverageTemporalFoundation())
			{
				tridasObjectCoverageCoveragetemporalfoundation.add(tridasObject.getCoverage().getCoverageTemporalFoundation());
			}
			searchBean.setTridasObjectCoverageCoveragetemporalfoundation(tridasObjectCoverageCoveragetemporalfoundation);

			// tridas.object.location.locationType
			// tridas.object.location.locationComment
			List<String> tridasObjectLocationLocationcomment = searchBean.getTridasObjectLocationLocationcomment();
			if (tridasObjectLocationLocationcomment == null)
				tridasObjectLocationLocationcomment = new ArrayList<String>();
			List<String> tridasObjectLocationLocationtype = searchBean.getTridasObjectLocationLocationtype();
			if (tridasObjectLocationLocationtype == null)
				tridasObjectLocationLocationtype = new ArrayList<String>();
			if (tridasObject.isSetLocation())
			{
				TridasLocation location = tridasObject.getLocation();
				if (location.isSetLocationComment())
					tridasObjectLocationLocationcomment.add(location.getLocationComment());
				if (location.isSetLocationType())
					tridasObjectLocationLocationtype.add(location.getLocationType().value());
				
				// Handle geo location; lng, lat
				if (location.isSetLocationGeometry())
				{
					TridasLocationGeometry locationGeometry = location.getLocationGeometry();
					// for now only use Points 
					if (locationGeometry.isSetPoint() && locationGeometry.getPoint().isSetPos()) 
					{
						PointType point = locationGeometry.getPoint();
						Pos pos = point.getPos();
						// we need the two first coordinates
						if (pos.isSetValues() && pos.getValues().size() > 1)
						{
							// Use DendroFileIO to detect the CRS and convert coordinates to WGS84 when needed
							try 
							{
								GMLPointSRSHandler handler = new GMLPointSRSHandler(point);
								Double latitude = handler.getWGS84LatCoord();
								Double longitude = handler.getWGS84LongCoord();
								searchBean.setLat(latitude);
								searchBean.setLng(longitude);
								logger.debug("Indexing WGS84 coordinates: " + 
										"[" + latitude + ", " + longitude + "] from: " +
										point.getSrsName());

							}
							catch(ProjectionException e)
							{
								logger.info("Could not convert coordinates to WGS84: " + 
										point.getSrsName() + 
										"[" + pos.getValues().get(0) + ", " + pos.getValues().get(1) + "]", e);
							}
							/* Without DendroFileIO
							//
							// The order of the coordinates is a source of confusion and discussion, 
							// it is not specified by the GML, but by the Coordinate Reference System in the srsName
							// Officially WGS84 should be lat/long and have an "urn:ogc:def:crs:EPSG:6.6:4326"
							// But most users expect long/lat if "WGS84" or "EPSG:4326" is used and only lat/long 
							// if the 'official' urn is used.
							//
							// TRiDABASE now uses "WGS 84" long/lat
							// and TRiCYCLE uses "urn:ogc:def:crs:EPSG:6.6:4326" with lat/long
							// A quick fix to let DCCD work with both is to check for the "urn:ogc:def:crs"

							// determine order
							boolean isLonLat = true;
							if (point.isSetSrsName())
							{
								String srsName = point.getSrsName().trim().toLowerCase();
								if (srsName.startsWith("urn:ogc:def:crs"))
									isLonLat = false; // lat/lon
							}

							// set the coordinates in the bean
							if (isLonLat)
							{
								// lon/lat
								searchBean.setLng(pos.getValues().get(0));
								searchBean.setLat(pos.getValues().get(1));
							}
							else
							{
								// lat/lon
								searchBean.setLat(pos.getValues().get(0));
								searchBean.setLng(pos.getValues().get(1));
							}
							*/
						}
					}
				}
				
			}
			searchBean.setTridasObjectLocationLocationcomment(tridasObjectLocationLocationcomment);
			searchBean.setTridasObjectLocationLocationtype(tridasObjectLocationLocationtype);

			// tridas.object.genericField
			List<String> tridasObjectGenericfield = searchBean.getTridasObjectGenericfield();
			if (tridasObjectGenericfield == null)
				tridasObjectGenericfield = new ArrayList<String>();

			if (tridasObject.isSetGenericFields())
			{
				for (TridasGenericField genericfield : tridasObject.getGenericFields())
				{
					if (genericfield.isSetValue())
						tridasObjectGenericfield.add(genericfield.getValue());
				}
			}
			searchBean.setTridasObjectGenericfield(tridasObjectGenericfield);
		}

		// logger.debug("After filling:\n" + searchBean.toString());
		return searchBean;
	}

	// only fill with the'open access' information
	//
	// Open access information:
	// - Object title
	// SHOULD also have Type!!!!
	public DccdSB minimalFillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// child-objects, or parent-objects info might already be there
			// just add our own information

			// tridas.object.title
			List<String> tridasObjectTitle = searchBean.getTridasObjectTitle();
			if (tridasObjectTitle == null)
				tridasObjectTitle = new ArrayList<String>();
			if (tridasObject.isSetTitle())
			{
				tridasObjectTitle.add(tridasObject.getTitle());
			}
			searchBean.setTridasObjectTitle(tridasObjectTitle);
			
			// tridas.object.type
			// tridas.object.type.normal
			List<String> tridasObjectType = searchBean.getTridasObjectType();
			if (tridasObjectType == null)
				tridasObjectType = new ArrayList<String>();
			List<String> tridasObjectTypeNormal = searchBean.getTridasObjectTypeNormal();
			if (tridasObjectTypeNormal == null)
				tridasObjectTypeNormal = new ArrayList<String>();
			if (tridasObject.isSetType())
			{
				ControlledVoc type = tridasObject.getType();

				if (type.isSetValue())
					tridasObjectType.add(type.getValue());
				else
					tridasObjectType.add("");

				if (type.isSetNormal())
					tridasObjectTypeNormal.add(type.getNormal());
				else
					tridasObjectTypeNormal.add("");
			}
			searchBean.setTridasObjectType(tridasObjectType);
			searchBean.setTridasObjectTypeNormal(tridasObjectTypeNormal);			
		}
		return searchBean;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasObject.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasObject == null)
			return; // nothing to do

		// add sub-objects..
		Iterator<TridasObject> i = tridasObject.getObjects().iterator();
		while (i.hasNext())
		{
			TridasObject subObject = i.next();
			ObjectEntity subObjectEntity = new ObjectEntity(subObject);
			subEntities.add(subObjectEntity);
			subObjectEntity.buildEntitySubTree();// go deeper
		}

		// add elements
		Iterator<TridasElement> iElem = tridasObject.getElements().iterator();
		while (iElem.hasNext())
		{
			TridasElement element = iElem.next();
			ElementEntity elementEntity = new ElementEntity(element);
			subEntities.add(elementEntity);
			elementEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasObject;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasObject = (TridasObject) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasObject.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasObject == null)
			return; // nothing to do
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// subentities are object or element
			Object tridas = subEntity.getTridasAsObject();

			if (tridas.getClass().equals(TridasObject.class) || tridas.getClass().equals(TridasObjectEx.class))
			{// .getName().contentEquals("org.tridas.schema.TridasObject")) {
				// object
				tridasObject.getObjects().add((TridasObject) tridas);
			}
			else
			{
				// element
				tridasObject.getElements().add((TridasElement) tridas);
			}
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasObject objectToPrune = tridasObject;

		if (objectToPrune != null && 
			elementsPruned == null &&
			objectsPruned == null) 
		{
			elementsPruned = objectToPrune.getElements();
			List<TridasElement> emptyElm = Collections.emptyList();
			objectToPrune.setElements(emptyElm);
	
			objectsPruned = objectToPrune.getObjects();
			List<TridasObject> emptyObj = Collections.emptyList();
			objectToPrune.setObjects(emptyObj);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasObject objectToUnprune = tridasObject;

		if (objectToUnprune != null && 
			elementsPruned != null && 
			objectsPruned != null) 
		{
			objectToUnprune.setElements(elementsPruned);
			elementsPruned = null;

			objectToUnprune.setObjects(objectsPruned);
			objectsPruned = null;
		}
	}
	
	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		
		if (hasTridas())
		{
			TridasObject tridas = (TridasObject)getTridasAsObject();
			if (tridas.isSetFiles()) 
			{
				List<TridasFile> files = tridas.getFiles();
				resultList.addAll(getFileNames(files));
			}
		}
		
		//logger.debug("Found Associated Files: " + resultList.size());
		
		return resultList;		
	}			
}
