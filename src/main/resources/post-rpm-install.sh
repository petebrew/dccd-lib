#!/usr/bin/env bash

#
# This postinstall script performs many of the steps necessary to install and configure
# the instructure for the DCCD web server software.
# The numbered steps match the manual installation steps described in the Github INSTALL.md file:
# https://github.com/DANS-KNAW/dccd-webui/blob/master/INSTALL.md
#
# @author Peter Brewer (p.brewer@ltrr.arizona.edu)


printf "\nCONFIGURING DCCD WEB APPLICATION BACKEND...\n\n"


#########################################
# Get passwords and other input from user
#########################################

# Helper function that asks user to define a password, then checks it with a repeat
# Call it with the name of the variable that you would like the new password stored
# inside e.g.:
#   getNewPwd mynewvariable
function getNewPwd()
{
    local __resultvar=$1
    echo "Please enter the new password:"
    read -s pwd1
    echo "Please repeat the new password:"
    read -s pwd2

    # Check both passwords match
        if [ $pwd1 != $pwd2 ]; then
           echo "Error - passwords do not match!  Please try again..."
           getNewPwd
        else
           eval $__resultvar="'$pwd1'"
        fi
}

#
# Ask the user for the passwords required for all the various users within the DCCD system
#

printf "Create new password for fedora_db_admin\n"
getNewPwd fedora_db_admin

printf "\n\nCreate new password for fedoraAdmin\n"
getNewPwd fedoraAdmin

printf "\n\nCreate new password for fedoraIntCallUser\n"
getNewPwd fedoraIntCallUser

printf "\n\nCreate new password for ldapadmin\n"
getNewPwd ldapadmin
ldapadminsha=`slappasswd -h "{SSHA}" -s "$ldapadmin"`

printf "\n\nCreate new password for dccduseradmin\n"
getNewPwd dccduseradmin
dccduseradminsha=`slappasswd -h "{SSHA}" -s "$dccduseradmin"`

printf "\n\nCreate new password for dccd_webui\n"
getNewPwd dccd_webui

printf "\n\nCreate new password for dccd_oai\n"
getNewPwd dccd_oai

printf "\n\nCreate new password for dccd_rest\n"
getNewPwd dccd_rest

printf "\n\n"
read -p "Enter email address for the system administrator: " adminEmail
printf "\n\n"


################################
# Java JDK
################################

printf "  Configuring Java environment for DCCD:\n"

# 2.2.1 Download JDK
# User should do this manually if they don't want OpenJDK

# 2.2.2 Install JDK
# User should do this manually if they don't want OpenJDK

# 2.2.3 Add the JAVA_HOME environment variable
/opt/dccd/util/java.sh 
cp /opt/dccd/util/java.sh /etc/profile.d/

# 2.2.4 Add java to alternatives
# User should do this manually if they don't want OpenJDK


################################
# Apache Tomcat
################################

printf "  Configuring Apache Tomcat environment for DCCD:\n"

# 2.4.1 Install Tomcat 6
# DONE by rpm-maven-plugin

# 2.4.2 Give the Tomcat 6 jvm more memory to work with
echo 'JAVA_OPTS="${JAVA_OPTS} -Xmx2048m -Xms2048m -server -XX:PermSize=256m -XX:MaxPermSize=256m -XX:+AggressiveHeap"' >> /etc/tomcat6/tomcat6.conf

# 2.4.3 Configure Tomcat 6 to expect UTF-8 in percent-encoded bytes
cp /etc/tomcat6/server.xml /etc/tomcat6/server.xml.bak
sed -i -e 's/redirectPort=\"8443\" \/>/redirectPort=\"8443\" URIEncoding=\"UTF-8\" \/>/' /etc/tomcat6/server.xml

# 2.4.4	Configure the Tomcat daemon to start automatically
chkconfig tomcat6 on


################################
# Apache HTTP Server
################################

printf "  Configuring Apache HTTP Server for DCCD:\n"

# 2.5.1 Install Apache HTTP Server
# DONE by rpm-maven-plugin

# 2.5.2 Configure it so it serves as a tomcat proxy
# DONE by rpm-maven-plugin - (apache conf file is stored in src/java/resources/dccd.conf)

# 2.5.3 Set up IPTables
iptables -A INPUT -p tcp -m tcp --dport 80 -j ACCEPT
service iptables save
apachectl -k graceful



################################
# PostGreSQL
################################

printf "  Configuring PostGreSQL for DCCD:\n"

# 2.6.1 Install PostGreSQL
# DONE by rpm-maven-plugin

