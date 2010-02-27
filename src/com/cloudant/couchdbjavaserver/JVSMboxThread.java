package com.cloudant.couchdbjavaserver;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;


public class JVSMboxThread implements Runnable {

	private OtpNode node;
	private String name = "";
	private JavaViewServer server;
	
	private OtpErlangObject o;
	private OtpErlangTuple msg;
	private OtpErlangPid from;
	private OtpErlangAtom req;
	private OtpErlangList data;
	private OtpErlangString resp;

	
    public JVSMboxThread(OtpNode node, String name) {
    	this.node = node;
    	this.name = name;
    	this.server = new JavaViewServer();
    }


    public void run() {
    	OtpMbox mbox = null;
    	try {
    		mbox = node.createMbox(this.name);
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}

        while( true ) {
            try {
            	o = mbox.receive();
                System.out.println("raw msg: " + o);
                
                if( o instanceof OtpErlangTuple ) {
                    // process message
                    msg = (OtpErlangTuple)o;
                    req = (OtpErlangAtom)( msg.elementAt(0) );
                    data = (OtpErlangList)( msg.elementAt(1) );
                    from = (OtpErlangPid)( msg.elementAt(2) );
                    
                    // link to calling pid so this mbox dies if pid dies	
                    mbox.link(from);

                    if( req.atomValue().equals("prompt")) {
                    	// craft response                    	
                    	resp = new OtpErlangString(server.prompt(data));
	                    mbox.send( from, resp );
                    }
                }
            } catch( OtpErlangExit exit ) {
            	System.out.println("mailbox thread closing... later, scro");
            	mbox.close();
            } catch( Exception e ) {
            	System.out.println("Exception in JVSMboxThread.run");
                e.printStackTrace();
                break;
            }
        }
    }
}
