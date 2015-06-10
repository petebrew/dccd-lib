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
package nl.knaw.dans.dccd.model.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestTridasTaxonomy
{
	@Test
	public void testConstructTridasTaxonomy() throws Exception
	{
		// construct the table data for testing
		String[][] arrayTable = {
				{"1","vehicles", 	""},
				{"2","cars", 		"1"},
				{"3","boats", 		"1"},
				{"4","sail boats",	"3"}
		};
		
		// convert to Lists
		List<List<String>> listTable = new ArrayList<List<String>>();
		for(String[] row : arrayTable)
		{
			listTable.add(Arrays.asList(row));	
		}
		
		TridasTaxonomy taxonomy = new TridasTaxonomy(listTable);
		
		// TODO test it
		int numberOfTerms = taxonomy.getTerms().size();
		//System.out.println("Number of terms in  = " + numberOfTerms);

	}
}
