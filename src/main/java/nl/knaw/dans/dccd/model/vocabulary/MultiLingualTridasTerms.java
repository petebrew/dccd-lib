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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.dccd.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The terms for use in Tridas elements or atributes (properties) 
 * in multiple languages. 
 * Its not a dictionary, because there are no descriptions of the terms
 * 
 * Note
 * In the simple case we have one line per term/concept and the 
 * translation from one language to another is simply 
 * finding the other language on the same line. 
 * But in general there is no exact translation because the concepts differ slightly. 
 * Each line in the list of terms is still a relation or mapping between terms, 
 * but there can be more in the list. 
 * 
 * 
 * @author dev
 *
 */
public class MultiLingualTridasTerms
{
	private static final Logger logger = LoggerFactory.getLogger(MultiLingualTridasTerms.class);
	
	// two dimensional table of strings
	// List of Lists, and not List of arrays
	private List<List<String>> termLines = new ArrayList<List<String>>();//empty
	
	// also need mapping from language name to index!
	// LanguageProvider uses the two letter code (lowercase)
	// so we should test if it corresponds to a Locale...
	// HashMap
	private HashMap<String, Integer> languageIndexMap = new HashMap<String, Integer>();
	
	// TODO unit-test
	//
	// construct from the table data; 
	//  First line contains languages, 
	//  and value strings are not yet trimmed
	public MultiLingualTridasTerms(List<List<String>> tableDataLines)
	{
		if (tableDataLines.isEmpty())
		{
			logger.debug("No data for terms");
			return;
		}
		
		List<String> langLine = tableDataLines.get(0);
		if (langLine.isEmpty())
		{
			logger.debug("No language specification for terms");
			return;
		}
		
		// fill the language map
		for (int langIndex = 0; langIndex < langLine.size(); langIndex++)
		{
			String langString = langLine.get(langIndex).trim();
			// TODO error if the key is already there!
			
			languageIndexMap.put(langString, langIndex);
		}
		
		// TODO only use languages, ignore extra columns?
		// add lines and also check if we have a term for each language
		int numLanguages = langLine.size();
		for(int lineIndex = 1; lineIndex < tableDataLines.size(); lineIndex++)
		{
			List<String> line = tableDataLines.get(lineIndex);
			
			if (line.size() < numLanguages)
			{
				// we have a problem, missing some language(s)
				// but skip line
				// TODO give warning
				logger.debug("Skipping line: " + lineIndex + ", not enough terms");
				continue;
			}
			else
			{
				// trim strings
				for(int i=0; i < line.size(); i++) 
					line.set(i, line.get(i).trim());
				// copy the line
				termLines.add(line);
			}
		}	
	}
	
	public List<List<String>> getTermLines()
	{
		return termLines;
	}

	public HashMap<String, Integer> getLanguageIndexMap()
	{
		return languageIndexMap;
	}
	public boolean supportsLanguage(String languageCode)
	{
		Integer languageIndex = getLangueageIndex(languageCode);
		if (languageIndex != null)
			return true;
		
		logger.debug("Language not found for code:" + " \"" + languageCode + "\"");
		// Maybe it has to do with different encodings for the strings?
		String langString = "";
		Set<String> keySet = languageIndexMap.keySet();
		for(String key : keySet)
			langString += " \""+key+"\"";
		logger.debug("language codes:" + langString);
		
		return false;
	}
	
