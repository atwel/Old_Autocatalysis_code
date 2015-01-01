package ac_lang;

import java.util.ArrayList;
import repast.simphony.random.RandomHelper;



/**
 * Successfully draws balls with a uniform probability for all types of balls.
 * This probability is 1 / maxBallType.
 *
 * @version $Revision$ $Date$
 */
public class UniformUrn extends AbstractUrn{

  private double probability;

  public UniformUrn(double maxBallType) {
    probability = 1d / maxBallType;
  }


  @Override
  public boolean hasBall(int x, int y, Integer ball){ 
	    double p = RandomHelper.nextDouble();
	    return p <= probability;
  }
  
  /**
   * Assumes that hasBall has been called first.
   */
  @Override
  public boolean removeBall (int x, int y, Integer ball){
	  return true;
  }

  /**
   * This returns an empty ArrayList as returning one of "infinite" size would   * be impossible.
   */
  public ArrayList<Object> getAllBalls () {
    return new ArrayList<Object>();
  }

}
