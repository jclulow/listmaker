/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListMemberFetcher {

	private LDAPConnection ldap;
	private String hostname;
	private int port;
	private String bindDN;
	private String password;
	private int errorCount = 0;

	public ListMemberFetcher(String hostname, int port, String bindDN, 
	  String password) {
		this.hostname = hostname;
		this.port = port;
		this.bindDN = bindDN;
		this.password = password;
		this.ldap = new LDAPConnection();
		connect();
	}

	public void close() {
		ldap.close();
	}

	// XXX - this is crap, just remove it...
	private void connect() {
		for (;;) {
			try {
				if (!ldap.isConnected()) {
					ldap.connect(hostname, port);
					ldap.bind(bindDN, password);
				}
				errorCount = 0;
				return;
			} catch (LDAPException ldape) {
				if (++errorCount >= 10)
					throw new RuntimeException("Could " +
					  "not connect to LDAP server, " +
					  " failed 10 times: " + 
					  ldape.getMessage(), ldape);
				else {
					System.err.println("LDAP ERROR: " +
					  ldape.getMessage());
				}
				sleep(500);
			}
		}
	}
	
	public Set<String> getAddresses(String baseDn, String filter) {
		Set<String> result = new HashSet<String>();
		try {
			SearchRequest sr = new SearchRequest(baseDn, 
			  SearchScope.SUB, filter, "mail");
			SearchResult res = ldap.search(sr);
			List<SearchResultEntry> lll = res.getSearchEntries();
			for (SearchResultEntry sre: lll) {
				String mail = sre.getAttribute("mail").
				  getValue();
				if (mail != null && mail.contains("@"))
					result.add(mail.trim().toLowerCase());
			}
			return result;
		} catch (LDAPException ex) {
			throw new RuntimeException("Could not fetch all" +
			  " matching e-mail addresses.", ex);
		}
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException i) {
		}
	}
}

