package com.cloudant.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudant.couchdbjavaserver.JavaView;
import com.cloudant.javaviews.LuceneDoc;



public class CouchdbIndexReader extends IndexReader {
	
	private static boolean cache = false;
	private String databaseUrl;
	private String user;
	private String password;
	private String indexPath = "_design/lucene/_view/index";
	private String fieldPath = "_design/fields/_view/";
	private DocIdMap dmap;
	static boolean DEBUG = false;
	private Map<Term, Map<Integer, List<Integer>>> data = null;
	// mapping of <field, term> pairs
	private Map<String, SortedMap<String, Object>> termData = null;
	
	public static IndexReader open(String url) throws IOException {
		return new CouchdbIndexReader(url, null, null);
	}

	public static IndexReader open(String url, String user, String password) throws IOException {
		return new CouchdbIndexReader(url, user, password);
	}
	
	public CouchdbIndexReader(String url, String user, String password) throws IOException {
		if (url == null) throw new IOException("Must provide url for CouchDbIndexReader");
		databaseUrl = url + (url.endsWith("/") ? "" : "/");
		try {
			URL testUrl = new URL(databaseUrl);
		} catch (MalformedURLException e) {
			throw new IOException("invalid url for CouchDbIndexReader: " + url);
		}
		this.user = user;
		this.password = password;
		dmap = new DocIdMap();
		data = new HashMap<Term, Map<Integer, List<Integer>>>();
		termData = new HashMap<String, SortedMap<String, Object>>();
	}

