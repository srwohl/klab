/*
 * This file is part of k.LAB.
 * 
 * k.LAB is free software: you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * A copy of the GNU Affero General Public License is distributed in the root
 * directory of the k.LAB distribution (LICENSE.txt). If this cannot be found 
 * see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2018 integratedmodelling.org and any authors mentioned
 * in author tags. All rights reserved.
 */
package org.integratedmodelling.klab.exceptions;

// TODO: Auto-generated Javadoc
/**
 * The Class KlabInternalErrorException.
 *
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public class KlabInternalErrorException extends KlabException {

    private static final long serialVersionUID = 461213337593957416L;

    /**
     * Instantiates a new klab internal error exception.
     */
    public KlabInternalErrorException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new klab internal error exception.
     *
     * @param arg0 the arg 0
     */
    public KlabInternalErrorException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new klab internal error exception.
     *
     * @param e the e
     */
    public KlabInternalErrorException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Instantiates a new klab internal error exception.
     * @param message the message
     * @param cause the cause
     */
    public KlabInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
