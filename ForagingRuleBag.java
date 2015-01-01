package ac_lang;


public class ForagingRuleBag extends RuleBag {

	public ForagingRuleBag(){
		super();
		this.ruletype = "production";
	}
	
	/**
	 * There is no addRule method in the superclass so that we can 
	 * make sure the bags have only the right type of rule.
	 * @param frule
	 */
	
	public void addRule(ForagingRule frule){
		int index = (Integer) frule.getIndex();
		Rules rules = (Rules) ruleMap.get(index);
		rules.add(frule);
	}

	public void setOwner(Cell cell){
		this.owner = cell;
	}
		
}
