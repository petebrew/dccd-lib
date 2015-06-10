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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// a hierarchical structure
// but for now only a list of terms...
public class TridasTaxonomy
{
	private static final Logger logger = LoggerFactory.getLogger(TridasTaxonomy.class);
	private static final int EMPTY_ID = -1; 
	
	// for storing the terms with hierarchical information
	public class TaxonTerm 
	{
		public int id;
		public String name;
		public int parentIndex;
		TaxonTerm(int id, int parentIndex, String name)
		{
			this.id = id;
			this.name = name;
			this.parentIndex = parentIndex;
		}
	};
	private List<TaxonTerm> taxonTerms = new ArrayList<TaxonTerm>();
	// Note maybe use a hashMap instead of a list because the id's must be unique and searchable
	
	// Node for the taxonomy structure
	public class TaxonNode 
	{
		private TaxonTerm term;
	
		private List<TaxonNode> subNodes = new ArrayList<TaxonNode>();
		
		public TaxonNode(TaxonTerm term)
		{
			this.term = term;
		}
	
		public String getName()
		{
			return term.name;
		}
		
		public TaxonTerm getTerm()
		{
			return term;
		}

		public List<TaxonNode> getSubNodes()
		{
			return subNodes;
		}
		
		public List<String> toTreeString(String indent)
		{
			List<String> list = new ArrayList<String>();
			list.add(indent + "+-" + getTerm().name + " [" + getTerm().id + "]");

			// then the sun entities
			Iterator<TaxonNode> i = getSubNodes().iterator();
			while (i.hasNext())
			{
				TaxonNode node = i.next();
				list.addAll(node.toTreeString(indent + "  ")); // recursion
			}

			return list;
		}		
	}
	private TaxonNode rootNode = null;
	
	public TaxonNode getRootNode()
	{
		return rootNode;
	}

	private void constructNodeTree()
	{
		// add empty root node
		rootNode = null; //new TaxonNode(new TaxonTerm(1, EMPTY_ID, "---")); 
		
		// add the node without a parent (id = EMPTY_ID)
		// only one!
		for(TaxonTerm taxonTerm : taxonTerms)
		{
			if (taxonTerm.parentIndex == EMPTY_ID)
			{
				rootNode = new TaxonNode(taxonTerm);
				constructNodeSubTree(rootNode);
				break;
			}
		}
		
		// TEST print 
		//List<String> lines = toTreeString(" ");
		//for (String line : lines)
		//	System.out.println(line);			
	}
	
	private void constructNodeSubTree(TaxonNode parentNode)
	{
		int parentId = parentNode.getTerm().id;
		List<TaxonNode> subNodes = parentNode.getSubNodes();
		
		for(TaxonTerm taxonTerm : taxonTerms)
		{
			if (taxonTerm.id == EMPTY_ID) continue; // skip!
			
			if (taxonTerm.parentIndex == parentId)
			{
				// add child
				TaxonNode subNode = new TaxonNode(taxonTerm);
				subNodes.add(subNode);
//logger.debug("added node: " + taxonTerm.name + " [" + taxonTerm.id + "]");
				constructNodeSubTree(subNode);// recurse
			}
		}
	
	}

	// print tree for test purposes
	public List<String> toTreeString(String indent)
	{
		List<String> list = new ArrayList<String>();

		if (rootNode != null)
		{
			list.addAll(rootNode.toTreeString(" "));
		}		
		return list;
	}
	
	// construct from comma separated value (CSV) data
	// - term index (needed for a child to refer to its parent)
	// - column is the term (or type name)
	// - column has the term parent index, 
	//
	// Also the topmost (root) term has no parent (empty field)
	// Note XML might be a beter format for hierarchical data, 
	// but CSV is what we can get our hands on quickly from Excel data
	public TridasTaxonomy(List<List<String>> csvDataLines)
	{
		if (csvDataLines.isEmpty())
			return;
			
		int idColumnIndex = 0;
		int nameColumnIndex = 1;
		int parentColumnIndex = 2;
				
		// parse the parent indexes and build the list of terms
		for(int lineIndex = 0; lineIndex < csvDataLines.size(); lineIndex++)
		{
			List<String> dataLine = csvDataLines.get(lineIndex);

			// skip
			if (dataLine.size() < 2) // parentindex not needed for root elements
			{
				logger.debug("Not enough data; skipping line: " + lineIndex);
				continue; // skip!
			}
			
			// get index
			int id = parseIdentifier (dataLine, idColumnIndex);

			// get Parent index
			int parentIndex = parseIdentifier (dataLine, parentColumnIndex);
			
			// get Name 
			String termName = dataLine.get(nameColumnIndex).trim(); // remove surrounding whitespace
			
			//logger.debug("Taxon: " + id + "\t " + termName + "\t " + parentIndex);
			taxonTerms.add(new TaxonTerm(id, parentIndex, termName));
		}
		
//TEST		
constructNodeTree();		
	}
	
	private int parseIdentifier (List<String> dataLine, int columnIndex)
	{
		int id = EMPTY_ID;
		String trimmedIdString = "";
		
		if (dataLine.size() > columnIndex)
		{
			String idString = dataLine.get(columnIndex);

			// trim to be sure that leading whitespace doesn't prevent parsing
			trimmedIdString = idString.trim();

			try 
			{
				id = Integer.parseInt(trimmedIdString); 
			}
			catch (NumberFormatException e)
			{
				logger.debug("Unable to parse to integer: \"" + trimmedIdString + "\", assuming empty");
			}
		}
		else
		{
			logger.debug("No id at column: " + columnIndex);
		}
		
		return id;
	}
	
	// A list of terms without hierarchy
	public List<String> getTerms()
	{
		List<String> terms = new ArrayList<String>();
				
		for(TaxonTerm taxonTerm : taxonTerms)
		{
			terms.add(taxonTerm.name);
			// Note: not sorted, the original order should be optimal!
		}
		//logger.debug("number of terms: " + terms.size());		
		return terms;
	}

	// Note: we have broader/narrower terms when using a taxonomy
}
