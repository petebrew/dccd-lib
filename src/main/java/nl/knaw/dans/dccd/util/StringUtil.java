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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StringUtil
{
	/**
	 * Reduce multiple consecutive whitspaces to a single space (also removing the \n, \t etc.)
	 * and also trim the string
	 * 
	 * @param string
	 * @return
	 */
	public static String cleanWhitespace(String string)
	{
		String reduced = string.replaceAll("\\s+", " ");
		
		return reduced.trim();
	}
	
	public static List<String> getUniqueStrings(List<String> strings)
	{
		List<String> resultList = new ArrayList<String>();
		resultList.addAll(strings);
		
		// Make sure that each string is unique
		// using a HashSet is a known solution 
		Set set = new HashSet(resultList);
		String[] array = (String[])(set.toArray(new String[set.size()]));
		
		//List<String> list = Arrays.asList(array);
		//resultList = list;
		// Note Arrays.asList returns a List that does not support remove, it's a crippled List!
		// remove throws a UnsupportedOperationException
		resultList.clear();
		for(int i = 0; i < array.length; i++)
		{
			resultList.add(array[i]);
		}
		
		return resultList;
	}
	
	/**
	 * Collect multiple occurrences of the strings
	 * returns an empty list if none are found
	 * Note: each term that occurs multiple times is added only once to the resulting list!
	 * 
	 * could be even more generic to work with any type of object that has an equals?
	 * now used for References from TRiDaS to treering data files
	 */
	public static List<String> getDuplicates(List<String> strings)
	{
		List<String> duplicates = new ArrayList<String>();

		// brute force O(n^2)
		// Note; if we use a fast sorting alg. we could improve

		// copy the given list and sort it
		List<String> sortedItems = new ArrayList<String>(strings);
		Collections.sort(sortedItems);
		Iterator<String> iter = sortedItems.iterator();

		if (iter.hasNext())
		{
			String item = iter.next(); // initialize
			while (iter.hasNext())
			{
			    String nextItem = iter.next();
			    if (item.equals(nextItem))
			    {
			    	// duplicate detected
			    	duplicates.add(item);
			    	// skip all others as well
			    	while (iter.hasNext())
			    	{
			    		nextItem = iter.next();
			    		if (!item.equals(nextItem))
			    		{
			    			// it's different so we have
			    			// a new item to search duplicates for
			    			item = nextItem;
			    			break;
			    		}
			    	}
			    }
			}
		}
		return duplicates;
	}

	/**
	 * Ignores case differences and the resulting (duplicate)strings are made lowercase
	 * 
	 * @param strings
	 * @return
	 */
	public static List<String> getDuplicatesIgnoreCase(List<String> strings)
	{
		// copy the given list and convert to lowercase
		List<String> lowercasedItems = new ArrayList<String>();
		for(String item : strings)
		{
			lowercasedItems.add(item.toLowerCase());
		}

		return getDuplicates(lowercasedItems);
	}
	
	public static String constructCommaSeparatedString(List<String> list)
	{
		return merge(list, ", ");
	}

	/**
	 * Merge all strings into a single string with the delimiter in between the items
	 * Can be seen as the reverse of a string splitter
	 *
	 * @param list
	 * @param seperator
	 * @return
	 */
	public static String merge(final List<String> list, final String seperator)
	{
		if (list == null)
			return "";

		StringBuilder sb = new StringBuilder();
		for (String item : list)
		{
			// skip empty strings
			if (item.length() == 0) continue;

			if (sb.length() > 0) sb.append(seperator); // not so efficient
			sb.append(item);
		}
		return sb.toString();
	}
	
	// Use reflection to invoke the given method
	public static String constructCommaSeparatedString(List<Object> list, String methodName)
	{
		StringBuilder sb = new StringBuilder();
		for (Object item : list)
		{
			if (sb.length() > 0) sb.append(", ");
			try {
				Method method = item.getClass().getMethod(methodName);
				String itemAsString = (String)method.invoke(item);
				sb.append(itemAsString);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// TODO
	// add truncated version; see CombinedUpload.getAssociatedFilesUploadedMessage
}
