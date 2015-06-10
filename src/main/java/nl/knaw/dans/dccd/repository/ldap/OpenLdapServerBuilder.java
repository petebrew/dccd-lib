package nl.knaw.dans.dccd.repository.ldap;

import java.io.File;
import java.io.IOException;

import javax.naming.NamingException;

import nl.knaw.dans.common.ldap.ds.Constants;
import nl.knaw.dans.common.ldap.management.AbstractSchema;
import nl.knaw.dans.common.ldap.management.LdapServerBuilder;

/**
 * Create the LDAP schema files
 * 
 * Note that OpenLDAP2.4 wants ldif, so we could try to produce those *.ldif instead of *.schema files
 * 
 * @author paulboon
 *
 */
public class OpenLdapServerBuilder extends LdapServerBuilder
{

    private String providerUrl;
    private String securityPrincipal;

    public OpenLdapServerBuilder() throws NamingException
    {
        super();
    }

    public OpenLdapServerBuilder(String providerUrl, String securityPrincipal, String securityCredentials) throws NamingException
    {
        super();
        this.providerUrl = providerUrl;
        this.securityPrincipal = securityPrincipal;
        setSecurityCredentials(securityCredentials);
        System.out.println("Starting OpenLsapServerBuilder");
        System.out.println("providerUrl=" + getProviderUrl());
        System.out.println("securityPrincipal=" + getSecurityPrincipal());
        System.out.println("securityCredentials=" + getSecurityCredentials());
    }

    @Override
    public String getProviderUrl()
    {
        if (providerUrl == null)
        {
            providerUrl = Constants.OPENLDAP_DEFAULT_PROVIDERURL;
        }
        return providerUrl;
    }

    @Override
    public String getSecurityPrincipal()
    {
        if (securityPrincipal == null)
        {
            securityPrincipal = Constants.OPENLDAP_DEFAULT_SECURITY_PRINCIPAL;
        }
        return securityPrincipal;
    }
    
    @Override
	public void buildContexts() throws NamingException {
		// do nothing!
	}
    
	@Override
    public void buildSchemas() throws NamingException, IOException
    {
        System.out.println("Cannot programmatically create schemas in openLdap.");
        File schemaDir = new File("schema");
        schemaDir.mkdir();
        for (AbstractSchema schema : getSchemas())
        {
            schema.exportForOpenLdap();
        }
        System.out.println("Schemas are in " + schemaDir.getAbsolutePath());
    }

    public static void main(String[] args) throws NamingException, IOException
    {
        String providerUrl = args.length > 0 ? args[0] : null;
        String securityPrincipal = args.length > 1 ? args[1] : null;
        String securityCredentials = args.length > 2 ? args[2] : null;

        OpenLdapServerBuilder builder = new OpenLdapServerBuilder(providerUrl, securityPrincipal, securityCredentials);
        builder.buildServer();
    }

}
