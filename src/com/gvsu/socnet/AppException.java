/** AppException.java */
package com.gvsu.socnet;

/****************************************************************
 * com.ciscomputingclub.silencer.AppException
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/

public class AppException extends Exception {

	/** long serialVersionUID */
	private static final long serialVersionUID = 1L;
	public String msg = null;

	public AppException(String msg) {
		super(msg);
		this.msg = msg;
	}

	@Override
	public String toString() {
		return msg;
	}
}
