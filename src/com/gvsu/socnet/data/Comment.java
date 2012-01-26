/** Comment.java */
package com.gvsu.socnet.data;

import android.widget.ImageView;

/****************************************************************
 * data.Comment
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class Comment {

//	private ImageView pic;
	private String user;
	private String body;

	public Comment(/*ImageView pic,*/ String user, String body) {
//		this.pic = pic;
		this.user = user;
		this.body = body;
	}
	
	@Override
	public String toString(){
		return user+":\n\t"+body;
	}

}
