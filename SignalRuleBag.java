package ac_lang;


public class SignalRuleBag extends RuleBag{
	
	public SignalRuleBag(){
		super();
		this.ruletype = "signal";
	}
	
	/**
	 * There is no addRule method in the superclass so that we can 
	 * make sure the bags have only the right type of rule.
	 * @param srule
	 */
	
	public void addRule(SignalRule srule){
		int index = (Integer) srule.getIndex();
		Rules rules = (Rules) ruleMap.get(index);
		rules.add(srule);
	}

	public void setOwner(Cell cell){
		this.owner = cell;
	}
		

}
