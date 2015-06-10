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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.common.lang.dataset.DatasetSB;
import nl.knaw.dans.common.lang.dataset.DatasetState;
import nl.knaw.dans.common.lang.search.bean.AbstractSearchBeanFactory;

import nl.knaw.dans.common.lang.ClassUtil;
import nl.knaw.dans.common.lang.search.SearchEngine;
import nl.knaw.dans.common.lang.search.SearchRequest;
import nl.knaw.dans.common.lang.search.SearchResult;
import nl.knaw.dans.common.lang.search.exceptions.SearchEngineException;
import nl.knaw.dans.common.lang.search.simple.EmptySearchResult;
import nl.knaw.dans.common.lang.search.simple.SimpleField;
import nl.knaw.dans.common.lang.search.simple.SimpleSearchHit;
import nl.knaw.dans.common.lang.search.simple.SimpleSearchRequest;
import nl.knaw.dans.common.solr.SolrSearchEngine;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.repository.DccdRepositoryException;
import nl.knaw.dans.dccd.repository.fedora.FedoraRepositoryService;
import nl.knaw.dans.dccd.search.DccdIndex;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;
import nl.knaw.dans.dccd.search.DccdSearchBeanFactory;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasProject;

/**
 * This implementation uses Solr.
 *
 * @author paulboon
 *
 */
public class DccdSearchService implements SearchService {
	private static Logger logger = Logger.getLogger(DccdSearchService.class);
	// singleton pattern with lazy construction
	private static DccdSearchService service = null;
	public static SearchService getService() {
		if (service == null) {
			service = new DccdSearchService();
		}
		return service;
	}

	private SolrSearchEngine searchEngine = null;

	private SolrSearchEngine easySearchEngine = null;

	//TODO: hide this from the services. This should be part of the
	// data layer, not the business layer
	synchronized SearchEngine getSearchEngine()
	{
		if (searchEngine == null)
		{
			Properties settings = DccdConfigurationService.getService().getSettings();
			String solrUrl = settings.getProperty("solr.url");
			try
			{
				searchEngine  = new SolrSearchEngine(solrUrl, new DccdSearchBeanFactory());
//				searchEngine  = new SolrSearchEngine(solrUrl, new DccdSearchBeanFactory(), false);
			} catch (MalformedURLException e)
			{
				logger.error("Malformed URL for Solr searchengine: " + solrUrl, e);
			}
		}
		return searchEngine;
	}

	synchronized SearchEngine getEasySearchEngine()
	{
		if (easySearchEngine == null)
		{
			Properties settings = DccdConfigurationService.getService().getSettings();
			String solrUrl = settings.getProperty("easy.solr.url");
			// Note: was hardcoded to "http://localhost:8983/solr"
			
			if (solrUrl != null && !solrUrl.isEmpty()) 
			{
				try
				{
					AbstractSearchBeanFactory easySearchBeanFactory = new AbstractSearchBeanFactory()
					{
						public Class<?>[] getSearchBeanClasses()
						{
							return new Class<?>[] { DatasetSB.class };
						}
					};
					easySearchEngine  = new SolrSearchEngine(solrUrl, easySearchBeanFactory);
				} catch (MalformedURLException e)
				{
					logger.error("Malformed URL for EASY Solr searchengine", e);
				}				
			}
		}
		return easySearchEngine;
	}


	// TODO Don't create the new SolrSearchEngine every time...
	public void updateSearchIndex(Project project) throws SearchServiceException
	{
		// TODO get URL from properties like with the 'OLD' search
		try
		{
			SearchEngine searchEngine = getSearchEngine();
			// construct the beans and index them
			Collection<? extends Object> dccdBeans = project.getSearchBeans();
			searchEngine.indexBeans(dccdBeans);
		}
		catch (SearchEngineException e)
		{
			throw new SearchServiceException(e);
		}

	}


	public void deleteSearchIndex(Project project) throws SearchServiceException
	{
		// TODO get URL from properties like with the 'OLD' search
		try
		{
			SearchEngine searchEngine = getSearchEngine();
			// construct the beans and index them
			Collection<? extends Object> dccdBeans = project.getSearchBeans();
			searchEngine.deleteBeans(dccdBeans);
		}
		catch (SearchEngineException e)
		{
			throw new SearchServiceException(e);
		}

	}

