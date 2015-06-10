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

import org.junit.Test;

public class TestUser
{
	//private static final Logger logger  = LoggerFactory.getLogger(TestUser.class);
	final String PASSWD = "testtest";

/* note: JiBX binding only works if inside ProjectCreationMetadata */
    @Test
	public void testJiBXBinding() throws Exception
	{
//    	DccdUserImpl testUser = new DccdUserImpl();
//		testUser.setId("testuser");   // uid
//		testUser.setSurname("Janssen");     // sn
//		testUser.setEmail("jan.jansen@bar.com");
//		testUser.setCity("Knollendam");
//		testUser.addRole(Role.USER);
//		testUser.addRole(Role.ADMIN);
//		testUser.setPassword(PASSWD);
//		testUser.setInitials("T");
//		testUser.setState(DccdUser.State.ACTIVE);
//		testUser.setOrganization("someorg");
//
//		logger.debug("\n" + testUser.asXMLString(1) + "\n");
//
//		DccdUserImpl testUser2 =
//			(DccdUserImpl) JiBXObjectFactory.unmarshal(DccdUserImpl.class, testUser.asObjectXML());
//        assertEquals(testUser.asXMLString(), testUser2.asXMLString());
//
	}
}
