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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nl.knaw.dans.common.jibx.bean.JiBXDublinCoreMetadata;
import nl.knaw.dans.common.lang.dataset.AccessCategory;
import nl.knaw.dans.common.lang.dataset.DatasetSB;
import nl.knaw.dans.common.lang.dataset.DatasetState;
import nl.knaw.dans.common.lang.repo.AbstractDataModelObject;
import nl.knaw.dans.common.lang.repo.BinaryUnit;
import nl.knaw.dans.common.lang.repo.DmoNamespace;
import nl.knaw.dans.common.lang.repo.MetadataUnit;
import nl.knaw.dans.common.lang.repo.bean.DublinCoreMetadata;
import nl.knaw.dans.common.lang.reposearch.HasSearchBeans;
import nl.knaw.dans.common.lang.search.IndexDocument;
import nl.knaw.dans.dccd.model.DccdUser.Role;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ObjectEntity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;
import nl.knaw.dans.dccd.tridas.EmptyObjectFactory;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasVocabulary;


/**
 * Holds the 'root' of our 'object tree'
 * A dendrochronological research of a particular object or group of objects
 *
 * note: maybe refactor to DendroProjectData, or DendroDataModel?
 *
 * @author paulboon
 *
 */
public class Project extends AbstractDataModelObject implements HasSearchBeans, Serializable
{
	private static Logger logger = Logger.getLogger(Project.class);
	private static final long serialVersionUID = 7530516066486651381L;

	public static final String DCCD_PROJECT_CONTENTMODEL = "fedora-system:DCCD-PROJECT";
	public static final String OBJECT_NAMESPACE = "dccd";
	public static final DmoNamespace NAMESPACE = new DmoNamespace(OBJECT_NAMESPACE);
	
	// the genericfield name for the treering data file format
	public static final String DATAFILE_INDICATOR =  "dccd.treeringdatafile";
	public static final String DATAFILE_INDICATOR_UPLOADED = DATAFILE_INDICATOR+".uploaded";//"dccd.treeringdatafile.uploaded";

	private ProjectCreationMetadata creationMetadata = new ProjectCreationMetadata();
	private ProjectPermissionMetadata permissionMetadata = new ProjectPermissionMetadata();
	private ProjectAdministrativeMetadata administrativeMetadata = new ProjectAdministrativeMetadata();

	private List<DccdOriginalFileBinaryUnit> originalFileBinaryUnits = new ArrayList<DccdOriginalFileBinaryUnit>(); 
	private List<DccdAssociatedFileBinaryUnit> associatedFileBinaryUnits = new ArrayList<DccdAssociatedFileBinaryUnit>(); 
	
	private ProjectVocabulary vocabulary = new ProjectVocabulary();// but without a Tridas object
	
	// tree of DendroEntity objects
	public EntityTree entityTree = new EntityTree(); // just an empty one
	// tridas tree, as generated with JAXB
	private TridasProject tridas = null; // note: could use transient and make classes not serializable
	// The filename, if this project was read from a file (TRiDaS), we need to store this name
	private String fileName = "";
	// The selection for the language used in the TRiDaS is not part of TRiDaS
	// therefore we keep it at the Project level
	private Locale tridasLanguage = Locale.ENGLISH;
	// used as (system) identifier when storing or retrieving or referring
//	private String sid = "";
	// used as short descriptive name for this project when displaying
	private String title = "";

	/**
     * Default Constructor
     */
	public Project()
	{
		super(); // sid will be set later
	}

	public Project(String id)
	{
		super(id);

//		this.sid = id;
	}

	/** Constructor
	 *
	 * @param id
	 * @param title
	 */
	public Project(String id, String title)
	{
		super(id);

//		this.sid = id;
		this.title = title;
	}

