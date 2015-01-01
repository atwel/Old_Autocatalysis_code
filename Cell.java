package ac_lang;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import ac_lang.RuleBag.Rules;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.SimUtilities;



/**
 * This is the main object of the model. It is a "cell" that produces outputs and signals, has distribution
 * and movement rules and an ability to carry products around.
 * 
 * @version $Revision$ $Date$
 */

public class Cell {
	
	private Driver driver;
	private ContinuousSpace<Object> space;
	private AbstractUrn masterUrn;
	private ProductionRuleBag productionBag;
	private DistributionRuleBag distributionBag;
	private SignalRuleBag signalBag;
	private ForagingRuleBag foragingBag;
	private DeliveringRuleBag deliveringBag;
	public int carryingCapacity;
	public int numRules;
	private int maxRuleCount;
	private boolean isActive = false;
	private String nghType;
	
	
	/**
	 * The basic agent of the model. It has a variety of rules for transforming, communicating about, foraging for, distributing, and delivering products.
	 * It can also carry products around.
	 * 
	 * @param space
	 * @param values
	 * @param productionBag
	 * @param distributionBag
	 * @param signalBag
	 * @param foragingBag
	 * @param deliveringBag
	 * @param carrying
	 */
	public Cell(Driver ddriver, ContinuousSpace<Object> space, AbstractUrn masterUrn, ProductionRuleBag productionBag, DistributionRuleBag distributionBag,
			SignalRuleBag signalBag, ForagingRuleBag foragingBag, DeliveringRuleBag deliveringBag, String nghtype, int carrying, int maxRuleCount){
		
		this.driver = ddriver;
		this.space = space;
		this.masterUrn = masterUrn;
		this.carryingCapacity = carrying;
		this.maxRuleCount = maxRuleCount;
		this.numRules = productionBag.countRules();
		this.nghType = nghtype;
		
		this.productionBag = productionBag;
		productionBag.setOwner(this);
		this.distributionBag = distributionBag;
		distributionBag.setOwner(this);
		this.signalBag = signalBag;
		signalBag.setOwner(this);
		this.foragingBag = foragingBag;
		foragingBag.setOwner(this);
		this.deliveringBag = deliveringBag;
		deliveringBag.setOwner(this);

	}
	
		
	public boolean drawBall(int x,int y, Integer ball){
		return masterUrn.removeBall(x,y,ball);	
	}
	
	public void activate(){
		this.isActive = true;
	}
	
	public void deactivate(){
		this.isActive = false;
	}
	