	public int getLuceneId(String couchid) {
		return dmap.getLuceneId(couchid);
	}
	public String getCouchId(int luceneid) throws NoSuchFieldException {
		return dmap.getCouchId(luceneid);
	}
	@Override
	protected void doClose() throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("doClose");
	}

	@Override
	protected void doCommit(Map<String, String> arg0) throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("doCommit");

	}

	@Override
	protected void doDelete(int arg0) throws CorruptIndexException, IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("doDelete");

	}

	@Override
	protected void doSetNorm(int arg0, String arg1, byte arg2)
			throws CorruptIndexException, IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("doSetNorm");

	}

	@Override
	protected void doUndeleteAll() throws CorruptIndexException, IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("doUndeleteAll");

	}

	@Override
	public int docFreq(Term arg0) throws IOException {
		// This is only true for non wild card terms. Fix for wild card.
		if (DEBUG) System.err.println("docFreq");
		JSONArray arr = CouchIndexUtils.GetTermData(user, password, databaseUrl, indexPath, arg0);
		return arr.length();
	}

	@Override
	public Document document(int arg0, FieldSelector arg1)
			throws CorruptIndexException, IOException {
		// TODO Auto-generated method stub
		try {
//			if (DEBUG) System.err.println("lucene id: " + arg0);
			String couchId = getCouchId(arg0);
//			if (DEBUG) System.err.println("Get Doc Couch Id: " + couchId + " lucene id: " + arg0);
			JSONObject jdoc = CouchIndexUtils.GetJSONDocument(user,password,databaseUrl+couchId);
			if (jdoc == null) return null;
			Document doc = new Document();
			Map<String, String> map = CouchIndexUtils.MapJSONObject(jdoc,"");
			for (String field : map.keySet()) {
				if (arg1 == null || arg1.accept(field) != FieldSelectorResult.NO_LOAD) {
					doc.add(new Field(field, true, map.get(field), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
				}
			}
			return doc;
		} catch (NoSuchFieldException nfe) {
			throw new IOException(nfe.getMessage());
		}
	}

	@Override
	public Collection<String> getFieldNames(FieldOption arg0) {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("getFieldNames");
		return null;
	}

	@Override
	public TermFreqVector getTermFreqVector(int arg0, String arg1)
			throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("TermFreqVector1");
		return null;
	}

	@Override
	public void getTermFreqVector(int arg0, TermVectorMapper arg1)
			throws IOException {
		if (DEBUG) System.err.println("TermFreqVector2");
		// TODO Auto-generated method stub

	}

	@Override
	public void getTermFreqVector(int arg0, String arg1, TermVectorMapper arg2)
			throws IOException {
		if (DEBUG) System.err.println("TermFreqVector3");
		// TODO Auto-generated method stub

	}

	@Override
	public TermFreqVector[] getTermFreqVectors(int arg0) throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("TermFreqVector4");
		return null;
	}

	@Override
	public boolean hasDeletions() {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("hasDeletions");
		return false;
	}

	@Override
	public boolean isDeleted(int arg0) {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("isDeleted");
		return false;
	}

	@Override
	public int maxDoc() {
		// TODO Auto-generated method stub
//		if (DEBUG) System.err.println("maxDoc: " + dmap.maxDoc());
//		return dmap.maxDoc();
		return numDocs();
	}

	@Override
	public byte[] norms(String arg0) throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("norms");
		return null;
	}

	@Override
	public void norms(String arg0, byte[] arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub
		if (DEBUG) System.err.println("norms2");

	}

	@Override
	public int numDocs() {
		// TODO Auto-generated method stub
		String summary = CouchIndexUtils.GetDocument(user, password, databaseUrl);
		JSONObject jobj = CouchIndexUtils.ConvertStringToJSON(summary);
		try {
			final int num = jobj.getInt("doc_count");
			if (DEBUG) System.err.println("numDocs: " + num);
			return num;
		} catch (JSONException e) {
			System.out.println("Error getting doc_count");
			return 0;
		}
	}


   @Override
    public TermPositions termPositions() {
      if (DEBUG) System.err.println("MemoryIndexReader.termPositions");
      
      return new TermPositions() {
  
//        private boolean hasNext;
        private int cursor = 0;
//        private ArrayIntList current;
        private List<DocPositions> current;
        private int iPos = 0;
        private Term term;
        
        public void seek(Term term) {
          this.term = term;
          if (DEBUG) System.err.println(".seek: " + term);
        	  if (cache && data != null && data.containsKey(term)) {
        		  // cached
        	  } else { 
        		  JSONArray arr = CouchIndexUtils.GetTermData(user, password, databaseUrl, indexPath, term);        	  
//        		  System.out.println("Term Data for " + term.toString() + " " + arr.toString());
        		  
        		  if (arr != null) {
        			  Map<Integer, List<Integer>> tmap = new HashMap<Integer, List<Integer>>();
        			  for (int i = 0; i < arr.length(); i ++) {
        				  try {
        				  JSONObject jobj = arr.getJSONObject(i);
//        				  System.out.println(jobj.toString());
        				  String docId = jobj.getString("_id");
        				  int luceneId = dmap.getLuceneId(docId);
        				  JSONArray pos = jobj.getJSONArray("p");
        				  List<Integer> positions = new ArrayList<Integer>();
        				  for (int iPos=0; iPos < pos.length(); iPos++) {
// format screwy
        					  positions.add(pos.getInt(iPos));
        				  }
//         				  System.out.println("couch " + docId + " lucene " + luceneId);
        				  tmap.put(luceneId, positions);
        				  } catch (JSONException je) {
        					  System.out.println(je.toString());
        				  }
        				  
        			  }
        			  data.put(term, tmap);
        		  }
        	  }
    		  Map<Integer, List<Integer>> thisTerm = data.get(term);
    		  current = new ArrayList<DocPositions>();
    		  for (Integer i : thisTerm.keySet()) {
    			  current.add(new DocPositions(i,thisTerm.get(i)));
    		  }
    		  // WARNING must be sorted.  current should probably be some sort of automatically sorted list.
    		  Collections.sort(current);
              if (DEBUG) System.err.println(".seek: size of doc list " + current.size());
//    		  hasNext = (current.size() > 0);
    		  cursor = -1;
    		  iPos = 0;
      	}
  
        public void seek(TermEnum termEnum) {
          if (DEBUG) System.err.println(".seekEnum");
          seek(termEnum.term());
        }
  
        public int doc() {
//          if (DEBUG) System.err.println(".doc");
          int returnValue = current.get(cursor).id;
          if (DEBUG) System.err.println(".doc " + cursor + " " + returnValue + " " + term);
          return current.get(cursor).id;
        }
  
        public int freq() {
          int freq = current.get(cursor).positions.size();
          int id = current.get(cursor).id;
          if (DEBUG) System.err.println(".id: " + id +  " freq: " + freq);
          return freq;
        }
  
        public boolean next() {
            if (DEBUG) System.err.println(".next " + term);
        	iPos = 0;
            cursor++;
//            hasNext = cursor < current.size();
            return cursor < current.size();
         }
  
        public int read(int[] docs, int[] freqs) {
            if (DEBUG) System.err.println(".read");
          int iRead = 0;
          boolean hasNext = next();
          while (hasNext && iRead < docs.length) {
        	  docs[iRead] = doc();
        	  freqs[iRead] = freq();
        	  iRead++;
        	  hasNext = next();
          }
          return iRead;
        }
  
        public boolean skipTo(int target) {
            if (DEBUG) System.err.println(".skipTo " + target + " for " + term);
            do {
              if (!next())
                    return false;
            } while (target > doc());
            return true;
          }
  
        public void close() {
          if (DEBUG) System.err.println(".close");
        }
        
        public int nextPosition() { // implements TermPositions
            if (DEBUG) System.err.println(".nextPosition");
        	if (iPos >= freq()) {
        		throw new ArrayIndexOutOfBoundsException();
        	}
        	final int pos = current.get(cursor).positions.get(iPos);
        	iPos++;
        	if (DEBUG) System.err.println(term + " in position " + pos);
        	return pos;
        }
        
        /**
         * Not implemented.
         * @throws UnsupportedOperationException
         */
        public int getPayloadLength() {
            if (DEBUG) System.err.println(".getPayloadLength");
          
          throw new UnsupportedOperationException();
        }
         
        /**
         * Not implemented.
         * @throws UnsupportedOperationException
         */
        public byte[] getPayload(byte[] data, int offset) throws IOException {
            if (DEBUG) System.err.println(".getPayload");
          throw new UnsupportedOperationException();
        }

        public boolean isPayloadAvailable() {
          // unsuported
            if (DEBUG) System.err.println(".isPayloadAvailable");

          return false;
        }

      };
    }
  
    @Override
    public TermDocs termDocs() {
        if (DEBUG) System.err.println(".termDocs");
     return termPositions();
    }

	@Override
	public TermEnum terms() throws IOException {
		// TODO Auto-generated method stub
        if (DEBUG) System.err.println(".terms");

		return null;
	}

	@Override
	public TermEnum terms(Term arg0) throws IOException {
		if (arg0 == null) System.err.println(".terms2 null argument");
        if (DEBUG) System.err.println(".terms2 " + arg0.toString());
        if (!termData.containsKey(arg0.field())) {
        	JSONArray jarr = CouchIndexUtils.GetSortData(user, password, databaseUrl, fieldPath, arg0.field());
        	SortedMap<String, Object> arr = new TreeMap<String, Object>();
        	System.out.println("length of sort data: " + jarr.length());
        	for (int i = 0; i < jarr.length(); i++) {
        		try {
        		JSONObject jobj = jarr.getJSONObject(i);
        		String uuid = jobj.getString("id");
        		Object obj = jobj.get("key");
        		arr.put(uuid, obj);
//        		int max = Math.max(200, jarr.toString().length());
        		if (i < 10) System.out.println(jobj.toString());
        		} catch (JSONException je) {
        			System.err.println(je.toString());
        		}
        	}
        	termData.put(arg0.field(), arr);
        }
		return new thisTermEnum(arg0.field());
	}
	
	private class thisTermEnum extends TermEnum {
		String currentField;
		Object currentObject;
		SortedMap<Object, Integer> map;
		Iterator<Object> iter = null; 
		
		public thisTermEnum(String field) {
			currentField = field;
			// this map contains everything -- need to prune
			SortedMap<String,Object> fullMap = termData.get(currentField);
			if (fullMap == null) {
				System.out.println("no data in termdata map");
				return;
			}
			map = new TreeMap<Object, Integer>();
			// <= on purpose!!!
			System.out.println("Warming cache to " + dmap.maxDoc());
			for (int i = 0; i <= dmap.maxDoc(); i++) {
				try {
					String couchId = dmap.getCouchId(i);
					Object key = fullMap.get(couchId);
					int newVal = 1;
					if (map.containsKey(key)) newVal += map.get(key);
					System.out.println("Adding " + key.toString());
					map.put(key, newVal);
				} catch (NoSuchFieldException e) {
						System.out.println(e.toString());
				}
			}
			iter = map.keySet().iterator();
			if (iter != null) {
				currentObject = iter.next();
			}
		}
		
		public void close() throws IOException {
			map.clear();
		}

		@Override
		public int docFreq() {
			return map.get(currentObject);
		}

		public boolean next() throws IOException {
//			System.err.println(".termenum next");
			if (map == null) throw new IOException("No term data");
			if (iter != null && iter.hasNext()) {
				currentObject = iter.next();
				return true;
			} else {
				return false;
			}
		}

		public Term term() {
			if (currentObject == null) System.err.println("null object");
			System.out.println(currentField + " " + String.valueOf(currentObject));
			return new Term(currentField, String.valueOf(currentObject));
		}
		
	}
	
	private class DocIdMap {
		Map<String, Integer> couchToLucene;
		Map<Integer, String> luceneToCouch;
		int lastInt = 0;
		public DocIdMap() {
			couchToLucene = new HashMap<String,Integer>();
			luceneToCouch = new HashMap<Integer,String>();
			lastInt = -1;
		}
		
		public int getLuceneId(String couchid) {
			if (couchid == null) return -1;
			if (couchToLucene.containsKey(couchid)) { 
				return couchToLucene.get(couchid);
			} else {
				lastInt++;
				couchToLucene.put(couchid, lastInt);
				luceneToCouch.put(lastInt, couchid);
				return lastInt;
			}
		}
		 
		public String getCouchId (int luceneId) throws NoSuchFieldException {
			if (luceneToCouch.containsKey(luceneId)) {
				return luceneToCouch.get(luceneId);
			} else {
				throw new NoSuchFieldException("Unknown lucene document id " + luceneId);
			}
		}
		
		public int maxDoc() {
			return lastInt;
		}
		
		
	}
	  private class DocPositions implements Comparable {
		  public int id;
		  public List<Integer> positions;
		  public DocPositions(int id, List<Integer> positions) {
			  this.id = id;
			  this.positions = positions;
		  }
		public int compareTo(Object arg0) {
			return id - ((DocPositions)arg0).id;
		}
	  }
		public static JavaView getClass(String classname, List<URL> libs) throws Exception {
			if (DEBUG) System.err.println("Attempting to load: " + classname);
			URLClassLoader loader = new URLClassLoader(libs.toArray(new URL[0]));
			Class<JavaView> loadClass = (Class<JavaView>) loader.loadClass(classname);
			Class<JavaView> cl = loadClass;
			Object o = cl.newInstance();
			return (JavaView)o;		
		}
		
		public Analyzer getAnalyzer() throws FileNotFoundException {
			String[] designDoc = indexPath.split("_view");
			if (designDoc == null || designDoc.length == 0) return null;
			String designDocUrl = databaseUrl + designDoc[0];
			if (DEBUG) System.err.println(designDocUrl);
			JSONObject jobj = CouchIndexUtils.GetJSONDocument(user, password, designDocUrl);
			if (jobj == null) {
				throw new FileNotFoundException("Cannot find design doc");
			}
			if (DEBUG) System.err.println(jobj.toString());
			List<URL> libs = new ArrayList<URL>();
			try {
				JSONObject attachments = jobj.getJSONObject("_attachments");
				if (DEBUG) System.err.println(attachments.toString());				
				Iterator<String> keys = (Iterator<String>)attachments.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.endsWith(".jar")) {
						try {
							final URL thisUrl = new URL(designDocUrl + key);
							if (DEBUG) System.err.println("Adding " + thisUrl.toString());				
							libs.add(thisUrl);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					String map = CouchIndexUtils.findFieldString("map", jobj);
					if (map == null) throw new FileNotFoundException("Can't find map field in desgin doc");
					JSONObject objectify = new JSONObject(map);
					String className = objectify.getString("classname");
					if (map == null) throw new FileNotFoundException("Can't find class name");
					JavaView jv = getClass(className, libs);
					if (jv == null ) {
						throw new FileNotFoundException("Can't get class from couchdb");
					}
					if (jv instanceof LuceneDoc) {
						return ((LuceneDoc)jv).getAnalyzer();
					} else {
						return null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				
			} catch (JSONException e) {
				return null;
			}
			
		}

}
