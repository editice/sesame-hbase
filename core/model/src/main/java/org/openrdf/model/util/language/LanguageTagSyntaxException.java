/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * * $Id: LanguageTagSyntaxException.java,v 1.7 2008/01/02 12:10:07 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
 */
/*
 * LanguageTagSyntaxException.java
 *
 * Created on July 25, 2001, 9:32 AM
 */

package org.openrdf.model.util.language;

/**
 * A LanguageTag did not conform to RFC3066. This exception is for the syntactic
 * rules of RFC3066 section 2.1.
 * 
 * @author jjc
 */
public class LanguageTagSyntaxException extends java.lang.Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5425207434895448094L;

	/**
	 * Constructs an <code>LanguageTagSyntaxException</code> with the specified
	 * detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	LanguageTagSyntaxException(String msg) {
		super(msg);
	}
}
