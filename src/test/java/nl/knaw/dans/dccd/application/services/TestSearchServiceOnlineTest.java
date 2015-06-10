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

import java.util.Collection;
import java.util.Iterator;

import nl.knaw.dans.dccd.model.Project;

import org.junit.Test;

public class TestSearchServiceOnlineTest
{
	// dccd:185 locally this is  "Roman fortress Velsen 1"
	// dccd:199 Schepenhuis
	// dccd:212 De Schiphorst (heeft deathYear)
	// just get a bunch for testing
	final static String[] sids =
	{"dccd:175","dccd:185","dccd:186","dccd:195","dccd:197","dccd:198","dccd:199","dccd:200",
		"dccd:205","dccd:206","dccd:207","dccd:208","dccd:209","dccd:210","dccd:211",
		"dccd:212","dccd:213", "dccd:214","dccd:215","dccd:465","dccd:555"};

	@Test
	public void testUpdateSearchIndex() throws Exception
	{
		for (String sid : sids)
		{
			System.out.println("Retrieving: "+ sid);
			Project project = getProjectFromRepo(sid);
			System.out.println("Updating search index");
			DccdSearchService.getService().updateSearchIndex(project);
			// what to check now ?
		}
	}

	@Test
	public void testUpdateSearchIndexWithForcedOwners() throws Exception
	{
		int i = 0;
		for (String sid : sids)
		{
			System.out.println("Retrieving: "+ sid);
			Project project = getProjectFromRepo(sid);
			if (i%2 == 0)
				project.setOwnerId("normaltestuser");
			else
				project.setOwnerId("admintestuser");
			i++;
			System.out.println("Updating search index");
			DccdSearchService.getService().updateSearchIndex(project);
			// what to check now ?
		}
	}

	@Test
	public void testDeleteSearchIndex() throws Exception
	{
		for (String sid : sids)
		{
			Project project = getProjectFromRepo(sid);

			DccdSearchService.getService().deleteSearchIndex(project);
			// what to check now ?
		}
	}

	@Test
	public void testBeanConversion() throws Exception
	{
		String sid = sids[0];// just one

		Project project = getProjectFromRepo(sid);

		// get the beans
		Collection<? extends Object> searchBeans = project.getSearchBeans();

		for (Iterator<? extends Object> iter = searchBeans.iterator(); iter.hasNext();)
		{
		   Object object = (Object) iter.next();

		   // TODO test the fields that are needed...

			// just show the content
		   System.out.println(object.toString());
		}
	}

	private Project getProjectFromRepo(String sid) throws Exception
	{
		//Project project = new Project();
		//project.setSid(sid); //??
		//project.setStoreId(sid);

		// get project from Repository
		Project project = DccdDataService.getService().getProject(sid);

		return project;
	}

}