	/** search through all data, no filtering ! */
	public SearchResult<? extends DccdSB> doSearch(SearchRequest request)
		throws SearchServiceException
	{
		// Make sure that highlighting is disabled, 
		// because otherwise we get a 'Bad Request' error from the Solr Server
		request.setHighlightingEnabled(false);

		try
		{
			request.setIndex(new DccdIndex());

			// avoid adding the filter for DccdSB
			// if there is already one that is derived from it.
			// Otherwise the derived ones are overruled by DccdSB
			boolean hasDccdSBSubType = false;
			for (Class<?> clazz : request.getFilterBeans())
			{
				if (ClassUtil.classExtends(clazz, DccdSB.class))
				{
					hasDccdSBSubType = true;
					break;
				}
			}
			if (!hasDccdSBSubType)
				request.addFilterBean(DccdSB.class);

			SearchEngine searchEngine = getSearchEngine();
			return (SearchResult<? extends DccdSB>) searchEngine.searchBeans(request);
		}
		catch (SearchEngineException e)
		{
			throw new SearchServiceException(e);
		}

	}

	public SearchResult<DccdProjectSB> findProjectArchivedWithSameTridasIdentifier(TridasIdentifier tridasIdentifier) throws SearchServiceException
	{
		String identifierValueString = "";
		String domainString = "";
		boolean hasDomain = false;
		
		if (tridasIdentifier.isSetValue())
		{
			identifierValueString = tridasIdentifier.getValue();
		}
		
		// put quotes around it
		identifierValueString = "\""+identifierValueString+"\"";

		if (tridasIdentifier.isSetDomain()) 
		{
			hasDomain = true;
			domainString = tridasIdentifier.getDomain();
			// put quotes around it
			domainString = "\""+domainString+"\"";
		}

		SimpleSearchRequest searchRequest = new SimpleSearchRequest();
    	searchRequest.setOffset(0);
    	searchRequest.setLimit(1); // only one needed
    	searchRequest.setHighlightingEnabled(false);
    	
    	// a project, we don't need the objects they have the same project ID
    	searchRequest.addFilterBean(DccdProjectSB.class); 
		
    	// restrict results to archived/published 
		SimpleField<String> stateField = new SimpleField<String>(DccdSB.ADMINISTRATIVE_STATE_NAME, 
																 DatasetState.PUBLISHED.toString());
		searchRequest.addFilterQuery(stateField);    	
    	
		// search for the id (value)
		SimpleField<String> idField = new SimpleField<String>(DccdSB.TRIDAS_PROJECT_IDENTIFIER_EXACT_NAME, 
															  identifierValueString);
		searchRequest.addFilterQuery(idField);    	
		
		// AND when specified also the domain
		if (hasDomain)
		{
			SimpleField<String> idDomainField = new SimpleField<String>(DccdSB.TRIDAS_PROJECT_IDENTIFIER_DOMAIN_EXACT_NAME, 
																		domainString);
			searchRequest.addFilterQuery(idDomainField);    	
		}
		
		SearchResult<? extends DccdSB> result = doSearch(searchRequest);
		
		// LOGGING
		if (logger.isDebugEnabled()) 
		{
			logger.debug("number of hits: " + result.getHits().size());
			for (Object hitObj : result.getHits())
			{
				SimpleSearchHit hit = (SimpleSearchHit) hitObj;
				if (hit.getData() instanceof DccdProjectSB)
		        {
					DccdProjectSB dccdProjectHit = (DccdProjectSB)hit.getData();
					logger.debug( "status: " + dccdProjectHit.getAdministrativeState() +
					" Title: " + dccdProjectHit.getTridasProjectTitle() + 
					", id: " + dccdProjectHit.getTridasProjectIdentifier() +
					", domain: " + dccdProjectHit.getTridasProjectIdentifierDomain());
		        }
			}
		}

		return (SearchResult<DccdProjectSB>) result;
	}
	
