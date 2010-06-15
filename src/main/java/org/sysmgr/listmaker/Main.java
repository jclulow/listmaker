/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

	private static void printUsage() {
		Properties p = Utils.loadPropertiesFromClasspath(
		  "maven-app.properties");
		System.err.println("Usage: " + p.getProperty("pom.name") +
		  " -p <config.properties> <listname>");
		System.err.println("Version: " + p.getProperty("pom.version"));
	}

	public static void main(String[] argv)
	  throws IOException, ParseException {
		Options o = new Options();
		o.addOption("p", "props", true, 
		  "Name of configuration properties file");

		CommandLineParser clp = new GnuParser();
		CommandLine cl = clp.parse(o, argv);

		// Load properties file
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(
			  cl.getOptionValue("props")));
		} catch (Throwable t) {
			printUsage();
			System.err.println("ERROR: Could not load" + 
			  " properties file: " + t.getMessage());
			System.exit(2);
			return;
		}

		// Parse method
		Nexus n = new Nexus(config);
		Action a = null;
		if (cl.getArgList() == null || cl.getArgList().size() < 1) {
			System.err.println("ERROR: Must specify a list name!");
		} else {
			a = new SyncAction(n, (String) cl.getArgList().get(0));
		}

		if (a == null) {
			printUsage();
			System.exit(1);
			return;
		}		
		a.doAction();
		a.close();
	}

	private static boolean isCommand(CommandLine cl, String match) {
		return ((String) cl.getArgList().get(0)).trim().
			equalsIgnoreCase(match);
	}
}

