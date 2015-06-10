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

import junit.framework.TestCase;
import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.ldap.ds.LdapClient;
import nl.knaw.dans.common.ldap.ds.StandAloneDS;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.dccd.model.DccdOrganisationImpl;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.DccdUser.Role;
import nl.knaw.dans.dccd.model.DccdUserImpl;
import nl.knaw.dans.dccd.repository.ldap.DccdLdapOrganisationRepo;
import nl.knaw.dans.dccd.repository.ldap.DccdLdapUserRepo;

/**
 * These test only work with a running LDAP server!
 *
 * TODO: cleanup the entry's, add more asserts and refactor
 *
 * @author paulboon
 *
 */
public class TestLdapRepositoryOnlineTest extends TestCase {

	/**
	 * For now, figure out if we can use the common dans-ldap project.
	 * Just add a dccd user etc.
	 */
	public void testLdapRepository() {

		//# LDAP server properties
		// ldap.providerURL=ldap://localhost:10389
		// ldap.securityPrincipal=uid=admin,ou=system
		// ldap.securityCredentials=secret
		// ldap.context.users=ou=users,ou=easy,dc=dans,dc=knaw,dc=nl
		// ldap.context.organisations=ou=organisations,ou=dccd,dc=dans,dc=knaw,dc=nl
        StandAloneDS supplier = new StandAloneDS();
        supplier.setProviderURL("ldap://localhost:10389");
        supplier.setSecurityPrincipal("uid=admin,ou=system");
        supplier.setSecurityCredentials("secret");
        LdapClient client = new LdapClient(supplier);


        // --- DccdUser
        final String PASSWD = "testtest";

        DccdLdapUserRepo userRepo =
        	new DccdLdapUserRepo(client, "ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl");
        DccdUser testUser = new DccdUserImpl();

		testUser.setId("testuser");   // uid
		testUser.setSurname("Janssen");     // sn
		testUser.setEmail("jan.jansen@bar.com");
		testUser.setCity("Knollendam");
		testUser.addRole(Role.USER);
		testUser.addRole(Role.ADMIN);
		//testUser.addRole(Role.ARCHIVIST);
		testUser.setPassword(PASSWD);
		testUser.setInitials("T");

		// allready usable
		testUser.setAcceptConditionsOfUse(true);
		testUser.setState(DccdUser.State.ACTIVE);

		try {
			if (userRepo.exists(testUser.getId()))
				userRepo.delete(testUser);
			String uid = userRepo.add(testUser);
			System.out.println("added test user with uid: " + uid);

	        DccdUser rjan = userRepo.findById(uid);
	        rjan.removeRole(Role.ARCHIVIST);
	        userRepo.update(rjan);
			System.out.println("Updated test user with uid: " + uid);

			// check password
			assertTrue(userRepo.authenticate("testuser", PASSWD));
			userRepo.delete(rjan);// cleanup
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// --- DccdOrganisation

        DccdLdapOrganisationRepo organisationRepo =
        	new DccdLdapOrganisationRepo(client, "ou=organisations,ou=dccd,dc=dans,dc=knaw,dc=nl");

	    DccdOrganisation testOrg = new DccdOrganisationImpl("testOrganisation");

	    String organisationId = "";

        try {
			if (organisationRepo.exists(testOrg.getId()))
				organisationRepo.delete(testOrg);
			organisationId = organisationRepo.add(testOrg);
	        System.out.println("added test organisation with id: " + organisationId);
	        DccdOrganisation rTest1 = organisationRepo.findById(organisationId);
	        rTest1.setDescription("This is a test organisation");

	        // the following id's are what was needed before we had
	        // LdapUserPathTranslator
	        //rTest1.addUserId("uid=someone,"+"ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl");
	        //rTest1.addUserId("uid=someone too,"+"ou=users,ou=dccd,dc=dans,dc=knaw,dc=nl");
	        //
	        // now the translator takes care of the extra's
	        rTest1.addUserId("someone");
	        rTest1.addUserId("someone too");

	        organisationRepo.update(rTest1);

	        // Now read it back
	        DccdOrganisation readbackAfterUserAdd = organisationRepo.findById(organisationId);
	        //System.out.println("readback from ldap repo: " + readbackAfterUserAdd.getUserIdsString());
	        // it should have the same id's we added
	        assertTrue(readbackAfterUserAdd.hasUserId("someone", "someone too"));

	        // delete this organisation
	        organisationRepo.delete(readbackAfterUserAdd);

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO: have new user join the new Organisation?
		// TODO test: find users with organisation?
		// TODO test: find organisation with user?
	}

	// just add, no cleanup afterwards...
	public void testAddOrganisationsInLdapRepository() throws RepositoryException {
        StandAloneDS supplier = new StandAloneDS();
        supplier.setProviderURL("ldap://localhost:10389");
        supplier.setSecurityPrincipal("uid=admin,ou=system");
        supplier.setSecurityCredentials("secret");
        LdapClient client = new LdapClient(supplier);

        DccdLdapOrganisationRepo organisationRepo =
        	new DccdLdapOrganisationRepo(client, "ou=organisations,ou=dccd,dc=dans,dc=knaw,dc=nl");

	    DccdOrganisation testOrg = new DccdOrganisationImpl();
	    testOrg.setId("test-"+ "organisation" + "-1");
	    testOrg.setState(DccdOrganisation.State.ACTIVE);
	    testOrg.setDescription("test-" + "description");
	    testOrg.setAddress("test-" + "address");
	    testOrg.setPostalCode("test-" + "postalCode");
	    testOrg.setCity("test-" + "city");
	    testOrg.setCountry("test-" + "country");
	    addOrganisationInLdapRepository(organisationRepo, testOrg);
	    testOrg.setId("test-"+ "organisation" + "-2");
	    addOrganisationInLdapRepository(organisationRepo, testOrg);
	    testOrg.setId("test-"+ "organisation" + "-3");
	    addOrganisationInLdapRepository(organisationRepo, testOrg);
	}

	private void addOrganisationInLdapRepository(DccdLdapOrganisationRepo organisationRepo, DccdOrganisation testOrg) throws RepositoryException {
		if (organisationRepo.exists(testOrg.getId()))
			organisationRepo.delete(testOrg);
		organisationRepo.add(testOrg);
        System.out.println("added test organisation with id: " + testOrg.getId());
	}
}
