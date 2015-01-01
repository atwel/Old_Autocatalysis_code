package ac_lang;

import java.util.ArrayList;

/**
 * Successfully draws a balls of type 1. Otherwise failure.
 *
 * @version $Revision$ $Date$
 */
public class OnesUrn extends AbstractUrn {

  private static final Integer ONE = new Integer(1);
  
  public OnesUrn(){}

  public boolean hasBall(Integer ball){
	  return (ball.equals(ONE));
  }
  
  public boolean removeBall (int x, int y, Integer ball ) {
    return (ball.equals(ONE));
  }

  /**
   * This returns an empty ArrayList as there's not much point in returning
   * one full of 1s.
   */
  public ArrayList<Integer> getAllBalls () {
    return new ArrayList<Integer>();
  }
}