	// called when deserializing from the repository, 
	// sets information neeeded when downloading
	public void addOriginalFileBinaryUnit(String fileName, String unitId) throws IOException
	{
		DccdOriginalFileBinaryUnit unit = new DccdOriginalFileBinaryUnit();
		//unit.setFile(file);
		unit.setFileName(fileName);
		
		unit.setUnitId(unitId);
		originalFileBinaryUnits.add(unit);
	}
	
	// called when constructng/uplaoding
	public void addOriginalFile(File file) throws IOException
	{
		DccdOriginalFileBinaryUnit unit = new DccdOriginalFileBinaryUnit();
		unit.setFile(file);

		// construct the unitId
		int index = originalFileBinaryUnits.size();
		unit.setUnitId(DccdOriginalFileBinaryUnit.UNIT_ID_PREFIX + 
				(index+1));	
		
		originalFileBinaryUnits.add(unit);
	}

	// called when deserializing from the repository, 
	// sets information neeeded when downloading
	public void addAssociatedFileBinaryUnit(String fileName, String unitId) throws IOException
	{
		DccdAssociatedFileBinaryUnit unit = new DccdAssociatedFileBinaryUnit();
		//unit.setFile(file);
		unit.setFileName(fileName);
		
		unit.setUnitId(unitId);
		associatedFileBinaryUnits.add(unit);
	}
	
	public void addAssociatedFile(File file) throws IOException
	{
		DccdAssociatedFileBinaryUnit unit = new DccdAssociatedFileBinaryUnit();
		unit.setFile(file);

		// construct the unitId
		int index = associatedFileBinaryUnits.size();
		unit.setUnitId(DccdAssociatedFileBinaryUnit.UNIT_ID_PREFIX + 
				(index+1));	

		associatedFileBinaryUnits.add(unit);
	}
	
	public ProjectAdministrativeMetadata getAdministrativeMetadata()
	{
		return administrativeMetadata;
	}

	public void setAdministrativeMetadata(
			ProjectAdministrativeMetadata administrativeMetadata)
	{
		this.administrativeMetadata = administrativeMetadata;
	}
	
	public ProjectPermissionMetadata getPermissionMetadata()
	{
		return permissionMetadata;
	}

	public void setPermissionMetadata(ProjectPermissionMetadata permissionMetadata)
	{
		this.permissionMetadata = permissionMetadata;
	}

	public ProjectCreationMetadata getCreationMetadata()
	{
		return creationMetadata;
	}

