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

/**
 * A mailbox process that runs as a Java thread.  Messages are received from
 * Erlang and dispatched to a JavaViewServer instance
 * 
 * @author boorad
 *
 */
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
    		server.setName(this.name);
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	boolean running = true;
        while( running ) {
            try {
            	o = mbox.receive();
                //System.out.println("raw msg: " + o);
                
                if( o instanceof OtpErlangTuple ) {
                    // process message
                    msg = (OtpErlangTuple)o;
                    req = (OtpErlangAtom)( msg.elementAt(0) );
                    from = (OtpErlangPid)( msg.elementAt(1) );
                    
                    // link to calling pid so this mbox dies if pid dies	
                    //mbox.link(from);

                    if( req.atomValue().equals("prompt")) {
                        data = (OtpErlangList)( msg.elementAt(2) );
                    	// craft response                    	
                    	resp = new OtpErlangString(server.prompt(data));
	                    mbox.send( from, response(resp) );
                    } else if( req.atomValue().equals("stop")) {
                    	mbox.send( from, new OtpErlangAtom("stop") );
                    	running = false;
                    } else {
                    	System.out.println("req: " + req.atomValue());
                    }
                }
            } catch( OtpErlangExit exit ) {
            	mbox.close();
            	break;
            } catch( Exception e ) {
            	System.out.println("Exception in JVSMboxThread.run");
                e.printStackTrace();
                break;
            }
        }
        System.out.println("mailbox thread closing: " + this.name);
    }
    
    private OtpErlangTuple response(OtpErlangString resp) {
    	OtpErlangObject[] arr = new OtpErlangObject[2];
    	arr[0] = new OtpErlangString(this.name);
    	arr[1] = resp;
    	return new OtpErlangTuple(arr);
    }

}