# 2.6.2 Initialize the database
service postgresql initdb

# 2.6.3 Configure auto-vacuum (optional)
cp /var/lib/pgsql/data/postgresql.conf /var/lib/pgsql/data/postgresql.conf.bak
sed -i -e 's/#track_counts = on/track_counts = on/' /var/lib/pgsql/data/postgresql.conf
sed -i -e 's/#autovacuum = on/autovacuum = on/' /var/lib/pgsql/data/postgresql.conf

# 2.6.4 Configure database to accept user/password credentials
# User should do this step manually after installation

# 2.6.5	Start the daemon
chkconfig postgresql on
service postgresql start



################################
# OpenLDAP
################################

printf "  Configuring OpenLDAP for DCCD:\n"

# 2.7.1 Install OpenLDAP servers and clients
# DONE by rpm-maven-plugin

# 2.7.2 Remove the “default” OpenLDAP database (optional)
# rm /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif

# 2.7.3 Start the OpenLDAP daemon
chkconfig slapd on
service slapd start



################################
# DCCD Fedora Commons Repository
################################

printf "  Configuring Fedora Commons Repository for DCCD:\n"

# 3.1.1 Create a database for Fedora Commons in PostGreSQL
su - postgres -c "psql -U postgres < /opt/dccd/dccd-fedora-commons-repository/create-fedora-db.sql"

# 3.1.2 Set the fedora_db_admin password
su - postgres -c "psql -U postgres -d postgres -c \"alter user fedora_db_admin with password '$fedora_db_admin';\""

# 3.1.3 Set the FEDORA_HOME environment variable
cp /opt/dccd/dccd-fedora-commons-repository/fedora.sh /etc/profile.d/
/opt/dccd/dccd-fedora-commons-repository/fedora.sh

# 3.1.4 Run the Fedora Commons installer
printf "Downloading Fedora Commons v3.5 installer.  This may take some time...\n";
wget -O /opt/dccd/dccd-fedora-commons-repository/fcrepo-installer-3.5.jar http://sourceforge.net/projects/fedora-commons/files/fedora/3.5/fcrepo-installer-3.5.jar/download
cp /opt/dccd/dccd-fedora-commons-repository/install.properties /opt/dccd/dccd-fedora-commons-repository/install.properties.2
sed -i -e 's/database.password=/database.password=$fedora_db_admin/' /opt/dccd/dccd-fedora-commons-repository/install.properties.2
sed -i -e 's/fedora.admin.pass=/fedora.admin.pass=$fedoraAdmin/' /opt/dccd/dccd-fedora-commons-repository/install.properties.2
java -jar /opt/dccd/dccd-fedora-commons-repository/fcrepo-installer-3.5.jar /opt/dccd/dccd-fedora-commons-repository/install.properties.2
rm /opt/dccd/dccd-fedora-commons-repository/install.properties.2
chown -R tomcat:tomcat /opt/fedora-3.5

# 3.1.5 Create a symbolic link to the fedora installation
ln -s /opt/fedora-3.5 /opt/fedora

# 3.1.6 Create and configure location of data store and resource index
mkdir -p /data/fedora/objects 
mkdir -p /data/fedora/datastreams
mkdir -p /data/fedora/fedora-xacml-policies/repository-policies/default
mkdir -p /data/fedora/resourceIndex
chown -R tomcat:tomcat /data/fedora
cp /opt/fedora/server/config/fedora.fcfg /opt/fedora/server/config/fedora.fcfg.bak
sed -i -e 's/data\/objects/\/data\/fedora\/objects/' /opt/fedora/server/config/fedora.fcfg
sed -i -e 's/data\/datastreams/\/data\/fedora\/datastreams/' /opt/fedora/server/config/fedora.fcfg
sed -i -e 's/data\/resourceIndex/\/data\/fedora\/resourceIndex/' /opt/fedora/server/config/fedora.fcfg

# 3.1.7 Add Fedora Commons users
cp /opt/dccd/dccd-fedora-commons-repository/fedora-users.xml /opt/fedora/server/config/
sed -i -e 's/password:fedoraAdmin/$fedoraAdmin/' /opt/fedora/server/config/fedora-users.xml
sed -i -e 's/password:dccd_webui/$dccd_webui/' /opt/fedora/server/config/fedora-users.xml
sed -i -e 's/password:dccd_rest/$dccd_rest/' /opt/fedora/server/config/fedora-users.xml
sed -i -e 's/password:dccd_oai/$dccd_oai/' /opt/fedora/server/config/fedora-users.xml
sed -i -e 's/password:fedoraIntCallUser/$fedoraIntCallUser/' /opt/fedora/server/config/fedora-users.xml

