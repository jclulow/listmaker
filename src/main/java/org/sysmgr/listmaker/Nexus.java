/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.util.Properties;

public class Nexus {

	private ListMemberFetcher lmf;
	private Properties config;

	public String ps(String name) {
		String val = config.getProperty(name);
		if (val == null)
			throw new RuntimeException("Must specify a value for '"
			  + name + "' in properties file.");
		return val;
	}

	public int pi(String name) {
		try {
			return Integer.parseInt(ps(name));
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Must specify a valid" +
			  " integer value for '" + name + 
			  "' in properties file.");
		}
	}

	public ListMemberFetcher getListMemberFetcher() {
		if (lmf == null) {
			lmf = new ListMemberFetcher(
			  ps("ldap.hostname"),
			  pi("ldap.port"),
			  ps("ldap.binddn"),
			  ps("ldap.bindpw"));
		}
		return lmf;
	}

	public MailmanList getMailmanList(String listName) {
		return new MailmanList(ps("mailman.bindir"),
		  listName);
	}

	public Nexus(Properties config) {
		this.config = config;
	}
}

