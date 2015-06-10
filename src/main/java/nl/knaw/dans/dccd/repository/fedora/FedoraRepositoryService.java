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
package nl.knaw.dans.dccd.repository.fedora;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.knaw.dans.common.fedora.DatastreamAccessor;
import nl.knaw.dans.common.fedora.ObjectManager;
import nl.knaw.dans.common.fedora.Repository;
import nl.knaw.dans.common.fedora.RepositoryAccessor;
import nl.knaw.dans.common.fedora.RepositoryInfo;
import nl.knaw.dans.common.fedora.fox.ControlGroup;
import nl.knaw.dans.common.fedora.fox.Datastream;
import nl.knaw.dans.common.fedora.fox.DatastreamVersion;
import nl.knaw.dans.common.fedora.fox.DigitalObject;
import nl.knaw.dans.common.fedora.fox.DobState;
import nl.knaw.dans.common.fedora.fox.XMLContent;
import nl.knaw.dans.common.jibx.bean.JiBXDublinCoreMetadata;
import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.xml.XMLDeserializationException;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.common.lang.repo.bean.DublinCoreMetadata;
import nl.knaw.dans.dccd.model.EntityTree;
import nl.knaw.dans.dccd.model.EntityTreeBuilder;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.repository.DccdRepositoryException;
import nl.knaw.dans.dccd.repository.DccdRepositoryService;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.tridas.schema.TridasProject;

import fedora.server.types.gen.MIMETypedStream;

/** 
 * TODO remove this class when the project list view is replaced by search browsing view
 * 
 * Use the Fedora repository to store dendro data
 *
 * mainly using the FedoraStore to accomplice this
 *
 * @author paulboon
 *
 */
public class FedoraRepositoryService implements DccdRepositoryService 
{ 
	private static Logger logger = Logger.getLogger(FedoraRepositoryService.class);
    //private FedoraStore store = null;
	final static String DO_ID_NS = "dccd"; // used for the Fedora digital object PID
	final static String FORMAT_FOXML_1_1 = "info:fedora/fedora-system:FOXML-1.1"; // foxml format

	// reuse the JAXB context, improves performance
	// especially for retrieveEntity() which can be called for each entity in a project
	private JAXBContext jaxbContextForTridas = null;

	// Note maybe reuse these objects; construct once use many!
	private ObjectManager objectManager = null;
	private DatastreamAccessor datastreamAccessor = null;
	private Repository repository = null;

	private String baseURLString = "";

	private JAXBContext getJaxbContextForTridas() throws JAXBException {
		if (jaxbContextForTridas == null) {
			jaxbContextForTridas = JAXBContext.newInstance("org.tridas.schema");
		}
		return jaxbContextForTridas;
	}

	/** Initialize by connecting to the store
	 *
	 * @param protocol
	 * @param host
	 * @param port
	 * @param user
	 * @param pass
	 * @param context
	 * @throws Exception
	 */
    public FedoraRepositoryService(String protocol, String host, int port, String user, String pass, String context)
            throws DccdRepositoryException {

    	baseURLString = protocol + "://" + host + ":" + port + "/" + context;

		logger.info("initializing store at: " + baseURLString);
//		try {
			// Note: could keep refernces, and don't recreate every time?
			// Fedora.getObjectManager() does lazy construction...
			repository = new Repository(baseURLString, user, pass);
			objectManager = new ObjectManager(repository);
			datastreamAccessor =  new DatastreamAccessor(repository);
//		} catch (RepositoryException e) {
//			throw new DccdRepositoryException(e);
//		}
    }

    /**
     *  low-level function, just to get the foxml of a given object
     *  Need this to convert with a stylesheet and then update the solr indexer!
     *  When using the schema approach the update is the same as when done from a external script
     *  But we could generate the indexer input (xml) from the Project data ...
     *
     * @return
     */
    public String getObjectXML(String Sid) throws DccdRepositoryException {
    	byte[] foxmlBytes = null;
    	String xmlStr = "";
    	try {
    		foxmlBytes = repository.getFedoraAPIM().getObjectXML(Sid);
    		xmlStr = new String(foxmlBytes, "UTF-8");// utf-8
		} catch (RemoteException e) {
			throw new DccdRepositoryException(e);
		} catch (RepositoryException e) {
			throw new DccdRepositoryException(e);
		} catch (UnsupportedEncodingException e) {
			// this really should not happen, the encoding must be UTF-8
			throw new RuntimeException(e);
		}

		return xmlStr;
    }