	// TODO remove?
	public SearchResult<DccdProjectSB> findProjectArchivedWithSameTridasIdentifier(Project project) throws SearchServiceException
	{
		String identifierValueString = "";
		String domainString = "";
		boolean hasDomain = false;
		
		TridasProject tridasProject = project.getTridas();
		if (tridasProject != null && 
			tridasProject.isSetIdentifier())
		{
			identifierValueString = "";
			
			if (tridasProject.getIdentifier().isSetValue())
			{
				identifierValueString = tridasProject.getIdentifier().getValue();
			}
			
			// put quotes around it
			identifierValueString = "\""+identifierValueString+"\"";

			if (tridasProject.getIdentifier().isSetDomain()) 
			{
				hasDomain = true;
				domainString = tridasProject.getIdentifier().getDomain();
				// put quotes around it
				domainString = "\""+domainString+"\"";
			}
		}
		else
		{
			// note: this is not likely because the project should have been validated for that first
			logger.warn("Project without a usefull id"); 			
			return new EmptySearchResult<DccdProjectSB>(); 
		}

		SimpleSearchRequest searchRequest = new SimpleSearchRequest();
    	searchRequest.setOffset(0);
    	searchRequest.setLimit(1); // only one needed
    	searchRequest.setHighlightingEnabled(false);
    	
    	// a project, we don't need the objects they have the same project ID
    	searchRequest.addFilterBean(DccdProjectSB.class); 
		
    	// restrict results to archived/published 
		SimpleField<String> stateField = new SimpleField<String>(DccdSB.ADMINISTRATIVE_STATE_NAME, 
																 DatasetState.PUBLISHED.toString());
		searchRequest.addFilterQuery(stateField);    	
    	
		// search for the id (value)
		SimpleField<String> idField = new SimpleField<String>(DccdSB.TRIDAS_PROJECT_IDENTIFIER_EXACT_NAME, 
															  identifierValueString);
		searchRequest.addFilterQuery(idField);    	
		
		// AND when specified also the domain
		if (hasDomain)
		{
			SimpleField<String> idDomainField = new SimpleField<String>(DccdSB.TRIDAS_PROJECT_IDENTIFIER_DOMAIN_EXACT_NAME, 
																		domainString);
			searchRequest.addFilterQuery(idDomainField);    	
		}
		
		SearchResult<? extends DccdSB> result = doSearch(searchRequest);
		
		// LOGGING
		if (logger.isDebugEnabled()) 
		{
			logger.debug("number of hits: " + result.getHits().size());
			for (Object hitObj : result.getHits())
			{
				SimpleSearchHit hit = (SimpleSearchHit) hitObj;
				if (hit.getData() instanceof DccdProjectSB)
		        {
					DccdProjectSB dccdProjectHit = (DccdProjectSB)hit.getData();
					logger.debug( "status: " + dccdProjectHit.getAdministrativeState() +
					" Title: " + dccdProjectHit.getTridasProjectTitle() + 
					", id: " + dccdProjectHit.getTridasProjectIdentifier() +
					", domain: " + dccdProjectHit.getTridasProjectIdentifierDomain());
		        }
			}
		}

		return (SearchResult<DccdProjectSB>) result;
	}

	
	/* OLD STUFF BELOW */


	/** Update the search index,
	 * needs to be done when an indexed document has changed or added to the repository
	 * This implementation works with documents from Fedora (and index in Solr).
	 *
	 * @see #getFoxml()
	 * @see #transformFoxml()
	 * @see #updateSolr()
	 *
	 * @param sid
	 * @throws SearchServiceException
	 */
	public void updateSearchIndex(String sid) throws SearchServiceException {
		if (sid == null) throw new IllegalArgumentException();

		// get the data/document from the Repository
		String foxmlStr = getFoxml(sid);

		// transform for indexing
		Document transformedDoc = transformFoxml(foxmlStr);

		// feed to the indexing beast
		updateSolr(transformedDoc);
	}

	/**	 Get the foxml from the Fedora Repository
	 *
	 * @param sid Identifier for the fedora object
	 * @return The Fedora object xml as string (UTF-8)
	 * @throws SearchServiceException
	 */
	private  String getFoxml(String sid) throws SearchServiceException {
		if (sid == null) throw new IllegalArgumentException();

		Properties settings = DccdConfigurationService.getService().getSettings();
		final String protocol = settings.getProperty("fedora.protocol");
		final String host     = settings.getProperty("fedora.host");
		final int 	port      = Integer.parseInt(settings.getProperty("fedora.port"));
		final String user     = settings.getProperty("fedora.user");
		final String password = settings.getProperty("fedora.password");
		final String context  = settings.getProperty("fedora.context");

		logger.info("Geting foxml from fedora");

		String foxmlStr = "";
		FedoraRepositoryService repo;
		try {
			repo = new FedoraRepositoryService(protocol, host, port, user, password, context);
			foxmlStr = repo.getObjectXML(sid);
		} catch (DccdRepositoryException e) {
			throw new SearchServiceException(e);
		}

		return foxmlStr;
	}

