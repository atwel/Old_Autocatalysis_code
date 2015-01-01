package ac_lang;



import java.util.ArrayList;
import java.util.Iterator;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.*;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.*;
import repast.simphony.util.SimUtilities;



public class ACLangBuilder extends DefaultContext<Object> implements ContextBuilder<Object>{
	
	public static AbstractUrn urn;
	public static ArrayList<ProductionRule> rules;
	public static ArrayList<ProductionRuleBag> pbags;
	public static ContinuousSpace<Object> space;
	public static String nghType;
	public static String reproType;
	public static ArrayList<Cell> lightsOn;
	private int countCells;
	private Driver driver;
	
	
	@Override
	public Context<Object> build(Context<Object> context) {
		
		context.setId("ac_lang");
		
		// This grabs a collection of parameters from the GUI
		final Parameters parameters = RunEnvironment.getInstance().getParameters();
		
		
		// The following pulls in the rest and puts them in local variables
		final int spaceSize = ((Integer) parameters.getValue(Constants.PARAMETER_ID_SPACE_SIZE)).intValue();
		
		final int carryCap = ((Integer) parameters.getValue(Constants.PARAMETER_ID_CARRYING_CAPACITY)).intValue();
		
		int countRules = ((Integer) parameters.getValue(Constants.PARAMETER_ID_NUM_RULES)).intValue();
		
		final int countBalls = ((Integer) parameters.getValue(Constants.PARAMETER_ID_NUM_BALLS)).intValue();
		
		final int ballTypes = ((Integer) parameters.getValue(Constants.PARAMETER_ID_BALL_TYPE)).intValue();
		
		countCells = ((Integer) parameters.getValue(Constants.PARAMETER_ID_CELL_COUNT)).intValue();
		
		final String enviroType = parameters.getValueAsString(Constants.PARAMETER_ID_ENVIRO_TYPE);
		
		final String searchIntel = parameters.getValueAsString(Constants.PARAMETER_ID_SEARCH_INTEL);
		
		nghType = parameters.getValueAsString(Constants.PARAMETER_ID_NGH_TYPE);
		
		final String chemType = parameters.getValueAsString(Constants.PARAMETER_ID_CHEMISTRY);
		
		reproType = parameters.getValueAsString(Constants.PARAMETER_ID_REPRO_TYPE);
		
		
		// Integer division ensures that only integer values go in
		int countPerType = countBalls/ballTypes; 
		
		
		// This is the master urn for the whole simulation
		urn = createUrn(enviroType, searchIntel, ballTypes, countPerType);
		
		
	    
		// This next bit of code is a kludgy way of getting the same number of rules as previous versions of Padgett's work.
		// I didn't change the code properly because I'd like to see it changed to something closer to what I have (scales in n, not n^2 
		// that would maintain the probability of finding a neighbor who can use it).
		//if (chemType.equals("ALL")){
		//countRules = (int) ((Math.pow(ballTypes, 2) - ballTypes)*30);}
		//else
		//{countRules = ballTypes*30;}
		countRules = 200;
		System.out.println("Total number of rules is "+countRules);
		
	    pbags = new ArrayList<ProductionRuleBag>();
		for (int i=0; i< countCells; i++){
			pbags.add(new ProductionRuleBag(ballTypes));
		}
		

		
		rules = new ArrayList<ProductionRule>();
		
		
		if (chemType.equals("ALL")){
			createALLchem(ballTypes, countRules);
			}
		else {
			createSOLOchem(ballTypes,countRules);
			}
		
		
		// After this, we have a full list of production rule bags with the appropriate
		// type of rules distributed among them.
		String reportStr = chemType+","+nghType+","+reproType+","+enviroType+","+searchIntel+","+ballTypes;
		Driver ddriver = new Driver(context, nghType, ballTypes, reportStr);
		context.add(ddriver);
		driver=ddriver;
		
		
		// This is the space in which the cells reside
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), spaceSize, spaceSize);

		


		for (double i = .5; i <= spaceSize; i++){
			for(double j = .5; j <=spaceSize; j++){
				if (pbags.size()>0){
					int rando = RandomHelper.nextIntFromTo(0, pbags.size()-1);
					ProductionRuleBag mybag = pbags.remove(rando);


					// Creating the various other rule bags for the cells 
					DistributionRuleBag tbag = new DistributionRuleBag();
					SignalRuleBag sbag = new SignalRuleBag();
					ForagingRuleBag fbag = new ForagingRuleBag();
					DeliveringRuleBag vbag = new DeliveringRuleBag();
				
					// Actual construction of the cell
					Cell newcell = new Cell(ddriver, space, urn, mybag, tbag, sbag, fbag, vbag, nghType, carryCap, countRules);
					context.add(newcell);
				
					// Moving the cell to its starting position in the space
					space.moveTo(newcell, i,j);
				}
				
					
			}
			
		}


		driver.setUpCellList();
		driver.createNetwork();
		
		return context;
		
	}



	
	public AbstractUrn createUrn(String enviroType, String searchIntel, int ballTypes, int countPerType){
	    System.out.print("Starting run with ");
		if (enviroType.equals("ENDOGENOUS_P") && searchIntel.equals("RANDOM"))
        	{ urn = new EndogenousProportionalUrn(ballTypes, countPerType, true);
        	System.out.println("Urn type EndoProp, ones");}
	    
	    else if (enviroType.equals("ENDOGENOUS_R") && searchIntel.equals("RANDOM"))
        	{ urn = new EndogenousProportionalUrn(ballTypes, countPerType, false);
        	System.out.println("Urn type EndoProp");}
	    
	    else if (enviroType.equals("RICH") && searchIntel.equals("RANDOM"))
        	{ urn = new UniformUrn((double) ballTypes);
        	System.out.println("Urn type RICH RANDOM");}
	    
	    else if (enviroType.equals("POOR"))
        	{ urn = new OnesUrn();
        	System.out.println("POOR");}//search intelligence doesn't mean anything when there is only one thing out there
	    
	    else if (enviroType.equals("ENDOGENOUS_P") && searchIntel.equals("SELECTIVE"))
    		{ urn = new EndogenousUrn(ballTypes, countPerType, true);
    		System.out.println("Urn type Endo, ones");}
	    
	    else if (enviroType.equals("ENDOGENOUS_R") && searchIntel.equals("SELECTIVE"))
    		{ urn = new EndogenousUrn(ballTypes, countPerType, false);
    		System.out.println("Urn type Endo");}
	    
	    else if (enviroType.equals("RICH") && searchIntel.equals("SELECTIVE"))
    		{ urn = new InfiniteUrn();
    		System.out.println("RICH");}    

	    return urn;
	}
	
	/**
	 * This method distributes exactly the specified number of rules randomly into the rule bags for the ALL chemistry.
	 * If there n types of balls, there are n^2 - n valid rules (i->i rules are removed). The total desired number of rules
	 * divided by the number of combinations often yields a real number, so we round down and add back in the missing number
	 * of rules at random. This is done by giving each combination the appropriate probability of adding another rule instance using
	 * a list of booleans.
	 * @param ballTypes
	 * @param countRules
	 */
	
	public void createALLchem(int ballTypes, int countRules){
		int ruleCombos = ((int) Math.pow(ballTypes,2) - ballTypes);
		int countPerType = (int) Math.floor(((double)countRules)/((double)ruleCombos));
		int missing = countRules - (ruleCombos*countPerType); // # rules less than countRules because of rounding
		
		ArrayList<Boolean> chances = new ArrayList<Boolean>();
		for (int i=0; i < missing;i++) chances.add(true);
		for (int i=0; i< ruleCombos-missing;i++) chances.add(false);
		SimUtilities.shuffle(chances, RandomHelper.getUniform());
		Iterator<Boolean> myiter = chances.iterator();// This iterator will let the combinations in the for-loop below
		// add an additional rule randomly to yield the exact number of rules.
		
		for (int input = 1; input <= ballTypes; input++) {
		      for (int output = 1; output <= ballTypes; output++) {
		        if (input != output) {
		        	distributeProductionRule(input, output, countPerType);
		        	if (myiter.next()) distributeProductionRule(input,output,1);
		        }
		      }
		}
	}
	
	/**
	 * This method distributes exactly the specified number of rules randomly into the rule bags for the SOLOH chemistry.
	 * There is one possible rule for each type of ball. The total desired number of rules
	 * divided by the number of combinations often yields a real number, so we round down and add back in the missing number
	 * of rules at random. This is done by giving each combination the appropriate probability of adding another rule instance using
	 * a list of booleans.
	 * @param ballTypes
	 * @param countRules
	 */
	
	public void createSOLOchem(int ballTypes, int countRules){
		int countPerType = (int) Math.floor(((double)countRules)/((double)ballTypes));
		int missing = countRules - (ballTypes*countPerType); // # rules less than countRules because of rounding
		
		ArrayList<Boolean> chances = new ArrayList<Boolean>();
		for (int i=0; i < missing;i++) chances.add(true);
		for (int i=0; i< ballTypes-missing;i++) chances.add(false);
		SimUtilities.shuffle(chances, RandomHelper.getUniform());
		Iterator<Boolean> myiter = chances.iterator(); // This iterator will let the combinations in the for-loop below
		// add an additional rule randomly to yield the exact number of rules.
		for (int input = 1; input < ballTypes; input++) {
		      int output = input + 1;
		      distributeProductionRule(input, output, countPerType);
		      if (myiter.next()) distributeProductionRule(input,output,1);
		}
		distributeProductionRule(ballTypes, 1, countPerType);
		if (myiter.next()) distributeProductionRule(ballTypes,1,1);
		
	}
	
	
	public void distributeProductionRule(int input, int output, int countRules){
    	for (int i = 0; i < countRules; i++) {
    		int index = RandomHelper.nextIntFromTo(0, pbags.size()-1);
    		ProductionRuleBag mybag = pbags.get(index);
    		ProductionRule r = new ProductionRule(input, output, mybag);
    		rules.add(r);//Does this actually get used?
    		mybag.addRule(r);
    	}
	}
	
	
}