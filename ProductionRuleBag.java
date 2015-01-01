package ac_lang;



public class ProductionRuleBag extends RuleBag{

	public final int ballTypes;
		
	public ProductionRuleBag(int ballTypes){
		super();
		this.ruletype = "production";
		this.ballTypes = ballTypes;
	}
	
	/**
	 * There is no addRule method in the superclass so that we can 
	 * make sure the bags have only the right type of rule.
	 * @param prule
	 */
	
	public void addRule(ProductionRule prule){
		int index = (Integer) prule.getIndex();
		if (ruleMap.containsKey(index)){
			Rules rules = (Rules) ruleMap.get(index);
			rules.add(prule);}
		else{
			Rules newrules = new Rules(prule);
			ruleMap.put(index, newrules);
			updateNetwork(prule);
		}
		
	}

	public void setOwner(Cell cell){
		this.owner = cell;
	}

		
}
	
