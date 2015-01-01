package ac_lang;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.TreeMap;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.SimUtilities;

public class Driver {
	
	private ArrayList<Cell> actives;
	private String nghType;
	private Context<Object> model;
	private int cycleCount;
	private LinkedHashSet<HyperCycle> cycles = new LinkedHashSet<HyperCycle>();
	private int lengthLongestCycle;
	private int newRules = 0;
	private ArrayList<Cell> cellList;
	private int checktest = 0;
	private final String report;
	
	public Driver(Context<Object> model,String nghType,int ballTypes, String report){
		this.nghType = nghType;
		this.model = model;
		this.report = report;
		actives = new ArrayList<Cell>();
		cellList = new ArrayList<Cell>();
		ScheduleParameters params = ScheduleParameters.createOneTime(1,1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(params, this, "doModelStep");
		double END;
		
		if (ballTypes==2) END=270000d;
		else if (ballTypes==3) END=410000d; 
		else if (ballTypes==4) END=580000d; 
		else if (ballTypes==5) END=770000d; 
		else if (ballTypes==6) END=980000d; 
		else if (ballTypes==7) END=1210000d; 
		else if (ballTypes==8) END=1460000d; 
		else END=1720000d; 

		RunEnvironment.getInstance().endAt(END);
		ScheduleParameters endparams = ScheduleParameters.createAtEnd(1d);
		System.out.println(endparams);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(endparams,this,"printEnd");
		
		


	}
	
	public Context<Object> getContext(){
		return this.model;
	}
	
	public void setUpCellList(){
		
		final Iterable<Object> remainingCells = this.model.getObjects(Cell.class);
        
        for (final Object cell : remainingCells){
        	if (cell instanceof Cell){
        	cellList.add((Cell) cell);}
        }
        SimUtilities.shuffle(cellList, RandomHelper.getUniform());
		//Pulling out any cells that already don't have rules.

        ArrayList<Cell> toBeRemoved = new ArrayList<Cell>();
		Iterator<Cell> list = cellList.iterator();
		while (list.hasNext()){
			Cell cell = list.next();
			if (cell.getNumRules()==0){
				toBeRemoved.add(cell);
				}	
			}
		for (Cell cell : toBeRemoved){
			cell.die(this.model);
		}
	}
	
	public String getNghType(){
		return this.nghType;
	}
	
	public void addToActives(Cell cell){
		actives.add(cell);
		//System.out.println("Actives: "+actives);
	}
	
	public void incrementNewRuleCount(){
		newRules++;
	}
	
	public void doModelStep(){
		removeRules();
		deactivateCells();
		Cell cell = getRandomCell();
		cell.activate();
		actives.add(cell);
		AbstractRule rule = cell.getRandomProductionRule();
		int ball = ((ProductionRule) rule).getOutput();
	
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double time = schedule.getTickCount();
		ScheduleParameters params = ScheduleParameters.createOneTime(time+1);



		if (cell.getUrn().removeBall(0,0,ball)){
			rule.activate();
			schedule.schedule(params, cell, "passBall", ball);
		}
		else{
			schedule.schedule(params, this, "doModelStep");
		}
		if (endChecks()) {
			RunEnvironment.getInstance().endRun();

		}
	}
	
	public void printEnd(){
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double time = schedule.getTickCount();
		calculateCycleCount();
		String output = report +","+RandomHelper.getSeed()+","+time+","+cycleCount+","+lengthLongestCycle;
	    System.out.println(output);
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("/users/atwell/Desktop/output.txt", true)));
		    out.println(output);
		    out.close();
		} catch (IOException e) {
		}

	}
	
	public boolean endChecks(){
		if (cellList.size()<=2 || cycleCount == 0 ){
			//System.out.println("Checking to end");
			if (cellList.size()==2 && cycleCount !=0){
				if (checktest==10){
					//double time = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
					//System.out.println("Paused at time = "+ time);
					return true;
				}
				else{
					checktest++;
					return false;
				}
			}
			else{
				checktest=0;
				return true;
			}
		}
		else{
			checktest = 0;
			return false;}
		}

	
	public void deactivateCells(){
		for (Cell cell : actives){
			cell.deactivate();
		}
		actives.clear();
	}
	public void removeRules(){
		for (int i = 0; i<newRules;i++){
			Cell cell = getRandomCell();
			cell.removeRandomProductionRule();
			if (cell.numRules==0) cell.die(this.model);
		}
		newRules = 0;
		
	}
	
	public void killCell(Cell cell){
		cellList.remove(cell);
	}
	
	public Cell getRandomCell() {
		int index = RandomHelper.nextIntFromTo(0, cellList.size()-1);
        return cellList.get(index);
	}
	
	public int getCountCells(){
		return cellList.size();
	}
	
	public int getLongestCycleLength(){
		return lengthLongestCycle;
	}
	public int getCycleCount() {
	    return calculateCycleCount();
	  }

	  /**
	   * Creates a network of Rules where an edge between two Rules
	   * exists if Rule i has a rule whose output value can activate
	   * Rule j. This creates a directed graph.
	   */
	public void createNetwork() {
		for (int i=0;i<cellList.size(); i++){
			cellList.get(i).createAdjacencyList();
	      }
	    }


	  
	  /**
	   * Returns an Iterator for iterating through the current set of Hyper2Cycle-s.
	   */

	  public Iterator<HyperCycle> cycleIterator() {
	    return cycles.iterator();
	  }

	  public String cycleCountAsString() {
	    StringBuffer b = new StringBuffer("\"Cycle Length\"\t\"Count\"\n");
	    calculateCycleCount();

	    // we use a TreeMap because it stores its keys in ascending order.
	    TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
	    for (Iterator<HyperCycle> iter = cycles.iterator(); iter.hasNext(); ) {
	      HyperCycle cycle = (HyperCycle)iter.next();
	      // +1 because the cycle does not include the start element as the end
	      // element
	      Integer length = new Integer(cycle.size());
	      Integer count = (Integer)map.get(length);
	      if (count == null) {
	        count = new Integer(1);
	        map.put(length, count);
	      } else {
	        map.put(length, new Integer(count.intValue() + 1));
	      }
	    }

	    for (Iterator<Integer> iter = map.keySet().iterator(); iter.hasNext(); ) {
	      Integer length = (Integer)iter.next();
	      b.append(length);
	      b.append("\t");
	      b.append(map.get(length));
	      b.append("\n");
	    }

	    return b.toString();
	  }


	  /**
	   * Count the number of hyper-cycles in this Space. We do this by
	   * doing a depth first traversal of our Rule network, looking for cycles.
	   */
	  public int calculateCycleCount() {
		lengthLongestCycle =0;
	    cycleCount = 0;
	    cycles.clear();
	    Stack<StackElement> stack = new Stack<StackElement>();


	    Iterator<Cell> cells = cellList.iterator();

	    while(cells.hasNext()){
	    	ProductionRuleBag bag = cells.next().getProductionBag();
	        for (Iterator<NetRule> iter = bag.netRuleIterator(); iter.hasNext();) {
	        	NetRule r = (NetRule)iter.next();
	            ArrayList<NetRule> visited = visit(r, stack);
	            stack.clear();
	            for (int k = 0, n = visited.size (); k < n; k++) {
	              ((NetRule) visited.get (k)).status = NetRule.UNSEEN;
	            }

	        }
	      }

	    return cycleCount;
	  }

	  private ArrayList<NetRule> visit(NetRule rule, Stack<StackElement> stack) {
	    ArrayList<NetRule> visited = new ArrayList<NetRule>();

	    // Chain is defined below
	    Chain chain = new Chain();
	    NetRule lastPopped = rule;
	    stack.push(new StackElement(-1, rule));

	    while (!stack.empty()) {

	      // StackElement is defined below.
	      StackElement elem = (StackElement)stack.pop();
	      NetRule r = elem.rule;
	      chain.add(r);
	      if (lastPopped != rule && !lastPopped.hasLinkTo(r)) {
	        // cut dead ends out of chain.
	        chain.removeElements(elem.parentIndex + 1, chain.size() - 1);
	      }

	      lastPopped = r;
	      r.status = NetRule.VISITED;
	      visited.add(r);
	      for (Iterator<NetRule> iter = r.adjacentIterator(); iter.hasNext();) {
	    	  
	        NetRule adj = (NetRule)iter.next();

	        if (adj.equals(rule)) {
	          HyperCycle cycle = new HyperCycle(chain);
	          if (! cycles.contains(cycle)) {
	            cycleCount++;
	            cycles.add(cycle);
	            if (chain.size() > lengthLongestCycle){
	            	lengthLongestCycle = chain.size();
	            }
	          }
	        }
	        if (adj.status == NetRule.UNSEEN) {
	          adj.status = NetRule.UNVISITED;
	          StackElement e = new StackElement(chain.size() - 1, adj);
	          stack.push(e);
	        }
	      }

	    }

	    return visited;
	  }

	  class StackElement {
	    int parentIndex = 0;
	    NetRule rule;

	    public StackElement(int index, NetRule r) {
	      parentIndex = index;
	      this.rule = r;
	    }
	  }

	  class Chain extends ArrayList<NetRule> {

		private static final long serialVersionUID = 1L;

		public void removeElements(int fromIndex, int toIndex) {
	      super.removeRange(fromIndex, toIndex);
	    }
	  }
		
	

}
