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

import java.util.List;

import nl.knaw.dans.dccd.model.vocabulary.MultiLingualTridasTerms;

import org.junit.Test;

public class TestDccdVocabularyService
{	
	// only try to read the terms
	@Test
	public void testProjectCategory() throws Exception
	{
		final String PROJECT_CATEGORY = "project.category";
		//System.out.println("Number of terms in " + "\'" + PROJECT_CATEGORY + "\'" + " = " + 
		//		DccdVocabularyService.getService().getTerms(PROJECT_CATEGORY).size());
		//System.out.println("Number of languages in " + "\'" + PROJECT_CATEGORY + "\'" + " = " + 
		//		DccdVocabularyService.getService().getMultiLingualTridasTerms(PROJECT_CATEGORY).getLanguageIndexMap().size());
		
		String text = "d";
		List<String> terms = DccdVocabularyService.getService().getTermsStartingWith(PROJECT_CATEGORY, text);
		//for(String term : terms)
		//{
		//	System.out.println(term);
		//}
		
		// must have at least a 'dating' type
	}
	
	// test object.type
	// only try to read the terms
	@Test
	public void testObjectType() throws Exception
	{
		final String OBJECT_TYPE = "object.type";
		//System.out.println("Number of terms in " + "\'" + OBJECT_TYPE + "\'" + " = " + 
		//		DccdVocabularyService.getService().getTerms(OBJECT_TYPE).size());
		//
		//System.out.println("Number of languages in " + "\'" + OBJECT_TYPE + "\'" + " = " + 
		//		DccdVocabularyService.getService().getMultiLingualTridasTerms(OBJECT_TYPE).getLanguageIndexMap().size());
			
		String text = "a";
		List<String> terms = DccdVocabularyService.getService().getTerms(OBJECT_TYPE);//StartingWith(OBJECT_TYPE, text);
		//for(String term : terms) System.out.println("["+term+"]");
		
		MultiLingualTridasTerms objectTypeTerms = DccdVocabularyService.getService().getMultiLingualTridasTerms(OBJECT_TYPE);
		//System.out.println("description index: " + objectTypeTerms.getDescriptionIndex());
		
		/*
		String toFind = "inhout";
		Boolean found = DccdVocabularyService.getService().hasTerm(OBJECT_TYPE, toFind, "nl");
		System.out.println("term " + toFind + ", found=" + found);
		toFind = "internal";
		found = DccdVocabularyService.getService().hasTerm(OBJECT_TYPE, toFind, "en");
		System.out.println("term " + toFind + ", found=" + found);
		*/
	}	
	
	// test derivedseries.type
	// only try to read the terms
	@Test
	public void testDerivedseriesType() throws Exception
	{
		final String VOC_NAME= "derivedseries.type";
		//System.out.println("Number of terms in " + "\'" + VOC_NAME + "\'" + " = " + 
		//		DccdVocabularyService.getService().getTerms(VOC_NAME).size());
		//
		//System.out.println("Number of languages in " + "\'" + VOC_NAME + "\'" + " = " + 
		//		DccdVocabularyService.getService().getMultiLingualTridasTerms(VOC_NAME).getLanguageIndexMap().size());
			
		String text = "a";
		List<String> terms = DccdVocabularyService.getService().getTerms(VOC_NAME);//StartingWith(VOC_NAME, text);
		//for(String term : terms) System.out.println("["+term+"]");
		
		MultiLingualTridasTerms vocTerms = DccdVocabularyService.getService().getMultiLingualTridasTerms(VOC_NAME);
	}
	
	// only try to read the terms
	@Test
	public void testElementTaxon() throws Exception
	{
		final String ELEMENT_TAXON = "element.taxon";
		//System.out.println("Number of terms in " + "\'" + ELEMENT_TAXON + "\'" + " = " + 
		//		DccdVocabularyService.getService().getTerms(ELEMENT_TAXON).size());

		String text = "a"; // only 'A', so testing case insensitivity
		List<String> terms = DccdVocabularyService.getService().getTermsStartingWith(ELEMENT_TAXON, text);
		//for(String term : terms)
		//{
		//	System.out.println(term);
		//}
		
		// catalog of live is BIG and stable, so we know we must have some results for 'a'
	}
}