	// TODO implement unit-test for non-set language etc.
	// get the terms for a specific language
	// 			
	// Note: return a freshly constructed list, so callers can mess with it without others seeing it 
	//
	// Note: we could keep the terms per language 
	// or even initialize them at construction
	// when generating the lists on the fly is to slow.
	public List<String> getTerms(String languageCode)
	{
		List<String> terms = new ArrayList<String>();
		List<String> uniqueTerms = new ArrayList<String>();

//		try
//		{
//			byte[] utf8Bytes = languageCode.getBytes("UTF8");
//			languageCode = new String(utf8Bytes, "UTF8");
//		}
//		catch (UnsupportedEncodingException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Integer languageIndex = getLangueageIndex(languageCode);
		if (languageIndex != null)
		{
			for(List<String> termLine : termLines)
			{
				terms.add(termLine.get(languageIndex));
			}
	
			uniqueTerms = StringUtil.getUniqueStrings(terms);
			Collections.sort(uniqueTerms);
			
			/*	OLD method for getting unique terms		
			// Have each term only once in the list, and sort it
			//sort first
			Collections.sort(terms);
			// copy only unique terms
			if (!terms.isEmpty())
			{
				uniqueTerms.add(terms.get(0));
				int lastIndex = 0;
				for (int i = 0; i < terms.size(); i++)
				{
					String term = terms.get(i);
					if (! uniqueTerms.get(lastIndex).contentEquals(term) )
					{
						uniqueTerms.add(term);
						lastIndex++;
					}
				}
			}
			*/
		}
		else
		{
			logger.debug("Language not found for code:" + " \"" + languageCode + "\"");
			// Maybe it has to do with different encodings for the strings?
			String langString = "";
			Set<String> keySet = languageIndexMap.keySet();
			for(String key : keySet)
				langString += " \""+key+"\"";
			logger.debug("language codes:" + langString);
		}
		
		return uniqueTerms;
	}
	
	public String getLanguageCode(int index)
	{
		String code = "";// return empty string if not found
		
		Set<String> keySet = languageIndexMap.keySet();
		Iterator<String> iterator = keySet.iterator();
		while(iterator.hasNext()) {
			String key = iterator.next().toString();
			int value = languageIndexMap.get(key);
			if (value == index)
			{
				code = key;
				break; // Found
			}
		}
		
		return code;
	}
	
	
	// get all translations for the given term in the given language (expand term)?
	// this is a direct relation because the other terms must be on the same line.
	// Note: A more complete list would be to get AllRelatedTerms, 
	// does the translation also for all terms found recursive until stable?
	public List<String> getAllTranslationsForTerm(String term, String languageCode)
	{
		List<String> transTerms = new ArrayList<String>();
		List<String> uniqueTransTerms = new ArrayList<String>();
		
		// find the lines with the term for the given language
		Integer languageIndex = getLangueageIndex(languageCode);
		if (languageIndex != null)
		{
			for(List<String> termLine : termLines)
			{
				String termCandidate = termLine.get(languageIndex);
				if (termCandidate.equalsIgnoreCase(term))
				{
					// found, add all terms of this line to the list
					// except the term itself
					for(int i = 0; i < termLine.size(); i++)
					{
						if (i == languageIndex) continue; // Skip!
						String transTerm = termLine.get(i);
						transTerms.add(transTerm);
					}
				}
			}
			
			// Make collection unique
			StringUtil.getUniqueStrings(transTerms);
			
		}
		else
		{
			// unsupported language
			logger.debug("Language not found for code:" + " \"" + languageCode + "\"");
		}
		
		return uniqueTransTerms;
	}
	
	// possible members: 
	//
	// getSupportedLangages(), return a list of the lang strings
	//  supportsLanguage , but this is hasTerms()
	// translateTerm(term, fromlang, tolang) 
	//  - we need fromLang: the same string could be a term in different languages
	
	/**
	 *  We use the language code "description" for a column with the term description
	 *  TODO refactor this, also with a description we have a sort of Dictionary...
	 */
	public boolean hasDescription()
	{
		return languageIndexMap==null? false : languageIndexMap.containsKey("description");		
	}
	public int getDescriptionIndex()
	{
		return languageIndexMap==null? -1 : languageIndexMap.get("description");
	}
	
	private Integer getLangueageIndex(String languageCode)
	{
		Integer languageIndex = languageIndexMap.get(languageCode);
		
		// TODO, try a two letter index?
		
		return languageIndex;
	}
	
	/**
	 * Convert to a two letter lowercase language code
	 * ISO 639
	 * 
	 * @param languageCode
	 * @return
	 */
	private String getShortCode(String languageCode)
	{
		String shortcode = languageCode.substring(0, 2).toLowerCase();
		return shortcode;
	}
}
