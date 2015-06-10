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
package nl.knaw.dans.dccd.search;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import nl.knaw.dans.common.lang.search.bean.annotation.SearchBean;
import nl.knaw.dans.common.lang.search.bean.annotation.SearchField;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.model.DccdUser.Role;

import org.joda.time.DateTime;

@SearchBean(defaultIndex=DccdIndex.class, typeIdentifier="dccd")
public class DccdSB implements Serializable
{
	private static final long serialVersionUID = 8404125027815160677L;

	static final String ID_SEPERATOR = "/";
	/** 
	 * Compose and decompose ID
	 * We need to construct the 'id' for the search index;
	 * with which we can retrieve the object (inside the project)
	 * from the repository
	 * Also when we get a search result we need to 'interpret'
	 * the id and get the information from it from retrieving from the Repository
	 */
	public void composeId()
	{
		// if there is nothing to compose, don't overwrite anything
		if (getPid() != null && getPid().length() != 0 )
		{
			if (getDatastreamId() != null && getDatastreamId().length() != 0)
			{
				setId(getPid() + ID_SEPERATOR + getDatastreamId());
			}
			else
			{
				// no datastreamid
				setId(getPid() + ID_SEPERATOR);
			}
		}
	}

	public void decomposeId()
	{
		// if there is nothing to decompose, don't overwrite anything
		if (getId() != null && getId().length() != 0) {
			// Note: the Pid should not contain an ID_SEPERATOR
			int sep_index = id.indexOf(ID_SEPERATOR);//'/');
			String sid = id.substring(0, sep_index);
			setPid(sid);
			String dsid = "";// fragment (datastream) id
			dsid = id.substring(sep_index + ID_SEPERATOR.length());
			setDatastreamId(dsid);
			//logger.info("Search result ID: " + id + ", extracted sid: " + sid + ", dsid: " + dsid);
		}
	}

