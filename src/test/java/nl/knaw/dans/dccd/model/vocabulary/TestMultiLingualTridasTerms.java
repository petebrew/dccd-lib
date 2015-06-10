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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestMultiLingualTridasTerms
{
	@Test
	public void testGetTerms() throws Exception
	{
		// construct the table data for testing
		// should be non ordered and with non-unique terms
		String[][] arrayTable = 
		{
				{"abc","b"},
				{"a","b"},
				{"c","b"},
				{"b","b"},
				{"a","b"},
				{"c","b"},
				{"a","b"},
				{"b","b"},
				{"c","b"},
				{"b","b"}
		};
		
		// convert to Lists
		List<List<String>> listTable = new ArrayList<List<String>>();
		for(String[] row : arrayTable)
		{
			listTable.add(Arrays.asList(row));	
		}
		
		MultiLingualTridasTerms terms = new MultiLingualTridasTerms(listTable);
		List<String> resultTerms = terms.getTerms("abc");

		//for(String term : resultTerms)
		//{
		//	System.out.println(term);
		//}
		
		// should be "a,b,c"
		assertEquals(resultTerms.get(0), "a");
		assertEquals(resultTerms.get(1), "b");
		assertEquals(resultTerms.get(2), "c");
		
		resultTerms = terms.getTerms("b");
		// should be "b"
		assertEquals(resultTerms.get(0), "b");
	}	

	/*
	@Test
	public void testConstructMultiLingualTridasTerms() throws Exception
	{
		//MultiLingualTridasTerms
		// if testReadCSV did not fail we can have CSV data
		String cSVString = "en,    nl,   de\n"+
		   "one,   een,  ein\n"+
		   "two,   twee, zwei\n"+
		   "three, drie, drei";
		StringReader reader = new StringReader(cSVString);
		List<List<String>> linesRead = TableDataUtil.ReadDelimited(reader, ",");
		
		// TODO construct lines without reader
		
		MultiLingualTridasTerms terms = new MultiLingualTridasTerms(linesRead);
		List<String> enTerms = terms.getTerms("en");
		List<String> nlTerms = terms.getTerms("nl");
		List<String> deTerms = terms.getTerms("de");
		
		// TODO assert things...
	}
	*/
}
