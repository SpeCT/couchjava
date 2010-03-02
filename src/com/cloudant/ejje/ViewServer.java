package com.cloudant.ejje;

import com.ericsson.otp.erlang.OtpNode;

public abstract class ViewServer {

	public abstract Runnable getMboxThread(OtpNode node, String name);

}
