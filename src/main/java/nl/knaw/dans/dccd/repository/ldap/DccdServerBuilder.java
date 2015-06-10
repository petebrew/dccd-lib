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
package nl.knaw.dans.dccd.repository.ldap;

//import java.util.Hashtable;
//import java.util.Properties;
//
//import javax.naming.Context;
//import javax.naming.NamingException;
//import javax.naming.directory.Attribute;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.BasicAttribute;
//import javax.naming.directory.BasicAttributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.InitialDirContext;
//
//import nl.knaw.dans.common.ldap.ds.DirContextSupplier;
////import nl.knaw.dans.common.ldap.management.DansServerBuilder;
//import nl.knaw.dans.dccd.application.services.DccdConfigurationService;

// TODO remove

/**
 * Builds the directory server, but only the DCCD specific parts (1.3.6.1.4.1.33188.2).
 * ServerBuilder has default attributes for the ApacheDS server.
 * You must run the ServerBuilder from the dans common "dans-ldap" project first!
 *
 * When using the ApacheDS LDAPbrowser you will see the DANS context
 * at: DIT/Root DSE/dc=dans,dc=knaw,dc=nl
 * and you can find the attributes
 * at: DIT/Root DSE/ou=schema/cn=other/ou=attributeTypes
 * and the classes
 * at: DIT/Root DSE/ou=schema/cn=other/ou=objectClasses
 *
 * @see <a href="http://trac.dans.knaw.nl/eof/wiki/ApacheDS">http://trac.dans.knaw.nl/eof/wiki/ApacheDS</a>
 * @author ecco Feb 13, 2009
 */