# 3.1.8 Change password of fedoraIntCallUser
sed -i -e 's/changeme/$fedoraIntCallUser/' /opt/fedora/server/config/beSecurity.xml

# 3.1.9 Limit access to passwords
chmod 0600 /opt/fedora/server/config/fedora-users.xml
chmod 0600 /opt/fedora/server/config/fedora.fcfg
chmod 0600 /opt/fedora/server/config/beSecurity.xml

# 3.1.10 Ensure that Fedora "upload" directory has enough disk space
# This optional step should be done by users if necessary

# 3.1.11 Deploy Saxon (Optional)
# Not necessary for DCCD

# 3.1.12 Start Tomcat 6
service tomcat6 start


################################
# DCCD LDAP Directory 
################################

printf "  Configuring LDAP environment for DCCD:\n"

# 3.2.1 Create a separate directory folder for DCCD
mkdir /var/lib/ldap/dccd
chown ldap:ldap /var/lib/ldap/dccd

# 3.2.2 Add DANS and DCCD schemas
ldapadd -v -Y EXTERNAL -H ldapi:/// -f /opt/dccd/ldap/dans-schema.ldif

# 3.2.3 Add DCCD database
ldapadd -v -Y EXTERNAL -H ldapi:/// -f /opt/dccd/ldap/dccd-db.ldif

# 3.2.5 Change the ldapadmin password
sed -i -e "s?CHANGEME?$ldapadminsha?" /opt/dccd/ldap/change-ldapadmin-pw.ldif
ldapadd -v -Y EXTERNAL -H ldapi:/// -f /opt/dccd/ldap/change-ldapadmin-pw.ldif

# 3.2.4 Add basic entries to the DCCD database
sed -i -e 's/FILL.IN.YOUR@VALID-EMAIL/$adminEmail/' /opt/dccd/ldap/dccd-basis.ldif
ldapadd -w $ldapadminsha -D cn=ldapadmin,dc=dans,dc=knaw,dc=nl -f /opt/dccd/ldap/dccd-basis.ldif

# 3.2.6 Change the dccdadmin user’s application password
sed -i -e "s?CHANGEME?$dccduseradminsha?" /opt/dccd/ldap/change-dccdadmin-user-pw.ldif
ldapadd -w $ldapadminsha -D cn=ldapadmin,dc=dans,dc=knaw,dc=nl -f /opt/dccd/ldap/change-dccdadmin-user-pw.ldif

################################
# DCCD SOLR Search Index 
################################

printf "  Configuring Aoache SOLR environment for DCCD:\n"

# 3.5.1 Install Apache SOLR 3.5
cd /opt/dccd/ 
wget http://archive.apache.org/dist/lucene/solr/3.5.0/apache-solr-3.5.0.tgz
tar -xzf apache-solr-3.5.0.tgz -C /opt
rm /opt/dccd/apache-solr-3.5.0.tgz
chown -R tomcat:tomcat /opt/apache-solr-3.5.0

# 3.5.2 Create a symbolic link to the SOLR installation and war
ln -s /opt/apache-solr-3.5.0 /opt/apache-solr
ln -s /opt/apache-solr-3.5.0/dist/apache-solr-3.5.0.war /opt/apache-solr/solr.war

# 3.5.3 Create and the solr.home-directory
mkdir -p /data/solr/cores/dendro/data
mkdir -p /data/solr/cores/dendro/conf
cp /opt/dccd/solr/config-all/solr.xml /data/solr
cp /opt/dccd/solr/protwords.txt /data/solr/cores/dendro/conf
cp /opt/dccd/solr/schema.xml /data/solr/cores/dendro/conf
cp /opt/dccd/solr/solrconfig.xml /data/solr/cores/dendro/conf
cp /opt/dccd/solr/stopwords.txt /data/solr/cores/dendro/conf
cp /opt/dccd/solr/synonyms.txt /data/solr/cores/dendro/conf
chown -R tomcat:tomcat /data/solr

# 3.5.4 Copy the Tomcat 6 context container
# n.b. don’t confuse this solr.xml with the file from the previous step with the same name
cp /opt/dccd/solr/config-tomcat/solr.xml /etc/tomcat6/Catalina/localhost


################################
# DCCD Web frontend 
################################



################################
# DCCD RESTful interface
################################



################################
# DCCD OAI Module 
################################


printf "\n\nDCCD backend configuration complete!\n\n";