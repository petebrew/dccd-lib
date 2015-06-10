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

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.Test;

//import nl.knaw.dans.dccd.model.Project;
//import nl.knaw.dans.dccd.repository.xml.XMLFilesRepositoryService;
//
//import org.tridas.schema.ControlledVoc;
//import org.tridas.schema.ObjectFactory;
//import org.tridas.schema.TridasAddress;
//import org.tridas.schema.TridasIdentifier;
//import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasProject;

public class TestJAXB
{
	//private static Logger logger = Logger.getLogger(TestJAXB.class);
	
	@Test
	public void testLoading() throws Exception
	{
/*
		String filename = "complexexamplev121.xml";
		//FileInputStream is = new FileInputStream (filename);
		InputStream is = this.getClass().getResourceAsStream(filename);

		JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object o = unmarshaller.unmarshal(is);

		logger.info("Unmarshalled object of class: " + o.getClass().getName());

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//testing
		java.io.StringWriter sw = new StringWriter();
		marshaller.marshal(o, sw);

		System.out.print(sw.toString());
*/
	}

	@Test
	public void testMarshalling() throws Exception
	{
    	TridasProject projectTridas = new TridasProject();
    	projectTridas.setTitle("my test project");

    	// Add a tridas object entity
//    	TridasObject objectTridas = new TridasObject();
//    	objectTridas.setTitle("my test object");
//    	projectTridas.getObjects().add(objectTridas);

    	// TODO fix namespace
    	JAXBContext jaxbContext = JAXBContext.newInstance("org.tridas.schema");
    	Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//testing
		java.io.StringWriter sw = new StringWriter();
		marshaller.marshal(projectTridas, sw);

		//System.out.print(sw.toString());
	}

}
