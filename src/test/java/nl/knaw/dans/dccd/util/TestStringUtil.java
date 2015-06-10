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
package nl.knaw.dans.dccd.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestStringUtil
{
	@Test
	public void testCleanWhitespace() throws Exception
	{
		String cleanString = "word, and other word";
		String dirtyString = "\n\n word,   and   \t\t\n\n   other word \n";

		// no cleaning needed
		String result = StringUtil.cleanWhitespace(cleanString);
		assertEquals(result, cleanString);
		
		// need to clean
		result = StringUtil.cleanWhitespace(dirtyString);
		assertEquals(result, cleanString);
		
		//System.out.println("\"" + testManyString + "\"" + "=" + "\"" + result + "\"");
	}
	
	@Test
	public void testDuplicates() throws Exception
	{
		//List<String> testListEmpty = Arrays.asList(new String[] {});
		List<String> testList0 = Arrays.asList(new String[] {"a","b"});
		List<String> testList1 = Arrays.asList(new String[] {"a","a"});
		List<String> testList2 = Arrays.asList(new String[] {"a","a","b","b"});
		
		List<String> duplicates = StringUtil.getDuplicates(testList2);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 2);
		
		duplicates = StringUtil.getDuplicates(testList1);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 1);
		
		duplicates = StringUtil.getDuplicates(testList0);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 0);
	}
	
	// ignore case
	@Test
	public void testDuplicatesIgnoreCase() throws Exception
	{
		List<String> testList0 = Arrays.asList(new String[] {"a","b"});
		List<String> testList1 = Arrays.asList(new String[] {"a","A"});
		List<String> testList2 = Arrays.asList(new String[] {"a","a","B","b"});
		
		List<String> duplicates = StringUtil.getDuplicatesIgnoreCase(testList2);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 2);
		
		duplicates = StringUtil.getDuplicatesIgnoreCase(testList1);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 1);
		
		duplicates = StringUtil.getDuplicatesIgnoreCase(testList0);
		//System.out.print(duplicates);
		assertEquals(duplicates.size(), 0);
	}
}
