/*
 * Copyright (c) 2014-2015, Argonne National Laboratory.
 *
 * SVN Information:
 *   $HeadURL$
 *   $Date$
 *   $Revision$
 *   $Author$
 */
package gov.anl.aps.cdb.common.exceptions;

import gov.anl.aps.cdb.common.constants.CdbStatus;

/**
 * Invalid session exception.
 */
public class InvalidSession extends CdbException {

    /**
     * Default constructor.
     */
    public InvalidSession() {
        super();
        setErrorCode(CdbStatus.CDB_INVALID_SESSION);
    }

    /**
     * Constructor using error message.
     *
     * @param message error message
     */
    public InvalidSession(String message) {
        super(message);
        setErrorCode(CdbStatus.CDB_INVALID_SESSION);
    }

    /**
     * Constructor using throwable object.
     *
     * @param throwable throwable object
     */
    public InvalidSession(Throwable throwable) {
        super(throwable);
        setErrorCode(CdbStatus.CDB_INVALID_SESSION);
    }

    /**
     * Constructor using error message and throwable object.
     *
     * @param message error message
     * @param throwable throwable object
     */
    public InvalidSession(String message, Throwable throwable) {
        super(message, throwable);
        setErrorCode(CdbStatus.CDB_INVALID_SESSION);
    }

}
