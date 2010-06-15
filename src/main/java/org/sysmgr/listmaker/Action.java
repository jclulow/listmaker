/*
 * Copyright (c) 2010, Joshua M. Clulow. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

package org.sysmgr.listmaker;

import java.io.Closeable;

public interface Action extends Closeable {

	public void doAction();
	public void close();
}

