/** BannerException.java */
package com.gvsu.socnet.data;

/****************************************************************
 * com.ciscomputingclub.silencer.BannerException
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/

public class AsyncException extends Exception
{

    /** long serialVersionUID */
    private static final long serialVersionUID = 1L;
    public String msg = null;

    public AsyncException(String msg)
    {
        super(msg);
        this.msg = msg;
    }

    @Override
    public String toString()
    {
        return msg;
    }
}
