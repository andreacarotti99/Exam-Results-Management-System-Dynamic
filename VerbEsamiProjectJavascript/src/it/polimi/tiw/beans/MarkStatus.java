package it.polimi.tiw.beans;


//Enum: https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
public enum MarkStatus {
	NOT_INSERTED(0),
	INSERTED(1),
	PUBLISHED(2),
	REJECTED(3),
	RECORDED(4);
	

	private final int value;

	MarkStatus(int value) {
		this.value = value;
	}

	public static MarkStatus getMarkStatusFromInt(int value) {
		switch (value) {
		case 0:
			return MarkStatus.NOT_INSERTED;
		case 1:
			return MarkStatus.INSERTED;
		case 2:
			return MarkStatus.PUBLISHED;
		case 3:
			return MarkStatus.REJECTED;
		case 4:
			return MarkStatus.RECORDED;
		}
		return null;
	}

	public int getValue() {
		return value;
	}
}