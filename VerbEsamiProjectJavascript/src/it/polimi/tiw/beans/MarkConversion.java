package it.polimi.tiw.beans;

public class MarkConversion {

	public static String getMarkInfo(int value) {
		if (value >= 18 && value <= 30) {
			return Integer.toString(value);
		}
		
		switch(value) {
		case 0:
			return "<empty>";
		case 1:
			return "absent";
		case 2:
			return "failed";
		case 3:
			return "skip next round";
		case 31:
			return "30 with honor";
		}
		
		return "Error";
	}
}
