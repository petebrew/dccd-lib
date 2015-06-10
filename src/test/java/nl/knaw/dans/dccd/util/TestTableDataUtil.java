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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

public class TestTableDataUtil
{
	@Test
	public void testReadDelimited() throws Exception
	{
		String delimiter = ",";
		
		String delimitedString = 
						   "en"+delimiter+"    nl"+delimiter+"   de\n"+
						   "one"+delimiter+"   een"+delimiter+"  ein\n"+
						   "two"+delimiter+"   twee"+delimiter+" zwei\n"+
						   "three"+delimiter+" drie"+delimiter+" drei";
		StringReader reader = new StringReader(delimitedString);
		
		List<List<String>> linesRead = TableDataUtil.ReadDelimited(reader, delimiter);
				
		// construct CSV string
		StringBuilder sb = new StringBuilder();
		for(int lineIndex = 0; lineIndex < linesRead.size(); lineIndex++)
		{
			if (lineIndex > 0) // not the first line
				sb.append("\n");
			List<String> line = linesRead.get(lineIndex);
			for (int valueIndex = 0; valueIndex < line.size(); valueIndex++)
			{
				if (valueIndex > 0) // not the first value on the line
					sb.append(delimiter);
				sb.append(line.get(valueIndex));
			}
		}
		String result = sb.toString();
		assertEquals(delimitedString, result);
		
		//System.out.print(result);
	}

	/*
	@Test
	public void testReadTabdelimited()
	{
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("element.taxon.txt");
		
		InputStreamReader reader=null;
		//try
		//{
			reader = new InputStreamReader(resourceAsStream);//, "UTF-8");
		//}
		//catch (UnsupportedEncodingException e)
		//{
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		List<List<String>> linesRead = DccdVocabularyService.getService().ReadDelimited(reader, "\t");
				
		// TODO assert some things...
		
		// construct string
		StringBuilder sb = new StringBuilder();
		for(int lineIndex = 0; lineIndex < linesRead.size(); lineIndex++)
		{
			if (lineIndex > 0) // not the first line
				sb.append("\n");
			List<String> line = linesRead.get(lineIndex);
			for (int valueIndex = 0; valueIndex < line.size(); valueIndex++)
			{
				if (valueIndex > 0) // not the first value on the line
					sb.append(",");
				sb.append(line.get(valueIndex));
			}
		}
		String result = sb.toString();
		
		//try {
		//    System.setOut(new PrintStream(System.out, true, "UTF-8"));
		//} catch(Exception e) {
		//    e.printStackTrace();
		//}
		System.out.print(result);
		
	}
	*/
	
}
