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
package nl.knaw.dans.dccd.tridas;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import nl.knaw.dans.common.lang.search.SearchRequest;
import nl.knaw.dans.common.lang.search.SearchResult;
import nl.knaw.dans.common.lang.search.simple.SimpleField;
import nl.knaw.dans.common.lang.search.simple.SimpleSearchHit;
import nl.knaw.dans.common.lang.search.simple.SimpleSearchRequest;
import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.DccdSearchService;
import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.application.services.UserServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.repository.xml.XMLFilesRepositoryService;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;

public class IngestOnlineTest
{
	private static Logger logger = Logger.getLogger(IngestOnlineTest.class);

	private DccdUser user = null;
	private final String titleBase = "BigTestIngest";//"TestIngest";
	private final int numProjects = 1000;//100;
	private static net.opengis.gml.schema.ObjectFactory	gmlFactory = new net.opengis.gml.schema.ObjectFactory();
	
	@Test
	public void testIngestALot() throws Exception 
	{
		String userId = getUser().getId();

		// project to ingest
		File inputFile = new File("TestData/Input/dccd-webapp-testdata-minimal-v_1_tridas.xml");
		
		for (int i = 0; i < numProjects; i++)
		{
			// NOTE we could reuse the tridas, but then we still need to call DccdDataService.getService().createProject(userId);
			FileInputStream fis = new FileInputStream(inputFile.getAbsolutePath());
			Project inputProject = XMLFilesRepositoryService.createDendroProjectFromTridasXML(fis, userId);		
			// store the (original) xml filename
			inputProject.setFileName(inputFile.getName());
			inputProject.entityTree.buildTree(inputProject.getTridas());
			
			// Change title
			String title = titleBase + (i+1) + "of" + numProjects;
			ProjectEntity projectEntity = inputProject.entityTree.getProjectEntity();
			TridasProject tridasProject = (TridasProject)projectEntity.getTridasAsObject();
			tridasProject.setTitle(title);
			projectEntity.setTitle(title);
			inputProject.setTitle(title);

			// change the Object Location?
			List<TridasObject> tridasObjects = tridasProject.getObjects();
			// take the first one
			if (!tridasObjects.isEmpty())
			{
				TridasObject tridasObject = tridasObjects.get(0);
				if (tridasObject.isSetLocation() && 
					tridasObject.getLocation().isSetLocationGeometry())
				{
					net.opengis.gml.schema.PointType pt = gmlFactory.createPointType();
					net.opengis.gml.schema.Pos pos = gmlFactory.createPos();
					pt.setSrsName("WGS 84");
					List<Double> values = pos.getValues();
					values.add((double)i*(double)0.1); // long
					values.add((double)i*(double)0.1); // lat
					pt.setPos(pos);
					tridasObject.getLocation().getLocationGeometry().setPoint(pt);
				}
			}
			// Store the project (as Draft!)
			logger.info("Store in repository...");
			DccdDataService.getService().storeProject(inputProject);
			logger.info("Done");
		}
	}

	@Test
	public void testDeleteIngestALot() throws Exception 
	{
		String userId = getUser().getId();
		
		SearchRequest request =  new SimpleSearchRequest();
		SimpleField<String> projectTitleField = new SimpleField<String>(DccdSB.TRIDAS_PROJECT_TITLE_NAME);

		for (int i = 0; i < numProjects; i++)
		{
			String title = titleBase + (i+1) + "of" + numProjects;
			projectTitleField.setValue(title);
			request.addFilterQuery(projectTitleField);
			// owner, just to be sure
			SimpleField<String> ownerIdField = new SimpleField<String>(DccdSB.OWNER_ID_NAME, userId);
			request.addFilterQuery(ownerIdField);
			// only projects
			request.addFilterBean(DccdProjectSB.class);
	
			SearchResult<? extends DccdSB> result = DccdSearchService.getService().doSearch(request);
			for (Object hitObj : result.getHits())
			{
				SimpleSearchHit hit = (SimpleSearchHit) hitObj;
				//DccdSB
				DccdSB dccdHit = (DccdSB)hit.getData();
				logger.debug( "status: " + dccdHit.getAdministrativeState() +
						" Title: " + dccdHit.getTridasProjectTitle() + 
						", id: " + dccdHit.getTridasProjectIdentifier() +
						", domain: " + dccdHit.getTridasProjectIdentifierDomain());
	
				// get the project with the given Id and delete it?
				try
				{
					Project storedProject = DccdDataService.getService().getProject(dccdHit.getPid());
					logger.info("Remove from repository...");
					DccdDataService.getService().deleteProject(storedProject, getUser());
					logger.info("Done");				
				}
				catch (DataServiceException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}		
	}
	
	/*
	 * 
	 */
	private DccdUser getUser()
	{
		// get the user that is uploaded
		// fake a user, but this one must exist in LDAP!!!
		// We could try to Mock the DccdUserService...
		if (user == null)
		{
			try
			{
				user = DccdUserService.getService().getUserById("normaltestuser");
			}
			catch (UserServiceException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return user;
	}	
}
