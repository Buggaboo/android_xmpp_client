package nl.sison.xmpp;

import java.lang.ref.WeakReference;

import android.os.Binder;

public class XMPPServiceBinder extends Binder {
	WeakReference<XMPPService> wr_service;

	public XMPPServiceBinder(XMPPService service) {
		super();
		this.wr_service = new WeakReference<XMPPService>(service);

	}

	XMPPService getService() {
		return wr_service.get();
	}
}
