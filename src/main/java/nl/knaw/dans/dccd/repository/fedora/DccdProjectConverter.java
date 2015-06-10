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
package nl.knaw.dans.dccd.repository.fedora;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.knaw.dans.common.fedora.fox.DatastreamVersion;
import nl.knaw.dans.common.fedora.fox.DigitalObject;
import nl.knaw.dans.common.fedora.store.AbstractDobConverter;
import nl.knaw.dans.common.jibx.JiBXObjectFactory;
import nl.knaw.dans.common.lang.repo.bean.DublinCoreMetadata;
import nl.knaw.dans.common.lang.repo.exception.ObjectDeserializationException;
import nl.knaw.dans.common.lang.xml.XMLDeserializationException;
import nl.knaw.dans.dccd.model.DccdAssociatedFileBinaryUnit;
import nl.knaw.dans.dccd.model.DccdOriginalFileBinaryUnit;
import nl.knaw.dans.dccd.model.EntityTree;
import nl.knaw.dans.dccd.model.EntityTreeBuilder;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.ProjectAdministrativeMetadata;
import nl.knaw.dans.dccd.model.ProjectCreationMetadata;
import nl.knaw.dans.dccd.model.ProjectPermissionMetadata;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.model.ProjectVocabulary;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasVocabulary;

public class DccdProjectConverter extends AbstractDobConverter<Project>
{
	private static Logger	logger					= Logger.getLogger(DccdProjectConverter.class);
	// reuse the JAXB context, improves performance
	// especially for retrieveEntity() which can be called for each entity in a project
	private JAXBContext		jaxbContextForTridas	= null;

	public DccdProjectConverter()
	{
//		super(Project.OBJECT_NAMESPACE);
		super(Project.NAMESPACE);
	}

	private JAXBContext getJaxbContextForTridas() throws JAXBException
	{
		if (jaxbContextForTridas == null)
		{
			jaxbContextForTridas = JAXBContext.newInstance("org.tridas.schema");
		}
		return jaxbContextForTridas;
	}

	@Override
	public void deserialize(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		super.deserialize(digitalObject, project);

		deserializeFileUnits(digitalObject, project);
		deserializeMetadata(digitalObject, project);
		deserializeEntities(digitalObject, project);
		
		deserializeVocabulary(digitalObject, project);
	}

	private void deserializeVocabulary(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		// retrieve the vocabulary
		logger.debug("Retrieving vocabulary: " + ProjectVocabulary.VOCABULARY_ID);
		DatastreamVersion vocVersion = digitalObject.getLatestVersion(ProjectVocabulary.VOCABULARY_ID);
		
		// Optional, not every project has one
		if (vocVersion != null)
		{
			try
			{
				// String entityXMLString = entityVersion.asXMLString();
				String vocXMLString = vocVersion.getXmlContent().getElement().asXML();
				Unmarshaller unmarshaller = getJaxbContextForTridas().createUnmarshaller();
	
				// Need UTF-8 because the JAXB want's it for unmarshalling the tridas
				ByteArrayInputStream input = new ByteArrayInputStream(vocXMLString.getBytes("UTF-8"));
				Object tridas = unmarshaller.unmarshal(input);
				
				project.setTridasVocabulary((TridasVocabulary)tridas);
				project.getVocabulary().setDirty(false); 
			}
			catch (JAXBException e)
			{
				throw new ObjectDeserializationException(e);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ObjectDeserializationException(e);
			}
		}
	}
	
	
	// TODO refactor into smaller functions
	private void deserializeMetadata(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		// retrieve tridas language from the Dublin Core metadata
		logger.debug("Retrieving Dublin Core metadata for digital object. sid=" + digitalObject.getSid());
		try
		{
			DublinCoreMetadata latestDublinCoreMetadata = digitalObject.getLatestDublinCoreMetadata();
			// retrieve the language setting
			List<String> tridasLanguageList = latestDublinCoreMetadata.getLanguage();
			// only adjust if it was set, otherwise keep the default
			if (tridasLanguageList.size() > 0)
			{
				// Note: DC.Language allows for a list of languages in the content,
				// we take the first one as the primary language!
				project.setTridasLanguage(new Locale(tridasLanguageList.get(0)));
			}
		}
		catch (XMLDeserializationException e)
		{
			logger.info("Could not deserialize XML for the Dublin Core");
			throw new ObjectDeserializationException(e);
		}

		// retrieve the ProjectCreationMetadata
		DatastreamVersion creationVersion = digitalObject.getLatestVersion(ProjectCreationMetadata.UNIT_ID);
		if (creationVersion == null)
		{
			throw new ObjectDeserializationException("No project creation metadata found on retrieved digital object. sid=" + digitalObject.getSid());
		}
		// Use JiBX to get the object
		Element creationElement = creationVersion.getXmlContentElement();
		try
		{
			ProjectCreationMetadata creationMetadata = (ProjectCreationMetadata) JiBXObjectFactory.unmarshal(ProjectCreationMetadata.class, creationElement);
			project.setCreationMetadata(creationMetadata);
		}
		catch (XMLDeserializationException e)
		{
			throw new ObjectDeserializationException(e);
		}

		// retrieve the ProjectPermissionMetadata
		DatastreamVersion permissionVersion = digitalObject.getLatestVersion(ProjectPermissionMetadata.UNIT_ID);
		if (permissionVersion == null)
		{
			throw new ObjectDeserializationException("No project creation metadata found on retrieved digital object. sid=" + digitalObject.getSid());
		}
		// Use JiBX to get the object
		Element permissionElement = permissionVersion.getXmlContentElement();
		try
		{
			ProjectPermissionMetadata permissionMetadata = (ProjectPermissionMetadata) JiBXObjectFactory.unmarshal(ProjectPermissionMetadata.class,
					permissionElement);
			project.setPermissionMetadata(permissionMetadata);
		}
		catch (XMLDeserializationException e)
		{
			throw new ObjectDeserializationException(e);
		}

		// retrieve the ProjectAdministrativeMetadata
		DatastreamVersion administrativeVersion = digitalObject.getLatestVersion(ProjectAdministrativeMetadata.UNIT_ID);
		if (administrativeVersion == null)
		{
			throw new ObjectDeserializationException("No project administrative metadata found on retrieved digital object. sid=" + digitalObject.getSid());
		}
		// Use JiBX to get the object
		Element administrativeElement = administrativeVersion.getXmlContentElement();
		try
		{
			ProjectAdministrativeMetadata administrativeMetadata = (ProjectAdministrativeMetadata) JiBXObjectFactory.unmarshal(
					ProjectAdministrativeMetadata.class, administrativeElement);
			project.setAdministrativeMetadata(administrativeMetadata);
		}
		catch (XMLDeserializationException e)
		{
			throw new ObjectDeserializationException(e);
		}
	}

