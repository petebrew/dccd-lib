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
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import nl.knaw.dans.common.fedora.Fedora;
import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.ResourceLocator;
import nl.knaw.dans.common.lang.dataset.DatasetState;
import nl.knaw.dans.common.lang.mail.Attachement;
import nl.knaw.dans.common.lang.mail.CommonMailer;
import nl.knaw.dans.common.lang.mail.MailComposer;
import nl.knaw.dans.common.lang.mail.MailComposerException;
import nl.knaw.dans.common.lang.mail.Mailer.MailerException;
import nl.knaw.dans.common.lang.repo.AbstractDmoFactory;
import nl.knaw.dans.common.lang.repo.DmoStoreId;
import nl.knaw.dans.common.lang.repo.exception.DmoStoreEventListenerException;
import nl.knaw.dans.common.lang.repo.exception.ObjectExistsException;
import nl.knaw.dans.common.lang.repo.exception.ObjectNotInStoreException;
import nl.knaw.dans.common.lang.util.StreamUtil;
import nl.knaw.dans.dccd.mail.DccdMailer;
import nl.knaw.dans.dccd.mail.DccdMailerConfigurationException;
import nl.knaw.dans.dccd.model.DccdOrganisation;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.ProjectCreationMetadata;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.repository.DccdRepositoryException;
import nl.knaw.dans.dccd.repository.DccdRepositoryService;
import nl.knaw.dans.dccd.repository.fedora.DccdFedoraStore;
import nl.knaw.dans.dccd.repository.fedora.FedoraRepositoryService;

import org.apache.log4j.Logger;

//TODO: LB20090923: Get rid of all this static usage and start using the
// Singleton pattern. This may assist in using Spring lateron, which
// will make it easy to have different configuration for getting
// the repositoryService that is used by this service

/**
 * Data services, for the dccd (tridas based) data
 *
 * @author paulboon
 *
 */
public class DccdDataService implements DataService {
	private static Logger logger = Logger.getLogger(DccdDataService.class);

	static final String UPDATE_LOG_MSG_LABEL = "update: ";
	static final String INGEST_LOG_MSG_LABEL = "ingest: ";

/* ==============Start of NEW impl============== */
	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String PROTOCOL = settings.getProperty("fedora.protocol");// "http";
	static final String HOST     = settings.getProperty("fedora.host");//"localhost";//"dendro01.dans.knaw.nl";
	static final int 	PORT     = Integer.parseInt(settings.getProperty("fedora.port"));//8082;//80;
	static final String CONTEXT  = settings.getProperty("fedora.context");//"fedora";
	static final String BASE_URL = PROTOCOL + "://"  + HOST + ":" + PORT + "/" + CONTEXT;
	static final String FEDORA_USER = settings.getProperty("fedora.user");//"fedoraAdmin";
	static final String PASSWORD = settings.getProperty("fedora.password");//"fedoraAdmin";
	static final String STORE_NAME = "dccd";
	
	final String MIME_TYPE_PDF = "application/pdf";

	private static final String ARCHIVING_CONFIRMATION_MAIL_TEXT = "ArchivingConfirmationMail.txt";
	private static final String ARCHIVING_CONFIRMATION_SUBJECT = "DCCD: archive confirmation";
	
	private DccdFedoraStore store = null;

	public DccdFedoraStore getStore()
	{
		if (store == null)
		{
			Fedora fedora = new Fedora(BASE_URL, FEDORA_USER, PASSWORD);
			store = new DccdFedoraStore(
					STORE_NAME,
					fedora,
					((DccdSearchService) DccdSearchService.getService()).getSearchEngine(),
					((DccdSearchService) DccdSearchService.getService()).getEasySearchEngine()
				);
		}
		return store;
	}

