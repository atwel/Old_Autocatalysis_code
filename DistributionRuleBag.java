package ac_lang;



public class DistributionRuleBag extends RuleBag{

	public DistributionRuleBag(){
		super();
		this.ruletype = "distribution";
	}
	
	/**
	 * There is no addRule method in the superclass so that we can 
	 * make sure the bags have only the right type of rule.
	 * @param prule
	 */
	
	public void addRule(DistributionRule trule){
		int index = (Integer) trule.getIndex();
		Rules rules = (Rules) ruleMap.get(index);
		rules.add(trule);
	}

	public void setOwner(Cell cell){
		this.owner = cell;
	}
		
}
