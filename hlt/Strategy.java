package hlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

public class Strategy {
	public static ArrayList<Dial> dials;
	public static HashMap<EntityId, String> statuses = new HashMap<EntityId, String>();
	public static HashMap<EntityId, Dial> dialsForShips = new HashMap<EntityId, Dial>();
	public static HashMap<EntityId, Stack<Direction>> stacks = new HashMap<EntityId, Stack<Direction>>();

	public static void initDials(Game game) {
		final Position shipyardPos = game.me.shipyard.position;
		dials = new ArrayList<Dial>();
		
		for (int dialId = 1; dialId <= Constants.MAX_DIALS; dialId++) {
			switch(dialId) {
				case 1:
					dials.add(new Dial(dialId, 0, shipyardPos.x, 0, shipyardPos.y - 1));
					break;
				case 2:
					dials.add(new Dial(dialId, shipyardPos.x + 1, game.gameMap.width - 1, 0, shipyardPos.y));
					break;
				case 3:
					dials.add(new Dial(dialId, shipyardPos.x, game.gameMap.width - 1, 
							shipyardPos.y + 1, game.gameMap.height - 1));
					break;
				case 4:
					dials.add(new Dial(dialId, 0, shipyardPos.x - 1, shipyardPos.y, game.gameMap.height - 1));
					break;
			}
		}
	}
	
	public static Position setTarget(GameMap gameMap, Dial dial) {
		int maxHalite = Integer.MIN_VALUE;
		int xMax = -1;
		int yMax = -1;
		
		for (int i = dial.xLower; i <= dial.xUpper; i++) {
			for (int j = dial.yLower; j <= dial.yUpper; j++) {
				if (gameMap.cells[i][j].halite > maxHalite) {
					maxHalite = gameMap.cells[i][j].halite;
					xMax = i;
					yMax = j;
				}
			}
		}
		
		return new Position(xMax, yMax);
	}
	
	public static Dial findDial(Collection<Ship> ships) {
		for (Dial dial : dials) {
			boolean shipFound = false;
			//Log.log("Dial " + dial.id + " has limits: x = [" + dial.xLower + ", " + dial.xUpper + "]"
					//+ "\ty = [" + dial.yLower + ", " + dial.yUpper + "]");
			for (Ship ship : ships) {
				//Log.log("Ship " + ship.id + " is at position " + ship.position);
				if (dial.contains(ship.position)) {
					shipFound = true;
					
					//Log.log("Dial " + dial.id + " contains ship " + ship.id);
					break;
				}
			}
			
			if (!shipFound) {
				Log.log("No ship was found on dial " + dial.id + ". The new ship will have dial " + dial.id);
				return dial;
			}
		}
		
		return null;
	}
	
	public static int calcHaliteSum(GameMap gameMap, String direction, int start, int stop, int fixed) {
		final MapCell[][] cells = gameMap.cells;
		int sum = 0;
		
		if (direction.equals("line")) {
			for (int i = start; i <= stop; i++) {
				sum += cells[fixed][i].halite;
			}
		} else {
			for (int i = start; i <= stop; i++) {
				sum += cells[i][fixed].halite;
			}
		}

		return sum;
	}
	
	public static String getProfitableDirection(GameMap gameMap, Ship ship) {
		int haliteSumLine;
		int haliteSumColumn;

		switch(dialsForShips.get(ship.id).id) {
			case 1:
				haliteSumLine = calcHaliteSum(gameMap, "line", 0, ship.position.x, ship.position.y);
				haliteSumColumn = calcHaliteSum(gameMap, "column", 0, ship.position.y, ship.position.x);
				break;
			case 2:
				haliteSumLine = calcHaliteSum(gameMap, "line", ship.position.x, gameMap.width - 1, ship.position.y);
				haliteSumColumn = calcHaliteSum(gameMap, "column", 0, ship.position.y, ship.position.x);
				break;
			case 3:
				haliteSumLine = calcHaliteSum(gameMap, "line", ship.position.x, gameMap.width - 1, ship.position.y);
				haliteSumColumn = calcHaliteSum(gameMap, "column", ship.position.y, gameMap.height - 1, ship.position.x);
				break;
			default:
				haliteSumLine = calcHaliteSum(gameMap, "line", 0, ship.position.x, ship.position.y);
				haliteSumColumn = calcHaliteSum(gameMap, "column", ship.position.y, gameMap.height - 1, ship.position.x);
		}
		
		Log.log("Halite calculated on the line was " + haliteSumLine);
		Log.log("Halite calculated on the column was " + haliteSumColumn);
		
		if (haliteSumLine > haliteSumColumn) {
			return "line";
		} else {
			return "column";
		}
	}
	
