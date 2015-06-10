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

import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.repository.xml.XMLFilesRepositoryService;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestXMLFilesRepositoryService
{
	@Test
	public void testConstructTridasFilename()
	{
		// use a fake project, assume only title is used
		Project project = new Project();
		
		project.setTitle("  ..  .keep\t\nthis\\/*?<>:|\"\'");
		String filename = XMLFilesRepositoryService.constructTridasFilename(project);
		//System.out.println(filename);

		// no / or \ or...  also the single quote, just to be sure
		// first char is not a whitespace or a dot
		assertEquals("keep this _tridas.xml", filename);
		
		// create a long string, could be separate test
		String title = "0123456789";
		final int DOUBLING = 5; // Note: 2^DOUBLING is the total number of times the string is appended
		for(int i=0; i < 5; i++) 
			title = title + title;// grow fast!
		project.setTitle(title);
		filename = XMLFilesRepositoryService.constructTridasFilename(project);
		assertFalse(filename.length() > 128);
		assertFalse(filename.length() < 127); // but not to small
	}
}