/*
public class DccdServerBuilder extends DansServerBuilder
{
	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String LDAP_URL = settings.getProperty("ldap.url");//"ldap://localhost:10389"

	public static final String DANS_CONTEXT                 = "dc=dans,dc=knaw,dc=nl";
    public static final String dcDans                       = "dans";
    public static final String dcKNAW                       = "knaw";
    public static final String dcNL                         = "nl";

    public static final String DCCD_CONTEXT                 = "ou=dccd," + DANS_CONTEXT;
    public static final String ORGANISATIONS_CONTEXT        = "ou=organisations," + DCCD_CONTEXT;
    public static final String USERS_CONTEXT                = "ou=users," + DCCD_CONTEXT;

    public static final String DEFAULT_PROVIDERURL          = LDAP_URL;//"ldap://localhost:10389";
    public static final String DEFAULT_SECURITY_PRINCIPAL   = "uid=admin,ou=system";
    public static final String DEFAULT_SECURITY_CREDENTIALS = "secret";

    public static final String CONTEXT_FACTORY              = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String SIMPLE_AUTHENTICATION        = "simple";

    public static void main(String[] args) throws NamingException
    {
        String providerURL = DEFAULT_PROVIDERURL;
        if (args != null && args.length > 0)
        {
            providerURL = args[0];
        }
        String securityPrincipal = DEFAULT_SECURITY_PRINCIPAL;
        if (args != null && args.length > 1)
        {
            securityPrincipal = args[1];
        }
        String securityCredentials = DEFAULT_SECURITY_CREDENTIALS;
        if (args != null && args.length > 2)
        {
            securityCredentials = args[2];
        }

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        env.put(Context.SECURITY_AUTHENTICATION, SIMPLE_AUTHENTICATION);

        env.put(Context.PROVIDER_URL, providerURL);
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        DirContext ctx = new InitialDirContext(env);

        DccdServerBuilder builder = new DccdServerBuilder(ctx);
        try
        {
            builder.buildServer();
            System.out.println("ServerBuilder finished.");
        }
        finally
        {
            ctx.close();
        }
    }

    public DccdServerBuilder(DirContextSupplier supplier) throws NamingException
    {
        super(supplier);
    }

    public DccdServerBuilder(DirContext ctx) throws NamingException
    {
        super(ctx);
    }

    public void buildServer() throws NamingException
    {
        buildContexts();
        buildSchema();
    }

    protected void buildSchema() throws NamingException
    {
        super.buildSchema();

        System.out.println("BUILDING DCCD SCHEMA");
        // ATTRIBUTES

        // dccdRoles
        String name = "AttributeDefinition/dccdRoles";
        if (!isSchemaBound(name))
        {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("NUMERICOID", "1.3.6.1.4.1.33188.2.1.4");
            attrs.put("NAME", "dccdRoles");
            attrs.put("DESC", "roles of an dccdUser");
            attrs.put("EQUALITY", "caseIgnoreMatch");
            attrs.put("SYNTAX", "1.3.6.1.4.1.1466.115.121.1.15"); // DirectoryString
            //attrs.put("SINGLE-VALUE", "FALSE"); >> leads to "SINGLE-VALUE", "TRUE". So just leave it out.
            createSchema(name, attrs);
        }
        printSchema(name);

        // dccdDAI
        name = "AttributeDefinition/dccdDAI";
        if (!isSchemaBound(name))
        {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("NUMERICOID", "1.3.6.1.4.1.33188.2.1.5");
            attrs.put("NAME", "dccdDAI");
            attrs.put("DESC", "Digital Author Identifier");
            attrs.put("EQUALITY", "caseIgnoreMatch");
            attrs.put("SYNTAX", "1.3.6.1.4.1.1466.115.121.1.15"); // DirectoryString
            attrs.put("SINGLE-VALUE", "TRUE");
            createSchema(name, attrs);
        }
        printSchema(name);

        //CLASSES
        // Note: we have two, there is an opportunity to generalize the addition of ClassDefinitions!

        // dccdUser
        name = "ClassDefinition/dccdUser";
        if (isSchemaBound(name))
        {
        	getSchema().destroySubcontext(name);
            System.out.println("Schema destroyed: " + name);
        }
        if (!isSchemaBound(name))
        {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("NUMERICOID", "1.3.6.1.4.1.33188.2.2.1");
            attrs.put("NAME", "dccdUser");
            attrs.put("DESC", "An entry which represents a user of DCCD");
            attrs.put("SUP", "dansUser");//"inetOrgPerson");
            attrs.put("STRUCTURAL", "true");

            Attribute must = new BasicAttribute("MUST", "uid");
            must.add("objectclass");
            attrs.put(must);

            Attribute may = new BasicAttribute("MAY");
            may.add("dccdRoles");
            may.add("dccdDAI");
            attrs.put(may);

            createSchema(name, attrs);
        }
        printSchema(name);

        // dccdUserOrganisation
        name = "ClassDefinition/dccdUserOrganisation";
        if (isSchemaBound(name))
        {
        	getSchema().destroySubcontext(name);
            System.out.println("Schema destroyed: " + name);
        }
        if (!isSchemaBound(name))
        {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("NUMERICOID", "1.3.6.1.4.1.33188.2.2.2");
            attrs.put("NAME", "dccdUserOrganisation");
            attrs.put("DESC", "An entry which represents a organisation in the dccd application");
            attrs.put("SUP", "organizationalUnit");
            attrs.put("STRUCTURAL", "true");

            Attribute must = new BasicAttribute("MUST", "ou"); // was "cn"
            must.add("objectclass");
            attrs.put(must);

            Attribute may = new BasicAttribute("MAY");
            may.add("dansState");
            may.add("description");
            may.add("postalAddress");
            may.add("postalCode");
            may.add("l");
            may.add("st");
            may.add("uniqueMember");
            attrs.put(may);

            createSchema(name, attrs);
        }
        printSchema(name);

        System.out.println("END BUILDING SCHEMA");
    }


    protected void buildContexts() throws NamingException
    {
    	super.buildContexts();

        System.out.println("BUILDING DCCD CONTEXTS");

        String dn = DCCD_CONTEXT;
        if (!isContextBound(dn))
        {
            Attributes attrs = new BasicAttributes();
            Attribute oc = new BasicAttribute("objectclass");
            oc.add("extensibleObject");
            oc.add("organizationalUnit");
            oc.add("top");

            attrs.put(oc);
            attrs.put("ou", "dccd");

            buildContext(dn, attrs);
        }
        printContext(dn);

        dn = ORGANISATIONS_CONTEXT;
        if (!isContextBound(dn))
        {
            Attributes attrs = new BasicAttributes();
            Attribute oc = new BasicAttribute("objectclass");
            oc.add("extensibleObject");
            oc.add("organizationalUnit");
            oc.add("top");

            attrs.put(oc);
            attrs.put("ou", "organisations"); // was cn instaed of ou

            buildContext(dn, attrs);
        }
        printContext(dn);

        dn = USERS_CONTEXT;
        if (!isContextBound(dn))
        {
            Attributes attrs = new BasicAttributes();
            Attribute oc = new BasicAttribute("objectclass");
            oc.add("extensibleObject");
            oc.add("organizationalUnit");
            oc.add("top");

            attrs.put(oc);
            attrs.put("ou", "users");

            buildContext(dn, attrs);
        }
        printContext(dn);

        System.out.println("END BUILDING CONTEXTS");
    }

//    private void printContext(String dn) throws NamingException
//    {
//        NamingEnumeration<Binding> bindings = ctx.listBindings(dn);
//        while (bindings.hasMore())
//        {
//            System.out.println("Binding: " + bindings.next().getNameInNamespace());
//        }
//
//        Attributes attrs = ctx.getAttributes(dn);
//        NamingEnumeration<? extends Attribute> all = attrs.getAll();
//        System.out.println("Attributes:");
//        while (all.hasMore())
//        {
//            Attribute attr = all.next();
//            for (int i = 0; i < attr.size(); i++)
//            {
//                System.out.println("\t" + attr.getID() + "=" + attr.get(i));
//            }
//        }
//
//    }
//
//    private boolean isContextBound(String context) throws NamingException
//    {
//        boolean hasContext = false;
//        try
//        {
//            ctx.listBindings(context);
//            hasContext = true;
//            System.out.println("Context found: " + context);
//        }
//        catch (NameNotFoundException e)
//        {
//            System.out.println("Context does not exist: " + context);
//        }
//        return hasContext;
//    }
//
//    private void buildContext(String name, Attributes attrs) throws NamingException
//    {
//        ctx.createSubcontext(name, attrs);
//        System.out.println("Added subContext: " + name);
//    }

}
*/
