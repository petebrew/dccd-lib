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
package nl.knaw.dans.dccd.application.services;

import java.util.List;

import nl.knaw.dans.dccd.model.Project;

import org.junit.Test;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasProject;

/**
 *  It is an on-line test;
 *  Because the validation also checks if a project has an unique identifier (in the repostory)
 *  the repository service must be available
 * 
 * @author dev
 *
 */
public class TestDccdProjectValidationServiceOnlineTest
{
	// Simple test, no checking
	@Test
	public void testValidateProject() throws Exception
	{
		// construct project
		Project project = createMinimalValidTridasProject();
		project.getTridas().setInvestigator(null);
		project.getTridas().setTitle(null);
		project.getTridas().setPeriod(null);
		// Note that the DCCD validation also checks for some empty strings and 
		// identifier.domain, category and type[0] are therefore missing
		
		// from reading the schema, we know what we need
		// This is also done in the UIMapper, where we specify which attributes are required
		//
		// "Title"
		// "Types" , at least one
		// "Investigator"
		// "Period"
		// "Category"
		// "Laboratories", at least one

		List<ValidationErrorMessage> messages = DccdProjectValidationService.getService().validate(project);	
		for(ValidationErrorMessage msg : messages)
		{
			System.out.println(msg.getMessage() + ", " + msg.getClassName()+ ", " + msg.getFieldName() );
		}
		
		// - output 
		// Missing required field, org.tridas.schema.TridasProject, investigator
		// Missing required field, org.tridas.schema.TridasProject, period
		// Missing required field, org.tridas.schema.TridasProject, title
		// Missing required field, org.tridas.schema.TridasProject, identifier.domain
		// Missing DCCD controlled vocabulary, org.tridas.schema.TridasProject, category
		// Missing DCCD controlled vocabulary, org.tridas.schema.TridasProject, type[0]		
		
		// ?Why not: 
		// Missing required field , org.tridas.schema.TridasProject, laboratories[0].address
		// Missing required field , org.tridas.schema.TridasProject, laboratories[0].name
	}

	/**
	 * This test does not need to be online!
	 * 
	 * @throws Exception
	 */
 	@Test
	public void testValidateTridasAgainsSchema() throws Exception
	{
		Project project = createMinimalValidTridasProject();
		
		// for testing;  corrupt it!
		//project.getTridas().getLaboratories().clear();
		//project.getTridas().setInvestigator(null);
		
		// actual testing
		List<ValidationErrorMessage> messages = DccdProjectValidationService.getService().validateAgainstTridasSchema(project);	
		
		for(ValidationErrorMessage msg : messages)
		{
			System.out.println(msg.getMessage() + ", " + msg.getClassName()+ ", " + msg.getFieldName() );
		}
	}
	
 	/**
 	 * Valid according to the TRiDaS XML schema, but not for DCCD!
 	 * @return
 	 */
 	private Project createMinimalValidTridasProject()
 	{
		ObjectFactory objectFactory = new ObjectFactory();
		
		// construct minimal project, with lots of empty fields
		Project project = new Project();
		TridasProject projectTridas = objectFactory.createTridasProject();
		projectTridas.setComments("Test");
		
		// set the title, so we know we don't get an error about it!
		projectTridas.setTitle("My Title");
		
		// Identifier, but with an empty domain?
		TridasIdentifier identifier = objectFactory.createTridasIdentifier();
		projectTridas.setIdentifier(identifier);
		
		// Type
		ControlledVoc type = objectFactory.createControlledVoc();
		projectTridas.getTypes().add(type);
		
		// Laboratory, but incomplete
		TridasLaboratory laboratory =  objectFactory.createTridasLaboratory();
		projectTridas.getLaboratories().add(laboratory);
		TridasLaboratory.Name name = new TridasLaboratory.Name();
		laboratory.setName(name);
		TridasAddress address = objectFactory.createTridasAddress();
		laboratory.setAddress(address);
		
		// Category
		ControlledVoc category = objectFactory.createControlledVoc();
		projectTridas.setCategory(category);
		
		// Investigator
		projectTridas.setInvestigator("Investigator");
		
		// Period
		projectTridas.setPeriod("Period");
		
		project.setTridas(projectTridas);
		project.entityTree.buildTree(projectTridas);
		return project;
 	}
}
