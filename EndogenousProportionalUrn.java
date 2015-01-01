package ac_lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;



/**
*
* @version $Revision$ $Date$
*/
public class EndogenousProportionalUrn extends AbstractUrn {

 private HashMap<Integer, Stack<Integer>> ballSet = new HashMap<Integer, Stack<Integer>>();
 private ArrayList<Integer> allBalls = new ArrayList<Integer>();
 private int totalBalls;

 public EndogenousProportionalUrn(int maxBallType, int numBallsOfEachType,
                                  boolean ones) {
   
   totalBalls = maxBallType * numBallsOfEachType;
   if (ones) {

     for (int i = 0; i < totalBalls; i++) {
       addBall(0, 0, new Integer(1));
     }

   } else {
     // we create numBallsOfEachType Integer-s where each integer is
     // a "type" of ball, and add these to our urn.
     for (int i = 1; i <= maxBallType; i++) {
       for (int j = 0; j < numBallsOfEachType; j++) {
         addBall(0, 0, new Integer(i));
       }
     }
   }


 }

 @Override
 public void addBall(int x, int y, Integer ball) {
   Stack<Integer> stack = (Stack<Integer>) ballSet.get(ball);
   if (stack == null) {
     stack = new Stack<Integer>();
     ballSet.put(ball, stack);
   }

   stack.push(ball);
   allBalls.add(ball);
 }

 @Override
 public boolean hasBall(int x, int y, Integer ball) {
   if (ballSet.containsKey(ball)) {
     double ballCount = ((Stack<Integer>) ballSet.get(ball)).size();
     double prob = ballCount / totalBalls;
     double val = new Random().nextDouble();
     return val <= prob;
   }

   return false;
 }

 @Override
 public boolean removeBall(int x, int y, Integer ball) {
   if (hasBall(x, y, ball)) {
     Stack<Integer> stack = (Stack<Integer>) ballSet.get(ball);
     stack.pop();
     if (stack.size() == 0) ballSet.remove(ball);
     allBalls.remove(ball);
     return true;
   }

   return false;
 }
 

 public ArrayList<Integer> getAllBalls() {
   return allBalls;
 }

 public String toString() {
   StringBuffer b = new StringBuffer("EndogenousProportionalUrn:\n");
   Iterator<Integer> iter = ballSet.keySet().iterator();
   while (iter.hasNext()) {
     Object key = iter.next();
     b.append(" ");
     b.append(key);
     b.append(" - ");
     Stack<Integer> s = (Stack<Integer>) ballSet.get(key);
     b.append(s.size());
     b.append("\n");
   }

   return b.toString();
 }

}