	public void updateProject(Project project) throws DataServiceException
	{
		try
		{
			getStore().update(project, UPDATE_LOG_MSG_LABEL + project.getStoreId());
		}
		catch (ObjectExistsException e)
		{
			throw new DataServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
	}

	public void storeProject(Project project) throws DataServiceException
	{
		try
		{
			getStore().ingest(project, INGEST_LOG_MSG_LABEL + project.getStoreId());
		}
		catch (ObjectExistsException e)
		{
			throw new DataServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
	}

	public Project getProject(String projectId) throws DataServiceException
	{
		try
		{
//			return (Project) getStore().retrieve(projectId);
			return (Project) getStore().retrieve(new DmoStoreId(projectId));
			// Note: could have all callers use a DmoStoreId instead of the String
		}
		catch (ObjectNotInStoreException e)
		{
			throw new DataServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
	}

	public Project createProject() throws DataServiceException
	{
		logger.warn("No creation metadata generated");

		try
		{
//			return (Project) getStore().createDmo(Project.class);
			getStore(); // initialize
			return (Project) AbstractDmoFactory.newDmo(Project.NAMESPACE);
		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
	}

	public Project createProject(String userId) throws DataServiceException
	{
		Project project = null;

		try
		{
//			project = (Project) getStore().createDmo(Project.class);
			getStore(); // initialize
			project = (Project) AbstractDmoFactory.newDmo(Project.NAMESPACE);

			// get the User and the Organisation
			DccdUserService dccdUserService = DccdUserService.getService();
			DccdUser user = dccdUserService.getUserById(userId);
			DccdOrganisation organisation = dccdUserService.getOrganisationById(user.getOrganization());

			// initially the creator is the owner, according to the Fedora object property
			// The DCCD manager and legalOwners are kept in the Administrative metadata
			project.setOwnerId(user.getId());

			ProjectCreationMetadata creationMetadata = new ProjectCreationMetadata();
			creationMetadata.setOrganisation(organisation);
			creationMetadata.setUser(user);
			project.setCreationMetadata(creationMetadata);

			// Administrative metadata
			project.getAdministrativeMetadata().setManagerId(user.getId());
			project.getAdministrativeMetadata().setLegalOwnerOrganisationId(organisation.getId());

		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
		catch (UserServiceException e)
		{
			throw new DataServiceException(e);
		}

		return project;
	}

	// Note: unarchiving is available until versioning is implemented! 
	public void unarchiveProject(Project project, DccdUser user) throws DataServiceException
	{
		// check if user is allowed
		if (project.isManagementAllowed(user))
		{			
			// change the status and update the project
			project.getAdministrativeMetadata().setAdministrativeState(DatasetState.DRAFT);
			updateProject(project);
			
			// NOTE no mail is being sent
			logger.debug("unarchived project; is set to DRAFT status in the repository");
		}
		else
		{
			logger.debug("Rejected request to unarchive project " + project.getSid() + 
					" by user " + user.getId());
		}
	}
	
	// Note: not using a archiving service just for sending the mail
	public void archiveProject(Project project, DccdUser user) throws DataServiceException
	{
		// TODO check if user is allowed, note that license cannot be checked here
		//project.isManagementAllowed(user);
		
		// change the status and update the project
		project.getAdministrativeMetadata().setAdministrativeState(DatasetState.PUBLISHED);
		updateProject(project);
		
		// Send the email to the user
	    MailComposer composer = new MailComposer(user, project);
        InputStream inStream = this.getClass().getResourceAsStream(ARCHIVING_CONFIRMATION_MAIL_TEXT);

        String subject = ARCHIVING_CONFIRMATION_SUBJECT + " for " + project.getTitle();
        
        try
        {
            String messagePlaintext = composer.compose(inStream);

            CommonMailer mailer = (CommonMailer) DccdMailer.getDefaultInstance();
            
            // attach license file
            String licenseFileLocation = "Licence_en.pdf";
			try
			{
				URL url = ResourceLocator.getURL(licenseFileLocation);
				byte[] bytes = StreamUtil.getBytes(url.openStream());
				Attachement att = new Attachement("Licence_en.pdf", MIME_TYPE_PDF, bytes);
	            Attachement[] attachments = { att };
	            mailer.sendSimpleMail(subject, messagePlaintext, attachments, user.getEmail());
			}
			catch (IOException e)
			{
				logger.error("Could not attach license text from: [" + licenseFileLocation + "] Sending mail without it!");
				// just sent the mail without an attachment
				mailer.sendSimpleMail(subject, messagePlaintext, user.getEmail());
			}

        }
		catch (MailComposerException e)
		{
			logger.error("Could not compose a mail message to " + user.getEmail() + ": ", e);
			// Hint: check the message template file
		}
		catch (DccdMailerConfigurationException e)
		{
			logger.error("Could not configure mailer to send a registration mail to " + user.getEmail() + ": ", e);
		}
		catch (MailerException e)
		{
			logger.error("Could not send a registration mail to " + user.getEmail() + ": ", e);
		}	
	}

	
	// Delete project (only draft is removed from the repository for now)
	// Note:
	// instead of setting the status to deleted and update the project in the repo
	// with project.getAdministrativeMetadata().setAdministrativeState(DatasetState.DELETED);
	// and then calling updateProject(project);
	public void deleteProject(Project project, DccdUser user) throws DataServiceException
	{
		// check action is allowed
		if (!project.isManagementAllowed(user) || 
			!project.isDeletable())
		{
			throw new DataServiceException("delete was not allowed");
		}
		

		String logMessage = "Deleting project: " + project.getSid() + " by user: " + user.getId();
		logger.debug(logMessage);
		project.registerDeleted();
		
		boolean force = false; // it's not supported, but for a draft we could use it in the future
		try
		{
			getStore().purge(project, force, logMessage);
		}
		catch (DmoStoreEventListenerException e)
		{
			throw new DataServiceException(e);
		}
		catch (RepositoryException e)
		{
			throw new DataServiceException(e);
		}
	}
		
    public URL getFileURL(String sid, String unitId) 
    {
    	return getStore().getFileURL(sid, unitId);
    }
	
/*============== End of new impl ==============*/

    // TODO remove old code
    
    
	private static Collection< Project > dendroProjects;
	private static DccdRepositoryService repository = null;
	// singleton pattern with lazy construction
	private static DccdDataService service = null;
	public static DccdDataService getService() {
		if (service == null) {
			service = new DccdDataService();
		}
		return service;
	}
	// fedora repo settings, note: object candidate
	// Note: would like to have special (local) strings when testing/debugging
	//
	// These are now for a local=development configuration!!!!
	/*
 	static final String PROTOCOL = "http";
	static final String HOST     = "localhost";//"dendro01.dans.knaw.nl";
	static final int 	PORT     = 8082;//80;
	static final String USER     = "fedoraAdmin";
	static final String PASSWORD = "fedoraAdmin";
	static final String CONTEXT  = "fedora";
	 */
//	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String protocol = settings.getProperty("fedora.protocol");// "http";
	static final String host     = settings.getProperty("fedora.host");//"localhost";//"dendro01.dans.knaw.nl";
	static final int 	port     = Integer.parseInt(settings.getProperty("fedora.port"));//8082;//80;
	static final String user     = settings.getProperty("fedora.user");//"fedoraAdmin";
	static final String password = settings.getProperty("fedora.password");//"fedoraAdmin";
	static final String context  = settings.getProperty("fedora.context");//"fedora";

	// provide for reuse of the repository object, make once, use many times
	private DccdRepositoryService getRepository() throws DataServiceException {
		if (repository == null) {
	    	// use the Fedora repo
	    	try {
	    		//repository = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
	    		repository = new FedoraRepositoryService(protocol, host, port, user, password, context);
			} catch (DccdRepositoryException e) {
				//logger.error("Could not get projects list");
				//e.printStackTrace();
				throw new DataServiceException(e);
			}

		}
		return repository;
	}

	/** Only loads the first time, keeps the projects and returns those in later requests.
	 * This means no synchronization with the repository, but it is useful for demonstration!
	 * @throws DataServiceException
	 */
	 public Collection< Project > getProjects() throws DataServiceException {
		if (dendroProjects != null ) {
			logger.info("Returning perviously loaded projects");
			return dendroProjects; // already loaded data!
		}

		//RepositoryService repo = new XMLFilesRepositoryService();
		//DccdRepositoryService repo = null;

    	// use the Fedora repo
    	try {
    		//repo = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
    		//dendroProjects = repo.getDendroProjects();
    		dendroProjects = getRepository().getDendroProjects();

		} catch (DccdRepositoryException e) {
			//logger.error("Could not get projects list");
			//e.printStackTrace();
			throw new DataServiceException(e);
		}

		return dendroProjects;
	}

	 /**
	  * force a reload on next call to getDendroProjects
	  * need to call when the files have changes as a result of an upload
	  * Note: this is only used because the projects are cached,
	  * if this is removed in later versions, there is no need to reset
	  */
	static public void reset() {
		dendroProjects = null;
	}

	/**
	 * Store in fedora repo
	 */
//	public void storeProject(Project project) throws DataServiceException
//	{
//		//DccdRepositoryService repo = null;
//
//	   	// use the Fedora repo
//	   	try {
//	   		//repo = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
//			//repo.ingest(project); // !!! exceptions
//			getRepository().ingest(project);
//		} catch (DccdRepositoryException e) {
//			//logger.error("Could not store project");
//			//e.printStackTrace();
//			throw new DataServiceException(e);
//		}
//
//		//TODO: always reset, or make reset unneeded!
//	}

	/** retrieve from the fedora repo
	 *
	 * @param project
	 * @throws DataServiceException
	 */
//	public void getProject(Project project) throws DataServiceException
//	{
//		//DccdRepositoryService repo = null;
//
//    	// use the Fedora repo
//    	try {
//    		//repo = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
//    		//repo.retrieve(project); // !!! exceptions
//    		getRepository().retrieve(project);
//		} catch (DccdRepositoryException e) {
//			//logger.error("Could not retrieve project");
//			//e.printStackTrace();
//			throw new DataServiceException(e);
//		}
//	}

	/** retrieve the tree from the fedora repo
	 */
	public void getProjectEntityTree(Project project) throws DataServiceException
	{
		//DccdRepositoryService repo = null;

	   	// use the Fedora repo
	   	try {
	   		//repo = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
			//repo.retrieveEntityTree(project); // !!! exceptions
			getRepository().retrieveEntityTree(project);
		} catch (DccdRepositoryException e) {
			//logger.error("Could not retrieve entity tree");
			//e.printStackTrace();
			throw new DataServiceException(e);
		}
	}

	/**
	 *  retrieve an entity from the fedora repo
	 */
    public void retrieveEntity(String id, Entity entity) throws DataServiceException {
		//DccdRepositoryService repo = null;

		// use the Fedora repo
    	try {
    		//repo = new FedoraRepositoryService(PROTOCOL, HOST, PORT, USER, PASSWORD, CONTEXT);
    		//repo.retrieveEntity(id, entity); // !!! exceptions
    		getRepository().retrieveEntity(id, entity);
		} catch (DccdRepositoryException e) {
			//logger.error("Could not retrieve entity");
			//e.printStackTrace();
			throw new DataServiceException(e);
		}
    }

}
