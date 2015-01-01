package ac_lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import repast.simphony.random.RandomHelper;




/**
*
* The main class for all the "bags" that hold the different types of rules
*
* @version $Revision$ $Date$
*/

public class RuleBag {
	
	public Cell owner;
	public String ruletype;
	public AbstractRule activeRule;
	public HashMap<Integer, Rules> ruleMap;
	
	// This internal class is stored as the value in the ruleMap shown above. It
	// contains a NetRule and a list of rules that accept the various possible inputs.
	
	static class Rules {

	  HashSet<NetRule> netRules = new HashSet<NetRule> ();
	  ArrayList<AbstractRule> rules = new ArrayList<AbstractRule> ();

	    public Rules(AbstractRule rule) {
	        rules.add (rule);
	        netRules.add (rule.createNetRule ());
	    }

	    public void add(AbstractRule r) {
	      NetRule nr = r.createNetRule ();
	      if (netRules.contains(nr)) {
	        nr = find(nr);
	        nr.count++;
	      } else {
	        netRules.add(nr);
	      }

	      rules.add (r);
	    }

	    public int countRules() {
	      return rules.size();
	    }

	    public int countNetRules() {
	      return netRules.size();
	    }


	    public void remove(AbstractRule r) {
	      NetRule nr = r.createNetRule();
	      rules.remove(r);
	      if (netRules.contains(nr)) {
	        nr = find(nr);
	        nr.count--;

	        if (nr.count == 0) netRules.remove(nr);
	      }
	    }
	    
	    public AbstractRule getRule(){
	    	return rules.get(0);
	    }

	    public int ruleSize () {
	        return rules.size ();
	      }
	    
	    public int netRulesSize () {
	        return netRules.size ();
	      }
	    
	    public boolean isEmpty() {
	      return rules.isEmpty();
	    }

	    public boolean containsNetRuleFor(AbstractRule r) {
	      return netRules.contains(r.createNetRule());
	    }

	    public NetRule find(NetRule nr) {
	      for (Iterator<NetRule> iter = netRules.iterator(); iter.hasNext();) {
	        NetRule cnr = iter.next();
	        if (nr.equals(cnr)) return cnr;
	      }

	      throw new IllegalArgumentException("NetRule not found");
	    }

	    public String toString() {
	    	
	      return (rules.toString()+", "+ netRules.toString());
	    }

	  }
	
	static class NetRuleIterator implements Iterator<NetRule> {

	    // this is an Interator over a list of Rules
	    private Iterator<Rules> iter;
	    private Iterator<NetRule> innerIter;
	    private boolean isEmpty;

	    public NetRuleIterator (Iterator<Rules> iter) {
	      this.iter = iter;
	      isEmpty = !(iter.hasNext ());
	      if (!isEmpty) innerIter = iter.next ().netRules.iterator ();
	    }

	    public boolean hasNext () {
	      if (isEmpty) return false;
	      boolean retVal = iter.hasNext ();
	      if (!retVal) return innerIter.hasNext();
	      return retVal;
	    }

	    public NetRule next () {
	      if (!innerIter.hasNext ()){
	    	  Rules hset = iter.next ();
	    	  innerIter = hset.netRules.iterator ();
	    	  if (!innerIter.hasNext()) {System.out.println("Empty new list!");
	    		  System.out.println(hset);}
	      }
	      return innerIter.next ();
	    }

	    public void remove () {
	      throw new UnsupportedOperationException ("remove is not supported");
	    }
	  }

	
	
	public RuleBag(){
		this.owner = null;
		this.ruletype = null;
		this.activeRule = null;
		this.ruleMap = new HashMap<Integer,Rules>();
	}
	
	public void setActiveRule(AbstractRule rule){
		this.activeRule = rule;
	}
		
	public int countRules(){
		int count = 0;
	    for (Iterator<Rules> iter = ruleMap.values ().iterator (); iter.hasNext ();) {
	        count += iter.next ().ruleSize ();
		}
		return count;
	}
	
	public void removeRule (AbstractRule arule) {
	    // get the Rules for this input value.
		ProductionRule rule = (ProductionRule) arule;
	    Rules rules = ruleMap.get (rule.getInput ());

	    // if there is no rules then just return because we don't have
	    // such a rule.
	    if (rules == null)
	      throw new IllegalArgumentException ("RuleBag can only remove Rules it contains");

	    // remove the rule from rules.
	    rules.remove (rule);



	    // if after removing the rule the list is empty then we also remove
	    // this key - value pair of which rules is the value. We need to
	    // do this because our hasRuleFor method checks to see if the
	    // ruleMap contains a rule's input value as a key and so if we didn't
	    // remove the empty list, this check would return true, although we don't
	    // have any rules in list for this input value.
	    if (rules.isEmpty ()) {
	    	ruleMap.remove (rule.getInput ());
	      
	    	// removing the NetRule from the adjacency lists of neighboring rules.
	    	ArrayList<Cell> nghs = owner.findNghs();

	    	for (int j = 0, l = nghs.size (); j < l; j++) {
	    		RuleBag ngh = (RuleBag) nghs.get (j).getProductionBag();
	    		ngh.removeAdjacentNetRule (rule.createNetRule ());
	    	}
	    } else if (!rules.containsNetRuleFor (rule)) {
	    	// rules no longer contains the NetRule for rule and so we need to
	    	// update the surrounding adjacency lists.
	    	ArrayList<Cell> nghs = owner.findNghs();
	    	for (int j = 0, l = nghs.size (); j < l; j++) {
	    		RuleBag ngh = nghs.get(j).getProductionBag();
	    		ngh.removeAdjacentNetRule (rule.createNetRule ());
	      }
	    }
	  }
	