	// Note: all entities are initialized with setDirty(false)
	// We need to do this to get the updating after editing correct! 
	private void deserializeEntities(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		// retrieve the entity tree and build it
		logger.debug("Retrieving entityTree from id: " + EntityTree.ENTITYTREE_ID);
		DatastreamVersion treeVersion = digitalObject.getLatestVersion(EntityTree.ENTITYTREE_ID);
		if (treeVersion == null)
		{
			// logger.error("No entityTree found on retrieved digital object. sid=" + digitalObject.getSid());
			throw new ObjectDeserializationException("No entityTree found on retrieved digital object. sid=" + digitalObject.getSid());
		}
		Element treeElement = treeVersion.getXmlContentElement();
		// Create the tree of entities
		EntityTree entityTree = project.entityTree;
		ProjectEntity projectEntity = EntityTreeBuilder.buildTree(treeElement);
		entityTree.setProjectEntity(projectEntity);
		projectEntity.setDirty(false); 
		
		// Note: could show tree using entityTree.asXMLString(1)

		// retrieve all entity's found in the tree
		List<Entity> entities = entityTree.getEntities();
		for (Entity entity : entities)
		{
			logger.debug("Retrieving entity from id: " + entity.getId());
			DatastreamVersion entityVersion = digitalObject.getLatestVersion(entity.getId());
			try
			{
				// String entityXMLString = entityVersion.asXMLString();
				String entityXMLString = entityVersion.getXmlContent().getElement().asXML();
				Unmarshaller unmarshaller = getJaxbContextForTridas().createUnmarshaller();

				// Need UTF-8 because the JAXB want's it for unmarshalling the tridas
				ByteArrayInputStream input = new ByteArrayInputStream(entityXMLString.getBytes("UTF-8"));
				Object tridas = unmarshaller.unmarshal(input);
				entity.setTridasObject(tridas);
				entity.setDirty(false); 
				
				// Note: could show entity with entity.asXMLString(1)
			}
			catch (JAXBException e)
			{
				throw new ObjectDeserializationException(e);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ObjectDeserializationException(e);
			}
		}
		// connect all (internal) tridas objects, according to the tree structure
		projectEntity.connectTridasObjectTree();
		
		// note: as long as we don't allow editing the structure, 
		// as far as updating/dirty is concerned the entities are independent units. 
		// and the 'entityTree' object also does not change!
		
		project.setTridas((TridasProject) projectEntity.getTridasAsObject());
		project.setTitle(projectEntity.getTitle());
	}

	private void deserializeFileUnits(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		deserializeOriginalFileUnits(digitalObject, project);
		deserializeAssociatedFileUnits(digitalObject, project);
	}

	private void deserializeOriginalFileUnits(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		int num = 0;
		boolean tryNext = true;

		while (tryNext)
		{
			num++; // advance
			String idString = DccdOriginalFileBinaryUnit.UNIT_ID_PREFIX + num;
			DatastreamVersion orgFileUnitVersion = digitalObject.getLatestVersion(idString);

			if (orgFileUnitVersion != null)
			{
				// original filename is in the Label,
				String fileName = orgFileUnitVersion.getLabel();
				// the id is the stream ID
				String unitId = orgFileUnitVersion.getStreamId();
				logger.debug("Found original file with name: " + fileName + ", unit id: " + unitId);
				try
				{
					project.addOriginalFileBinaryUnit(fileName, unitId);
				}
				catch (IOException e)
				{
					throw new ObjectDeserializationException(e);
				}
			}
			else
			{
				tryNext = false; // stop
			}
		}
		logger.debug("Found " + (num - 1) + " original files");
	}

	private void deserializeAssociatedFileUnits(DigitalObject digitalObject, Project project) throws ObjectDeserializationException
	{
		int num = 0;
		boolean tryNext = true;

		while (tryNext)
		{
			num++; // advance
			String idString = DccdAssociatedFileBinaryUnit.UNIT_ID_PREFIX + num;
			DatastreamVersion fileUnitVersion = digitalObject.getLatestVersion(idString);

			if (fileUnitVersion != null)
			{
				// original filename is in the Label,
				String fileName = fileUnitVersion.getLabel();
				// the id is the stream ID
				String unitId = fileUnitVersion.getStreamId();
				logger.debug("Found associated file with name: " + fileName + ", unit id: " + unitId);
				try
				{
					project.addAssociatedFileBinaryUnit(fileName, unitId);
				}
				catch (IOException e)
				{
					throw new ObjectDeserializationException(e);
				}
			}
			else
			{
				tryNext = false; // stop
			}
		}
		logger.debug("Found " + (num - 1) + " associated files");
	}
}
