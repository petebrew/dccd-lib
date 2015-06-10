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

import nl.knaw.dans.common.lang.search.bean.StringListCollapserConverter;
import nl.knaw.dans.common.lang.search.exceptions.SearchBeanConverterException;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.search.DccdObjectSB;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;

import org.apache.log4j.Logger;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasAddress;
//import org.tridas.schema.TridasCategory;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasResearch;
import org.tridas.util.TridasObjectEx;

/**
 * Topmost entity, wraps the TridasProject
 * 
 * @author paulboon
 */
public class ProjectEntity extends AbstractEntity
{
	@SuppressWarnings("unused")
	private static Logger				logger				= Logger.getLogger(ProjectEntity.class);
	private static final long			serialVersionUID	= 7889287410535003498L;
	private TridasProject				tridasProject		= null;
	private List<TridasDerivedSeries>	derivedSeriesPruned	= null;
	private List<TridasObject>			objectsPruned		= null;

	/**
	 *
	 */
	public ProjectEntity()
	{
		// empty
	}

	/**
	 * @param tridasProject
	 *        The tridas project to wrap
	 */
	public ProjectEntity(TridasProject tridasProject)
	{
		this.tridasProject = tridasProject;
		setTitle(tridasProject.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.PROJECT;
	}

	@Override
	public String getUnitLabel()
	{
		return "ProjectEntity";
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (!hasTridas())
			return searchBean; // just do nothing

		// tridas.project.title
		searchBean.setTridasProjectTitle(tridasProject.getTitle());

		// tridas.project.identifier
		if (tridasProject.isSetIdentifier())
		{
			searchBean.setTridasProjectIdentifier(tridasProject.getIdentifier().getValue());
			// for exact matching
			searchBean.setTridasProjectIdentifierExact(tridasProject.getIdentifier().getValue());
			
			if (tridasProject.getIdentifier().isSetDomain())
			{
				// Note that the domain is ignored in search, but needed as result
				searchBean.setTridasProjectIdentifierDomain(tridasProject.getIdentifier().getDomain());
				// for exact matching
				searchBean.setTridasProjectIdentifierDomainExact(tridasProject.getIdentifier().getDomain());
			}
		}

		// tridas.project.comments
		searchBean.setTridasProjectComments(tridasProject.getComments());

		// tridas.project.type
		// tridas.project.type.normal
		//
		if (tridasProject.isSetTypes() && !tridasProject.getTypes().isEmpty())
		{
			List<String> tridasProjectType = new ArrayList<String>();
			List<String> tridasProjectTypeNormal = new ArrayList<String>();
			List<ControlledVoc> types = tridasProject.getTypes();
			for (ControlledVoc type : types)
			{
				if (type.isSetValue())
					tridasProjectType.add(type.getValue());
				else
					tridasProjectType.add("");

				if (type.isSetNormal())
					tridasProjectTypeNormal.add(type.getNormal());
				else
					tridasProjectTypeNormal.add("");
			}
			searchBean.setTridasProjectType(tridasProjectType);
			searchBean.setTridasProjectTypeNormal(tridasProjectTypeNormal);
		}

		// tridas.project.description
		searchBean.setTridasProjectDescription(tridasProject.getDescription());

		// tridas.project.laboratory.name
		// tridas.project.laboratory.name.acronym
		// tridas.project.laboratory.place
		// tridas.project.laboratory.country
		if (tridasProject.isSetLaboratories() && !tridasProject.getLaboratories().isEmpty())
		{
			List<String> laboratoryNames = new ArrayList<String>();
			List<String> laboratoryNameAcronyms = new ArrayList<String>();
			List<String> laboratoryPlaces = new ArrayList<String>();
			List<String> laboratoryCountries = new ArrayList<String>();

			for (TridasLaboratory laboratory : tridasProject.getLaboratories())
			{
				if (laboratory.getName().isSetValue())
					laboratoryNames.add(laboratory.getName().getValue());
				else
					laboratoryNames.add("");

				if (laboratory.getName().isSetAcronym())
					laboratoryNameAcronyms.add(laboratory.getName().getAcronym());
				else
					laboratoryNameAcronyms.add("");

				if (laboratory.isSetAddress())
				{
					TridasAddress address = laboratory.getAddress();

					// Note: placing in Place, but should be renamed to CityOrTown
					// TODO add all other address fields
					if (address.isSetCityOrTown())
						laboratoryPlaces.add(address.getCityOrTown());
					else
						laboratoryPlaces.add("");

					if (address.isSetCountry())
						laboratoryCountries.add(address.getCountry());
					else
						laboratoryCountries.add("");
				}
				else
				{
					// padd with empty strings
					laboratoryPlaces.add("");
					laboratoryCountries.add("");
				}
			}
			searchBean.setTridasProjectLaboratoryName(laboratoryNames);
			searchBean.setTridasProjectLaboratoryNameAcronym(laboratoryNameAcronyms);
			searchBean.setTridasProjectLaboratoryAddressCityortown(laboratoryPlaces);
			searchBean.setTridasProjectLaboratoryAddressCountry(laboratoryCountries);
		}

		// Again, but now for the combined lab info
		// uses comma separated string
		if (tridasProject.isSetLaboratories() && !tridasProject.getLaboratories().isEmpty())
		{
			List<String> laboratoryCombined = new ArrayList<String>();
			for (TridasLaboratory laboratory : tridasProject.getLaboratories())
			{
				List<String> combined = new ArrayList<String>();

				if (laboratory.getName().isSetValue())
					combined.add(laboratory.getName().getValue());
				else
					combined.add("");

				if (laboratory.getName().isSetAcronym())
					combined.add(laboratory.getName().getAcronym());
				else
					combined.add("");

				if (laboratory.isSetAddress())
				{
					TridasAddress address = laboratory.getAddress();

					// Note: placing in Place, but should be renamed to CityOrTown
					// TODO add all other address fields
					if (address.isSetCityOrTown())
						combined.add(address.getCityOrTown());
					else
						combined.add("");

					if (address.isSetCountry())
						combined.add(address.getCountry());
					else
						combined.add("");
				}
				else
				{
					// padd with empty strings
					combined.add("");
					combined.add("");
				}

				// collapse into String, comma separated
				try
				{
					StringListCollapserConverter conv = new StringListCollapserConverter();
					String combinedString = (String) conv.toFieldValue(combined);

					laboratoryCombined.add(combinedString);
				}
				catch (SearchBeanConverterException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			searchBean.setTridasProjectLaboratoryCombined(laboratoryCombined);
		}

		// tridas.project.category
		// tridas.project.category.normal
		// tridas.project.category.normalTridas
		if (tridasProject.isSetCategory())
		{
			ControlledVoc category = tridasProject.getCategory();
			if (category.isSetValue())
				searchBean.setTridasProjectCategory(category.getValue());
			if (category.isSetNormal())
				searchBean.setTridasProjectCategoryNormal(category.getNormal());
//			if (category.isSetNormalTridas())
//				searchBean.setTridasProjectCategoryNormaltridas(category.getNormalTridas().value());
		}

		// tridas.project.investigator
		if (tridasProject.isSetInvestigator())
			searchBean.setTridasProjectInvestigator(tridasProject.getInvestigator());

		// tridas.project.period
		if (tridasProject.isSetPeriod())
			searchBean.setTridasProjectPeriod(tridasProject.getPeriod());

		// tridas.project.research.identifier
		// tridas.project.research.identifier.domain
		// tridas.project.research.description
		if (tridasProject.isSetResearches())
		{
			List<String> tridasProjectResearchIdentifier = new ArrayList<String>();
			List<String> tridasProjectResearchIdentifierDomain = new ArrayList<String>();
			List<String> tridasProjectResearchDescription = new ArrayList<String>();
			for (TridasResearch research : tridasProject.getResearches())
			{
				if (research.isSetIdentifier())
				{
					TridasIdentifier identifier = research.getIdentifier();
					if (identifier.isSetValue())
						tridasProjectResearchIdentifier.add(identifier.getValue());
					else
						tridasProjectResearchIdentifier.add("");
					if (identifier.isSetDomain())
						tridasProjectResearchIdentifierDomain.add(identifier.getDomain());
					else
						tridasProjectResearchIdentifierDomain.add("");
				}
				else
				{
					tridasProjectResearchIdentifier.add("");
					tridasProjectResearchIdentifierDomain.add("");
				}

				if (research.isSetDescription())
					tridasProjectResearchDescription.add(research.getDescription());
				else
					tridasProjectResearchDescription.add("");
			}
			searchBean.setTridasProjectResearchIdentifier(tridasProjectResearchIdentifier);
			searchBean.setTridasProjectResearchIdentifierDomain(tridasProjectResearchIdentifierDomain);
			searchBean.setTridasProjectResearchDescription(tridasProjectResearchDescription);
		}

		return searchBean;
	}

	// TODO Refactor into smaller functions
	// only fill with the'open access' information
	//
	// Open access information: 
	// - Project title
	// - Project identifier
	// - Project type
	// - Principal investigator
	// - Period of research
	// - Type of material that was studied (archaeology, ship's archaeology, furniture et cetera)
	// - Laboratory
	public DccdSB minimalFillSearchBean(DccdSB searchBean)
	{
		if (!hasTridas())
			return searchBean; // just do nothing

		// tridas.project.title
		searchBean.setTridasProjectTitle(tridasProject.getTitle());

		// tridas.project.identifier
		if (tridasProject.isSetIdentifier())
		{
			searchBean.setTridasProjectIdentifier(tridasProject.getIdentifier().getValue());
			// for exact matching
			searchBean.setTridasProjectIdentifierExact(tridasProject.getIdentifier().getValue());
			
			if (tridasProject.getIdentifier().isSetDomain())
			{
				// Note that the domain is ignored in search, but needed as result
				searchBean.setTridasProjectIdentifierDomain(tridasProject.getIdentifier().getDomain());
				// for exact matching
				searchBean.setTridasProjectIdentifierDomainExact(tridasProject.getIdentifier().getDomain());
			}
		}

		// tridas.project.type
		// tridas.project.type.normal
		//
		if (tridasProject.isSetTypes() && !tridasProject.getTypes().isEmpty())
		{
			List<String> tridasProjectType = new ArrayList<String>();
			List<String> tridasProjectTypeNormal = new ArrayList<String>();
			List<ControlledVoc> types = tridasProject.getTypes();
			for (ControlledVoc type : types)
			{
				if (type.isSetValue())
					tridasProjectType.add(type.getValue());
				else
					tridasProjectType.add("");

				if (type.isSetNormal())
					tridasProjectTypeNormal.add(type.getNormal());
				else
					tridasProjectTypeNormal.add("");
			}
			searchBean.setTridasProjectType(tridasProjectType);
			searchBean.setTridasProjectTypeNormal(tridasProjectTypeNormal);
		}

		// tridas.project.laboratory.name
		// tridas.project.laboratory.name.acronym
		// tridas.project.laboratory.place
		// tridas.project.laboratory.country
		if (tridasProject.isSetLaboratories() && !tridasProject.getLaboratories().isEmpty())
		{
			List<String> laboratoryNames = new ArrayList<String>();
			List<String> laboratoryNameAcronyms = new ArrayList<String>();
			List<String> laboratoryPlaces = new ArrayList<String>();
			List<String> laboratoryCountries = new ArrayList<String>();

			for (TridasLaboratory laboratory : tridasProject.getLaboratories())
			{
				if (laboratory.getName().isSetValue())
					laboratoryNames.add(laboratory.getName().getValue());
				else
					laboratoryNames.add("");

				if (laboratory.getName().isSetAcronym())
					laboratoryNameAcronyms.add(laboratory.getName().getAcronym());
				else
					laboratoryNameAcronyms.add("");

				if (laboratory.isSetAddress())
				{
					TridasAddress address = laboratory.getAddress();

					// Note: placing in Place, but should be renamed to CityOrTown
					// TODO add all other address fields
					if (address.isSetCityOrTown())
						laboratoryPlaces.add(address.getCityOrTown());
					else
						laboratoryPlaces.add("");

					if (address.isSetCountry())
						laboratoryCountries.add(address.getCountry());
					else
						laboratoryCountries.add("");
				}
				else
				{
					// padd with empty strings
					laboratoryPlaces.add("");
					laboratoryCountries.add("");
				}
			}
			searchBean.setTridasProjectLaboratoryName(laboratoryNames);
			searchBean.setTridasProjectLaboratoryNameAcronym(laboratoryNameAcronyms);
			searchBean.setTridasProjectLaboratoryAddressCityortown(laboratoryPlaces);
			searchBean.setTridasProjectLaboratoryAddressCountry(laboratoryCountries);
		}

		// Again, but now for the combined lab info
		// uses comma separated string
		if (tridasProject.isSetLaboratories() && !tridasProject.getLaboratories().isEmpty())
		{
			List<String> laboratoryCombined = new ArrayList<String>();
			for (TridasLaboratory laboratory : tridasProject.getLaboratories())
			{
				List<String> combined = new ArrayList<String>();

				if (laboratory.getName().isSetValue())
					combined.add(laboratory.getName().getValue());
				else
					combined.add("");

				if (laboratory.getName().isSetAcronym())
					combined.add(laboratory.getName().getAcronym());
				else
					combined.add("");

				if (laboratory.isSetAddress())
				{
					TridasAddress address = laboratory.getAddress();

					// Note: placing in Place, but should be renamed to CityOrTown
					// TODO add all other address fields
					if (address.isSetCityOrTown())
						combined.add(address.getCityOrTown());
					else
						combined.add("");

					if (address.isSetCountry())
						combined.add(address.getCountry());
					else
						combined.add("");
				}
				else
				{
					// padd with empty strings
					combined.add("");
					combined.add("");
				}

				// collapse into String, comma separated
				try
				{
					StringListCollapserConverter conv = new StringListCollapserConverter();
					String combinedString = (String) conv.toFieldValue(combined);

					laboratoryCombined.add(combinedString);
				}
				catch (SearchBeanConverterException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			searchBean.setTridasProjectLaboratoryCombined(laboratoryCombined);
		}

		// tridas.project.category
		// tridas.project.category.normal
		// tridas.project.category.normalTridas
		if (tridasProject.isSetCategory())
		{
			ControlledVoc category = tridasProject.getCategory();
			if (category.isSetValue())
				searchBean.setTridasProjectCategory(category.getValue());
			if (category.isSetNormal())
				searchBean.setTridasProjectCategoryNormal(category.getNormal());
//			if (category.isSetNormalTridas())
//				searchBean.setTridasProjectCategoryNormaltridas(category.getNormalTridas().value());
		}

		// tridas.project.investigator
		if (tridasProject.isSetInvestigator())
			searchBean.setTridasProjectInvestigator(tridasProject.getInvestigator());

		// tridas.project.period
		if (tridasProject.isSetPeriod())
			searchBean.setTridasProjectPeriod(tridasProject.getPeriod());

		return searchBean;
	}
	
	/**
	 * get the searchbeans for each ObjectEntity
	 * fill the beans with all information, regardless of permission.
	 * 
	 * @return
	 */
	public List<DccdSB> getSearchBeans()
	{
		List<DccdSB> searchBeans = new ArrayList<DccdSB>();
		if (!hasTridas())
			return searchBeans; // just an empty list

		// get all the ObjectEntity's in the tree
		List<Entity> entities = getSubTreeAsList();
		for (Entity entity : entities)
		{
			// only objects, exclude derived series
			if (entity instanceof ObjectEntity)
			{
				// create a bean
				DccdSB searchBean = new DccdObjectSB();
				// fill it
				// first with the project info, maybe this can be done more efficiently
				// because same conversions are done for every Object again
				searchBean = fillSearchBean(searchBean);
				// then the object info
				searchBean = entity.fillSearchBean(searchBean);

				// all the ObjectEntities subentities must fill this bean as well
				// but not Objects?
				List<Entity> subentities = entity.getSubTreeAsList();
				for (Entity subentity : subentities)
				{
					searchBean = subentity.fillSearchBean(searchBean);
				}

				// Note id should be the (system) identifier in the repository (sid)
				// just hoping that is is set correctly when read from the repository!!!
				searchBean.setDatastreamId(entity.getId());// should be the repository id for this peace of info

				// add to the list
				searchBeans.add(searchBean);
			}
		}

		return searchBeans;
	}

	/**
	 * Only fill beans with information allowed by the given permissionlevel
	 * 
	 * @param permissionLevel
	 * @return
	 */
	public List<DccdSB> getSearchBeans(ProjectPermissionLevel permissionLevel)
	{
		List<DccdSB> searchBeans = new ArrayList<DccdSB>();
		if (!hasTridas())
			return searchBeans; // just an empty list

		// get all the ObjectEntity's in the tree
		List<Entity> entities = getSubTreeAsList();
		for (Entity entity : entities)
		{
			// only objects (note: derived series are excluded from search)
			if (entity instanceof ObjectEntity)
			{
				// create a bean
				DccdSB searchBean = new DccdObjectSB();
				// fill it
				// first with the project info, maybe this can be done more efficiently
				// because same conversions are done for every Object again

				// handle minimal access, always fill with minimal and only when allowed; the rest
				searchBean = (DccdObjectSB) minimalFillSearchBean(searchBean);
				if (isPermittedBy(permissionLevel))
				{
					searchBean = (DccdObjectSB) fillSearchBean(searchBean);
				}
				
				// then the object info
				ObjectEntity objectEntity = (ObjectEntity)entity; // cast for readability
				searchBean = (DccdObjectSB) objectEntity.minimalFillSearchBean(searchBean);
				if (objectEntity.isPermittedBy(permissionLevel))
				{
					searchBean = (DccdObjectSB) objectEntity.fillSearchBean(searchBean);
				}
				
				// the ObjectEntities subentities which are allowed to fill this bean
				List<Entity> subentities = entity.getSubTreeAsList();
				for (Entity subentity : subentities)
				{
					if (subentity.isPermittedBy(permissionLevel))
					{
						searchBean = subentity.fillSearchBean(searchBean);
					}
				}

				// Note: id should be the (system) identifier in the repository (sid)
				searchBean.setDatastreamId(entity.getId());

				// add to the list
				searchBeans.add(searchBean);
			}
		}			
		
		return searchBeans;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasProject.class;
	}

	// TODO: LB20090923: 1) this method calls methods like a waterfall
	// while other tree walking mechanisms are contained within one class.
	// Preferred is to stick to one mechanism. Can another class take care of this?

	/**
	 * Builds the tree of entities which corresponds to the structure of the tridas data Delegates building by having each sub-entity build it's own sub-tree
	 */
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasProject == null)
			return; // nothing to do

		// add nodes for TridasObjects for this Project
		Iterator<TridasObject> i = tridasProject.getObjects().iterator();
		while (i.hasNext())
		{
			TridasObject object = i.next();
			ObjectEntity objectEntity = new ObjectEntity(object);
			subEntities.add(objectEntity);
			objectEntity.buildEntitySubTree();// go deeper
		}
		// add derivedseries
		Iterator<TridasDerivedSeries> iDerived = tridasProject.getDerivedSeries().iterator();
		while (iDerived.hasNext())
		{
			TridasDerivedSeries derivedSeries = iDerived.next();
			DerivedSeriesEntity derivedSeriesEntity = new DerivedSeriesEntity(derivedSeries);
			subEntities.add(derivedSeriesEntity);
			derivedSeriesEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the buildTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasProject;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasProject = (TridasProject) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasProject.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasProject == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// subentities are object or element
			Object tridas = subEntity.getTridasAsObject();

			if (tridas.getClass().equals(TridasObject.class) || tridas.getClass().equals(TridasObjectEx.class))
			{// .getName().contentEquals("org.tridas.schema.TridasObject")) {
				// object
				tridasProject.getObjects().add((TridasObject) tridas);
			}
			else
			{
				// derivedseries
				tridasProject.getDerivedSeries().add((TridasDerivedSeries) tridas);
			}
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasProject objectToPrune = tridasProject;

		if (objectToPrune != null && 
			derivedSeriesPruned == null &&
			objectsPruned == null)
		{
			derivedSeriesPruned = objectToPrune.getDerivedSeries();
			List<TridasDerivedSeries> emptyDS = Collections.emptyList();
			objectToPrune.setDerivedSeries(emptyDS);
	
			objectsPruned = objectToPrune.getObjects();
			List<TridasObject> emptyObj = Collections.emptyList();
			objectToPrune.setObjects(emptyObj);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasProject objectToUnprune = tridasProject;

		if (objectToUnprune != null && 
			derivedSeriesPruned != null && 
			objectsPruned != null) 
		{
			objectToUnprune.setDerivedSeries(derivedSeriesPruned);
			derivedSeriesPruned = null;

			objectToUnprune.setObjects(objectsPruned);
			objectsPruned = null;
		}
	}

	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		
		if (hasTridas())
		{
			TridasProject tridas = (TridasProject)getTridasAsObject();
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