	public void reproduceActiveRule(){
		productionBag.reproduceActiveRule();	
	}
	
		
	/**
	 * This method receives a product ball from an adjacent cell a determines whether
	 * it has a rule for it. If it does, it passes the correct output to another cell.
	 * If it doesn't, it drops the product ball back into the master urn.
	 * @param cell
	 * @param ball
	 */
	public void receiveBall(Cell cell, Integer ball){
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double time = schedule.getTickCount();
		ScheduleParameters params = ScheduleParameters.createOneTime(time+1);
		
		if (productionBag.hasRuleFor(ball)){
			this.activate();
			driver.addToActives(this);
			driver.incrementNewRuleCount(); //for balancing purposes later
			
			if (ACLangBuilder.reproType.equals("TARGET")){
				AbstractRule rule = productionBag.getRule(ball);
				rule.reproduce();
				schedule.schedule(params, this, "passBall", (((ProductionRule) rule).getOutput()));		
			}
			else{
				cell.reproduceActiveRule();
				AbstractRule rule = productionBag.getRule(ball);
				productionBag.setActiveRule(rule);
				schedule.schedule(params, this, "passBall", (((ProductionRule) rule).getOutput()));
			}
			
		}
		else{
			masterUrn.addBall(0, 0, ball); //dropping the ball back into the master urn.
			schedule.schedule(params, driver, "doModelStep");	
		}
	}
	
	
	/**
	 * This allows the cell to pass the ball. Because it is passed randomly, 
	 * each location has a 1/4 (VonNeumann) or 1/8 (Moore) chance of receiving the
	 * ball. If there is no activate cell in that location, the ball is placed back
	 * into the master urn.
	 * @param ball
	 */
	public void passBall(Integer ball){
		
		ArrayList<Cell> nghs = findNghs();
		int size = nghs.size();
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double time = schedule.getTickCount();
		ScheduleParameters params = ScheduleParameters.createOneTime(time+1);
		
		if (nghType.equals("MOORE")){
			int leftover =  8-size;
			int rando = RandomHelper.nextIntFromTo(1, 8);
			if (rando <= leftover){ 
				masterUrn.addBall(0, 0, ball);
				schedule.schedule(params, driver, "doModelStep");
			}
			else{
				Cell ngh = nghs.get(0);
				ngh.receiveBall(this, ball);
				}
		}else{
			int leftover =  4-size;
			int rando = RandomHelper.nextIntFromTo(1, 4);
			if (rando <= leftover){ 
				masterUrn.addBall(0, 0, ball);
				schedule.schedule(params, driver, "doModelStep");
			}
			else{
				Cell ngh = nghs.get(0);
				ngh.receiveBall(this, ball);
			}
		}
	}
	
	
	/**
	 * This method removes a rule at random by shuffling a list of remaining indices and
	 * removing the first rule with that index. If there are no more rules after that, 
	 * the cell is killed (along with its rule bags).
	 */	
	public void removeRandomProductionRule(){
		ArrayList<Integer> ruleList = new ArrayList<Integer>();
		for (int i=1;i<=productionBag.ballTypes;i++){
			if (productionBag.ruleMap.containsKey(i)){ruleList.add(i);}}
		SimUtilities.shuffle(ruleList, RandomHelper.getUniform());
		int index = ruleList.get(0);
		productionBag.removeRule(productionBag.ruleMap.get(index).getRule());
		if (getNumRules()==0) {
			die(driver.getContext());
		}
	}
	
	
	/**
	 * This method returns a random production rule from the cell's set of rules.
	 * @return a random production rule
	 */
	public AbstractRule getRandomProductionRule(){
		ArrayList<Integer> ruleList = new ArrayList<Integer>();
		
		for (int i=1;i<=productionBag.ballTypes;i++){
			if (productionBag.ruleMap.containsKey(i)){ruleList.add(i);}}
		
		SimUtilities.shuffle(ruleList, RandomHelper.getUniform());
		int index = ruleList.get(0);
		
		return productionBag.ruleMap.get(index).getRule();
	}

	
	/**
	 * This method removes the cell from the model. The triggering event is
	 * a loss of all production rules. It has to remove rules individually so 
	 * that the networked rules are also properly removed.
	 * @param context
	 */
	public void die(Context<Object> context){

		for (int rule : this.productionBag.ruleMap.keySet()){
				this.productionBag.ruleMap.remove(rule);	
		}
		context.remove(this.productionBag);
		context.remove(this.deliveringBag);
		context.remove(this.distributionBag);
		context.remove(this.signalBag);
		context.remove(this.foragingBag);
		driver.killCell(this); //removing from the driver's list of cells.
		context.remove(this);
	}
	
	
	/**
	 * This method returns a list of the cells alive neighbors. The type of neighborhood
	 * is defined as object field that is checked
	 * 
	 * @return an array of neighbors
	 */
	public ArrayList<Cell> findNghs(){
		ArrayList<Cell> nghs = new ArrayList<Cell>();
		if (nghType.equals("MOORE")){
			
			ContinuousWithin<Object> range = new ContinuousWithin<Object>(this.space, this, 1.1);
			for (Object ngh : range.query()){
				if (ngh instanceof Cell){
					nghs.add((Cell) ngh);
				}
			}
			SimUtilities.shuffle(nghs, RandomHelper.getUniform());
			return nghs;
		}
		
		if (nghType.equals("VON_NEUMANN")){
			ContinuousWithin<Object> range = new ContinuousWithin<Object>(this.space, this, 1.5);
			for (Object ngh : range.query()){
				if (ngh instanceof Cell){
					nghs.add((Cell) ngh);
				}
			}
			SimUtilities.shuffle(nghs, RandomHelper.getUniform());
			return nghs;
			
		}
		else
			return nghs;
	}
	
	
	/**
	 * This method tracks which product balls can be passed between neighbors
	 */
	public void createAdjacencyList () {
		ArrayList<Cell> nghs = findNghs();
		for (Iterator<Rules> iter = productionBag.ruleMap.values ().iterator (); iter.hasNext ();) {
		    Rules rules = iter.next ();
		    for (Iterator<NetRule> iter2 = rules.netRules.iterator (); iter2.hasNext ();) {
		      NetRule nRule = iter2.next ();
		      for (int j = 0, l = nghs.size (); j < l; j++) {
		         Cell ngh = (Cell) nghs.get (j);
		         if (ngh.productionBag.hasRuleFor(nRule.getOutput ()))
		            nRule.addAdjacentRules (ngh.productionBag.getNetRulesFor (nRule.getOutput ()));
		            
		        }
		      }
		    }
		  }
		
	
	
	
	public void setLocation(double x, double y){
		space.moveTo(this, x,y);
	}
	
	public int getNumRules(){
		numRules = productionBag.countRules();
		return numRules;
	}
	
	public NdPoint getLocation(){
		NdPoint mypoint = space.getLocation(this);
		return mypoint;
	}

	public ProductionRuleBag getProductionBag(){
		return productionBag;
	}
	
	public double getColor(){
		return (double) numRules/(maxRuleCount/4);
	}
	
	public Color getBorderColor(){
		if (this.isActive) return Color.GREEN;
		else return Color.BLACK;
	}
	
	public AbstractUrn getUrn(){
		return this.masterUrn;
	}
		

}