	/**
	 * From http://www.javapractices.com/topic/TopicAction.do?Id=55
	 *
	 * Intended only for debugging.
	 *
	 * <P>Here, a generic implementation uses reflection to print
	 * names and values of all fields <em>declared in this class</em>. Note that
	 * superclass fields are left out of this implementation.
	 *
	 * <p>The format of the presentation could be standardized by using
	 * a MessageFormat object with a standard pattern.
	 */
	@Override public String toString()
	{
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append( this.getClass().getName() );
		result.append( " Object {" );
		result.append(newLine);

		//determine fields declared in this class only (no fields of superclass)
		Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for ( Field field : fields  )
		{
			// filter... specific for this class
			// remove the  final statics (constants)
			//if (field.getName().endsWith("_NAME")) continue;
			if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) continue;

			result.append("  ");
			try
			{
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append( field.get(this) );
			}
			catch ( IllegalAccessException ex )
			{
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}

	/** shorthand for determining if both Lat and Lng are available
	 */
	public boolean hasLatLng() { return (hasLat() && hasLng()); }
	// Allow specifying the marker index after searching, useful when shown on a map
	public int latLngMarkerIndex = -1;
	
	// Helper functions to determine permission to view information
	// Note that this code is similar to that in the Project class, 
	// making a good starting point for refactoring	
	public boolean isManagementAllowed(DccdUser user)
	{
		if (user != null && 
				(user.hasRole(Role.ADMIN) || // admin may see everything
					user.getId().equals(getOwnerId())) // also the manager
		)
			return true;
		else
			return false;
	}
	
	// effective level is the permissionlevel, also taken into account
	// that admin and the owner has all rights
	public ProjectPermissionLevel getEffectivePermissionLevel(DccdUser user)
	{
		// at least minimal permissions
		ProjectPermissionLevel level = ProjectPermissionLevel.minimum();
	
		if (this.isManagementAllowed(user))
		{
			level = ProjectPermissionLevel.maximum(); // maximum: even download
		}
		else
		{
			// Note: we cannot get the users specific level from the SearchBean yet, 
			// but we will use the default level
			level = ProjectPermissionLevel.valueOf(getPermissionDefaultLevel());
		}
	
		return level;
	}
	
	// template
	//public final static String X_Y_NAME = "x.y";
	//@SearchField(name=X_Y_NAME)
	//private String xY;

	/* For retrieving from repository */

	// <field name="ID" type="string" indexed="true" stored="true" required="true" />
	public final static String ID_NAME = "ID";
	@SearchField(name=ID_NAME)
	private String id;

	// <field name="PID" type="string" indexed="true" stored="true" required="true" />
	public final static String PID_NAME = "PID";
	@SearchField(name=PID_NAME)
	private String pid;

	// <field name="DATASTREAMID" type="string" indexed="true" stored="true" required="true" />
	public final static String DATASTREAM_ID_NAME = "DATASTREAMID";
	@SearchField(name=DATASTREAM_ID_NAME)
	private String datastreamId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPid() {
		return pid;
	}

	public boolean hasPid() {
		return (pid != null && pid.length() > 0);
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getDatastreamId() {
		return datastreamId;
	}

	public boolean hasDatastreamId() {
		return (datastreamId != null && datastreamId.length() > 0);
	}

	public void setDatastreamId(String datastreamId) {
		this.datastreamId = datastreamId;
	}

	// TODO Trac and ProjectAdministrativeMetadata now use the term manager instead of owner, 
	// so managerId would be more appropriate
	//
	// <field name="ownerId" type="string" indexed="true" stored="true" required="true" />
	public final static String OWNER_ID_NAME = "ownerId";
	@SearchField(name=OWNER_ID_NAME)
	private String ownerId;

	public String getOwnerId()
	{
		return ownerId;
	}

	public boolean hasOwnerId() {
		return (ownerId != null && ownerId.length() > 0);
	}

	public void setOwnerId(String ownerId)
	{
		this.ownerId = ownerId;
	}

	// <field name="administrativeState" type="string" indexed="true" stored="true" required="true" />
	public final static String ADMINISTRATIVE_STATE_NAME = "administrativeState";
	@SearchField(name=ADMINISTRATIVE_STATE_NAME, required=true)
	private String administrativeState; // Note: maybe make it a DatasetState instead of a String
	public String getAdministrativeState()
	{
		return administrativeState;
	}

	public boolean hasAdministrativeState() {
		return (administrativeState != null && administrativeState.length() > 0);
	}

	public void setAdministrativeState(String administrativeState)
	{
		this.administrativeState = administrativeState;
	}

	// <field name="administrativeStateLastChange" type="date_utc" indexed="true" stored="true" required="true" />
	public final static String ADMINISTRATIVE_STATE_LASTCHANGE = "administrativeStateLastChange";
	@SearchField(name=ADMINISTRATIVE_STATE_LASTCHANGE, required=true)
	private DateTime administrativeStateLastChange; 
	public DateTime getAdministrativeStateLastChange()
	{
		return administrativeStateLastChange;
	}

	public boolean hasAdministrativeStateLastChange() {
		return (administrativeStateLastChange != null);
	}

	public void setAdministrativeStateLastChange(DateTime administrativeStateLastChange)
	{
		this.administrativeStateLastChange = administrativeStateLastChange;
	}

	// project permission metadata 
	//
	// <field name="permissionDefaultLevel" type="string" indexed="true" stored="true" required="false" />
	public final static String PERMISSION_DEFAULTLEVEL_NAME = "permissionDefaultLevel";
	@SearchField(name=PERMISSION_DEFAULTLEVEL_NAME, required=false)
	private String permissionDefaultLevel; // Note: maybe make it a XXX instead of a String
	public String getPermissionDefaultLevel()
	{
		return permissionDefaultLevel;
	}

	public boolean hasPermissionDefaultLevel() {
		return (permissionDefaultLevel != null && permissionDefaultLevel.length() > 0);
	}

	public void setPermissionDefaultLevel(String permissionDefaultLevel)
	{
		this.permissionDefaultLevel = permissionDefaultLevel;
	}

	
// TODO	add extra fields for finding exact matches!
// <field name="tridas.project.identifier.domain.exact" type="string" indexed="true" stored="true" multiValued="false" />
	public final static String TRIDAS_PROJECT_IDENTIFIER_DOMAIN_EXACT_NAME = "tridas.project.identifier.domain.exact";
	@SearchField(name=TRIDAS_PROJECT_IDENTIFIER_DOMAIN_EXACT_NAME)
	private String tridasProjectIdentifierDomainExact;

	public String getTridasProjectIdentifierDomainExact() {
	    return tridasProjectIdentifierDomainExact;
	}

	public void setTridasProjectIdentifierDomainExact(String tridasProjectIdentifierDomainExact) {
		this.tridasProjectIdentifierDomainExact = tridasProjectIdentifierDomainExact;
	}

	public boolean hasTridasProjectIdentifierDomainExact() {
		return (tridasProjectIdentifierDomainExact != null && tridasProjectIdentifierDomainExact.length() > 0);
	}
// <field name="tridas.project.identifier.exact" type="string" indexed="true" stored="true" multiValued="false" />	
	public final static String TRIDAS_PROJECT_IDENTIFIER_EXACT_NAME = "tridas.project.identifier.exact";
	@SearchField(name=TRIDAS_PROJECT_IDENTIFIER_EXACT_NAME)
	private String tridasProjectIdentifierExact;

	public String getTridasProjectIdentifierExact() {
	    return tridasProjectIdentifierExact;
	}

	public void setTridasProjectIdentifierExact(String tridasProjectIdentifierExact) {
		this.tridasProjectIdentifierExact = tridasProjectIdentifierExact;
	}

	public boolean hasTridasProjectIdentifierExact() {
		return (tridasProjectIdentifierExact != null && tridasProjectIdentifierExact.length() > 0);
	}

/*
	// <field name="tridas.object.title" type="text" indexed="true" stored="true" multiValued="true" />
	public final static String TRIDAS_OBJECT_TITLE_NAME = "tridas.object.title";
	// Could use collapser, then we have an index with multiValued="false"
	//@SearchField(name=TRIDAS_OBJECT_TITLE_NAME, converter=StringListCollapserConverter.class)
	@SearchField(name=TRIDAS_OBJECT_TITLE_NAME)
	private List<String> tridasObjectTitle;
*/

	/* TRiDaS */

	/* Start - generated code */
	// tridas.project.identifier.domain
	// tridas.project.laboratory.combined
	// tridas.project.title
	// tridas.project.identifier
	// tridas.project.comments
	// tridas.project.type
	// tridas.project.type.normal
	// tridas.project.description
	// tridas.project.laboratory.name
	// tridas.project.laboratory.name.acronym
	// tridas.project.laboratory.address.cityOrTown
	// tridas.project.laboratory.address.country
	// tridas.project.category
	// tridas.project.category.normal
	// tridas.project.category.normalTridas
	// tridas.project.investigator
	// tridas.project.period
	// tridas.project.research.identifier
	// tridas.project.research.identifier.domain
	// tridas.project.research.description
	// tridas.object.title
	// tridas.object.identifier
	// tridas.object.comments
	// tridas.object.type
	// tridas.object.type.normal
	// tridas.object.description
	// tridas.object.creator
	// tridas.object.coverage.coverageTemporalFoundation
	// tridas.object.location.locationType
	// tridas.object.location.locationComment
	// tridas.object.genericField
	// tridas.element.title
	// tridas.element.identifier
	// tridas.element.type
	// tridas.element.type.normal
	// tridas.element.description
	// tridas.element.taxon
	// tridas.element.shape
	// tridas.element.altitude
	// tridas.element.slope.angle
	// tridas.element.slope.azimuth
	// tridas.element.soil.description
	// tridas.element.soil.depth
	// tridas.element.bedrock.description
	// tridas.sample.title
	// tridas.sample.identifier
	// tridas.sample.type
	// tridas.sample.type.normal
	// tridas.sample.samplingDate
	// tridas.radius.title
	// tridas.radius.identifier
	// tridas.radius.woodCompleteness.pith
	// tridas.radius.woodCompleteness.heartwood
	// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPith
	// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPithFoundation
	// tridas.radius.woodCompleteness.sapwood
	// tridas.radius.woodCompleteness.sapwood.nrOfSapwoodRings
	// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBark
	// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBarkFoundation
	// tridas.radius.woodCompleteness.sapwood.lastRingUnderBark
	// tridas.radius.woodCompleteness.bark
	// tridas.measurementSeries.title
	// tridas.measurementSeries.identifier
	// tridas.measurementSeries.analyst
	// tridas.measurementSeries.dendrochronologist
	// tridas.measurementSeries.measuringDate
	// tridas.measurementSeries.measuringMethod
	// tridas.measurementSeries.measuringMethod.normal
	// tridas.measurementSeries.interpretation.provenance
	// tridas.measurementSeries.interpretation.deathYear
	// tridas.measurementSeries.interpretation.firstYear
	// tridas.measurementSeries.interpretation.pithYear
	// tridas.measurementSeries.interpretation.statFoundation.statValue
	// tridas.measurementSeries.interpretation.statFoundation.usedSoftware
	// tridas.measurementSeries.interpretation.statFoundation.type
	// tridas.measurementSeries.interpretation.statFoundation.type.normal
	// tridas.measurementSeries.interpretation.statFoundation.significanceLevel
	// tridas.measurementSeries.interpretationUnsolved

	// tridas.project.identifier.domain
	public final static String TRIDAS_PROJECT_IDENTIFIER_DOMAIN_NAME = "tridas.project.identifier.domain";
	@SearchField(name=TRIDAS_PROJECT_IDENTIFIER_DOMAIN_NAME)
	private String tridasProjectIdentifierDomain;

	public String getTridasProjectIdentifierDomain() {
	    return tridasProjectIdentifierDomain;
	}

	public void setTridasProjectIdentifierDomain(String tridasProjectIdentifierDomain) {
		this.tridasProjectIdentifierDomain = tridasProjectIdentifierDomain;
	}

	public boolean hasTridasProjectIdentifierDomain() {
		return (tridasProjectIdentifierDomain != null && tridasProjectIdentifierDomain.length() > 0);
	}

	// tridas.project.laboratory.combined
	public final static String TRIDAS_PROJECT_LABORATORY_COMBINED_NAME = "tridas.project.laboratory.combined";
	@SearchField(name=TRIDAS_PROJECT_LABORATORY_COMBINED_NAME)
	private List<String> tridasProjectLaboratoryCombined;

	public List<String> getTridasProjectLaboratoryCombined() {
	    return tridasProjectLaboratoryCombined;
	}

	public void setTridasProjectLaboratoryCombined(List<String> tridasProjectLaboratoryCombined) {
		this.tridasProjectLaboratoryCombined = tridasProjectLaboratoryCombined;
	}

	public boolean hasTridasProjectLaboratoryCombined() {
		return (tridasProjectLaboratoryCombined != null && tridasProjectLaboratoryCombined.size() > 0);
	}

	// tridas.project.title
	public final static String TRIDAS_PROJECT_TITLE_NAME = "tridas.project.title";
	@SearchField(name=TRIDAS_PROJECT_TITLE_NAME)
	private String tridasProjectTitle;

	public String getTridasProjectTitle() {
	    return tridasProjectTitle;
	}

	public void setTridasProjectTitle(String tridasProjectTitle) {
		this.tridasProjectTitle = tridasProjectTitle;
	}

	public boolean hasTridasProjectTitle() {
		return (tridasProjectTitle != null && tridasProjectTitle.length() > 0);
	}

	// tridas.project.identifier
	public final static String TRIDAS_PROJECT_IDENTIFIER_NAME = "tridas.project.identifier";
	@SearchField(name=TRIDAS_PROJECT_IDENTIFIER_NAME)
	private String tridasProjectIdentifier;

	public String getTridasProjectIdentifier() {
	    return tridasProjectIdentifier;
	}

	public void setTridasProjectIdentifier(String tridasProjectIdentifier) {
		this.tridasProjectIdentifier = tridasProjectIdentifier;
	}

	public boolean hasTridasProjectIdentifier() {
		return (tridasProjectIdentifier != null && tridasProjectIdentifier.length() > 0);
	}

	// tridas.project.comments
	public final static String TRIDAS_PROJECT_COMMENTS_NAME = "tridas.project.comments";
	@SearchField(name=TRIDAS_PROJECT_COMMENTS_NAME)
	private String tridasProjectComments;

	public String getTridasProjectComments() {
	    return tridasProjectComments;
	}

	public void setTridasProjectComments(String tridasProjectComments) {
		this.tridasProjectComments = tridasProjectComments;
	}

	public boolean hasTridasProjectComments() {
		return (tridasProjectComments != null && tridasProjectComments.length() > 0);
	}

	// tridas.project.type
	public final static String TRIDAS_PROJECT_TYPE_NAME = "tridas.project.type";
	@SearchField(name=TRIDAS_PROJECT_TYPE_NAME)
	private List<String> tridasProjectType;

	public List<String> getTridasProjectType() {
	    return tridasProjectType;
	}

	public void setTridasProjectType(List<String> tridasProjectType) {
		this.tridasProjectType = tridasProjectType;
	}

	public boolean hasTridasProjectType() {
		return (tridasProjectType != null && tridasProjectType.size() > 0);
	}

	// tridas.project.type.normal
	public final static String TRIDAS_PROJECT_TYPE_NORMAL_NAME = "tridas.project.type.normal";
	@SearchField(name=TRIDAS_PROJECT_TYPE_NORMAL_NAME)
	private List<String> tridasProjectTypeNormal;

	public List<String> getTridasProjectTypeNormal() {
	    return tridasProjectTypeNormal;
	}

	public void setTridasProjectTypeNormal(List<String> tridasProjectTypeNormal) {
		this.tridasProjectTypeNormal = tridasProjectTypeNormal;
	}

	public boolean hasTridasProjectTypeNormal() {
		return (tridasProjectTypeNormal != null && tridasProjectTypeNormal.size() > 0);
	}

	// tridas.project.description
	public final static String TRIDAS_PROJECT_DESCRIPTION_NAME = "tridas.project.description";
	@SearchField(name=TRIDAS_PROJECT_DESCRIPTION_NAME)
	private String tridasProjectDescription;

	public String getTridasProjectDescription() {
	    return tridasProjectDescription;
	}

	public void setTridasProjectDescription(String tridasProjectDescription) {
		this.tridasProjectDescription = tridasProjectDescription;
	}

	public boolean hasTridasProjectDescription() {
		return (tridasProjectDescription != null && tridasProjectDescription.length() > 0);
	}

	// tridas.project.laboratory.name
	public final static String TRIDAS_PROJECT_LABORATORY_NAME_NAME = "tridas.project.laboratory.name";
	@SearchField(name=TRIDAS_PROJECT_LABORATORY_NAME_NAME)
	private List<String> tridasProjectLaboratoryName;

	public List<String> getTridasProjectLaboratoryName() {
	    return tridasProjectLaboratoryName;
	}

	public void setTridasProjectLaboratoryName(List<String> tridasProjectLaboratoryName) {
		this.tridasProjectLaboratoryName = tridasProjectLaboratoryName;
	}

	public boolean hasTridasProjectLaboratoryName() {
		return (tridasProjectLaboratoryName != null && tridasProjectLaboratoryName.size() > 0);
	}

	// tridas.project.laboratory.name.acronym
	public final static String TRIDAS_PROJECT_LABORATORY_NAME_ACRONYM_NAME = "tridas.project.laboratory.name.acronym";
	@SearchField(name=TRIDAS_PROJECT_LABORATORY_NAME_ACRONYM_NAME)
	private List<String> tridasProjectLaboratoryNameAcronym;

	public List<String> getTridasProjectLaboratoryNameAcronym() {
	    return tridasProjectLaboratoryNameAcronym;
	}

	public void setTridasProjectLaboratoryNameAcronym(List<String> tridasProjectLaboratoryNameAcronym) {
		this.tridasProjectLaboratoryNameAcronym = tridasProjectLaboratoryNameAcronym;
	}

	public boolean hasTridasProjectLaboratoryNameAcronym() {
		return (tridasProjectLaboratoryNameAcronym != null && tridasProjectLaboratoryNameAcronym.size() > 0);
	}

	// tridas.project.laboratory.address.cityOrTown
	public final static String TRIDAS_PROJECT_LABORATORY_ADDRESS_CITYORTOWN_NAME = "tridas.project.laboratory.address.cityOrTown";
	@SearchField(name=TRIDAS_PROJECT_LABORATORY_ADDRESS_CITYORTOWN_NAME)
	private List<String> tridasProjectLaboratoryAddressCityortown;

	public List<String> getTridasProjectLaboratoryAddressCityortown() {
	    return tridasProjectLaboratoryAddressCityortown;
	}

	public void setTridasProjectLaboratoryAddressCityortown(List<String> tridasProjectLaboratoryAddressCityortown) {
		this.tridasProjectLaboratoryAddressCityortown = tridasProjectLaboratoryAddressCityortown;
	}

	public boolean hasTridasProjectLaboratoryAddressCityortown() {
		return (tridasProjectLaboratoryAddressCityortown != null && tridasProjectLaboratoryAddressCityortown.size() > 0);
	}

	// tridas.project.laboratory.address.country
	public final static String TRIDAS_PROJECT_LABORATORY_ADDRESS_COUNTRY_NAME = "tridas.project.laboratory.address.country";
	@SearchField(name=TRIDAS_PROJECT_LABORATORY_ADDRESS_COUNTRY_NAME)
	private List<String> tridasProjectLaboratoryAddressCountry;

	public List<String> getTridasProjectLaboratoryAddressCountry() {
	    return tridasProjectLaboratoryAddressCountry;
	}

	public void setTridasProjectLaboratoryAddressCountry(List<String> tridasProjectLaboratoryAddressCountry) {
		this.tridasProjectLaboratoryAddressCountry = tridasProjectLaboratoryAddressCountry;
	}

	public boolean hasTridasProjectLaboratoryAddressCountry() {
		return (tridasProjectLaboratoryAddressCountry != null && tridasProjectLaboratoryAddressCountry.size() > 0);
	}

	// tridas.project.category
	public final static String TRIDAS_PROJECT_CATEGORY_NAME = "tridas.project.category";
	@SearchField(name=TRIDAS_PROJECT_CATEGORY_NAME)
	private String tridasProjectCategory;

	public String getTridasProjectCategory() {
	    return tridasProjectCategory;
	}

	public void setTridasProjectCategory(String tridasProjectCategory) {
		this.tridasProjectCategory = tridasProjectCategory;
	}

	public boolean hasTridasProjectCategory() {
		return (tridasProjectCategory != null && tridasProjectCategory.length() > 0);
	}

	// tridas.project.category.normal
	public final static String TRIDAS_PROJECT_CATEGORY_NORMAL_NAME = "tridas.project.category.normal";
	@SearchField(name=TRIDAS_PROJECT_CATEGORY_NORMAL_NAME)
	private String tridasProjectCategoryNormal;

	public String getTridasProjectCategoryNormal() {
	    return tridasProjectCategoryNormal;
	}

	public void setTridasProjectCategoryNormal(String tridasProjectCategoryNormal) {
		this.tridasProjectCategoryNormal = tridasProjectCategoryNormal;
	}

	public boolean hasTridasProjectCategoryNormal() {
		return (tridasProjectCategoryNormal != null && tridasProjectCategoryNormal.length() > 0);
	}

	// tridas.project.category.normalTridas
	public final static String TRIDAS_PROJECT_CATEGORY_NORMALTRIDAS_NAME = "tridas.project.category.normalTridas";
	@SearchField(name=TRIDAS_PROJECT_CATEGORY_NORMALTRIDAS_NAME)
	private String tridasProjectCategoryNormaltridas;

	public String getTridasProjectCategoryNormaltridas() {
	    return tridasProjectCategoryNormaltridas;
	}

	public void setTridasProjectCategoryNormaltridas(String tridasProjectCategoryNormaltridas) {
		this.tridasProjectCategoryNormaltridas = tridasProjectCategoryNormaltridas;
	}

	public boolean hasTridasProjectCategoryNormaltridas() {
		return (tridasProjectCategoryNormaltridas != null && tridasProjectCategoryNormaltridas.length() > 0);
	}

	// tridas.project.investigator
	public final static String TRIDAS_PROJECT_INVESTIGATOR_NAME = "tridas.project.investigator";
	@SearchField(name=TRIDAS_PROJECT_INVESTIGATOR_NAME)
	private String tridasProjectInvestigator;

	public String getTridasProjectInvestigator() {
	    return tridasProjectInvestigator;
	}

	public void setTridasProjectInvestigator(String tridasProjectInvestigator) {
		this.tridasProjectInvestigator = tridasProjectInvestigator;
	}

	public boolean hasTridasProjectInvestigator() {
		return (tridasProjectInvestigator != null && tridasProjectInvestigator.length() > 0);
	}

	// tridas.project.period
	public final static String TRIDAS_PROJECT_PERIOD_NAME = "tridas.project.period";
	@SearchField(name=TRIDAS_PROJECT_PERIOD_NAME)
	private String tridasProjectPeriod;

	public String getTridasProjectPeriod() {
	    return tridasProjectPeriod;
	}

	public void setTridasProjectPeriod(String tridasProjectPeriod) {
		this.tridasProjectPeriod = tridasProjectPeriod;
	}

	public boolean hasTridasProjectPeriod() {
		return (tridasProjectPeriod != null && tridasProjectPeriod.length() > 0);
	}

	// tridas.project.research.identifier
	public final static String TRIDAS_PROJECT_RESEARCH_IDENTIFIER_NAME = "tridas.project.research.identifier";
	@SearchField(name=TRIDAS_PROJECT_RESEARCH_IDENTIFIER_NAME)
	private List<String> tridasProjectResearchIdentifier;

	public List<String> getTridasProjectResearchIdentifier() {
	    return tridasProjectResearchIdentifier;
	}

	public void setTridasProjectResearchIdentifier(List<String> tridasProjectResearchIdentifier) {
		this.tridasProjectResearchIdentifier = tridasProjectResearchIdentifier;
	}

	public boolean hasTridasProjectResearchIdentifier() {
		return (tridasProjectResearchIdentifier != null && tridasProjectResearchIdentifier.size() > 0);
	}

	// tridas.project.research.identifier.domain
	public final static String TRIDAS_PROJECT_RESEARCH_IDENTIFIER_DOMAIN_NAME = "tridas.project.research.identifier.domain";
	@SearchField(name=TRIDAS_PROJECT_RESEARCH_IDENTIFIER_DOMAIN_NAME)
	private List<String> tridasProjectResearchIdentifierDomain;

	public List<String> getTridasProjectResearchIdentifierDomain() {
	    return tridasProjectResearchIdentifierDomain;
	}

	public void setTridasProjectResearchIdentifierDomain(List<String> tridasProjectResearchIdentifierDomain) {
		this.tridasProjectResearchIdentifierDomain = tridasProjectResearchIdentifierDomain;
	}

	public boolean hasTridasProjectResearchIdentifierDomain() {
		return (tridasProjectResearchIdentifierDomain != null && tridasProjectResearchIdentifierDomain.size() > 0);
	}

	// tridas.project.research.description
	public final static String TRIDAS_PROJECT_RESEARCH_DESCRIPTION_NAME = "tridas.project.research.description";
	@SearchField(name=TRIDAS_PROJECT_RESEARCH_DESCRIPTION_NAME)
	private List<String> tridasProjectResearchDescription;

	public List<String> getTridasProjectResearchDescription() {
	    return tridasProjectResearchDescription;
	}

	public void setTridasProjectResearchDescription(List<String> tridasProjectResearchDescription) {
		this.tridasProjectResearchDescription = tridasProjectResearchDescription;
	}

	public boolean hasTridasProjectResearchDescription() {
		return (tridasProjectResearchDescription != null && tridasProjectResearchDescription.size() > 0);
	}

	// tridas.object.title
	public final static String TRIDAS_OBJECT_TITLE_NAME = "tridas.object.title";
	@SearchField(name=TRIDAS_OBJECT_TITLE_NAME)
	private List<String> tridasObjectTitle;

	public List<String> getTridasObjectTitle() {
	    return tridasObjectTitle;
	}

	public void setTridasObjectTitle(List<String> tridasObjectTitle) {
		this.tridasObjectTitle = tridasObjectTitle;
	}

	public boolean hasTridasObjectTitle() {
		return (tridasObjectTitle != null && tridasObjectTitle.size() > 0);
	}

	// tridas.object.identifier
	public final static String TRIDAS_OBJECT_IDENTIFIER_NAME = "tridas.object.identifier";
	@SearchField(name=TRIDAS_OBJECT_IDENTIFIER_NAME)
	private List<String> tridasObjectIdentifier;

	public List<String> getTridasObjectIdentifier() {
	    return tridasObjectIdentifier;
	}

	public void setTridasObjectIdentifier(List<String> tridasObjectIdentifier) {
		this.tridasObjectIdentifier = tridasObjectIdentifier;
	}

	public boolean hasTridasObjectIdentifier() {
		return (tridasObjectIdentifier != null && tridasObjectIdentifier.size() > 0);
	}

	// tridas.object.comments
	public final static String TRIDAS_OBJECT_COMMENTS_NAME = "tridas.object.comments";
	@SearchField(name=TRIDAS_OBJECT_COMMENTS_NAME)
	private List<String> tridasObjectComments;

	public List<String> getTridasObjectComments() {
	    return tridasObjectComments;
	}

	public void setTridasObjectComments(List<String> tridasObjectComments) {
		this.tridasObjectComments = tridasObjectComments;
	}

	public boolean hasTridasObjectComments() {
		return (tridasObjectComments != null && tridasObjectComments.size() > 0);
	}

	// tridas.object.type
	public final static String TRIDAS_OBJECT_TYPE_NAME = "tridas.object.type";
	@SearchField(name=TRIDAS_OBJECT_TYPE_NAME)
	private List<String> tridasObjectType;

	public List<String> getTridasObjectType() {
	    return tridasObjectType;
	}

	public void setTridasObjectType(List<String> tridasObjectType) {
		this.tridasObjectType = tridasObjectType;
	}

	public boolean hasTridasObjectType() {
		return (tridasObjectType != null && tridasObjectType.size() > 0);
	}

	// tridas.object.type.normal
	public final static String TRIDAS_OBJECT_TYPE_NORMAL_NAME = "tridas.object.type.normal";
	@SearchField(name=TRIDAS_OBJECT_TYPE_NORMAL_NAME)
	private List<String> tridasObjectTypeNormal;

	public List<String> getTridasObjectTypeNormal() {
	    return tridasObjectTypeNormal;
	}

	public void setTridasObjectTypeNormal(List<String> tridasObjectTypeNormal) {
		this.tridasObjectTypeNormal = tridasObjectTypeNormal;
	}

	public boolean hasTridasObjectTypeNormal() {
		return (tridasObjectTypeNormal != null && tridasObjectTypeNormal.size() > 0);
	}

	// tridas.object.description
	public final static String TRIDAS_OBJECT_DESCRIPTION_NAME = "tridas.object.description";
	@SearchField(name=TRIDAS_OBJECT_DESCRIPTION_NAME)
	private List<String> tridasObjectDescription;

	public List<String> getTridasObjectDescription() {
	    return tridasObjectDescription;
	}

	public void setTridasObjectDescription(List<String> tridasObjectDescription) {
		this.tridasObjectDescription = tridasObjectDescription;
	}

	public boolean hasTridasObjectDescription() {
		return (tridasObjectDescription != null && tridasObjectDescription.size() > 0);
	}

	// tridas.object.creator
	public final static String TRIDAS_OBJECT_CREATOR_NAME = "tridas.object.creator";
	@SearchField(name=TRIDAS_OBJECT_CREATOR_NAME)
	private List<String> tridasObjectCreator;

	public List<String> getTridasObjectCreator() {
	    return tridasObjectCreator;
	}

	public void setTridasObjectCreator(List<String> tridasObjectCreator) {
		this.tridasObjectCreator = tridasObjectCreator;
	}

	public boolean hasTridasObjectCreator() {
		return (tridasObjectCreator != null && tridasObjectCreator.size() > 0);
	}

	// tridas.object.coverage.coverageTemporalFoundation
	public final static String TRIDAS_OBJECT_COVERAGE_COVERAGETEMPORALFOUNDATION_NAME = "tridas.object.coverage.coverageTemporalFoundation";
	@SearchField(name=TRIDAS_OBJECT_COVERAGE_COVERAGETEMPORALFOUNDATION_NAME)
	private List<String> tridasObjectCoverageCoveragetemporalfoundation;

	public List<String> getTridasObjectCoverageCoveragetemporalfoundation() {
	    return tridasObjectCoverageCoveragetemporalfoundation;
	}

	public void setTridasObjectCoverageCoveragetemporalfoundation(List<String> tridasObjectCoverageCoveragetemporalfoundation) {
		this.tridasObjectCoverageCoveragetemporalfoundation = tridasObjectCoverageCoveragetemporalfoundation;
	}

	public boolean hasTridasObjectCoverageCoveragetemporalfoundation() {
		return (tridasObjectCoverageCoveragetemporalfoundation != null && tridasObjectCoverageCoveragetemporalfoundation.size() > 0);
	}

	// tridas.object.location.locationType
	public final static String TRIDAS_OBJECT_LOCATION_LOCATIONTYPE_NAME = "tridas.object.location.locationType";
	@SearchField(name=TRIDAS_OBJECT_LOCATION_LOCATIONTYPE_NAME)
	private List<String> tridasObjectLocationLocationtype;

	public List<String> getTridasObjectLocationLocationtype() {
	    return tridasObjectLocationLocationtype;
	}

	public void setTridasObjectLocationLocationtype(List<String> tridasObjectLocationLocationtype) {
		this.tridasObjectLocationLocationtype = tridasObjectLocationLocationtype;
	}

	public boolean hasTridasObjectLocationLocationtype() {
		return (tridasObjectLocationLocationtype != null && tridasObjectLocationLocationtype.size() > 0);
	}

	// tridas.object.location.locationComment
	public final static String TRIDAS_OBJECT_LOCATION_LOCATIONCOMMENT_NAME = "tridas.object.location.locationComment";
	@SearchField(name=TRIDAS_OBJECT_LOCATION_LOCATIONCOMMENT_NAME)
	private List<String> tridasObjectLocationLocationcomment;

	public List<String> getTridasObjectLocationLocationcomment() {
	    return tridasObjectLocationLocationcomment;
	}

	public void setTridasObjectLocationLocationcomment(List<String> tridasObjectLocationLocationcomment) {
		this.tridasObjectLocationLocationcomment = tridasObjectLocationLocationcomment;
	}

	public boolean hasTridasObjectLocationLocationcomment() {
		return (tridasObjectLocationLocationcomment != null && tridasObjectLocationLocationcomment.size() > 0);
	}

	// tridas.object.genericField
	public final static String TRIDAS_OBJECT_GENERICFIELD_NAME = "tridas.object.genericField";
	@SearchField(name=TRIDAS_OBJECT_GENERICFIELD_NAME)
	private List<String> tridasObjectGenericfield;

	public List<String> getTridasObjectGenericfield() {
	    return tridasObjectGenericfield;
	}

	public void setTridasObjectGenericfield(List<String> tridasObjectGenericfield) {
		this.tridasObjectGenericfield = tridasObjectGenericfield;
	}

	public boolean hasTridasObjectGenericfield() {
		return (tridasObjectGenericfield != null && tridasObjectGenericfield.size() > 0);
	}

	// tridas.element.title
	public final static String TRIDAS_ELEMENT_TITLE_NAME = "tridas.element.title";
	@SearchField(name=TRIDAS_ELEMENT_TITLE_NAME)
	private List<String> tridasElementTitle;

	public List<String> getTridasElementTitle() {
	    return tridasElementTitle;
	}

	public void setTridasElementTitle(List<String> tridasElementTitle) {
		this.tridasElementTitle = tridasElementTitle;
	}

	public boolean hasTridasElementTitle() {
		return (tridasElementTitle != null && tridasElementTitle.size() > 0);
	}

	// tridas.element.identifier
	public final static String TRIDAS_ELEMENT_IDENTIFIER_NAME = "tridas.element.identifier";
	@SearchField(name=TRIDAS_ELEMENT_IDENTIFIER_NAME)
	private List<String> tridasElementIdentifier;

	public List<String> getTridasElementIdentifier() {
	    return tridasElementIdentifier;
	}

	public void setTridasElementIdentifier(List<String> tridasElementIdentifier) {
		this.tridasElementIdentifier = tridasElementIdentifier;
	}

	public boolean hasTridasElementIdentifier() {
		return (tridasElementIdentifier != null && tridasElementIdentifier.size() > 0);
	}

	// tridas.element.type
	public final static String TRIDAS_ELEMENT_TYPE_NAME = "tridas.element.type";
	@SearchField(name=TRIDAS_ELEMENT_TYPE_NAME)
	private List<String> tridasElementType;

	public List<String> getTridasElementType() {
	    return tridasElementType;
	}

	public void setTridasElementType(List<String> tridasElementType) {
		this.tridasElementType = tridasElementType;
	}

	public boolean hasTridasElementType() {
		return (tridasElementType != null && tridasElementType.size() > 0);
	}

	// tridas.element.type.normal
	public final static String TRIDAS_ELEMENT_TYPE_NORMAL_NAME = "tridas.element.type.normal";
	@SearchField(name=TRIDAS_ELEMENT_TYPE_NORMAL_NAME)
	private List<String> tridasElementTypeNormal;

	public List<String> getTridasElementTypeNormal() {
	    return tridasElementTypeNormal;
	}

	public void setTridasElementTypeNormal(List<String> tridasElementTypeNormal) {
		this.tridasElementTypeNormal = tridasElementTypeNormal;
	}

	public boolean hasTridasElementTypeNormal() {
		return (tridasElementTypeNormal != null && tridasElementTypeNormal.size() > 0);
	}

	// tridas.element.description
	public final static String TRIDAS_ELEMENT_DESCRIPTION_NAME = "tridas.element.description";
	@SearchField(name=TRIDAS_ELEMENT_DESCRIPTION_NAME)
	private List<String> tridasElementDescription;

	public List<String> getTridasElementDescription() {
	    return tridasElementDescription;
	}

	public void setTridasElementDescription(List<String> tridasElementDescription) {
		this.tridasElementDescription = tridasElementDescription;
	}

	public boolean hasTridasElementDescription() {
		return (tridasElementDescription != null && tridasElementDescription.size() > 0);
	}

	// tridas.element.taxon
	public final static String TRIDAS_ELEMENT_TAXON_NAME = "tridas.element.taxon";
	@SearchField(name=TRIDAS_ELEMENT_TAXON_NAME)
	private List<String> tridasElementTaxon;

	public List<String> getTridasElementTaxon() {
	    return tridasElementTaxon;
	}

	public void setTridasElementTaxon(List<String> tridasElementTaxon) {
		this.tridasElementTaxon = tridasElementTaxon;
	}

	public boolean hasTridasElementTaxon() {
		return (tridasElementTaxon != null && tridasElementTaxon.size() > 0);
	}

	// tridas.element.shape
	public final static String TRIDAS_ELEMENT_SHAPE_NAME = "tridas.element.shape";
	@SearchField(name=TRIDAS_ELEMENT_SHAPE_NAME)
	private List<String> tridasElementShape;

	public List<String> getTridasElementShape() {
	    return tridasElementShape;
	}

	public void setTridasElementShape(List<String> tridasElementShape) {
		this.tridasElementShape = tridasElementShape;
	}

	public boolean hasTridasElementShape() {
		return (tridasElementShape != null && tridasElementShape.size() > 0);
	}

	// tridas.element.altitude
	public final static String TRIDAS_ELEMENT_ALTITUDE_NAME = "tridas.element.altitude";
	@SearchField(name=TRIDAS_ELEMENT_ALTITUDE_NAME)
	private List<Double> tridasElementAltitude;

	public List<Double> getTridasElementAltitude() {
	    return tridasElementAltitude;
	}

	public void setTridasElementAltitude(List<Double> tridasElementAltitude) {
		this.tridasElementAltitude = tridasElementAltitude;
	}

	public boolean hasTridasElementAltitude() {
		return (tridasElementAltitude != null && tridasElementAltitude.size() > 0);
	}

	// tridas.element.slope.angle
	public final static String TRIDAS_ELEMENT_SLOPE_ANGLE_NAME = "tridas.element.slope.angle";
	@SearchField(name=TRIDAS_ELEMENT_SLOPE_ANGLE_NAME)
	private List<Integer> tridasElementSlopeAngle;

	public List<Integer> getTridasElementSlopeAngle() {
	    return tridasElementSlopeAngle;
	}

	public void setTridasElementSlopeAngle(List<Integer> tridasElementSlopeAngle) {
		this.tridasElementSlopeAngle = tridasElementSlopeAngle;
	}

	public boolean hasTridasElementSlopeAngle() {
		return (tridasElementSlopeAngle != null && tridasElementSlopeAngle.size() > 0);
	}

	// tridas.element.slope.azimuth
	public final static String TRIDAS_ELEMENT_SLOPE_AZIMUTH_NAME = "tridas.element.slope.azimuth";
	@SearchField(name=TRIDAS_ELEMENT_SLOPE_AZIMUTH_NAME)
	private List<Integer> tridasElementSlopeAzimuth;

	public List<Integer> getTridasElementSlopeAzimuth() {
	    return tridasElementSlopeAzimuth;
	}

	public void setTridasElementSlopeAzimuth(List<Integer> tridasElementSlopeAzimuth) {
		this.tridasElementSlopeAzimuth = tridasElementSlopeAzimuth;
	}

	public boolean hasTridasElementSlopeAzimuth() {
		return (tridasElementSlopeAzimuth != null && tridasElementSlopeAzimuth.size() > 0);
	}

	// tridas.element.soil.description
	public final static String TRIDAS_ELEMENT_SOIL_DESCRIPTION_NAME = "tridas.element.soil.description";
	@SearchField(name=TRIDAS_ELEMENT_SOIL_DESCRIPTION_NAME)
	private List<String> tridasElementSoilDescription;

	public List<String> getTridasElementSoilDescription() {
	    return tridasElementSoilDescription;
	}

	public void setTridasElementSoilDescription(List<String> tridasElementSoilDescription) {
		this.tridasElementSoilDescription = tridasElementSoilDescription;
	}

	public boolean hasTridasElementSoilDescription() {
		return (tridasElementSoilDescription != null && tridasElementSoilDescription.size() > 0);
	}

	// tridas.element.soil.depth
	public final static String TRIDAS_ELEMENT_SOIL_DEPTH_NAME = "tridas.element.soil.depth";
	@SearchField(name=TRIDAS_ELEMENT_SOIL_DEPTH_NAME)
	private List<Double> tridasElementSoilDepth;

	public List<Double> getTridasElementSoilDepth() {
	    return tridasElementSoilDepth;
	}

	public void setTridasElementSoilDepth(List<Double> tridasElementSoilDepth) {
		this.tridasElementSoilDepth = tridasElementSoilDepth;
	}

	public boolean hasTridasElementSoilDepth() {
		return (tridasElementSoilDepth != null && tridasElementSoilDepth.size() > 0);
	}

	// tridas.element.bedrock.description
	public final static String TRIDAS_ELEMENT_BEDROCK_DESCRIPTION_NAME = "tridas.element.bedrock.description";
	@SearchField(name=TRIDAS_ELEMENT_BEDROCK_DESCRIPTION_NAME)
	private List<String> tridasElementBedrockDescription;

	public List<String> getTridasElementBedrockDescription() {
	    return tridasElementBedrockDescription;
	}

	public void setTridasElementBedrockDescription(List<String> tridasElementBedrockDescription) {
		this.tridasElementBedrockDescription = tridasElementBedrockDescription;
	}

	public boolean hasTridasElementBedrockDescription() {
		return (tridasElementBedrockDescription != null && tridasElementBedrockDescription.size() > 0);
	}

	// tridas.sample.title
	public final static String TRIDAS_SAMPLE_TITLE_NAME = "tridas.sample.title";
	@SearchField(name=TRIDAS_SAMPLE_TITLE_NAME)
	private List<String> tridasSampleTitle;

	public List<String> getTridasSampleTitle() {
	    return tridasSampleTitle;
	}

	public void setTridasSampleTitle(List<String> tridasSampleTitle) {
		this.tridasSampleTitle = tridasSampleTitle;
	}

	public boolean hasTridasSampleTitle() {
		return (tridasSampleTitle != null && tridasSampleTitle.size() > 0);
	}

	// tridas.sample.identifier
	public final static String TRIDAS_SAMPLE_IDENTIFIER_NAME = "tridas.sample.identifier";
	@SearchField(name=TRIDAS_SAMPLE_IDENTIFIER_NAME)
	private List<String> tridasSampleIdentifier;

	public List<String> getTridasSampleIdentifier() {
	    return tridasSampleIdentifier;
	}

	public void setTridasSampleIdentifier(List<String> tridasSampleIdentifier) {
		this.tridasSampleIdentifier = tridasSampleIdentifier;
	}

	public boolean hasTridasSampleIdentifier() {
		return (tridasSampleIdentifier != null && tridasSampleIdentifier.size() > 0);
	}

	// tridas.sample.type
	public final static String TRIDAS_SAMPLE_TYPE_NAME = "tridas.sample.type";
	@SearchField(name=TRIDAS_SAMPLE_TYPE_NAME)
	private List<String> tridasSampleType;

	public List<String> getTridasSampleType() {
	    return tridasSampleType;
	}

	public void setTridasSampleType(List<String> tridasSampleType) {
		this.tridasSampleType = tridasSampleType;
	}

	public boolean hasTridasSampleType() {
		return (tridasSampleType != null && tridasSampleType.size() > 0);
	}

	// tridas.sample.type.normal
	public final static String TRIDAS_SAMPLE_TYPE_NORMAL_NAME = "tridas.sample.type.normal";
	@SearchField(name=TRIDAS_SAMPLE_TYPE_NORMAL_NAME)
	private List<String> tridasSampleTypeNormal;

	public List<String> getTridasSampleTypeNormal() {
	    return tridasSampleTypeNormal;
	}

	public void setTridasSampleTypeNormal(List<String> tridasSampleTypeNormal) {
		this.tridasSampleTypeNormal = tridasSampleTypeNormal;
	}

	public boolean hasTridasSampleTypeNormal() {
		return (tridasSampleTypeNormal != null && tridasSampleTypeNormal.size() > 0);
	}

	// tridas.sample.samplingDate
	public final static String TRIDAS_SAMPLE_SAMPLINGDATE_NAME = "tridas.sample.samplingDate";
	@SearchField(name=TRIDAS_SAMPLE_SAMPLINGDATE_NAME)
	private List<DateTime> tridasSampleSamplingdate;

	public List<DateTime> getTridasSampleSamplingdate() {
	    return tridasSampleSamplingdate;
	}

	public void setTridasSampleSamplingdate(List<DateTime> tridasSampleSamplingdate) {
		this.tridasSampleSamplingdate = tridasSampleSamplingdate;
	}

	public boolean hasTridasSampleSamplingdate() {
		return (tridasSampleSamplingdate != null && tridasSampleSamplingdate.size() > 0);
	}

	// tridas.radius.title
	public final static String TRIDAS_RADIUS_TITLE_NAME = "tridas.radius.title";
	@SearchField(name=TRIDAS_RADIUS_TITLE_NAME)
	private List<String> tridasRadiusTitle;

	public List<String> getTridasRadiusTitle() {
	    return tridasRadiusTitle;
	}

	public void setTridasRadiusTitle(List<String> tridasRadiusTitle) {
		this.tridasRadiusTitle = tridasRadiusTitle;
	}

	public boolean hasTridasRadiusTitle() {
		return (tridasRadiusTitle != null && tridasRadiusTitle.size() > 0);
	}

	// tridas.radius.identifier
	public final static String TRIDAS_RADIUS_IDENTIFIER_NAME = "tridas.radius.identifier";
	@SearchField(name=TRIDAS_RADIUS_IDENTIFIER_NAME)
	private List<String> tridasRadiusIdentifier;

	public List<String> getTridasRadiusIdentifier() {
	    return tridasRadiusIdentifier;
	}

	public void setTridasRadiusIdentifier(List<String> tridasRadiusIdentifier) {
		this.tridasRadiusIdentifier = tridasRadiusIdentifier;
	}

	public boolean hasTridasRadiusIdentifier() {
		return (tridasRadiusIdentifier != null && tridasRadiusIdentifier.size() > 0);
	}

	// tridas.radius.woodCompleteness.pith
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_PITH_NAME = "tridas.radius.woodCompleteness.pith";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_PITH_NAME)
	private List<String> tridasRadiusWoodcompletenessPith;

	public List<String> getTridasRadiusWoodcompletenessPith() {
	    return tridasRadiusWoodcompletenessPith;
	}

	public void setTridasRadiusWoodcompletenessPith(List<String> tridasRadiusWoodcompletenessPith) {
		this.tridasRadiusWoodcompletenessPith = tridasRadiusWoodcompletenessPith;
	}

	public boolean hasTridasRadiusWoodcompletenessPith() {
		return (tridasRadiusWoodcompletenessPith != null && tridasRadiusWoodcompletenessPith.size() > 0);
	}

	// tridas.radius.woodCompleteness.heartwood
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_NAME = "tridas.radius.woodCompleteness.heartwood";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_NAME)
	private List<String> tridasRadiusWoodcompletenessHeartwood;

