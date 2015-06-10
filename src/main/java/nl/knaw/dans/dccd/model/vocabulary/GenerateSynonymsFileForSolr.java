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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import nl.knaw.dans.dccd.application.services.DccdVocabularyService;

/**
 * Generates a text file with the synonyms for the Solr search index. 
 * The content must be added to the Solr synonyms file:
 * /data/solr/cores/dendro/conf/synonyms.txt
 * The schema.xml file in the same directory must also use the synonyms: 
 * <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/>
 * 
 * This enables the DCCD user to find terms in a different language than he/she specified in the query
 * For instance; Church will expand to Kerk, Kirche and Église.
 * 
 * @author dev
 *
 */
public class GenerateSynonymsFileForSolr
{
	public static void main(String[] args) throws Exception
	{
		String filename = "dccd-synonyms.txt";
		FileWriter fstream = new FileWriter(filename);
        BufferedWriter out = new BufferedWriter(fstream);
        
        System.out.println("Generating synonyms");
        System.out.println("Writing output to: " + filename + 
        		", At: " + System.getProperty("user.dir"));

        out.write("# DCCD synonyms for supporting finding with terms in multiple languages");
        
		printVocabularyTerms("project.category.txt", out);
		printVocabularyTerms("project.type.txt", out);
		printVocabularyTerms("sample.type.txt", out);
		printVocabularyTerms("derivedseries.type.txt", out);
		
		printVocabularyTerms("object.type.txt", out);
		
		out.close();
		System.out.println("Done Generating synonyms");
	}
	
	private static void printVocabularyTerms(String vocabularyFileName, BufferedWriter out) throws IOException
	{
		// terms for the 'project.category' tridas property		
		InputStream resourceAsStream = DccdVocabularyService.class.getClassLoader()
										.getResourceAsStream(vocabularyFileName);
		// TODO check for null
		InputStreamReader reader = new InputStreamReader(resourceAsStream);
		List<List<String>> linesRead = DccdVocabularyService.getService().ReadTabledata(reader);
		MultiLingualTridasTerms terms = new MultiLingualTridasTerms(linesRead);
		List<List<String>> termLines = terms.getTermLines();
		System.out.println("\t vocabulary file: " + vocabularyFileName);
		
		//System.out.print("\n# " + vocabularyFileName +"\n");
		out.newLine();
		out.write("# " + vocabularyFileName);
		out.newLine();
		for(List<String> termLine : termLines)
		{
			// termLine
			// TODO filter out non-language columns ?
			// for now assume that upto and including the fourth column is a true term
			int lastColumnIndex = termLine.size()-1;
			if (lastColumnIndex > 3) lastColumnIndex = 3;

			//System.out.print(termLine.get(0));
			out.write(termLine.get(0));
			for(int i = 1; i <= lastColumnIndex; i++)
			{
				String term = termLine.get(i);
				
				// escape whitespace for solr
				term = term.trim().replace(" ", "\\ ");
				
				out.write("," + term);
			}
			//System.out.print("\n");
			out.newLine();
		}
		
	}
}
