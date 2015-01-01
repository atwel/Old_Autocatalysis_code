package ac_lang;

public class ProductionRule implements AbstractRule {
	
	private int input;
	private int output;
	private final ProductionRuleBag owner;
	
	
	public ProductionRule(int input, int output, ProductionRuleBag owner){
		this.input = input;
		this.output = output;
		this.owner = owner;
	}
	
	
	public NetRule createNetRule(){
		NetRule netRule = new NetRule(input, output, owner);
		return netRule;
	}
	
	public void activate(){
		owner.setActiveRule(this);
	}
	
	public boolean receiveBall(Cell nghCell, Integer ball){
		return true;
	}
	
	public void reproduce(){
		ProductionRule newrule = new ProductionRule(this.input, this.output, this.owner);
		owner.addRule(newrule);
	}

	
	
	public int getIndex(){
		return this.input;
	}

	public int getInput(){
		return input;
	}
	
	public int getOutput(){
		return output;
	}
	
	public RuleBag getOwner(){
		return owner;
	}
	
	
	public String toString(){
		return "jk";
	}
	
}
	
	
