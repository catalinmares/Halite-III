package hlt;

public class Dial {
	public final int id;
	public final int xUpper;
	public final int xLower;
	public final int yUpper;
	public final int yLower;
	
	public Dial(int id, int xLower, int xUpper, int yLower, int yUpper) {
		this.id = id;
		this.xUpper = xUpper;
		this.xLower = xLower;
		this.yUpper = yUpper;
		this.yLower = yLower;
	}
	
	public boolean contains(Position position) {
		if (position.x >= xLower && position.x <= xUpper && 
			position.y >= yLower && position.y <= yUpper) 
		{
			return true;
		}

		return false;
	}
}
