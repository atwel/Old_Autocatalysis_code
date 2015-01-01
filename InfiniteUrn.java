package ac_lang;

import java.util.ArrayList;


/**
 * Successfully draws balls in all cases. The urn is effectively of
 *  infinite size with respect to ball draws.
 *
 * @version $Revision$ $Date$
 */
public class InfiniteUrn extends AbstractUrn {


  /**
   * Assumes that hasBall has been called first.
   */
  @Override
  public boolean hasBall (int x, int y, Integer type) {
    return true;
  }
  
  public boolean removeBall(int x, int y, Integer type){
	  return true;
  }

  /**
   * This returns an empty ArrayList as returning one of "infinite" size would   * be impossible.
   */
  public ArrayList<Object> getAllBalls() {
    return new ArrayList<Object>();
  }
}
