/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.util.Set;

public class SyncAction implements Action
{
	private Nexus n;
	private String listname;

	public SyncAction(Nexus n, String listname) {
		this.listname = listname;
		this.n = n;
	}

	public void doAction() {
		MailmanList ml = n.getMailmanList(listname);

		Set<String> inMailMan = ml.getListMembers();
		Set<String> inLDAP = n.getListMemberFetcher().getAddresses(
			n.ps("list." + listname + ".basedn"),
			n.ps("list." + listname + ".filter"));

		SetDiff<String> diff = new SetDiff<String>(inMailMan, inLDAP);

		MailmanList.Result radd = ml.addMembers(
		  diff.getRightOnlyEntries());
		for (String ss: radd.getSucceeded())
			System.out.println("+" + ss);

		MailmanList.Result rrem = ml.removeMembers(
		  diff.getLeftOnlyEntries());
		for (String ss: rrem.getSucceeded())
			System.out.println("-" + ss);

		for (String ss: radd.getFailed())
			System.err.println("ERROR: Could not add    " + ss);
		for (String ss: rrem.getFailed())
			System.err.println("ERROR: Could not remove " + ss);
		
	}

	public void close() {
	}

}

