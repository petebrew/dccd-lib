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

import static org.junit.Assert.*;
import nl.knaw.dans.common.jibx.JiBXObjectFactory;
import nl.knaw.dans.common.lang.xml.XMLException;
import nl.knaw.dans.dccd.model.DccdUser.Role;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProject
{
    private static final Logger logger  = LoggerFactory.getLogger(TestProject.class);
	final String PASSWD = "testtest";

	@Test
	public void testConstructor()
    {
		// default constructor
		Project project = new Project();
		assertNull(project.getTridas());
		assertNotNull(project.entityTree);
		assertNull(project.entityTree.getProjectEntity());
//		assertEquals("", project.getSid());
		assertEquals(null, project.getSid());
		assertEquals("", project.getTitle());

		// constructor with arguments
		project = new Project("someid", "sometitle");
		assertNull(project.getTridas());
		assertNotNull(project.entityTree);
		assertNull(project.entityTree.getProjectEntity());
		assertEquals("someid", project.getSid());
		assertEquals("sometitle", project.getTitle());
    }

	@Test
	public void testPermissionJiBXBindings() throws XMLException
	{
		ProjectPermissionMetadata permissionMetadata = getSimpleProjectPermissionMetadata();

		//logger.debug("\n" + permissionMetadata.asXMLString(1) + "\n");

		ProjectPermissionMetadata permissionMetadata2 =
			(ProjectPermissionMetadata) JiBXObjectFactory.unmarshal(ProjectPermissionMetadata.class, permissionMetadata.asObjectXML());
        assertEquals(permissionMetadata.asXMLString(), permissionMetadata2.asXMLString());
	}

	@Test
	public void testProjectCreationMetadataJiBXBindings() throws XMLException
	{
		ProjectCreationMetadata creationMetadata = getSimpleProjectCreationMetadata();

		//logger.debug("\n" + creationMetadata.asXMLString(1) + "\n");

		ProjectCreationMetadata creationMetadata2 =
			(ProjectCreationMetadata) JiBXObjectFactory.unmarshal(ProjectCreationMetadata.class, creationMetadata.asObjectXML());
        assertEquals(creationMetadata.asXMLString(), creationMetadata2.asXMLString());
	}

	@Test
	public void testProjectAdministrativeMetadataJiBXBindings() throws XMLException
	{
		ProjectAdministrativeMetadata administrativeMetadata = getSimpleProjectAdministrativeMetadata();

		//logger.debug("\n" + administrativeMetadata.asXMLString(1) + "\n");

		ProjectAdministrativeMetadata administrativeMetadata2 =
			(ProjectAdministrativeMetadata) JiBXObjectFactory.unmarshal(ProjectAdministrativeMetadata.class, administrativeMetadata.asObjectXML());
        assertEquals(administrativeMetadata.asXMLString(), administrativeMetadata2.asXMLString());
	}

	private ProjectAdministrativeMetadata getSimpleProjectAdministrativeMetadata()
	{
		ProjectAdministrativeMetadata metadata = new ProjectAdministrativeMetadata();

		// TODO change it's bits
		// managerId, legalOwnerOrganisationId need to be specified
		metadata.setManagerId("testuser");
		metadata.setLegalOwnerOrganisationId("testOrganisation");

		return metadata;
	}

	private ProjectPermissionMetadata getSimpleProjectPermissionMetadata ()
	{
		ProjectPermissionMetadata metadata = new ProjectPermissionMetadata();

		metadata.setUserPermission("toobjectuser", ProjectPermissionLevel.OBJECT);
		metadata.setUserPermission("tovaluesuser", ProjectPermissionLevel.VALUES);

		return metadata;
	}

	private ProjectCreationMetadata getSimpleProjectCreationMetadata()
	{
		ProjectCreationMetadata metadata = new ProjectCreationMetadata();

	   	DccdUserImpl testUser = new DccdUserImpl();
		testUser.setId("testuser");   // uid
		testUser.setSurname("Janssen");     // sn
		testUser.setEmail("jan.jansen@bar.com");
		testUser.setCity("Knollendam");
		testUser.addRole(Role.USER);
		testUser.addRole(Role.ADMIN);
		testUser.setPassword(PASSWD);
		testUser.setInitials("T");
		testUser.setState(DccdUser.State.ACTIVE);
		testUser.setOrganization("someorg");

		metadata.setUser(testUser);

	   	DccdOrganisationImpl testOrg = new DccdOrganisationImpl("testOrganisation");
	    testOrg.setState(DccdOrganisation.State.ACTIVE);
	    testOrg.setDescription("a test organisation");

	    metadata.setOrganisation(testOrg);

		return metadata;
	}

}