	public void removeAdjacentNetRule (NetRule nRule) {
		    for (Iterator<NetRule> iter = netRuleIterator (); iter.hasNext ();) {
		    	NetRule netRule = (NetRule) iter.next ();
		    	netRule.removeAdjacentRule (nRule);
		    }
		  }

	
	  /**
	   * Returns a Rule for handling the specified Integer. Call hasRuleFor(Integer)
	   * before calling this method to insure that this RuleBag has a Rule
	   * for the specified ball. If this RuleBag has more than one such Rule,
	   * it returns one at random.
	   *
	   * @throws IllegalArgumentException if this RuleBag does not contain
	   * a Rule to handle the specified ball.
	   */
    public AbstractRule getRule (Integer ball) {
	    Rules rules = ruleMap.get (ball);
	    if (rules == null) throw new IllegalArgumentException ("No rule for ball");

	    int index = RandomHelper.nextIntFromTo(0, rules.rules.size () - 1);
	    return rules.rules.get (index);
	  }

    public void reproduceActiveRule(){
    	this.activeRule.reproduce();
    	updateNetwork(this.activeRule);
    }
    
	public String getType(){
		return ruletype;
	}
	
	public Iterator<NetRule> netRuleIterator () {
		return new NetRuleIterator (ruleMap.values ().iterator ());
	}
	
	public boolean hasRuleFor (Integer ball) {
		// we have rule for this ball, if the ruleMap contains this key.
		// see addRule for how rules are added which insures that this
		// method works.
		return ruleMap.containsKey (ball);
	}

	/**
	 * Returns the ArrayList of Rules activated by the specified Integer.
	*/
	public Collection<NetRule> getNetRulesFor (Integer ball) {
	    Rules rules = ruleMap.get (ball);
	    return rules.netRules;
	  }
	
	
	/**
	   * Updates the rule network when the specified Rule has been added to this
	   * RuleBag. Note that this Rule should already have been added to this
	   * RuleBag. We are only updating the network here. Updating consists of 1.
	   * seeing whether this rule can link with any neighboring rules. If so,
	   * those rules are added to this rule's adjacency list. And 2. whether any
	   * neighboring rules can link with this rule. If so, then the specified
	   * Rule is added to the neighbor rule's list.
	   */
	 public void updateNetwork (AbstractRule r) {
		 Rules rules = ruleMap.get (((ProductionRule) r).getInput ());
		 for (Iterator<NetRule> iter = rules.netRules.iterator (); iter.hasNext ();) {
			 NetRule nRule = iter.next ();
			 if (nRule.isNew) {
				 ArrayList<Cell> nghs = owner.findNghs();
				 Integer output = nRule.getOutput ();
				 // 	Integer input = nRule.getInput ();
				 for (int j = 0, l = nghs.size (); j < l; j++) {
					 RuleBag ngh = (RuleBag) nghs.get(j).getProductionBag();

					 // can this nRule link to neighbor net rules (nRule.ouput ==
					 // ngh.netRules.input
					 if (ngh.hasRuleFor (output))
						 nRule.addAdjacentRules (ngh.getNetRulesFor (output));

					 // add to neighbor net rules adjacency lists if nRule.input ==
					 // neighbor net rule output.
					 ngh.createAdjacencyList(nRule);
				 }
				 nRule.isNew = false;
			 }
	    }
	  }
	  
	  /**
	   * Adds otherRule to the adjacency lists of this RuleBag's NetRules iff
	   * they can link to each other: otherRule.input -> this.RuleBag's.netRules.
	   * output.
	   */
	  public void createAdjacencyList (NetRule otherRule) {
	    for (Iterator<Rules> iter = ruleMap.values ().iterator (); iter.hasNext ();) {
	      Rules rules = iter.next ();
	      for (Iterator<NetRule> iter2 = rules.netRules.iterator (); iter2.hasNext ();) {
	        NetRule nRule = iter2.next ();
	        if (nRule.getOutput ().equals (otherRule.getInput ())) {
	          nRule.addAdjacentRule (otherRule);
	        }
	      }
	    }
	  }
}
