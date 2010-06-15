/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MailmanList {
	private String name;
	private String bindir;
	private ProcessBuilder pbListMembers;
	private ProcessBuilder pbAddMembers;
	private ProcessBuilder pbRemoveMembers;

	public class Result {
		private Set<String> failed = new HashSet<String>();
		private Set<String> succeeded = new HashSet<String>();
		public Set<String> getFailed() {
			return Collections.unmodifiableSet(failed);
		}
		public Set<String> getSucceeded() {
			return Collections.unmodifiableSet(succeeded);
		}
	}

	public MailmanList(String bindir, String name) {
		if (name == null || name.length() < 1) {
			throw new IllegalArgumentException("Must provide " +
			  " a non-empty listname");
		}
		this.name = name;
		this.bindir = bindir;
		pbListMembers = new ProcessBuilder(
			bindir + "/list_members", name)
			.directory(new File(bindir));
		pbAddMembers = new ProcessBuilder(bindir + "/add_members",
		  "-r", "-", "-w", "n", "-a", "n", name).
		  directory(new File(bindir));
		pbRemoveMembers = new ProcessBuilder(bindir + "/remove_members",
		  "-f", "-", "-n", "-N", name).directory(new File(bindir));
	}

	public Set<String> getListMembers() {
		Process p = null;
		try {
			p = pbListMembers.start();
			InputCollector stderr = new InputCollector(
			  p.getErrorStream());
			InputCollector stdout = new InputCollector(
			  p.getInputStream());
			int rc = p.waitFor();
			if (rc != 0) {
				throw new RuntimeException("Could not get " +
				  "list of members: " + 
				  stderr.getLines().get(0));
			}
			Set<String> members = 
			  new HashSet<String>(stdout.getLines());
			for (String m: stdout.getLines()) {
				if (m != null && m.contains("@"))
					members.add(m.trim().toLowerCase());
			}
			return members;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (p != null) {
					p.destroy();
				}
			} catch (Throwable t) {}
		}
	}

	public Result addMembers(String... members) {
		Set<String> ms = new HashSet<String>();
		for (String m: members) {
			ms.add(m);
		}
		return addMembers(ms);
	}

	public Result addMembers(Set<String> members) {
		Process p = null;
		Result r = new Result();
		try {
			p = pbAddMembers.start();
			InputCollector stderr = new InputCollector(
			  p.getErrorStream());
			InputCollector stdout = new InputCollector(
			  p.getInputStream());
			PrintWriter stdin = new PrintWriter(
			  p.getOutputStream());
			for (String m: members) {
				stdin.println(m);
			}
			Utils.closeQuietly(stdin);
			Set<String> toAdd = new HashSet<String>(members);

			int rc = p.waitFor();
			if (rc != 0) {
				throw new RuntimeException("Could not add " +
				  "members: " + stderr.getLines().get(0));
			}
			
			int ok = 0;
			for (String l: stdout.getLines()) {
				String[] la = l.split(":", 2);
				if (la.length != 2 || la[1] == null ||
				  la[1].length() < 1) {
					// ignore malformed output line
				} else {
					String a = la[0].trim();
					String b = la[1].trim();
					if (a.equalsIgnoreCase("subscribed")) {
						toAdd.remove(b);
						r.succeeded.add(b);
					} else if (a.equalsIgnoreCase(
					  "bad/invalid email address")) {
						r.failed.add(b);
					} else if (a.equalsIgnoreCase(
					  "already a member")) {
						toAdd.remove(b);
						r.succeeded.add(b);
					} else {
						r.failed.add(b);
						// unknown message, so 
						//    assume failure
					}
				}
			}
			r.failed.addAll(toAdd); // if not explicitly mentioned
			//  then it probably failed
			return r;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (p != null) {
					p.destroy();
				}
			} catch (Throwable t) {}
		}
	}

	public Result removeMembers(String... members) {
		Set<String> ms = new HashSet<String>();
		for (String m: members) {
			ms.add(m);
		}
		return removeMembers(ms);
	}

	public Result removeMembers(Set<String> members) {
		Process p = null;
		Result r = new Result();
		try {
			p = pbRemoveMembers.start();
			InputCollector stderr = new InputCollector(
			  p.getErrorStream());
			InputCollector stdout = new InputCollector(
			  p.getInputStream());
			PrintWriter stdin = new PrintWriter(
			  p.getOutputStream());
			for (String m: members) {
				stdin.println(m);
			}
			Utils.closeQuietly(stdin);
			r.succeeded.addAll(members);
			    // ^^ if we don't hear about it, it went OK

			int rc = p.waitFor();
			if (rc != 0) {
				throw new RuntimeException("Could not " +
				  "remove members: " + 
				  stderr.getLines().get(0));
			}

			int ok = 0;
			for (String l: stdout.getLines()) {
				String[] la = l.split(":", 2);
				if (la.length != 2 || la[1] == null ||
				  la[1].length() < 1) {
					// ignore malformed output line
				} else {
					String a = la[0].trim();
					String b = la[1].trim();
					if (a.equalsIgnoreCase(
					  "no such member")) {
						// probably OK, ignore
					} else {
						// Unknown message
						//   so assume failure
						r.succeeded.remove(b);
						r.failed.add(b);
					}
				}
			}
			return r;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (p != null) {
					p.destroy();
				}
			} catch (Throwable t) {}
		}
	}
}