	/** Transform the given foxml to a Solr indexing document
	 *
	 * @param foxml The xml of the fodora object (indexed)
	 * @return The xml document (for updating the Solr index)
	 * @throws SearchServiceException
	 */
	private  Document transformFoxml(String foxml) throws SearchServiceException {
		if (foxml == null) throw new IllegalArgumentException();

		// get the xslt to transform with, specific for Solr
		final String DCCD_TO_SOLR_XSLT_FILENAME = "dccdToSolr.xslt";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL xsltUrl = loader.getResource(DCCD_TO_SOLR_XSLT_FILENAME);

		logger.info("Transform foxml to indexing document using " + DCCD_TO_SOLR_XSLT_FILENAME);
		// transform foxml with xslt
		Document document = null;
	    Transformer transformer = null;
	    Document transformedDoc = null;
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			document = DocumentHelper.parseText( foxml );
			//transformer = transformerFactory.newTransformer( new StreamSource(xsltStr ) );
			transformer = transformerFactory.newTransformer(new StreamSource(xsltUrl.getFile() ) );
		    DocumentSource source = new DocumentSource( document );
		    DocumentResult result = new DocumentResult();
		    transformer.transform( source, result );
		    transformedDoc = result.getDocument();
		} catch (TransformerConfigurationException e) {
			// ? should not happen ?
			throw new SearchServiceException(e);
		} catch (DocumentException e) {
			throw new SearchServiceException(e);
		} catch (TransformerException e) {
			throw new SearchServiceException(e);
		}

