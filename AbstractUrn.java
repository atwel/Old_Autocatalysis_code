package ac_lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This class is the holder of the production balls that aren't presently possessed by cells
 * It can receive, remove, and query the presence of the various balls with it. 
 * 
 * I'm pretty sure the addBall method could just be an empty method to override, but given 
 * that everything is working, I've left it
 * @author Jon
 *
 */
public class AbstractUrn {

	  double total = 0;
	  double totalCum = 0;

	  TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
	  HashMap<Integer, Integer> mapCum = new HashMap<Integer, Integer>();


	  public void addBall (int x, int y, Integer i) {
	    total++;
	    Integer count = (Integer) map.get (i);
	    if (count == null) {
	      map.put(i, 1);
	    } else {
	      map.put(i, count.intValue () + 1);
	    }

	    totalCum++;
	    count = (Integer) mapCum.get (i);
	    if (count == null) {
	      mapCum.put( i, 1);
	    } else {
	      mapCum.put( i, count.intValue () + 1);
	    }
	  }
	  
	  public boolean removeBall(int x, int y, Integer ball){
		  return false;
	  }
	  
	  public boolean hasBall(int x, int y, Integer ball){
		    return false;
	  }

	  public String toString() {
	    StringBuffer b = new StringBuffer("\"type\"\t\"count\"\n");
	    for (Iterator<Integer> iter = map.keySet ().iterator (); iter.hasNext ();) {
	      Integer val = (Integer) iter.next ();
	      Integer count = (Integer) map.get (val);
	      b.append(val);
	      b.append("\t");
	      b.append(count.intValue() / total);
	      b.append("\n");
	    }

	    total = 0;
	    map.clear();

	    return b.toString();
	  }
}
