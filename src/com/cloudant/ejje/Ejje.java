package com.cloudant.ejje;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpNode;

/**
 * Ejje class is the main() entry point for a native Java View Server for
 * CouchDB.  The while loop establishes the 'ejje_main' registered mailbox
 * for this Java 'node' and Erlang can send messages.
 * 
 * The main purpose of this class is to accept 'mbox' requests from Erlang for
 * new mailboxes and spawn a thread (in a thread pool) for each request.  Each
 * mailbox thread is a JVSMboxThread object, executing its run() method.
 * 
 * The 'list' message returns a list of the active mbox threads.
 * 
 * 
 * @author boorad
 *
 */
public class Ejje {

    // set up mbox thread pool
    //  optimize to suit later (FixedThreadPool, etc)
    static ExecutorService threadExecutor = Executors.newCachedThreadPool();
    //static ExecutorService threadExecutor = Executors.newFixedThreadPool(24);

    /**
     * main entrypoint
     */
    public static void main( String[] args ) {

    	boolean running = true;
    	
        try {
        	
            OtpNode node = null;
        	if (args.length == 1) {
        		node = new OtpNode(args[0]);
        	} else if (args.length == 2) {
        		node = new OtpNode(args[0], args[1]);      		
        	} else {
        		System.out.println("com.cloudant.ejje.Ejje args: <nodename> <cookie>(optional)");
        		System.exit(0);
        	}
            OtpMbox mbox = node.createMbox("ejje_main");

            OtpErlangObject o;
            OtpErlangTuple msg;
            OtpErlangPid from;
            OtpErlangAtom req;
            OtpErlangString data;
            OtpErlangString ref;
            OtpErlangTuple resp;
            // set up authentication for url class loading
            if (System.getProperty("dbcore.user") != null && System.getProperty("dbcore.password") != null) {
            	Authenticator.setDefault (new Authenticator() {
            		protected PasswordAuthentication getPasswordAuthentication() {
            			return new PasswordAuthentication (System.getProperty("dbcore.user"), System.getProperty("dbcore.password").toCharArray());
            		}
            	});
            }

            while( running ) {
                try {
                    o = mbox.receive();
                    if( o instanceof OtpErlangTuple ) {
                        msg = (OtpErlangTuple)o;
                        req = (OtpErlangAtom)( msg.elementAt(0) );
                        from = (OtpErlangPid)( msg.elementAt(1) );
                        if( req.atomValue().equals("mbox") ) {
                            data = (OtpErlangString)( msg.elementAt(2) );
                            ref = (OtpErlangString)( msg.elementAt(3) );
                            resp = new_mbox(node, data, ref);
                        } else if( req.atomValue().equals("list") ) {
                        	resp = list_mailboxes(node);
                        } else {
                            resp = error_tuple("bad request: "
                                               + req.atomValue());
                        }
                        if( resp != null )
                        	mbox.send( from, resp );
                    }
                } catch( Exception e ) {
                    System.out.println("" + e);
                    break;
                }
            }
        } catch( IOException ioe ) {
            System.out.println("" + ioe);
        }
    }

    /**
     * generate an Erlang error tuple to send back
     * @param err
     * @return
     */
    static OtpErlangTuple error_tuple(String err) {
        OtpErlangObject ret[] = new OtpErlangObject[2];
        ret[0] = new OtpErlangAtom("error");
        ret[1] = new OtpErlangString(err);
        return new OtpErlangTuple(ret);
    }

    /**
     * return a list of active mailboxes
     * @param node
     * @return
     */
    static OtpErlangTuple list_mailboxes(OtpNode node) {
        OtpErlangObject ret[] = new OtpErlangObject[2];
        ret[0] = new OtpErlangAtom("ok");
        String[] names = node.getNames();
        OtpErlangObject[] nodes = new OtpErlangObject[names.length];
        for( int i=0; i < names.length; i++ ) {
        	nodes[i] = new OtpErlangString(names[i]);
        }
        ret[1] = new OtpErlangList(nodes);
        return new OtpErlangTuple(ret);
    }

    /**
     * start a new mbox thread and return an Erlang tuple with the thread's name
     * @param node
     * @param clazz
     * @param ref
     * @return
     */
    @SuppressWarnings("unchecked")
    static OtpErlangTuple new_mbox(OtpNode node, OtpErlangString clazz,
    		OtpErlangString ref) {
    	
    	String classname = clazz.stringValue();
    	String name = ref.stringValue();
    	Runnable thread = null;

        // instantiate a new mbox thread and run it
    	try {
        	ClassLoader loader = ClassLoader.getSystemClassLoader();
        	Class<ViewServer> cl =
        		(Class<ViewServer>)loader.loadClass(classname);
        	ViewServer vs = (ViewServer)cl.newInstance();
        	thread = vs.getMboxThread(node, name);
        } catch( IllegalAccessException iae) {
        	System.out.println("iae " + iae);
        } catch( InstantiationException ie) {
        	System.out.println("ie " + ie);
        } catch( ClassNotFoundException cnfe) {
        	System.out.println("cnfe " + cnfe);
        } catch( Exception e ) {
        	System.out.println("e " + e);
        }
        
        threadExecutor.execute(thread);

        // reply with the mbox's registered name
        OtpErlangObject ret[] = new OtpErlangObject[2];
        ret[0] = new OtpErlangAtom("ok");
        ret[1] = new OtpErlangAtom(name);
        return new OtpErlangTuple(ret);
    }
}