	public List<String> getTridasRadiusWoodcompletenessHeartwood() {
	    return tridasRadiusWoodcompletenessHeartwood;
	}

	public void setTridasRadiusWoodcompletenessHeartwood(List<String> tridasRadiusWoodcompletenessHeartwood) {
		this.tridasRadiusWoodcompletenessHeartwood = tridasRadiusWoodcompletenessHeartwood;
	}

	public boolean hasTridasRadiusWoodcompletenessHeartwood() {
		return (tridasRadiusWoodcompletenessHeartwood != null && tridasRadiusWoodcompletenessHeartwood.size() > 0);
	}

	// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPith
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_MISSINGHEARTWOODRINGSTOPITH_NAME = "tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPith";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_MISSINGHEARTWOODRINGSTOPITH_NAME)
	private List<Integer> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith;

	public List<Integer> getTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith() {
	    return tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith;
	}

	public void setTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith(List<Integer> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith) {
		this.tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith = tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith;
	}

	public boolean hasTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith() {
		return (tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith != null && tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith.size() > 0);
	}

	// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPithFoundation
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_MISSINGHEARTWOODRINGSTOPITHFOUNDATION_NAME = "tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPithFoundation";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_HEARTWOOD_MISSINGHEARTWOODRINGSTOPITHFOUNDATION_NAME)
	private List<String> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation;

	public List<String> getTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation() {
	    return tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation;
	}

	public void setTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation(List<String> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation) {
		this.tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation = tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation;
	}

	public boolean hasTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation() {
		return (tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation != null && tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation.size() > 0);
	}

	// tridas.radius.woodCompleteness.sapwood
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_NAME = "tridas.radius.woodCompleteness.sapwood";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_NAME)
	private List<String> tridasRadiusWoodcompletenessSapwood;

	public List<String> getTridasRadiusWoodcompletenessSapwood() {
	    return tridasRadiusWoodcompletenessSapwood;
	}

	public void setTridasRadiusWoodcompletenessSapwood(List<String> tridasRadiusWoodcompletenessSapwood) {
		this.tridasRadiusWoodcompletenessSapwood = tridasRadiusWoodcompletenessSapwood;
	}

	public boolean hasTridasRadiusWoodcompletenessSapwood() {
		return (tridasRadiusWoodcompletenessSapwood != null && tridasRadiusWoodcompletenessSapwood.size() > 0);
	}

	// tridas.radius.woodCompleteness.sapwood.nrOfSapwoodRings
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_NROFSAPWOODRINGS_NAME = "tridas.radius.woodCompleteness.sapwood.nrOfSapwoodRings";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_NROFSAPWOODRINGS_NAME)
	private List<Integer> tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings;

	public List<Integer> getTridasRadiusWoodcompletenessSapwoodNrofsapwoodrings() {
	    return tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings;
	}

	public void setTridasRadiusWoodcompletenessSapwoodNrofsapwoodrings(List<Integer> tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings) {
		this.tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings = tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings;
	}

	public boolean hasTridasRadiusWoodcompletenessSapwoodNrofsapwoodrings() {
		return (tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings != null && tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings.size() > 0);
	}

	// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBark
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_MISSINGSAPWOODRINGSTOBARK_NAME = "tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBark";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_MISSINGSAPWOODRINGSTOBARK_NAME)
	private List<Integer> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark;

	public List<Integer> getTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark() {
	    return tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark;
	}

	public void setTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark(List<Integer> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark) {
		this.tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark = tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark;
	}

	public boolean hasTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark() {
		return (tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark != null && tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark.size() > 0);
	}

	// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBarkFoundation
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_MISSINGSAPWOODRINGSTOBARKFOUNDATION_NAME = "tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBarkFoundation";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_MISSINGSAPWOODRINGSTOBARKFOUNDATION_NAME)
	private List<String> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation;

	public List<String> getTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation() {
	    return tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation;
	}

	public void setTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation(List<String> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation) {
		this.tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation = tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation;
	}

	public boolean hasTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation() {
		return (tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation != null && tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation.size() > 0);
	}

	// tridas.radius.woodCompleteness.sapwood.lastRingUnderBark
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_LASTRINGUNDERBARK_NAME = "tridas.radius.woodCompleteness.sapwood.lastRingUnderBark";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_SAPWOOD_LASTRINGUNDERBARK_NAME)
	private List<String> tridasRadiusWoodcompletenessSapwoodLastringunderbark;

	public List<String> getTridasRadiusWoodcompletenessSapwoodLastringunderbark() {
	    return tridasRadiusWoodcompletenessSapwoodLastringunderbark;
	}

	public void setTridasRadiusWoodcompletenessSapwoodLastringunderbark(List<String> tridasRadiusWoodcompletenessSapwoodLastringunderbark) {
		this.tridasRadiusWoodcompletenessSapwoodLastringunderbark = tridasRadiusWoodcompletenessSapwoodLastringunderbark;
	}

	public boolean hasTridasRadiusWoodcompletenessSapwoodLastringunderbark() {
		return (tridasRadiusWoodcompletenessSapwoodLastringunderbark != null && tridasRadiusWoodcompletenessSapwoodLastringunderbark.size() > 0);
	}

	// tridas.radius.woodCompleteness.bark
	public final static String TRIDAS_RADIUS_WOODCOMPLETENESS_BARK_NAME = "tridas.radius.woodCompleteness.bark";
	@SearchField(name=TRIDAS_RADIUS_WOODCOMPLETENESS_BARK_NAME)
	private List<String> tridasRadiusWoodcompletenessBark;

	public List<String> getTridasRadiusWoodcompletenessBark() {
	    return tridasRadiusWoodcompletenessBark;
	}

	public void setTridasRadiusWoodcompletenessBark(List<String> tridasRadiusWoodcompletenessBark) {
		this.tridasRadiusWoodcompletenessBark = tridasRadiusWoodcompletenessBark;
	}

	public boolean hasTridasRadiusWoodcompletenessBark() {
		return (tridasRadiusWoodcompletenessBark != null && tridasRadiusWoodcompletenessBark.size() > 0);
	}

	// tridas.measurementSeries.title
	public final static String TRIDAS_MEASUREMENTSERIES_TITLE_NAME = "tridas.measurementSeries.title";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_TITLE_NAME)
	private List<String> tridasMeasurementseriesTitle;

	public List<String> getTridasMeasurementseriesTitle() {
	    return tridasMeasurementseriesTitle;
	}

	public void setTridasMeasurementseriesTitle(List<String> tridasMeasurementseriesTitle) {
		this.tridasMeasurementseriesTitle = tridasMeasurementseriesTitle;
	}

	public boolean hasTridasMeasurementseriesTitle() {
		return (tridasMeasurementseriesTitle != null && tridasMeasurementseriesTitle.size() > 0);
	}

	// tridas.measurementSeries.identifier
	public final static String TRIDAS_MEASUREMENTSERIES_IDENTIFIER_NAME = "tridas.measurementSeries.identifier";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_IDENTIFIER_NAME)
	private List<String> tridasMeasurementseriesIdentifier;

	public List<String> getTridasMeasurementseriesIdentifier() {
	    return tridasMeasurementseriesIdentifier;
	}

	public void setTridasMeasurementseriesIdentifier(List<String> tridasMeasurementseriesIdentifier) {
		this.tridasMeasurementseriesIdentifier = tridasMeasurementseriesIdentifier;
	}

	public boolean hasTridasMeasurementseriesIdentifier() {
		return (tridasMeasurementseriesIdentifier != null && tridasMeasurementseriesIdentifier.size() > 0);
	}

	// tridas.measurementSeries.analyst
	public final static String TRIDAS_MEASUREMENTSERIES_ANALYST_NAME = "tridas.measurementSeries.analyst";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_ANALYST_NAME)
	private List<String> tridasMeasurementseriesAnalyst;

	public List<String> getTridasMeasurementseriesAnalyst() {
	    return tridasMeasurementseriesAnalyst;
	}

	public void setTridasMeasurementseriesAnalyst(List<String> tridasMeasurementseriesAnalyst) {
		this.tridasMeasurementseriesAnalyst = tridasMeasurementseriesAnalyst;
	}

	public boolean hasTridasMeasurementseriesAnalyst() {
		return (tridasMeasurementseriesAnalyst != null && tridasMeasurementseriesAnalyst.size() > 0);
	}

	// tridas.measurementSeries.dendrochronologist
	public final static String TRIDAS_MEASUREMENTSERIES_DENDROCHRONOLOGIST_NAME = "tridas.measurementSeries.dendrochronologist";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_DENDROCHRONOLOGIST_NAME)
	private List<String> tridasMeasurementseriesDendrochronologist;

	public List<String> getTridasMeasurementseriesDendrochronologist() {
	    return tridasMeasurementseriesDendrochronologist;
	}

	public void setTridasMeasurementseriesDendrochronologist(List<String> tridasMeasurementseriesDendrochronologist) {
		this.tridasMeasurementseriesDendrochronologist = tridasMeasurementseriesDendrochronologist;
	}

	public boolean hasTridasMeasurementseriesDendrochronologist() {
		return (tridasMeasurementseriesDendrochronologist != null && tridasMeasurementseriesDendrochronologist.size() > 0);
	}

	// tridas.measurementSeries.measuringDate
	public final static String TRIDAS_MEASUREMENTSERIES_MEASURINGDATE_NAME = "tridas.measurementSeries.measuringDate";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_MEASURINGDATE_NAME)
	private List<DateTime> tridasMeasurementseriesMeasuringdate;

	public List<DateTime> getTridasMeasurementseriesMeasuringdate() {
	    return tridasMeasurementseriesMeasuringdate;
	}

	public void setTridasMeasurementseriesMeasuringdate(List<DateTime> tridasMeasurementseriesMeasuringdate) {
		this.tridasMeasurementseriesMeasuringdate = tridasMeasurementseriesMeasuringdate;
	}

	public boolean hasTridasMeasurementseriesMeasuringdate() {
		return (tridasMeasurementseriesMeasuringdate != null && tridasMeasurementseriesMeasuringdate.size() > 0);
	}

	// tridas.measurementSeries.measuringMethod
	public final static String TRIDAS_MEASUREMENTSERIES_MEASURINGMETHOD_NAME = "tridas.measurementSeries.measuringMethod";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_MEASURINGMETHOD_NAME)
	private List<String> tridasMeasurementseriesMeasuringmethod;

	public List<String> getTridasMeasurementseriesMeasuringmethod() {
	    return tridasMeasurementseriesMeasuringmethod;
	}

	public void setTridasMeasurementseriesMeasuringmethod(List<String> tridasMeasurementseriesMeasuringmethod) {
		this.tridasMeasurementseriesMeasuringmethod = tridasMeasurementseriesMeasuringmethod;
	}

	public boolean hasTridasMeasurementseriesMeasuringmethod() {
		return (tridasMeasurementseriesMeasuringmethod != null && tridasMeasurementseriesMeasuringmethod.size() > 0);
	}

	// tridas.measurementSeries.measuringMethod.normal
	public final static String TRIDAS_MEASUREMENTSERIES_MEASURINGMETHOD_NORMAL_NAME = "tridas.measurementSeries.measuringMethod.normal";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_MEASURINGMETHOD_NORMAL_NAME)
	private List<String> tridasMeasurementseriesMeasuringmethodNormal;

	public List<String> getTridasMeasurementseriesMeasuringmethodNormal() {
	    return tridasMeasurementseriesMeasuringmethodNormal;
	}

	public void setTridasMeasurementseriesMeasuringmethodNormal(List<String> tridasMeasurementseriesMeasuringmethodNormal) {
		this.tridasMeasurementseriesMeasuringmethodNormal = tridasMeasurementseriesMeasuringmethodNormal;
	}

	public boolean hasTridasMeasurementseriesMeasuringmethodNormal() {
		return (tridasMeasurementseriesMeasuringmethodNormal != null && tridasMeasurementseriesMeasuringmethodNormal.size() > 0);
	}

	// tridas.measurementSeries.interpretation.provenance
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_PROVENANCE_NAME = "tridas.measurementSeries.interpretation.provenance";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_PROVENANCE_NAME)
	private List<String> tridasMeasurementseriesInterpretationProvenance;

	public List<String> getTridasMeasurementseriesInterpretationProvenance() {
	    return tridasMeasurementseriesInterpretationProvenance;
	}

	public void setTridasMeasurementseriesInterpretationProvenance(List<String> tridasMeasurementseriesInterpretationProvenance) {
		this.tridasMeasurementseriesInterpretationProvenance = tridasMeasurementseriesInterpretationProvenance;
	}

	public boolean hasTridasMeasurementseriesInterpretationProvenance() {
		return (tridasMeasurementseriesInterpretationProvenance != null && tridasMeasurementseriesInterpretationProvenance.size() > 0);
	}

	// tridas.measurementSeries.interpretation.deathYear
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_DEATHYEAR_NAME = "tridas.measurementSeries.interpretation.deathYear";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_DEATHYEAR_NAME)
	private List<Integer> tridasMeasurementseriesInterpretationDeathyear;

	public List<Integer> getTridasMeasurementseriesInterpretationDeathyear() {
	    return tridasMeasurementseriesInterpretationDeathyear;
	}

	public void setTridasMeasurementseriesInterpretationDeathyear(List<Integer> tridasMeasurementseriesInterpretationDeathyear) {
		this.tridasMeasurementseriesInterpretationDeathyear = tridasMeasurementseriesInterpretationDeathyear;
	}

	public boolean hasTridasMeasurementseriesInterpretationDeathyear() {
		return (tridasMeasurementseriesInterpretationDeathyear != null && tridasMeasurementseriesInterpretationDeathyear.size() > 0);
	}

	// tridas.measurementSeries.interpretation.firstYear
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_FIRSTYEAR_NAME = "tridas.measurementSeries.interpretation.firstYear";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_FIRSTYEAR_NAME)
	private List<Integer> tridasMeasurementseriesInterpretationFirstyear;

	public List<Integer> getTridasMeasurementseriesInterpretationFirstyear() {
	    return tridasMeasurementseriesInterpretationFirstyear;
	}

	public void setTridasMeasurementseriesInterpretationFirstyear(List<Integer> tridasMeasurementseriesInterpretationFirstyear) {
		this.tridasMeasurementseriesInterpretationFirstyear = tridasMeasurementseriesInterpretationFirstyear;
	}

	public boolean hasTridasMeasurementseriesInterpretationFirstyear() {
		return (tridasMeasurementseriesInterpretationFirstyear != null && tridasMeasurementseriesInterpretationFirstyear.size() > 0);
	}

	// tridas.measurementSeries.interpretation.lastYear
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_LASTYEAR_NAME = "tridas.measurementSeries.interpretation.lastYear";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_LASTYEAR_NAME)
	private List<Integer> tridasMeasurementseriesInterpretationLastyear;

	public List<Integer> getTridasMeasurementseriesInterpretationLastyear() {
	    return tridasMeasurementseriesInterpretationLastyear;
	}

	public void setTridasMeasurementseriesInterpretationLastyear(List<Integer> tridasMeasurementseriesInterpretationLastyear) {
		this.tridasMeasurementseriesInterpretationLastyear = tridasMeasurementseriesInterpretationLastyear;
	}

	public boolean hasTridasMeasurementseriesInterpretationLastyear() {
		return (tridasMeasurementseriesInterpretationLastyear != null && tridasMeasurementseriesInterpretationLastyear.size() > 0);
	}

	// tridas.measurementSeries.interpretation.pithYear
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_PITHYEAR_NAME = "tridas.measurementSeries.interpretation.pithYear";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_PITHYEAR_NAME)
	private List<Integer> tridasMeasurementseriesInterpretationPithyear;

	public List<Integer> getTridasMeasurementseriesInterpretationPithyear() {
	    return tridasMeasurementseriesInterpretationPithyear;
	}

	public void setTridasMeasurementseriesInterpretationPithyear(List<Integer> tridasMeasurementseriesInterpretationPithyear) {
		this.tridasMeasurementseriesInterpretationPithyear = tridasMeasurementseriesInterpretationPithyear;
	}

	public boolean hasTridasMeasurementseriesInterpretationPithyear() {
		return (tridasMeasurementseriesInterpretationPithyear != null && tridasMeasurementseriesInterpretationPithyear.size() > 0);
	}

	// tridas.measurementSeries.interpretation.statFoundation.statValue
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_STATVALUE_NAME = "tridas.measurementSeries.interpretation.statFoundation.statValue";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_STATVALUE_NAME)
	private List<Double> tridasMeasurementseriesInterpretationStatfoundationStatvalue;

	public List<Double> getTridasMeasurementseriesInterpretationStatfoundationStatvalue() {
	    return tridasMeasurementseriesInterpretationStatfoundationStatvalue;
	}

	public void setTridasMeasurementseriesInterpretationStatfoundationStatvalue(List<Double> tridasMeasurementseriesInterpretationStatfoundationStatvalue) {
		this.tridasMeasurementseriesInterpretationStatfoundationStatvalue = tridasMeasurementseriesInterpretationStatfoundationStatvalue;
	}

	public boolean hasTridasMeasurementseriesInterpretationStatfoundationStatvalue() {
		return (tridasMeasurementseriesInterpretationStatfoundationStatvalue != null && tridasMeasurementseriesInterpretationStatfoundationStatvalue.size() > 0);
	}

	// tridas.measurementSeries.interpretation.statFoundation.usedSoftware
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_USEDSOFTWARE_NAME = "tridas.measurementSeries.interpretation.statFoundation.usedSoftware";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_USEDSOFTWARE_NAME)
	private List<String> tridasMeasurementseriesInterpretationStatfoundationUsedsoftware;

	public List<String> getTridasMeasurementseriesInterpretationStatfoundationUsedsoftware() {
	    return tridasMeasurementseriesInterpretationStatfoundationUsedsoftware;
	}

	public void setTridasMeasurementseriesInterpretationStatfoundationUsedsoftware(List<String> tridasMeasurementseriesInterpretationStatfoundationUsedsoftware) {
		this.tridasMeasurementseriesInterpretationStatfoundationUsedsoftware = tridasMeasurementseriesInterpretationStatfoundationUsedsoftware;
	}

	public boolean hasTridasMeasurementseriesInterpretationStatfoundationUsedsoftware() {
		return (tridasMeasurementseriesInterpretationStatfoundationUsedsoftware != null && tridasMeasurementseriesInterpretationStatfoundationUsedsoftware.size() > 0);
	}

	// tridas.measurementSeries.interpretation.statFoundation.type
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_TYPE_NAME = "tridas.measurementSeries.interpretation.statFoundation.type";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_TYPE_NAME)
	private List<String> tridasMeasurementseriesInterpretationStatfoundationType;

	public List<String> getTridasMeasurementseriesInterpretationStatfoundationType() {
	    return tridasMeasurementseriesInterpretationStatfoundationType;
	}

	public void setTridasMeasurementseriesInterpretationStatfoundationType(List<String> tridasMeasurementseriesInterpretationStatfoundationType) {
		this.tridasMeasurementseriesInterpretationStatfoundationType = tridasMeasurementseriesInterpretationStatfoundationType;
	}

	public boolean hasTridasMeasurementseriesInterpretationStatfoundationType() {
		return (tridasMeasurementseriesInterpretationStatfoundationType != null && tridasMeasurementseriesInterpretationStatfoundationType.size() > 0);
	}

	// tridas.measurementSeries.interpretation.statFoundation.type.normal
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_TYPE_NORMAL_NAME = "tridas.measurementSeries.interpretation.statFoundation.type.normal";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_TYPE_NORMAL_NAME)
	private List<String> tridasMeasurementseriesInterpretationStatfoundationTypeNormal;

	public List<String> getTridasMeasurementseriesInterpretationStatfoundationTypeNormal() {
	    return tridasMeasurementseriesInterpretationStatfoundationTypeNormal;
	}

	public void setTridasMeasurementseriesInterpretationStatfoundationTypeNormal(List<String> tridasMeasurementseriesInterpretationStatfoundationTypeNormal) {
		this.tridasMeasurementseriesInterpretationStatfoundationTypeNormal = tridasMeasurementseriesInterpretationStatfoundationTypeNormal;
	}

	public boolean hasTridasMeasurementseriesInterpretationStatfoundationTypeNormal() {
		return (tridasMeasurementseriesInterpretationStatfoundationTypeNormal != null && tridasMeasurementseriesInterpretationStatfoundationTypeNormal.size() > 0);
	}

	// tridas.measurementSeries.interpretation.statFoundation.significanceLevel
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_SIGNIFICANCELEVEL_NAME = "tridas.measurementSeries.interpretation.statFoundation.significanceLevel";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATION_STATFOUNDATION_SIGNIFICANCELEVEL_NAME)
	private List<Double> tridasMeasurementseriesInterpretationStatfoundationSignificancelevel;

	public List<Double> getTridasMeasurementseriesInterpretationStatfoundationSignificancelevel() {
	    return tridasMeasurementseriesInterpretationStatfoundationSignificancelevel;
	}

	public void setTridasMeasurementseriesInterpretationStatfoundationSignificancelevel(List<Double> tridasMeasurementseriesInterpretationStatfoundationSignificancelevel) {
		this.tridasMeasurementseriesInterpretationStatfoundationSignificancelevel = tridasMeasurementseriesInterpretationStatfoundationSignificancelevel;
	}

	public boolean hasTridasMeasurementseriesInterpretationStatfoundationSignificancelevel() {
		return (tridasMeasurementseriesInterpretationStatfoundationSignificancelevel != null && tridasMeasurementseriesInterpretationStatfoundationSignificancelevel.size() > 0);
	}

	// tridas.measurementSeries.interpretationUnsolved
	public final static String TRIDAS_MEASUREMENTSERIES_INTERPRETATIONUNSOLVED_NAME = "tridas.measurementSeries.interpretationUnsolved";
	@SearchField(name=TRIDAS_MEASUREMENTSERIES_INTERPRETATIONUNSOLVED_NAME)
	private List<String> tridasMeasurementseriesInterpretationunsolved;

	public List<String> getTridasMeasurementseriesInterpretationunsolved() {
	    return tridasMeasurementseriesInterpretationunsolved;
	}

	public void setTridasMeasurementseriesInterpretationunsolved(List<String> tridasMeasurementseriesInterpretationunsolved) {
		this.tridasMeasurementseriesInterpretationunsolved = tridasMeasurementseriesInterpretationunsolved;
	}

	public boolean hasTridasMeasurementseriesInterpretationunsolved() {
		return (tridasMeasurementseriesInterpretationunsolved != null && tridasMeasurementseriesInterpretationunsolved.size() > 0);
	}

	/* End - generated code */


	// --- Geographical Location on Earth is specified by Longitude and Latitude ---
	// we assume in degrees (decimal) and according to WGS84 
	// NOTE could be generalized into a GeoSearchBean? interface and maybe an abstract class
	
	// Latitude
	public final static String LAT_NAME = "lat";
	@SearchField(name=LAT_NAME)
	private Double lat;

	public Double getLat() {
	    return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public boolean hasLat() {
		return (lat != null);
	}
	
	// Longitude
	public final static String LNG_NAME = "lng";
	@SearchField(name=LNG_NAME)
	private Double lng;

	public Double getLng() {
	    return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public boolean hasLng() {
		return (lng != null);
	}	
	
}
