package ac_lang;


public class DeliveringRuleBag extends RuleBag{
	
	public DeliveringRuleBag(){
		super();
		this.ruletype = "delivering";
	}
	
	
	public void addRule(DeliveringRule vrule){
		int index = (Integer) vrule.getIndex();
		Rules rules = (Rules) ruleMap.get(index);
		rules.add(vrule);
	}

	public void setOwner(Cell cell){
		this.owner = cell;
	}
}
