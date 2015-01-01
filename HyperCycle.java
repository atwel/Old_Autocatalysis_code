package ac_lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;


public class HyperCycle {

	  // we store the cycle of rules in this LinkedHashSet. We
	  // use a HashSet for equality comparisons and Linked so we preserve the
	  // order of the cycle.
	  private LinkedHashSet<NetRule> cycle;

	  /**
	   * Creates a Hyper2Cycle from the specified list.
	   */
	  public HyperCycle(ArrayList<NetRule> list) {
	    cycle = new LinkedHashSet<NetRule>(list);
	  }

	  /**
	   * Tests if some other Hyper2Cycle is equal to this one. Two Hyper2Cycles
	   * are considered equal if they contain the same rules regardless of the
	   * order of the rules.
	   */
	  public boolean equals(Object obj) {
	    if (obj == this) return true;
	    if (!(obj instanceof HyperCycle)) return false;
	    HyperCycle other = (HyperCycle)obj;
	    return cycle.equals(other.cycle);
	  }

	  public int size() {
	    return cycle.size();
	  }

	  /*
	   * If we override equals we need to override hashCode too.
	   */
	  public int hashCode() {
	    return cycle.hashCode();
	  }

	  public Iterator<NetRule> iterator() {
	    return cycle.iterator();
	  }

	  public String toString() {
	    return cycle.toString();
	  }
	}

