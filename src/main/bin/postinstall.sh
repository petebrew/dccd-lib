#!/usr/bin/env bash

#
# This postinstall script performs many of the steps necessary to install and configure
# the instructure for the DCCD web server software.
# The numbered steps match the manual installation steps described in the Github INSTALL.md file:
# https://github.com/DANS-KNAW/dccd-webui/blob/master/INSTALL.md
#
# @author Peter Brewer (p.brewer@ltrr.arizona.edu)



################################
# Java JDK
################################

printf "  Configuring Java environment for DCCD:\n"

# 2.2.1 Download JDK
# User should do this manually if they don't want OpenJDK

# 2.2.2 Install JDK
# User should do this manually if they don't want OpenJDK

# 2.2.3 Add the JAVA_HOME environment variable
/usr/share/dccd/util/java.sh 
cp /usr/share/dccd/util/java.sh /etc/profile.d/

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
rm /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif

# 2.7.3 Start the OpenLDAP daemon
chkconfig slapd on
service slapd start



################################
# DCCD Fedora Commons Repository
################################

printf "  Configuring Fedora Commons Repository for DCCD:\n"

# 3.1.1 Create a database for Fedora Commons in PostGreSQL
su - postgres -c "psql -U postgres < /usr/share/dccd/dccd-fedora-commons-repository/create-fedora-db.sql"

# 3.1.2 Set the fedora_db_admin password
read -s -p "Enter password for fedora_db_admin: " fedora_db_admin
su - postgres -c "psql -U postgres -d postgres -c \"alter user fedora_db_admin with password '$fedora_db_admin';\""

# 3.1.3 Set the FEDORA_HOME environment variable
cp /usr/share/dccd/dccd-fedora-commons-repository/fedora.sh /etc/profile.d/
/usr/share/dccd/dccd-fedora-commons-repository/fedora.sh

# 3.1.4 Run the Fedora Commons installer
cp /usr/share/dccd/dccd-fedora-commons-repository/install.properties /usr/share/dccd/dccd-fedora-commons-repository/install.properties.2
sed -i -e 's/database.password=/database.password=$fedora_db_admin/' /usr/share/dccd/dccd-fedora-commons-repository/install.properties.2
read -s -p "Enter password for fedoraAdmin: " fedoraAdmin
sed -i -e 's/fedora.admin.pass=/fedora.admin.pass=$fedoraAdmin/' /usr/share/dccd/dccd-fedora-commons-repository/install.properties.2
java -jar /usr/share/dccd/fedora-repo/fcrepo-installer-3.5.jar /usr/share/dccd/dccd-fedora-commons-repository/install.properties.2
rm /usr/share/dccd/dccd-fedora-commons-repository/install.properties.2
chown -R tomcat:tomcat /opt/fedora-3.5

# 3.1.5 Create a symbolic link to the fedora installation
ln -s /opt/fedora-3.5 /opt/fedora

# 3.1.6 Create and configure location of data store and resource index
#mkdir -p /data/fedora/objects 
#mkdir -p /data/fedora/datastreams
#mkdir -p /data/fedora/fedora-xacml-policies/repository-policies/default
#mkdir -p /data/fedora/resourceIndex
#chown -R tomcat:tomcat /data/fedora
# TODO


# 3.1.7 Add Fedora Commons users


# 3.1.8 Change password of fedoraIntCallUser



# 3.1.9 Limit access to passwords


# 3.1.10 Ensure that Fedora "upload" directory has enough disk space


# 3.1.11 Deploy Saxon (Optional)
# Not necessary for DCCD


# 3.1.12 Start Tomcat 6
service tomcat6 start


