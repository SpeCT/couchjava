package com.cloudant.couchdbjavaserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassUrls {
	
	private List<URL> libUrls = new ArrayList<URL>();


	private ClassUrls() {}
	
		 
		   /**
		    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
		    * or the first access to SingletonHolder.INSTANCE, not before.
		    */
		   private static class SingletonHolder { 
		     private static final ClassUrls INSTANCE = new ClassUrls();
		   }
		 
		   public static ClassUrls getInstance() {
		     return SingletonHolder.INSTANCE;
		   }
		   
		   public boolean addUrl(String url) {
//			   return true;
			   try {
				   libUrls.add(new URL(url));
				   return true;
			   } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				   e.printStackTrace();
				   return false;
			   }
		   }
		   
		   public List<URL> getUrls() {
			   return libUrls;
		   }
}
