# Listmaker: Mailman List Population from LDAP

We started out with direct LDAP integration into mailman for a mailing list
with some 26000 entries but it was problematic at best -- the lookups take
quite a while and seemed to make the mailman web interface unresponsive.

Listmaker will pull a list of objects from an LDAP directory (using a custom
filter) and compare this list against the current membership for a given static
list in mailman.  The mailman CLI will be used to incrementally update the
static list's membership based on this snapshot of the contents of the LDAP
directory.

Configuration is relatively straightforward -- take the example.properties file
and configure up as many lists/directories/etc as you like.  For each list, set
up a cron job thus:

     ./listmaker -p ~/props.file list-name
                                ^-- as it appears in "props.file"

NB: You'll need to ensure that whatever bind account you use has no maximum
search limit -- for now listmaker uses no paging of any kind as none was 
available in the target LDAP directory.

Patches welcome!