		// print transformedDoc
		/*
		try {
	    	OutputFormat format = OutputFormat.createPrettyPrint();
			System.out.println("xml:\n");
			XMLWriter writer = new XMLWriter( System.out, format );
	        writer.write( transformedDoc );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		return transformedDoc;
	}

	/** update the Solr index
	 *
	 * note: Candidate member of a SolrSearchService class
	 *
	 * @param doc The xml document that Solr needs for updating the index
	 */
	private void updateSolr(Document doc) throws SearchServiceException {
		Properties settings = DccdConfigurationService.getService().getSettings();
		// use Solr
		final String protocol = settings.getProperty("solr.protocol");
		final String host     = settings.getProperty("solr.host");
		final int 	port     = Integer.parseInt(settings.getProperty("solr.port"));
		final String context  = settings.getProperty("solr.context.update");
		String urlString = protocol + "://" + host + ":" + port + "/" + context;

		logger.info("Solr update url: " + urlString);

	  	// a post request with 'Content-type:text/xml; charset=utf-8'
		URLConnection conn = null;
	  	try {
			URL url = new URL(urlString);
			conn = url.openConnection();
			// protocol is http... but just in case
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection)conn).setRequestMethod("POST");
				((HttpURLConnection)conn).setDoOutput(true);
				((HttpURLConnection)conn).setDoInput(true);
				((HttpURLConnection)conn).setUseCaches(false);
				((HttpURLConnection)conn).setAllowUserInteraction(false);
				((HttpURLConnection)conn).setRequestProperty("Content-type", "text/xml; charset=UTF-8");
			} else {
				// warn, this is unusual
				// but maybe everything works?
				logger.warn("Connection to Solr is not using http protocol");
			}
			Writer writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			writer.write( doc.asXML() );
			writer.close();// will also flush
		} catch (MalformedURLException e) {
			throw new SearchServiceException(e);
		} catch (IOException e) {
			throw new SearchServiceException(e);
		}

		// do we want the response?
		// or should we just have a look at the errorStream?
		if (conn != null) {
			Scanner in = null;
			StringBuilder response = new StringBuilder();
			try {
				in = new Scanner(conn.getInputStream());
			} catch (IOException e) {
				if(!(conn instanceof HttpURLConnection))
					throw new SearchServiceException(e);
				InputStream err = ((HttpURLConnection)conn).getErrorStream();
				if (err == null)
					throw new SearchServiceException(e); // no extra info :-(
				in = new Scanner(err);
			}
			// either the input, or the error or we don't get here
			while(in.hasNextLine()) {
				response.append(in.nextLine() + "\n");
			}
			in.close();
			// show it in the logs
			//logger.info(response.toString());
		}
	}

	/**
	 * Search the repository using the Solr indexer
	 * Use offset and limit for paging the results
	 *
	 * Note: FedoraRepositoryService.getSearchResultAsXMLString()
	 * searches Fedora and does not using Solr!
	 *
	 * @param query The query to search for (Solr)
	 * @param offset Zero base index ofsset
	 * @param limit maximum number of resulting items to return
	 * @return
	 * @throws SearchServiceException
	 */
	public List<String> simpleSearch(String query, int offset, int limit) throws SearchServiceException {
     	// max results should be bigger than 0
       	if (limit < 1) throw new IllegalArgumentException("limit must be 1 or bigger");
       	if (offset < 0) throw new IllegalArgumentException("offset must be 0 or bigger");

		List<String> result = new ArrayList< String >(); // empty list

		Properties settings = DccdConfigurationService.getService().getSettings();
		final String protocol = settings.getProperty("solr.protocol");
		final String host     = settings.getProperty("solr.host");
		final int 	port     = Integer.parseInt(settings.getProperty("solr.port"));
		final String context  = settings.getProperty("solr.context");

		// use Solr to find project with the given query
		// the REST interface?
		//final int limit = 100;

		//String protocol = "http";
		//String host = "localhost";//"dendro01.dans.knaw.nl";
		//int port = 8082;//80;
		//String context = "solr-example/select";
	  	String baseURLString = protocol + "://" + host + ":" + port + "/" + context;

	   	String responseString = "";

		URL solrSearch;
		//BufferedReader in = null;
		Scanner in = null;
		StringBuilder response = new StringBuilder();
		try {
			// Note: the query string should be url encoded?
			String requestUrlString = baseURLString +
				"/?q=" + query + "&version=2.2&start=" + offset + "&rows=" + limit + "&indent=on";

			logger.info("Solr search request: " + requestUrlString);
			solrSearch = new URL(requestUrlString);
	        URLConnection fs = solrSearch.openConnection();
	        in = new Scanner(fs.getInputStream());
	        while (in.hasNextLine()) {
		        response.append(in.nextLine());
		        response.append("\n");
	        }
		} catch (MalformedURLException e) {
			// this really should not happen, the url is coded
			throw new RuntimeException(e);
		} catch (IOException e) {
			//throw e;
			throw new SearchServiceException(e);
		}
		if (in != null) in.close();

		responseString = response.toString();
		// show response
		//logger.info("Response: \n" + response.toString());

		// parse the xml string
		// make this into separate function
		Document domDoc;
		try {
			domDoc = DocumentHelper.parseText(responseString);
			Element xmlResponse = domDoc.getRootElement();
			// get result element
			Element xmlResult = xmlResponse.element("result");
			// for all doc elements
			for ( Iterator<?> i = xmlResult.elementIterator("doc"); i.hasNext(); ) {
	            Element docElement = (Element) i.next();
	            // get str element with attribute name="PID"
	            //Node node = docElement.selectSingleNode( "str[@name='PID']" );
	            // tridas objects instead of projects;
	            // using another ID and not the fedora object PID
	            Node node = docElement.selectSingleNode( "str[@name='ID']" );
	            logger.info("Found: " + node.getText());
	            result.add(node.getText());
	        }
		} catch (DocumentException e) {
			//throw e;
			throw new SearchServiceException(e);
		}

		return result;
	}

	/**
	 * Directly try to get results from Solr
	 *
	 * @param query
	 * @return
	 * @throws SearchServiceException
	 */
	public List<String> simpleSearch(String query) throws SearchServiceException {
		final int offset = 0;
		final int limit = 100;

		return simpleSearch(query, offset, limit);
	}

	public DccdSearchResult search(String query, int offset, int limit) throws SearchServiceException {
		DccdSearchResult result = new DccdSearchResult();

     	// max results should be bigger than 0
       	if (limit < 1) throw new IllegalArgumentException("limit must be 1 or bigger");
       	if (offset < 0) throw new IllegalArgumentException("offset must be 0 or bigger");

		List<String> resultIds = new ArrayList< String >(); // empty list

		Properties settings = DccdConfigurationService.getService().getSettings();
		final String protocol = settings.getProperty("solr.protocol");
		final String host     = settings.getProperty("solr.host");
		final int 	port     = Integer.parseInt(settings.getProperty("solr.port"));
		final String context  = settings.getProperty("solr.context");

		// use Solr to find project with the given query
		// the REST interface?
		//final int limit = 100;

		//String protocol = "http";
		//String host = "localhost";//"dendro01.dans.knaw.nl";
		//int port = 8082;//80;
		//String context = "solr-example/select";
	  	String baseURLString = protocol + "://" + host + ":" + port + "/" + context;

	   	String responseString = "";

		URL solrSearch;
		//BufferedReader in = null;
		Scanner in = null;
		StringBuilder response = new StringBuilder();
		URLConnection fs=null;
		try {
			// Note: the query string should be url encoded?
			String requestUrlString = baseURLString +
				"/?q=" + query + "&version=2.2&start=" + offset + "&rows=" + limit + "&indent=on" +
				"&debugQuery=true"; // debugging on TODO: remove in production!

			//"/?q=" + query + "&q.op=AND&version=2.2&start=" + offset + "&rows=" + limit + "&indent=on";

			// TODO:
			// What can we expect; HTTP response code: 400?
			// - why does this give an IO exception on openConnection
			logger.info("Solr search request: " + requestUrlString);
			solrSearch = new URL(requestUrlString);
	        fs = solrSearch.openConnection();
	        in = new Scanner(fs.getInputStream());
	        while (in.hasNextLine()) {
		        response.append(in.nextLine());
		        response.append("\n");
	        }
		} catch (MalformedURLException e) {
			// this really should not happen, the url is coded
			logger.debug("Malformed URL Exception while requesting Solr search");
			throw new RuntimeException(e);
		} catch (IOException e) {
			// Try to get an idea of what happened here...
			// HTTP response (errors can give indication about what went wrong)

			// get the error stream...
			if((fs instanceof HttpURLConnection))	{
				try {
					String responseMsg = ((HttpURLConnection)fs).getResponseMessage();
					response.append(responseMsg);
					// set this as the result
					//result.setResponseString(response.toString());
				} catch (IOException e1) {
					// ignore
					logger.debug("Unable to get response message about IO exception");
					//e1.printStackTrace();
				}

				/* html page with the response
				InputStream err = ((HttpURLConnection)fs).getErrorStream();
				if (err != null) {
					in = new Scanner(err);
			        while (in.hasNextLine()) {
				        response.append(in.nextLine());
				        response.append("\n");
			        }
				}
				*/
			}
			logger.debug("IO exception while reading Solr response: " + response);


			throw new SearchServiceException(response.toString(), e);
		}
		if (in != null) in.close();

		responseString = response.toString();
		// show response
		//logger.info("Response: \n" + responseString);

		// keep response for testing/debugging purposes
		result.setResponseString(responseString);

		// parse the xml string
		// note: maybe make this into separate function
		int numFound = 0;
		Document domDoc;
		try {
			domDoc = DocumentHelper.parseText(responseString);
			Element xmlResponse = domDoc.getRootElement();
			// get result element
			Element xmlResult = xmlResponse.element("result");

			//numFound
			//<result name="response" numFound="3" start="0">
			numFound = Integer.parseInt(xmlResult.attribute("numFound").getText(), 10);
			logger.info("Total hits: " + numFound);

			// for all doc elements
			for ( Iterator<?> i = xmlResult.elementIterator("doc"); i.hasNext(); ) {
	            Element docElement = (Element) i.next();
	            // get str element with attribute name="PID"
	            //Node node = docElement.selectSingleNode( "str[@name='PID']" );
	            // tridas objects instead of projects;
	            // using another ID and not the fedora object PID
	            Node node = docElement.selectSingleNode( "str[@name='ID']" );
	            if (node == null) {
	            	logger.warn("Found doc element without <str name=\"ID\" > subelement");
	            } else {
	            	logger.info("Found: " + node.getText());
	            	resultIds.add(node.getText());
	            }
	        }
		} catch (DocumentException e) {
			//throw e;
			logger.debug("Document Exception while parsing xml response from Solr: " +
					responseString);
			throw new SearchServiceException(e);
		}

		// update results
		result.setResultItemIds(resultIds);
		result.setNumFound(numFound);

		return result;
	}



}
