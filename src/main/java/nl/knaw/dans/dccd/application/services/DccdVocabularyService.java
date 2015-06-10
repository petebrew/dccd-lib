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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.knaw.dans.dccd.model.vocabulary.MultiLingualTridasTerms;
import nl.knaw.dans.dccd.model.vocabulary.TridasTaxonomy;
import nl.knaw.dans.dccd.util.CaseInsensitiveComparator;
import nl.knaw.dans.dccd.util.TableDataUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DccdVocabularyService
{
	private static final Logger logger = LoggerFactory.getLogger(DccdVocabularyService.class);
	private static MultiLingualTridasTerms projectCategoryTerms = null;
	private static MultiLingualTridasTerms projectTypeTerms = null;
	private static MultiLingualTridasTerms objectTypeTerms = null; // also for element
	private static MultiLingualTridasTerms sampleTypeTerms = null;
	private static MultiLingualTridasTerms derivedseriesTypeTerms = null;
	// NOT Interpretation.StatFoundation.Type
	private static TridasTaxonomy elementTaxonTaxonomy = null;
	
	// TODO make hashMap for all supported vocabulary names...
	// TODO make DccdVocabulary interface or abstract baseclass and have the 
	// MultiLingualTridasTerms and TridasTaxonomy extend or implement it
	private String defaultLanguageCode = "en";
	
	// singleton pattern with lazy construction
	private static DccdVocabularyService service = null;

	public static DccdVocabularyService getService()
	{
		if (service == null)
		{
			service = new DccdVocabularyService();
			init();
		}
		return service;
	}

	private DccdVocabularyService()
	{
		// disallow construction
	}

	private static void init()
	{
		// Read all the data we need, from files 
		projectCategoryTerms = createTermsFromFile("project.category.txt");
		projectTypeTerms = createTermsFromFile("project.type.txt");
		objectTypeTerms = createTermsFromFile("object.type.txt");
		sampleTypeTerms = createTermsFromFile("sample.type.txt");
		derivedseriesTypeTerms = createTermsFromFile("derivedseries.type.txt");
		
		initElementTaxonTaxonomy();
	}
	
	private static MultiLingualTridasTerms createTermsFromFile(final String vocFilename)
	{
		InputStream resourceAsStream = DccdVocabularyService.class.getClassLoader()
										.getResourceAsStream(vocFilename);
		
		// TODO check for null
		InputStreamReader reader = new InputStreamReader(resourceAsStream);
		List<List<String>> linesRead = DccdVocabularyService.getService().ReadTabledata(reader);
		return new MultiLingualTridasTerms(linesRead);
	}
	
	// elementTaxonTaxonomy
	private static void initElementTaxonTaxonomy()
	{
		// terms for the 'project.category' tridas property		
		InputStream resourceAsStream = DccdVocabularyService.class.getClassLoader()
										.getResourceAsStream("element.taxon.txt");
		// TODO check for null
		InputStreamReader reader = new InputStreamReader(resourceAsStream);
		List<List<String>> linesRead = DccdVocabularyService.getService().ReadTabledata(reader);
		elementTaxonTaxonomy = new TridasTaxonomy(linesRead);
	}

	
	// Note: could use reflection here, or a map
	public MultiLingualTridasTerms getMultiLingualTridasTerms(final String vocabularyName)
	{
		if (vocabularyName.contentEquals("project.category")) 
		{
			return projectCategoryTerms;
		}
		else if (vocabularyName.contentEquals("project.type")) 
		{
			return projectTypeTerms;
		}
		else if (vocabularyName.contentEquals("object.type") || 
				 vocabularyName.contentEquals("element.type")) 
		{
			return objectTypeTerms;
		}
		else if (vocabularyName.contentEquals("sample.type")) 
		{
			return sampleTypeTerms;
		}
		else if (vocabularyName.contentEquals("derivedseries.type")) 
		{
			return derivedseriesTypeTerms;
		}
		else
		{
			logger.debug("unknown vocabulary name: " + vocabularyName);
			return null; //Throw exception?
		}
	}

	// return the terms in the default language, English (UK)
	public List<String> getTerms(final String vocabularyName)
	{
		return getTerms(vocabularyName, defaultLanguageCode);
	}
	
	// return the terms in the given language, default when not available
	public List<String> getTerms(final String vocabularyName, final String languageCode)
	{
		String effectiveLanguageCode = languageCode;
		
		if (vocabularyName.contentEquals("project.category")) 
		{
			if (!projectCategoryTerms.supportsLanguage(languageCode))
				effectiveLanguageCode = defaultLanguageCode;
			
			return projectCategoryTerms.getTerms(effectiveLanguageCode);
		}
		else if (vocabularyName.contentEquals("project.type")) 
		{
			if (!projectTypeTerms.supportsLanguage(languageCode))
				effectiveLanguageCode = defaultLanguageCode;

			return projectTypeTerms.getTerms(effectiveLanguageCode);
		}
		else if (vocabularyName.contentEquals("element.taxon")) 
		{
			return elementTaxonTaxonomy.getTerms(); // no specific language
		}
		else if (vocabularyName.contentEquals("object.type") || 
				 vocabularyName.contentEquals("element.type")) 
		{
			if (!objectTypeTerms.supportsLanguage(languageCode))
				effectiveLanguageCode = defaultLanguageCode;

			return objectTypeTerms.getTerms(effectiveLanguageCode);
		}
		else if (vocabularyName.contentEquals("sample.type")) 
		{
			if (!sampleTypeTerms.supportsLanguage(languageCode))
				effectiveLanguageCode = defaultLanguageCode;

			return sampleTypeTerms.getTerms(effectiveLanguageCode);
		}
		else if (vocabularyName.contentEquals("derivedseries.type")) 
		{
			if (!derivedseriesTypeTerms.supportsLanguage(languageCode))
				effectiveLanguageCode = defaultLanguageCode;

			return derivedseriesTypeTerms.getTerms(effectiveLanguageCode);
		}
		else
		{
			//return empty list;
			return Collections.emptyList();
			// beter throw an UnsuportedVocabulary exception 
			// or a VocabularyServiceException?
		}
	}

	public boolean hasTerm(final String vocabularyName, String searchTerm)
	{
		return hasTerm(vocabularyName, searchTerm, defaultLanguageCode);
	}

	public boolean hasTerm(final String vocabularyName, String searchTerm, String languageCode)
	{
		List<String> allTerms =  getTerms(vocabularyName, languageCode);
		
		//String searchTerm = term.trim(); // just to be sure
		
	    CaseInsensitiveComparator cicomp = new CaseInsensitiveComparator();
		// find it
		// Ensure list sorted
	    Collections.sort(allTerms, cicomp);
	    // Search for element in list
	    //int index = Collections.binarySearch(allTerms, searchTerm);
    	// search case insensitive
	    int index = Collections.binarySearch(allTerms, searchTerm, cicomp);
	    
	    if (index < 0)
	    {
			return false;
	    }
	    else
	    {
	    	return true;
	    }
	}
	
	// Note: getTermsMatching was to general, 
	// and could be implemented when using regular expressions
	// 
	// the vocabularyName is closely related to the tridas element or attribute name 
	public List<String> getTermsStartingWith(final String vocabularyName, String start)
	{
		List<String> matchingTerms = new ArrayList<String>();
		
		List<String> allTerms =  getTerms(vocabularyName);
		
		// filter
		String lowercaseStart = start.toLowerCase();
		for (String term : allTerms)
		{
			// Make the comparison case insensitive
			String lowercaseTerm = term.toLowerCase();
			
			if (lowercaseTerm.startsWith(lowercaseStart))
			{
				matchingTerms.add(term);
			}
		}
		
		return matchingTerms;
	}	
	
	// Note on CSV or comma separated value files
	// the main problem with this format was that MSExcel 
	// wouldn't save CSV in UTF-8 or UTF-16.
	// Only tab delimited worked for UTF-16 characters.
	// Also the CSV has problems with commas in the values, 
	// needing double-quotes and escaping those quotes that are part of the value.
	// While it is unlikely that we would have tabs in our text values, 
	// because we work with describing terms or groups of terms 
	// and not with text being layed-out by tabs.
	
	// The table data is read from tab delimited (text) files in UTF-8 encoding
	// Perferably use an save option with "No BOM", otherwise the file start has this BOM, 
	// which gives problems on some String functions, like the Integer.parseInt() fails.
	//
	// When exporting from Excel files, make sure you have the languagecodes on the toprow, 
	// Then save as UTF-16 (*.txt), which is tabdelimited.
	// Use an text editor that can save to UTF-8 (No BOM).
	public List<List<String>> ReadTabledata(Reader in)
	{
		return TableDataUtil.ReadDelimited(in, "\t");
	}

	/**
	 * expose the taxonomy for taxonomy specific things 
	 * 
	 * @return
	 */
	public static TridasTaxonomy getElementTaxonTaxonomy()
	{
		return elementTaxonTaxonomy;
	}

}
