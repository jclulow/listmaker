/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputDiscarder
{

	private InputStream is;
	private BufferedReader br;
	private IDWorker idw;

	public InputDiscarder(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
		idw = new IDWorker();
		idw.setName("InputDiscarder-" + idw.getId());
		idw.start();
	}

	private class IDWorker extends Thread
	{

		@Override
		@SuppressWarnings("empty-statement")
		public void run()
		{
			try
			{
				String line = null;
				while ((line = br.readLine()) != null);
			} catch (IOException ex) {
			} finally {
				Utils.closeQuietly(br);
			}
		}
	}
}

