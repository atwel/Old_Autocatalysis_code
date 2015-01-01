package ac_lang;

/**
*
*A simple interface for all the different types of rule floating around in cells
*
* @version $Revision$ $Date$
*/


public interface AbstractRule {
	
	
	public void activate();
	public void reproduce();
	public String toString();
	public int getIndex();
	public NetRule createNetRule();
}