	public static Direction getLineMove(Ship ship) {
		switch(dialsForShips.get(ship.id).id) {
			case 1:
				return Direction.WEST;
			case 2:
				return Direction.EAST;
			case 3:
				return Direction.EAST;
			default:
				return Direction.WEST;
		}
	}
	
	public static Direction getColumnMove(Ship ship) {
		switch(dialsForShips.get(ship.id).id) {
			case 1:
				return Direction.NORTH;
			case 2:
				return Direction.NORTH;
			case 3:
				return Direction.SOUTH;
			default:
				return Direction.SOUTH;
		}
	}
	
	public static Direction getEffectiveDirection(Ship ship, String direction) {
		switch(direction) {
			case "line":
				return getLineMove(ship);
			default:
				return getColumnMove(ship);
		}
	}
	
	public static Command moveShip(Ship ship, Game game) {
		final Position shipyardPos = game.me.shipyard.position;
		final Direction direction;
		
		Log.log("Ship " + ship.id + "is at position " + ship.position + " and  has " + ship.halite + " on board");
		Stack<Direction> stack = stacks.get(ship.id);
		
		if (stacks.get(ship.id) != null && !stacks.get(ship.id).isEmpty()) {
			Log.log("Ship " + ship.id + " direction stack is:");
			
			for (Direction dir : stack) {
				Log.log("" + dir);
			}
		} else {
			Log.log("Ship " + ship.id + " has an empty direction stack");
		}
		
		// CAZUL IN CARE NAVA E IN SHIPYARD
		if (ship.position.equals(shipyardPos)) {
			if (!dialsForShips.containsKey(ship.id)) {
				dialsForShips.put(ship.id, findDial(game.me.ships.values()));
			}
			
			if (!stacks.containsKey(ship.id)) {
				stacks.put(ship.id, new Stack<Direction>());
			}
			
			statuses.put(ship.id, "Out of shipyard");
			
			Log.log("Ship " + ship.id + " is on the shipyard and will go on dial " + dialsForShips.get(ship.id).id + " with status " + statuses.get(ship.id));
			
			switch(dialsForShips.get(ship.id).id) {
				case 1:
					direction = Direction.NORTH;
					break;
					
				case 2:
					direction = Direction.EAST;
					break;
					
				case 3:
					direction = Direction.SOUTH;
					break;
					
				default:
					direction = Direction.WEST;
			}
			
			Stack<Direction> shipStack = stacks.get(ship.id);
			shipStack.push(direction);
			
			Log.log("After getting out of the shipyard, ship " + ship.id + " has " + shipStack.size() + " directions on stack");
			stacks.put(ship.id, shipStack);
			
			return ship.move(direction);
		}

		Log.log("Ship " + ship.id + " is not on the shipyard and has status " + statuses.get(ship.id));
		//Log.log("Ship currently has set dial " + dialsForShips.get(ship.id).id);

		// CAZUL IN CARE NAVA ABIA A IESIT DIN SHIPYARD
		if (statuses.get(ship.id).equals("Out of shipyard")) {
			Log.log("Ship " + ship.id + " just left the shipyard");
				
			if (!ship.canMove(game.gameMap.at(ship)) ||
				game.gameMap.at(ship).halite >= 50)
			{
				return ship.stayStill();
			}
				
			String nextDirection = getProfitableDirection(game.gameMap, ship);
				
			Log.log("After checking sums, ship " + ship.id + " decides to go on " + nextDirection);
				
			Direction effectiveDirection = getEffectiveDirection(ship, nextDirection);
			statuses.put(ship.id, "Mining");
			
			Log.log("Ship " + ship.id + " decided to go " + effectiveDirection);
				
			Stack<Direction> shipStack = stacks.get(ship.id);
			shipStack.push(effectiveDirection);
			stacks.put(ship.id, shipStack);
				
			// Aici caut target!
				
			return ship.move(effectiveDirection);
		}

		Log.log("Ship " + ship.id + " is not close to the shipyard");

		// CAZUL IN CARE NAVA E PE HARTA SI MINEAZA
		if (statuses.get(ship.id).equals("Mining")) {
			Log.log("Ship " + ship.id + " is currently in mining state");

			// CAZUL IN CARE S-A UMPLUT NAVA
			if (ship.isFull()) {
				statuses.put(ship.id, "Depositing");
						
				Stack<Direction> shipStack = stacks.get(ship.id);
				Direction startReturning = shipStack.pop();
				stacks.put(ship.id, shipStack);
						
				Log.log("Ship " + ship.id + " is full. It will now go back " + startReturning.invertDirection());
						
				return ship.move(startReturning.invertDirection());
			}

			Log.log("Ship " + ship.id + " did not gather enough halite yet");

			// CAZUL IN CARE TREBUIE SA RAMANA SA MINEZE PE POZITIA CURENTA
			if (game.gameMap.at(ship).halite >= 50) {
				Log.log("Ship decided to keep gathering halite from position " + ship.position);

				return ship.stayStill();
			}

			Stack<Direction> shipStack = stacks.get(ship.id);
			Direction newDirection = shipStack.peek();

			// CAZUL IN CARE MINEAZA SI A AJUNS LA MARGINEA HARTII
			if (!dialsForShips.get(ship.id).contains(ship.position.directionalOffset(newDirection))) {
				statuses.put(ship.id, "Depositing");

				// CAZUL IN CARE NAVA SE INTOARCE DE LA MARGINEA HARTII SI ARE HALITE SA SE MUTE
				if (ship.canMove(game.gameMap.at(ship))) {
					shipStack.pop();
					stacks.put(ship.id, shipStack);
					
					Log.log("Ship " + ship.id + " reached the limit of the dial. It will get back " + newDirection.invertDirection());
					
					return ship.move(newDirection.invertDirection());
				}
				
				// CAZUL IN CARE NAVA SE INTOARCE DE LA MARGINEA HARTII SI NU ARE HALITE SA SE MUTE
				Log.log("Ship " + ship.id + " reached the limit of the dial, but doesn't afford a move to go back");
				return ship.stayStill();
			}

			Log.log("Ship " + ship.id + " continues going " + newDirection);
			
			
			// CAZUL IN CARE MUT NAVA PE URMATOAREA POZITIE SI ARE SUFICIENT HALITE SA SE MUTE
			if (ship.canMove(game.gameMap.at(ship))) {
				shipStack.push(newDirection);
				stacks.put(ship.id, shipStack);
				
				Log.log("Ship " + ship.id + " continues navigating " + newDirection);
				
				return ship.move(newDirection);
			}

			// CAZUL IN CARE MUT NAVA PE URMATOAREA POZITIE SI NU ARE SUFICIENT HALITE SA SE MUTE
			Log.log("Ship wants to keep going further but doesn't afford the move");
			return ship.stayStill();
		}

		Log.log("Ship " + ship.id + " is currently returning to the shipyard");

		// CAZUL IN CARE NAVA SE INTOARCE IN SHIPYARD SI E LANGA SHIPYARD
		if (ship.position.getSurroundingCardinals().contains(shipyardPos)) {
			Log.log("Ship " + ship.id + " is next to the shipyard");
			
			// CAZUL IN CARE SHIPYARD-UL ESTE OCUPAT
			if (game.gameMap.at(game.me.shipyard).isOccupied()) {
				Log.log("Shipyard is currently occupied. Ship " + ship.id + " decides to wait");
				return ship.stayStill();
			}
			
			// CAZUL IN CARE NAVA SE INTOARCE IN SHIPYARD
			if (ship.canMove(game.gameMap.at(ship))) {
				Log.log("Shipyard is free. Ship " + ship.id + " moves to the shipyard");
				
				Stack<Direction> shipStack = stacks.get(ship.id);
				Direction nextDirection = shipStack.pop();
				stacks.put(ship.id, shipStack);
							
				return ship.move(nextDirection.invertDirection());
			}
			
			// CAZUL IN CARE NAVA VREA SA INTRE IN SHIPYARD DAR NU ARE SUFICIENT HALITE
			Log.log("Shipyard is free, but ship " + ship.id + " can't enter the shipyard because it doesn't afford the move");
			return ship.stayStill();
		}

		// CAZUL IN CARE NAVA TREBUIE SA SE INTOARCA SI NU E LANGA SHIPYARD
		if (ship.canMove(game.gameMap.at(ship))) {
			Log.log("Ship " + ship.id + " is not close to the shipyard. Moving closer");
			
			Stack<Direction> shipStack = stacks.get(ship.id);
			Direction returnDirection = shipStack.pop();
			stacks.put(ship.id, shipStack);

			return ship.move(returnDirection.invertDirection());
		}
		
		// CAZUL IN CARE NAVA TREBUIE SA SE INTOARCA DAR NU ARE SUFICIENT HALITE PENTRU MUTARE
		return ship.stayStill();
	}

	public static ArrayList<Command> planStrategy(Game game, Random rng) {
		final ArrayList<Command> commandQueue = new ArrayList<Command>();
		final GameMap gameMap = game.gameMap;
		final Player me = game.me;
		
		if (dials == null) {
			initDials(game);
			Log.log("Initialized dials");
			Log.log("Shipyard is at position " + me.shipyard.position);
		}

		for (Ship ship : me.ships.values()) {
			commandQueue.add(moveShip(ship, game));
		}
		
		if (me.ships.values().size() < Constants.MAX_SHIPS_ON_MAP && 
			!gameMap.at(me.shipyard).isOccupied() && me.halite >= 1000) 
		{
			commandQueue.add(me.shipyard.spawn());
			Log.log("Spawned a new ship");
		}

        return commandQueue;
	}
}