	public void setCreationMetadata(ProjectCreationMetadata creationMetadata)
	{
		this.creationMetadata = creationMetadata;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public Locale getTridasLanguage()
	{
		return tridasLanguage;
	}

	public void setTridasLanguage(Locale tridasLanguage)
	{
		this.tridasLanguage = tridasLanguage;
	}

	//TODO: LB20090923: throw exceptions when values have not been received yet for following properties .

	public TridasProject getTridas()
	{
		return tridas;
	}

	public void setTridas(TridasProject tridas)
	{
		this.tridas = tridas;
	}

	public boolean hasTridas()
	{
		return (this.tridas != null);
	}

	public ProjectVocabulary getVocabulary()
	{
		return vocabulary;
	}

	public void setVocabulary(ProjectVocabulary vocabulary)
	{
		this.vocabulary = vocabulary;
	}

	public void setTridasVocabulary(TridasVocabulary tridasVocabulary)
	{
		vocabulary.setTridasObject(tridasVocabulary);
	}
	
	public boolean hasTridasVocabulary()
	{
		return vocabulary.hasTridas();
	}
	
	
	// getTridasTridas, contains Project and Vocabulary
	public TridasTridas getTridasTridas()
	{
		// create one
		TridasTridas tridasTridas = (TridasTridas) EmptyObjectFactory.create(TridasTridas.class);
		
		if (hasTridas())
		{
			tridasTridas.getProjects().add(getTridas());
		}

		if (hasTridasVocabulary())
		{
			tridasTridas.setVocabulary((TridasVocabulary) vocabulary.getTridasAsObject());
		}
		
		return tridasTridas;
	}
	
	
	//TODO: LB20090923: the following two properties are used only for
	// receiving information about projects resulting from a search. It
	// is better to create a separate class for this. Something like
	// DendroProjectSearchResult
	// PBoon: No, sid is used for retrieving and ingesting

	public String getSid()
	{
//		return sid;
		return getStoreId();
	}

//	public void setSid(String sid)
//	{
//		this.sid = sid;
//	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Collection<IndexDocument> getIndexDocuments()
	{
		return null;
	}

	public boolean isDeletable()
	{
		// only a draft is deletable!
		if (getAdministrativeMetadata().getAdministrativeState() == DatasetState.DRAFT)
		{
			logger.debug("Deletable");
			return true;
		}
		else
		{
			logger.debug("Not deletable");
			return false;
		}		
	}

	@Override
	public String getLabel()
	{
		// This Tridas information seems most appropriate
		return getTitle();
	}

	public String getObjectNamespace()
	{
		return OBJECT_NAMESPACE;
	}
	
	/**
	 * Each search result corresponds to a search bean; 
	 * The project has a bean, but also each ObjectEntity in the Project produces a bean
	 */
	public Collection<? extends Object> getSearchBeans()
	{
		//return getCompleteSearchBeans();
		// Note: complete beans might be useful for testing
		
		return getPermittedSearchBeans();
	}
	
	/** all bean information, regardless of permissions
	 * 
	 * @return
	 */
	private Collection<? extends Object> getCompleteSearchBeans()
	{
		List<Object> searchBeans = new ArrayList<Object>();

		if (entityTree != null && entityTree.getProjectEntity() != null)
		{
			ProjectEntity projectEntity = entityTree.getProjectEntity();

			String stateString = getAdministrativeMetadata().getAdministrativeState().toString();
			DateTime lastStateChange = getAdministrativeMetadata().getLastStateChange();
			String permissionDefaultLevelString = getPermissionMetadata().getDefaultLevel().toString();
			
			// Add the Projects bean
			searchBeans.add(getCompleteProjectSearchBean());

			// Add all the Object beans
			List<DccdSB> newBeans = projectEntity.getSearchBeans();

			// set the id and state for all beans
			for (DccdSB newBean : newBeans)
			{
				newBean.setOwnerId(getOwnerId());
				// Note that these names are confusing and should be refactored
				newBean.setPid(getSid()); // the repository system id (sid)
				newBean.composeId();
				
				newBean.setAdministrativeState(stateString);
				newBean.setAdministrativeStateLastChange(lastStateChange);
				newBean.setPermissionDefaultLevel(permissionDefaultLevelString);
			}

			searchBeans.addAll(newBeans);

			/*
			// For easy
			DatasetSB dataset = new DatasetSB();
			dataset.setStoreId(getSid());
			dataset.setDublinCore(getDublinCoreMetadata());
			dataset.setAccessCategory(AccessCategory.ACCESS_ELSEWHERE);
			dataset.setState(getAdministrativeMetadata().getAdministrativeState());//DatasetState.PUBLISHED);

			searchBeans.add(dataset);
			*/
		}
		else
		{
			logger.debug("No project entity found!");
		}

		return searchBeans;
	}

	/**
	 * get the bean for the Project
	 * with all information, regardless the permission
	 *
	 * @return
	 */
	private DccdProjectSB getCompleteProjectSearchBean()
	{
		DccdProjectSB searchBean = new DccdProjectSB();
		//searchBean.setOwnerId(getOwnerId()); // should become managerId !
		searchBean.setOwnerId(getAdministrativeMetadata().getManagerId());
				
		// Note that these names are confusing and should be refactored
		searchBean.setPid(getSid()); // the repository system id (sid)
		searchBean.composeId();
		String stateString = getAdministrativeMetadata().getAdministrativeState().toString();
		searchBean.setAdministrativeState(stateString);
		DateTime lastStateChange = getAdministrativeMetadata().getLastStateChange();
		searchBean.setAdministrativeStateLastChange(lastStateChange);
		
		String permissionDefaultLevelString = getPermissionMetadata().getDefaultLevel().toString();
		searchBean.setPermissionDefaultLevel(permissionDefaultLevelString);
		
		if (entityTree != null && entityTree.getProjectEntity() != null)
		{
			ProjectEntity projectEntity = entityTree.getProjectEntity();
			searchBean = (DccdProjectSB) projectEntity.fillSearchBean(searchBean);

			List<Entity> subentities = projectEntity.getSubTreeAsList();
			for (Entity subentity : subentities)
			{
				searchBean = (DccdProjectSB) subentity.fillSearchBean(searchBean);
			}
		}
		else
		{
			logger.debug("No project entity found!");
		}

		return searchBean;
	}
	
	private Collection<? extends Object> getPermittedSearchBeans()
	{
		List<Object> searchBeans = new ArrayList<Object>();

		if (entityTree != null && entityTree.getProjectEntity() != null)
		{
			ProjectEntity projectEntity = entityTree.getProjectEntity();

			String stateString = getAdministrativeMetadata().getAdministrativeState().toString();
			DateTime lastStateChange = getAdministrativeMetadata().getLastStateChange();

			String permissionDefaultLevelString = getPermissionMetadata().getDefaultLevel().toString();
			
			// Add the Project bean
			//searchBeans.add(getPermittedProjectSearchBean());
			// Note: just return the complete Project bean, Only used for "My Projects", 
			// which is shown to the project manager who should be allowed to search and see every detail
			searchBeans.add(getCompleteProjectSearchBean());
			
			// Add all the Object beans
			List<DccdSB> newBeans = projectEntity.getSearchBeans(getPermissionMetadata().getDefaultLevel());
			
			// set the id and state for all beans
			for (DccdSB newBean : newBeans)
			{
				newBean.setOwnerId(getOwnerId());
				// Note that these names are confusing and should be refactored
				newBean.setPid(getSid()); // the repository system id (sid)
				newBean.composeId();
				
				newBean.setAdministrativeState(stateString);
				newBean.setAdministrativeStateLastChange(lastStateChange);
				newBean.setPermissionDefaultLevel(permissionDefaultLevelString);
			}

			searchBeans.addAll(newBeans);

			/*
 			// For easy
			DatasetSB dataset = new DatasetSB();
			dataset.setStoreId(getSid());
			dataset.setDublinCore(getDublinCoreMetadata());
			dataset.setAccessCategory(AccessCategory.ACCESS_ELSEWHERE);
			dataset.setState(getAdministrativeMetadata().getAdministrativeState());//DatasetState.PUBLISHED);

			searchBeans.add(dataset);
			*/
		}
		else
		{
			logger.debug("No project entity found!");
		}

		return searchBeans;
	}

	private DccdProjectSB getPermittedProjectSearchBean()
	{
		DccdProjectSB searchBean = new DccdProjectSB();
		//searchBean.setOwnerId(getOwnerId()); // should become managerId !
		searchBean.setOwnerId(getAdministrativeMetadata().getManagerId());
				
		// Note that these names are confusing and should be refactored
		searchBean.setPid(getSid()); // the repository system id (sid)
		searchBean.composeId();
		String stateString = getAdministrativeMetadata().getAdministrativeState().toString();
		searchBean.setAdministrativeState(stateString);
		DateTime lastStateChange = getAdministrativeMetadata().getLastStateChange();
		searchBean.setAdministrativeStateLastChange(lastStateChange);

		String permissionDefaultLevelString = getPermissionMetadata().getDefaultLevel().toString();
		searchBean.setPermissionDefaultLevel(permissionDefaultLevelString);
		
		if (entityTree != null && entityTree.getProjectEntity() != null)
		{
			ProjectEntity projectEntity = entityTree.getProjectEntity();
			//searchBean = (DccdProjectSB) projectEntity.fillSearchBean(searchBean);
			// Always fill with minimal (open acces) information 
			searchBean = (DccdProjectSB) projectEntity.minimalFillSearchBean(searchBean);
			// Only add all info when allowed.
			if (projectEntity.isPermittedBy(getPermissionMetadata().getDefaultLevel()))
			{
				searchBean = (DccdProjectSB) projectEntity.fillSearchBean(searchBean);
			}
			
			List<Entity> subentities = projectEntity.getSubTreeAsList();
			for (Entity subentity : subentities)
			{
				//searchBean = (DccdProjectSB) subentity.fillSearchBean(searchBean);
				// For ObjectEntity's also always fill with minimal (open acces) information 
				if (subentity instanceof ObjectEntity)
				{
					((ObjectEntity)subentity).minimalFillSearchBean(searchBean);
				}
				// futher filling respects the permissionlevel
				if (subentity.isPermittedBy(getPermissionMetadata().getDefaultLevel()))
				{
					searchBean = (DccdProjectSB) subentity.fillSearchBean(searchBean);
				}
			}
		}
		else
		{
			logger.debug("No project entity found!");
		}

		return searchBean;
	}
	
	@Override
	public Set<String> getContentModels()
	{
		Set<String> cms= super.getContentModels();
		cms.add(DCCD_PROJECT_CONTENTMODEL);
		return cms;
	}

	public DublinCoreMetadata getDublinCoreMetadata()
	{
		DublinCoreMetadata dc = new JiBXDublinCoreMetadata();
		dc.addLanguage(getTridasLanguage().getLanguage());
		if (hasTridas())
		{
			dc.addTitle(tridas.getTitle());
			dc.addCreator(tridas.getInvestigator());

			dc.addDescription(tridas.getDescription());
			dc.addFormat("TRiDaS");
			if (tridas.isSetIdentifier())
			{
				dc.addIdentifier(tridas.getIdentifier().getValue());
			}
			// What if there is no tridas identifier?

			for(String reference : tridas.getReferences())
			{
				dc.addRelation(reference);
			}

			// the subject of the TRiDaS are the objects
			for (TridasObject object : tridas.getObjects())
			{
				dc.addSubject(object.getTitle());
			}
		}
		return dc;
	}

	@Override
	public List<MetadataUnit> getMetadataUnits()
	{
		List<MetadataUnit> units = new ArrayList<MetadataUnit>();

		units.add(getDublinCoreMetadata());
		units.add(administrativeMetadata);
		units.add(creationMetadata);
		units.add(permissionMetadata);

		// entities
		if (hasTridas())
		{
			// Make sure that we have a valid entityTree
			//
			// create the entity tree, now use the tree from the project
	    	// note: recreate whole tree no matter what is already there!
			// NOT efficient!!!
//TEST TEST TEST TEST TEST TEST TEST 
//not tridas traversing, just the entities		
//			entityTree.buildTree(getTridas());

			units.add(entityTree);
			// get the units from the Entities
			ProjectEntity projectEntity = entityTree.getProjectEntity();
			units.add(projectEntity);
			units.addAll(projectEntity.getSubTreeAsList());
		}

		// vocabulary
		if (hasTridasVocabulary())
		{
			units.add(vocabulary);
		}
		
		return units;
	}

	
	@Override
	public List<BinaryUnit> getBinaryUnits()
	{
		List<BinaryUnit> units = new ArrayList<BinaryUnit>();
				
		// append the units for the originalFiles
		units.addAll(getOriginalFileBinaryUnits());
		
		// append the units for the associated files		
		units.addAll(getAssociatedFileBinaryUnits());
		
		return units;
	}

	public List<DccdOriginalFileBinaryUnit> getOriginalFileBinaryUnits()
	{
		return originalFileBinaryUnits;
	}

	public List<DccdAssociatedFileBinaryUnit> getAssociatedFileBinaryUnits()
	{
		return associatedFileBinaryUnits;
	}
		
	//--- permission related members
	// maybe these should be in a Authorisation service
	
	public boolean isManagementAllowed(DccdUser user)
	{
		if (user.hasRole(Role.ADMIN) || // admin may see everything
				user.getId().equals(
						getAdministrativeMetadata().getManagerId()) // also the manager
		)
			return true;
		else
			return false;
	}

	
	// you must have values level permission
	public boolean isDownloadAllowed(DccdUser user)
	{
		if (getEffectivePermissionLevel(user) == ProjectPermissionLevel.VALUES)
			return true;
		else
			return false;
	}
	
	// When non-published, don't allow viewing by the general public
	//
	// Note that if viewing is allowed, it does not allow for viewing everything, 
	// permissions need to be respected as well 
	public boolean isViewingAllowed(DccdUser user)
	{
		if (getAdministrativeMetadata().getAdministrativeState() == DatasetState.PUBLISHED || 
				isManagementAllowed(user))
			return true;
		else 
			return false;
	}
	
	// effective level is the permissionlevel, also taken into account
	// that admin and the owner has all rights
	public ProjectPermissionLevel getEffectivePermissionLevel(DccdUser user)
	{
		ProjectPermissionLevel level = ProjectPermissionLevel.MINIMAL; // minimum

		if (this.isManagementAllowed(user))
		{
			level = ProjectPermissionLevel.VALUES; // maximum: even download
		}
		else
		{
			level = getPermissionMetadata().getUserPermission(
					user.getId());
		}

		return level;
	}

	/**
	 * get all MeasurementSeriesEntity of the Tridas Project
	 *
	 * TODO: use getMeasurementSeriesEntities() from the entityTree
	 *
	 * @return The list of MeasurementSeriesEntity's
	 */
	public List<MeasurementSeriesEntity> getMeasurementSeriesEntities() {
		List<MeasurementSeriesEntity> resultList = new ArrayList<MeasurementSeriesEntity>();

		if (hasTridas())
		{
			// note: it would be more efficient if we could specify a class;
			// EntityTree would then only return the specified type of entities
			List<Entity> entityList = entityTree.getEntities();
			for(Entity entity : entityList)
			{
				if (entity instanceof MeasurementSeriesEntity)
					resultList.add((MeasurementSeriesEntity)entity);
			}
		}

		return resultList;
	}
	
	public List<DerivedSeriesEntity> getDerivedSeriesEntities() {
		List<DerivedSeriesEntity> resultList = new ArrayList<DerivedSeriesEntity>();

		if (hasTridas())
		{
			// note: it would be more efficient if we could specify a class;
			// EntityTree would then only return the specified type of entities
			List<Entity> entityList = entityTree.getEntities();
			for(Entity entity : entityList)
			{
				if (entity instanceof DerivedSeriesEntity)
					resultList.add((DerivedSeriesEntity)entity);
			}
		}

		return resultList;
	}

	/**
	 * When entity titles change (in Tridas) the entity tree has to be updated 
	 * and changes should be stored in the repository as well.
	 * Structural changes (adding/removal of entities) are not allowed by the web Application, 
	 * so we don't need to rebuild the whole tree
	 */
	public void updateEntityTree()
	{
		// get all entities
		if (hasTridas())
		{
			List<Entity> entityList = entityTree.getEntities();
			for(Entity entity : entityList)
			{
				updateEntityTree(entity);
			}
		}
	}
	
	// update the tree and entity for a specific Entity
	private void updateEntityTree(Entity entity)
	{
		if (entity.hasTridas())
		{
			String title = entity.getTridasTitle();
			
			if(title!= null && entity.getTitle().compareTo(title) != 0)
			{
				// title was different, update it
				entity.setTitle(title);
				entityTree.setDirty(true);
				
				// Project entity represents the whole project
				if (entity instanceof ProjectEntity)
					this.setTitle(title);
			}
		}		
	}
}
