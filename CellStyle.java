package ac_lang;

import java.awt.Color;

//import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;

public class CellStyle extends DefaultStyleOGL2D{
	
	
	@Override
    public Color getColor(final Object agent) {
            if (agent instanceof Cell) {
                    final Cell cell = (Cell) agent;
                    final double val = cell.getColor();
                    final int strength = (int) Math.min(255 * val, 255);
                    return new Color(0, strength, 0);
            }

            return super.getColor(agent);
	}
	
	@Override
	public Color getBorderColor(final Object agent){
		if (agent instanceof Cell) {
			return ((Cell) agent).getBorderColor();
        }
		return super.getColor(agent);
	}
	
	@Override
	public int getBorderSize(final Object agent){
		return 5;
	}
	
	@Override
	public float getScale(final Object agent){
		return 5.75f;
	}
	
	@Override
	public Position getLabelPosition(final Object agent){
		System.out.println("South");
		return Position.SOUTH;
	}
	
	//@ScheduledMethod(start=1, interval=1)
	@Override
	public String getLabel(final Object agent){
		Cell cellAgent = (Cell) agent;
		String val = String.valueOf(cellAgent.getNumRules());
		//System.out.print("gl ");
		//System.out.println(val);
		return val;
	}
	
	@Override
	public Color getLabelColor(final Object agent){
		//System.out.println("gLC");
		return new Color(0, 255, 255);
	}
	
}
