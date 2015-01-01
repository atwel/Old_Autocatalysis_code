package ac_lang;

//import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;


/**
 * NetRules are just like Rules but with different equality semantics. Two
 * NetRules are considered equal if they have same RuleBag and input and
 * output values. NetRules are used as the nodes in the network of rules
 * used to find hyper-cycles.
 *
 * @version $Revision$ $Date$
 */
public class NetRule {

  public static final int UNSEEN = 0;
  public static final int UNVISITED = 1;
  public static final int VISITED = 2;

  private Integer input;
  private Integer output;
  private RuleBag bag;
  int status = NetRule.UNSEEN;


  // how many rules of this type in the container RuleBag.
  int count = 1;


  boolean isNew;

  // stores the NetRules that are adjacent to this NetRule in the network.
  private HashSet<NetRule> adjacent = new HashSet<NetRule>();

  public NetRule (Integer input, Integer output, RuleBag bag) {
    this.input = input;
    this.output = output;
    this.bag = bag;
    //this.isNew = true;
  }

  public Integer getInput () {
    return input;
  }

  public Integer getOutput () {
    return output;
  }

  public RuleBag getRuleBag() {
    return bag;
  }

  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (! (obj instanceof NetRule)) return false;
    NetRule other = (NetRule)obj;
    return (other.input.equals(input) && other.output.equals(output) &&
            other.bag.equals(bag));
  }

  public int hashCode() {
    int result = 17;
    result = 37 * result + input.intValue();
    result = 37 * result + output.intValue();
    result = 37 * result + bag.hashCode();

    return result;
  }

  public void addAdjacentRule(NetRule rule) {
    adjacent.add(rule);
  }

  public void addAdjacentRules(Collection<NetRule> rules) {
    adjacent.addAll(rules);
  }

  public void removeAdjacentRule(NetRule rule) {
    adjacent.remove(rule);
  }

  public Iterator<NetRule> adjacentIterator() {
    return adjacent.iterator();
  }

  public int adjacentSize() {
    return adjacent.size();
  }

  public boolean hasLinkTo(NetRule r) {
    return adjacent.contains(r);
  }



  public String adjacentToString() {
    return adjacent.toString();
  }



  public String drawString() {
    return input + "," + output;
  }
}


