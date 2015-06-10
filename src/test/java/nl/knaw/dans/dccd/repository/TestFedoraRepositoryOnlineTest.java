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

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.repository.fedora.FedoraRepositoryService;

public class TestFedoraRepositoryOnlineTest extends TestCase {

	public void testFedoraRepository() {
		// empty
	}

	public void SKIPtestFedoraGetObjectXML() {
		//getObjectXML
    	// use the Fedora repo, for testing
    	String protocol = "http";
    	String host = "localhost";
    	int port = 8082;
    	String user = "fedoraAdmin";
    	String pass = "fedoraAdmin";
    	String context = "fedora";

    	try {
			FedoraRepositoryService fs = new FedoraRepositoryService(protocol, host, port, user, pass, context);
			System.out.print(fs.getObjectXML("dccd:135"));// yep, hardcoded PID, could be demo:100
    	} catch (DccdRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void SKIPtestFedoraRetrieveEntityTree() {
		Project project = new Project();
//		project.setSid("dccd:51"); // must be there for testing!!!
		project.setStoreId("dccd:51"); // must be there for testing!!!

    	// use the Fedora repo, for testing
    	String protocol = "http";
    	String host = "localhost";
    	int port = 8082;
    	String user = "fedoraAdmin";
    	String pass = "fedoraAdmin";
    	String context = "fedora";
    	try {
			FedoraRepositoryService fs = new FedoraRepositoryService(protocol, host, port, user, pass, context);

			fs.retrieveEntityTree(project);
			assertNotNull(project.entityTree);
			assertNotNull(project.entityTree.getProjectEntity());
			assertNull(project.entityTree.getProjectEntity().getTridasAsObject());
			// output the tree
			List<String> lines = project.entityTree.getProjectEntity().toTreeString("");
			for (String line : lines)
				System.out.println(line);

			// now get the project entity tridas?
			ProjectEntity projectEntity = project.entityTree.getProjectEntity();
			fs.retrieveEntity(project.getSid(), projectEntity);
			assertNotNull(projectEntity.getTridasAsObject());

			// get all and see if the result is correct?
			List<Entity> entities = project.entityTree.getEntities();
			for (Entity entity : entities) {
				fs.retrieveEntity(project.getSid(), entity);
				assertNotNull(entity.getTridasAsObject());
			}
			projectEntity.connectTridasObjectTree();
			// can we marshall?
			// note: should be part of a util.tridas package?

	    	// show tridas xml
	    	// note: we are not testing JAXB
			JAXBContext jaxbContext = null;
			try {
				System.out.println("\n TRiDaS XML");
				jaxbContext = JAXBContext.newInstance("org.tridas.schema");
				// now marshall the pruned clone
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//testing
				java.io.StringWriter sw = new StringWriter();
				marshaller.marshal(projectEntity.getTridasAsObject(), sw);
				System.out.print(sw.toString());
			} catch (JAXBException e1) {
				e1.printStackTrace();
				fail("JAXB exception");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}

	}

    public void SKIPtestGetDendroProjects()
    {
    	//note: It only works when the server is up
    	//RepositoryService repo = new XMLFilesRepositoryService();

    	// Note: should also test the FedoraRepositoryService constructor/initialization!

    	// use the Fedora repo, for testing
    	String protocol = "http";
    	String host = "localhost";
    	int port = 8082;
    	String user = "fedoraAdmin";
    	String pass = "fedoraAdmin";
    	String context = "fedora";
    	try {
			FedoraRepositoryService fs = new FedoraRepositoryService(protocol, host, port, user, pass, context);
			// Note: should we really retrieve all
			// better have some fake store,
			// probably fixed when using the common dans project!
			fs.getDendroProjects(1);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
    }

}
