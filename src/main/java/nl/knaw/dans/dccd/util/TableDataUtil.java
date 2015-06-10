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

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableDataUtil
{
	private static final Logger logger = LoggerFactory.getLogger(TableDataUtil.class);
	
	// to read table data in text format
	// Note: No escaping or quoting and preserving whitespace (no trimming)
	public static List<List<String>> ReadDelimited(Reader in, String delimiter)
	{
		List<List<String>> lines = new ArrayList<List<String>>();
				
		try
		{
			//create BufferedReader to read data
			BufferedReader br = new BufferedReader(in);
			String strLine = "";
			StringTokenizer st = null;
			int lineNumber = 0;
 
			// read separated data line by line
			while( (strLine = br.readLine()) != null)
			{
				lineNumber++;
 
				List<String> newLine = new ArrayList<String>();
				lines.add(newLine);

				// split delimiter separated line
				//
				// StringTokenizer and the String split function combine delimiters with nothing in-between 
				// to one delimiter effectively. 
				// This is nice with normal text and whitespace, but the table can have empty cells 
				// which we want to preserve
				int tokenStart = 0;
				while(true)
				{
					int dInd = strLine.indexOf('\t', tokenStart);
					if (dInd != -1) // found
					{
						// copy token
						newLine.add(strLine.substring(tokenStart, dInd));
						tokenStart = dInd + 1;
					}
					else
					{
						// no delimiter anymore
						// copy last token
						newLine.add(strLine.substring(tokenStart, strLine.length()));
						break;
					}
				}
				//logger.debug("line: " + lineNumber + ", size: " + newLine.size());
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception while reading data file: " + e);			
		}
		
		return lines;
	}

}