	/** Retrieve all projects but without the data in repository,
	 * only the id's and title's set to get the data when needed
	 *
	 * @return The dendro projects from the repository
	 */
    public Collection< Project > getDendroProjects() throws DccdRepositoryException{
    	final int maxResults = 100; // was 10
    	Collection< Project > result = null;

    	try {
    		result = getDendroProjects(maxResults);
    	} catch (DccdRepositoryException e) {
    		throw e;
    	}
    	return result;
    }

    /** same as getDendroProjects,
     * but now with given max number of projects to retrieve
	 *
 	 * Now the projects are without any real data, only a title and id.
	 * Later on you can use the id to retrieve the data for each project
	 *
     * @param limit
     * @return
     */
    public Collection< Project > getDendroProjects(int limit) throws DccdRepositoryException {
       	// max results should be bigger than 0
       	if (limit < 1) throw new IllegalArgumentException("limit must be 1 or bigger");

       	// First get the id's and titles for our dccd projects/data
    	String xmlResultString = "";
    	Collection<Project> dendroProjects = null;
		try {
	    	// get the xml result
			xmlResultString = getSearchResultAsXMLString(limit);
	    	// create project list
			dendroProjects = createProjectListFromXMLResultString(xmlResultString);
		} catch (IOException e) {
			throw new DccdRepositoryException(e);
		} catch (DocumentException e) {
			throw new DccdRepositoryException(e);
		}
		return dendroProjects;
	}

    /**
	 * Use the Fedora http request to get a xml with the data PID's
	 * With those we can retrieve the data
    *
    * @param limit
    * @return The xml string with the search result from Fedora
    * @throws IOException If communication with the server fails
    */
   private String getSearchResultAsXMLString(int limit) throws IOException
   {
	   	// max results should be bigger than 0
	   	if (limit < 1) throw new IllegalArgumentException("limit must be 1 or bigger");

	   	String result = "";

		URL fedoraSearch;
		//BufferedReader in = null;
		Scanner in = null;
		StringBuilder response = new StringBuilder();
		try {
			//String requestUrlString = "http://dendro01.dans.knaw.nl/fedora/search?query=pid~dccd:*&maxResults=50&xml=true&pid=true&title=true";
			//String requestUrlString = "http://localhost:8082/fedora/search?query=pid~dccd:*&maxResults=50&xml=true&pid=true&title=true";
			String requestUrlString = baseURLString + "/search?query=pid~dccd:*&maxResults="+limit+"&xml=true&pid=true&title=true";

			logger.info("request: " + requestUrlString);
			fedoraSearch = new URL(requestUrlString);
	        URLConnection fs = fedoraSearch.openConnection();
	        in = new Scanner(fs.getInputStream());
	        while (in.hasNextLine()) {
		        response.append(in.nextLine());
		        response.append("\n");
	        }
		} catch (MalformedURLException e) {
			// this really should not happen, the url is coded
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw e;
		}
		if (in != null) in.close();

		// show response
		//logger.info("Response: \n" + response.toString());
		result = response.toString();

   		return result;
   }

   /** Creates the list with projects specified by the given xml string
    * The project only have the id and title set and no further data
    * Create empty list if nothing found
    *
    * @param xmlResultString
    * @return
    * @throws DocumentException If the input string is incorrect parsing fails
    */
   @SuppressWarnings("unchecked")
private Collection< Project > createProjectListFromXMLResultString(String xmlResultString)
   	throws DocumentException
   {
      	if (xmlResultString == null || xmlResultString.length() == 0) throw new IllegalArgumentException("string must be specified");

		// should be xml from fedora, parse it with dom4j
   		// and fill the list with projects
		Collection< Project > dendroProjects = new ArrayList< Project >();
		Document domDoc;
		try {
			domDoc = DocumentHelper.parseText(xmlResultString);//response.toString());
			Element xmlResult = domDoc.getRootElement();
			Element xmlResultList = xmlResult.element("resultList");

			// logger.info("root: " + xmlResultList.asXML());

			// get all objectFields
			for ( Iterator i = xmlResultList.elementIterator( "objectFields" ); i.hasNext(); ) {
	            Element objectFields = (Element) i.next();
	            // get id
	            String id = objectFields.elementText("pid");
	            // get titel
	            String title = objectFields.elementText("title");
	            logger.info("Found Id: " + id + " Title: " + title);
	            Project dendroProject = new Project( id, title);
	            dendroProjects.add(dendroProject);
	        }
		} catch (DocumentException e) {
			throw e;
		}

		return dendroProjects;
   }

