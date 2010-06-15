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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class InputCollector
{

	private InputStream is;
	private BufferedReader br;
	private ICWorker icw;
	private List<String> lines = new LinkedList<String>();
	private RuntimeException fail = null;
	private CountDownLatch latch = new CountDownLatch(1);

	public InputCollector(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
		icw = new ICWorker();
		icw.setName("InputCollector-" + icw.getId());
		icw.start();
	}

	public List<String> getLines() {
		if (fail != null){
			throw fail;
		}
		try {
			latch.await();
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
		return Collections.unmodifiableList(lines);
	}

	private class ICWorker extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String line = null;
				while ((line = br.readLine()) != null)
				{
					lines.add(line);
				}
			} catch (IOException ex) {
				fail = new RuntimeException("Failed to read" +
				  " lines from input: " + ex.getMessage(), ex);
			} finally {
				Utils.closeQuietly(br);
				latch.countDown();
			}
		}
	}
}

