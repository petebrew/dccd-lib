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
package nl.knaw.dans.dccd.repository;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import nl.knaw.dans.common.fedora.Fedora;
import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.repo.DmoStoreId;
import nl.knaw.dans.common.lang.repo.exception.ObjectExistsException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.dccd.application.services.DccdConfigurationService;
import nl.knaw.dans.dccd.model.DccdOrganisationImpl;
import nl.knaw.dans.dccd.model.DccdUserImpl;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.ProjectCreationMetadata;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.model.ProjectPermissionMetadata;
import nl.knaw.dans.dccd.repository.fedora.DccdFedoraStore;

import org.junit.Test;
import org.tridas.schema.ControlledVoc;
//import org.tridas.schema.TridasCategory;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasProject;

public class TestDccdFedoraStoreOnlineTest
{
	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String PROTOCOL = settings.getProperty("fedora.protocol");// "http";
	static final String HOST     = settings.getProperty("fedora.host");//"localhost";//"dendro01.dans.knaw.nl";
	static final int 	PORT     = Integer.parseInt(settings.getProperty("fedora.port"));//8082;//80;
	static final String CONTEXT  = settings.getProperty("fedora.context");//"fedora";
	static final String BASE_URL = PROTOCOL + "://"  + HOST + ":" + PORT + "/" + CONTEXT;
	static final String FEDORA_USER = settings.getProperty("fedora.user");//"fedoraAdmin";
	static final String PASSWORD = settings.getProperty("fedora.password");//"fedoraAdmin";

	static final String STORE_ID = "dccd:1";
	static final String OWNER_ID = "nobody";
	static final Locale TRIDAS_LANG = Locale.FRENCH;

	private static final String CREATION_USERID = "testuserid";
	private static final String CREATION_ORGANISATIONID = "testorganisationid";

	@Test
	public void testStore()
	{
		System.out.println("fedora url: " + BASE_URL);

		Fedora fedora = new Fedora(BASE_URL, FEDORA_USER, PASSWORD);

		DccdFedoraStore store = new DccdFedoraStore("dccd-store", fedora);

		Project project = createMinimalProject();

		try
		{
			//store.purge(project, true, "test purge");
			store.ingest(project, "ingest test");
		}
		catch (ObjectExistsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Project retrievedProject = null;
		try
		{
//			retrievedProject = (Project) store.retrieve(project.getStoreId());
			retrievedProject = (Project) store.retrieve(new DmoStoreId(project.getStoreId()));

		}
		catch (ObjectNotInStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO; just output the TriDaS ...


		/* TODO test remove
		try
		{
			store.purge(project, false, "test purge"); // cannot force?
		} catch (ObjectNotInStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectIsNotDeletableException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	// Only retrieve of the (already) stored project
	@Test
	public void testRetrieveProject() throws Exception
	{
		System.out.println("fedora url: " + BASE_URL);
		Fedora fedora = new Fedora(BASE_URL, FEDORA_USER, PASSWORD);
		DccdFedoraStore store = new DccdFedoraStore("dccd-store", fedora);

		Project retrievedProject = null;
		try
		{
//			retrievedProject = (Project) store.retrieve(STORE_ID); // or choose another; dccd:146
			retrievedProject = (Project) store.retrieve(new DmoStoreId(STORE_ID)); // or choose another; dccd:146
		}
		catch (ObjectNotInStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO test if it is what we want;
		// lets output the data
		ProjectCreationMetadata creationMetadata = retrievedProject.getCreationMetadata();
		System.out.println("Creation metadata XML: ");
		System.out.print(creationMetadata.asXMLString(1));
		System.out.println("");
//creationMetadata.getUser().getDigitalAuthorIdentifier()

		if (retrievedProject!= null && retrievedProject.entityTree != null)
		{
			System.out.println("Project entity tree structure: ");
			// output the tree
			List<String> lines = retrievedProject.entityTree.getProjectEntity().toTreeString("");
			for (String line : lines)
				System.out.println(line);
		}
		else
		{
			System.out.println("No entity tree found!");
		}

		// TODO; just output the TriDaS ...

	}

	// create a minimal dccd Project for ingesting
	//
	// if it has no TRiDaS data, it should fail!
	// we could test this,
	// but for now I want to do a real ingest
	private Project createMinimalProject()
	{
		// don't read from file,
		// to much overhead for minimal project
		TridasProject tridasProject = new TridasProject();
		tridasProject.setTitle("test minimal");
		tridasProject.setPeriod("now");
		tridasProject.setInvestigator("someone");
		ControlledVoc category = new ControlledVoc();
		category.setValue("category");
		tridasProject.setCategory(category);
		List<ControlledVoc> types = tridasProject.getTypes();
		ControlledVoc type = new ControlledVoc();
		type.setValue("type");
		types.add(type);
		List<TridasLaboratory> laboratories = tridasProject.getLaboratories();
		TridasLaboratory laboratory = new TridasLaboratory();
		TridasLaboratory.Name labName = new TridasLaboratory.Name();
		labName.setValue("lab name");
		laboratory.setName(labName);
		laboratories.add(laboratory);

		Project project = new Project();
		project.setTridas(tridasProject);
		project.setTitle(tridasProject.getTitle());

		//project.setSid("dccd:999"); //? is the Sid differnt from the StoreId ?
		project.setStoreId(STORE_ID);
		project.setOwnerId(OWNER_ID);
		project.setTridasLanguage(TRIDAS_LANG);

		project.setCreationMetadata(getSimpleProjectCreationMetadata());
		project.setPermissionMetadata(getSimpleProjectPermissionMetadata());

		return project;
	}

	private ProjectCreationMetadata getSimpleProjectCreationMetadata()
	{
		ProjectCreationMetadata metadata = new ProjectCreationMetadata();
		DccdOrganisationImpl organisation = new DccdOrganisationImpl(CREATION_ORGANISATIONID);
		DccdUserImpl user = new DccdUserImpl(CREATION_USERID);
		user.setEmail("test@test.org");
		user.setSurname("tester");
		user.setInitials("T.T.");
		user.setOrganization(CREATION_ORGANISATIONID);
		metadata.setUser(user);
		metadata.setOrganisation(organisation);

		return metadata;
	}

	private ProjectPermissionMetadata getSimpleProjectPermissionMetadata ()
	{
		ProjectPermissionMetadata metadata = new ProjectPermissionMetadata();

		metadata.setUserPermission("toobjectuser", ProjectPermissionLevel.OBJECT);
		metadata.setUserPermission("tovaluesuser", ProjectPermissionLevel.VALUES);

		return metadata;
	}
}