	/** Get the entityTree for the project, other datastreams are ignored
	 *
	 * note: Seems not much more efficient than retrieving the complete project
	 * because it retrieves and unmarshall's the complete foxml
	 * although it doesn't convert all the tridas datastreams with JAXB
	 *
	 * @param project The project for which the entityTree is retrieved
	 */
	public void retrieveEntityTree(Project project) throws DccdRepositoryException {
      	if (project == null) throw new IllegalArgumentException("project must be specified");
      	if (project.getSid() == null || project.getSid().length() == 0) throw new IllegalArgumentException("project must have an id specified");

		String id = project.getSid();

		// get the tree stream
		MIMETypedStream stream = null;
		try {
			stream = datastreamAccessor.getDatastreamDissemination(id, EntityTree.ENTITYTREE_ID, null);
		} catch (RepositoryException e) {
			throw new DccdRepositoryException(e);
		}

		String xmlStr = "";
		try {
			xmlStr = new String(stream.getStream(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new DccdRepositoryException(e);
		}

		Document documentDom = null;
		try {
			documentDom = DocumentHelper.parseText( xmlStr );
		} catch (DocumentException e) {
			logger.error("dom4j exception");
			throw new DccdRepositoryException(e);
		}
    	Element treeElement = documentDom.getRootElement();

		// show tree
		System.out.println("\n--- Begin tree struct ---");
		System.out.print(treeElement.asXML());
		System.out.println("\n--- End tree struct ---");

		// create the entity tree
		// use the tree from the project
		EntityTree entityTree = project.entityTree;

		//entityTree.buildTree(treeElement);
		entityTree.setProjectEntity(EntityTreeBuilder.buildTree(treeElement));
	}

	/** Get the entity (tridas data from the datastream)
	 * from the given Fedora Object id,
	 * using the datastream id from the given entity
	 *
	 * @param fo_id
	 * @param entity
	 */
	public void retrieveEntity(String fo_id, Entity entity) throws DccdRepositoryException {
      	if (fo_id == null || fo_id.length() == 0) throw new IllegalArgumentException("object id must be specified");
     	if (entity == null) throw new IllegalArgumentException("entity must be specified");
     	if (entity.getId().length() == 0 ) throw new IllegalArgumentException("entity id must be specified");

		logger.info("Fragment id: " + entity.getId());
		// get the stream
		MIMETypedStream stream = null;
		try {
			stream = datastreamAccessor.getDatastreamDissemination(fo_id, entity.getId(), null);
		} catch (RepositoryException e) {
			throw new DccdRepositoryException(e);
		}
		//note should be xml, but we will find out if it's ok

		ByteArrayInputStream input = new ByteArrayInputStream (stream.getStream());
		// Need UTF-8 because the JAXB want's it for unmarshalling the tridas
		// note: can I just give a StingInputStream?

		// use JAXB to get the Tridas object
		JAXBContext jaxbContext = null;
		Object tridas = null;
		try {
			jaxbContext = getJaxbContextForTridas();//JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			tridas = unmarshaller.unmarshal(input);
			entity.setTridasObject(tridas);
		} catch (JAXBException e) {
			throw new DccdRepositoryException(e);
		}

	}

	/** Get the complete project data from the repository
	 * places the retrieved data into the given project
	 *
	 * @param project The project to be retrieved
	 */
   public void retrieve(Project project) throws DccdRepositoryException {
    	if (project == null) throw new IllegalArgumentException("project must be specified");
     	if (project.getSid() == null || project.getSid().length() == 0 ) throw new IllegalArgumentException("project id must be specified");
    	String id = project.getSid();

		// get the foxml (fedora object xml) and
		// create the digital object
		DigitalObject dob = null;
		try {
			dob = objectManager.getDigitalObject(id);
		} catch (RepositoryException e) {
			throw new DccdRepositoryException(e);
		}

		// get the DC
		try {
			DublinCoreMetadata dcmd = dob.getLatestDublinCoreMetadata();

			// retrieve the language setting
			List<String> tridasLanguageList = dcmd.getLanguage();
			// only adjust if it was set, otherwise keep the default
			if (tridasLanguageList.size() > 0) {
				// Note: DC.Language allows for a list of languages in the content,
				// we take the first one as the primary language!
				project.setTridasLanguage(new Locale(tridasLanguageList.get(0)));
			}
		} catch (XMLDeserializationException e) {
			logger.info("Could not deserialize XML for the Dublin Core");
			throw new DccdRepositoryException(e);
		}

    	// get the tree stream
		Datastream treeDatastream = dob.getDatastream(EntityTree.ENTITYTREE_ID);
		DatastreamVersion treeDatastreamVersion = treeDatastream.getLatestVersion();
		XMLContent xmlContent = treeDatastreamVersion.getXmlContent();
		Element treeElement = null;
		treeElement = xmlContent.getElement();
		// show tree
		System.out.println("\n--- Begin tree struct ---");
		System.out.print(treeElement.asXML());
		System.out.println("\n--- End tree struct ---");

		// Create the tree of entities, use the tree from the project
		EntityTree entityTree = project.entityTree;
		//entityTree.buildTree(treeElement);
		//ProjectEntity projectEntity = entityTree.getProjectEntity();
		ProjectEntity projectEntity = EntityTreeBuilder.buildTree(treeElement);
		entityTree.setProjectEntity(projectEntity);

    	// get all entity streams
		List<Entity> entities =  entityTree.getEntities();
    	for (Entity entity : entities) {
    		logger.debug("Fragment id: " + entity.getId());
    		// get xml from stream
    		Datastream entityDatastream = dob.getDatastream(entity.getId());
    		DatastreamVersion entityDatastreamVersion = entityDatastream.getLatestVersion();
    		//byte[] xmlData = entityDatastreamVersion.getBinaryContent();
    		XMLContent xmlEntityContent = entityDatastreamVersion.getXmlContent();
    		String xmlStr = xmlEntityContent.getElement().asXML();

    		// use JAXB to get the Tridas object
    		JAXBContext jaxbContext = null;
    		Object tridas = null;
    		try {
    			jaxbContext = getJaxbContextForTridas();//JAXBContext.newInstance("org.tridas.schema");
    			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

    			// This gives UTF-16 probably!
    			//ByteArrayInputStream input = new ByteArrayInputStream (xmlStr.getBytes());

    			ByteArrayInputStream input = new ByteArrayInputStream (xmlStr.getBytes("UTF-8"));
    			// Need UTF-8 because the JAXB want's it for unmarshalling the tridas
    			// note: can I just give a StingInputStream?

    			tridas = unmarshaller.unmarshal(input);
    			entity.setTridasObject(tridas);
    		} catch (JAXBException e) {
    			throw new DccdRepositoryException(e);
    		} catch (UnsupportedEncodingException e) {
    			throw new DccdRepositoryException(e);
			}
     	}

    	// connect all (internal) tridas objects
    	projectEntity.connectTridasObjectTree();
    	project.setTridas((TridasProject)projectEntity.getTridasAsObject());

    	//TODO: LB20090923: if the tridas has been set, the entityTree might
    	// also be build. One can do this lazily on the getter of the DendroProject
    	// for the EntityTree

    	//logger.info("id: "+ project.id + " entity id: "+projectEntity.getId());
    	//project.id = projectEntity.getId(); //??? need the fedora PID here???
    	project.setTitle(projectEntity.getTitle());
    }

    /** Ingest given project data; store it
     *
     * @param project
     * @throws RepositoryException
     */
    public void ingest (Project project) throws DccdRepositoryException {//throws RepositoryException {
    	if (project == null) throw new IllegalArgumentException("project must be specified");
    	if (!project.hasTridas()) throw new IllegalArgumentException("project must have tridas data");

		// create the entity tree, now use the tree from the project
    	// note: recreate whole tree no matter what is already there!
		EntityTree entityTree = project.entityTree;
		entityTree.buildTree(project.getTridas());

		// create a new Fedora digital object for this dendro project and
        // have Fedora generate an unique Id for us
		DigitalObject dob = new DigitalObject(DobState.Active, DO_ID_NS);

		// properties
		dob.setLabel(entityTree.getProjectEntity().getTitle()); // use title from Project
		dob.setOwnerId("testDepositorId"); // This should be a real id someday!

		// Add to the Fedora DC, no need to make a separate datastream
		DublinCoreMetadata dcmd = new JiBXDublinCoreMetadata();
		dcmd.addLanguage(project.getTridasLanguage().getLanguage());
		dcmd.addTitle(entityTree.getProjectEntity().getTitle()); // now we must do it ourselves
		try {
			dob.addDatastreamVersion(dcmd);
		} catch (XMLSerializationException e) {
			logger.info("Could not serialize XML for the Dublin Core");
			throw new DccdRepositoryException(e);
		}

		/* could have a separate datastream
		Datastream dcDatastream = new Datastream("DCM", ControlGroup.X); // there is only one!
		dob.putDatastream(dcDatastream); // actually add the stream to the dataobject!
		dcDatastream.setState(Datastream.State.A);
		DatastreamVersion dcVersion = dcDatastream.addDatastreamVersion(dcDatastream.nextVersionId(), "text/xml");
		dcVersion.setLabel("dublincoremetadata");
		try {
			dcVersion.setXmlContent(dcmd.asElement());
		} catch (XMLSerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    */

		// Add the entitytree structure datastream
		Datastream datastream = new Datastream(EntityTree.ENTITYTREE_ID, ControlGroup.X); // there is only one!
		dob.putDatastream(datastream); // actually add the stream to the dataobject!
		datastream.setState(Datastream.State.A);
		DatastreamVersion version = datastream.addDatastreamVersion(datastream.nextVersionId(), "text/xml");
		version.setLabel("entitytreestruct");
		version.setXmlContent(entityTree.getTreeStructAsDocument().getRootElement());

		// get all fragments convert them to datastreams
		// and add them to the digital object
		List<Entity> entities = entityTree.getEntities();
		for (Entity entity : entities) {
			String streamId = entity.getId();
// TESTING Stress testing:
// repeat this X times, change the Id as well, otherwise Fedoro won't take it
//for(int i=0; i<200; i++) {
//logger.info("i = "+ i);
//streamId+="-"+Integer.toString(i);
			datastream = new Datastream(streamId, ControlGroup.X);
			dob.putDatastream(datastream); // actually add the stream to the dataobject!
			datastream.setState(Datastream.State.A);
			version = datastream.addDatastreamVersion(datastream.nextVersionId(), "text/xml");
			version.setLabel(entity.getLabel());//("entity"); // Note: use title?
			//set the uri for the xml content; our TRiDaS fragments
			//URI streamFormatURI;
			//try {
			//	streamFormatURI = new URI("http://www.tridas.org/1.2");
			//	version.setFormatURI(streamFormatURI);
			//} catch (URISyntaxException e) {
			//	e.printStackTrace();
			//}

			// add entity to the datastreamversion
			try {
				String xmlString = entity.getXMLString();
				// Note: maybe there is to much conversion going on here,
				// at the end there must be an xml string?
				Document domDoc = DocumentHelper.parseText(xmlString);
				Element xmlContent = domDoc.getRootElement();

// only for the Project, add a xml:lang attribute
//if (entity instanceof ProjectEntity) {
//	xmlContent.addAttribute("xml:lang", project.getTridasLanguage().getLanguage());
//}
				//logger.info("dom4j XML: \n"+ xmlContent.asXML());
				version.setXmlContent(xmlContent);
			} catch (DocumentException e) {
				logger.info("Could not parse XML");
				throw new DccdRepositoryException(e);
			}
		} // end, for all entities
//}// end TESTING

		// Store digital object, with all streams in it
        String logMessage = new String("");
		try {
			objectManager.ingest(dob, logMessage);
//			project.setSid(dob.getSid());
			project.setStoreId(dob.getSid());
			logger.info("ingested project with sid: "+ dob.getSid());
		} catch (RepositoryException e) {
			throw new DccdRepositoryException(e);
		}

    }

    /**
     * Only gives log info' but should become more usefull by returning a sting?
     * possibly use for testing
     *
     * @throws RemoteException
     */
	public void describeRepository()
		throws RepositoryException {

		RepositoryInfo repoinfo = null; //nl.knaw.dans.common.store.
		RepositoryAccessor accessor;
		try {
			accessor = new RepositoryAccessor(repository);
			repoinfo = accessor.describeRepository();
			logger.info("SOAP Request: describeRepository...");
			logger.info("SOAP Response: repository version = " + repoinfo.getRepositoryVersion());
			logger.info("SOAP Response: repository name = " + repoinfo.getRepositoryName());
			logger.info("SOAP Response: repository pid namespace = " + repoinfo.getRepositoryPIDNamespace());
			logger.info("SOAP Response: repository default export = " + repoinfo.getDefaultExportFormat());
			logger.info("SOAP Response: repository base URL = " + repoinfo.getRepositoryBaseURL());
			logger.info("SOAP Response: repository OAI namespace = " + repoinfo.getOAINamespace());
			logger.info("SOAP Response: repository sample OAI identifier = " + repoinfo.getSampleOAIIdentifier());
			logger.info("SOAP Response: repository sample OAI URL = " + repoinfo.getSampleOAIURL());
			logger.info("SOAP Response: repository sample access URL = " + repoinfo.getSampleAccessURL());
			logger.info("SOAP Response: repository sample search URL = " + repoinfo.getSampleSearchURL());
			logger.info("SOAP Response: repository sample PID = " + repoinfo.getSamplePID());
		} catch (RepositoryException e) {
			//e.printStackTrace();
			throw e;
		}
	}

}
