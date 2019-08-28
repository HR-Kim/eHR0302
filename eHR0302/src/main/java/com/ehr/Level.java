package com.ehr;
/**
 * NextLevel 추가
 * @author sist
 *
 */
public enum Level {
	
	//커밋확인
	
	GOLD(3,null), SILVER(2,GOLD), BASIC(1,SILVER);
	
	private final int value;
	private final Level nextLevel;
	
	
	Level(int value,Level next){
		this.value = value;
		nextLevel = next;
	}
	
	
	public Level getNextLevel() {
		return nextLevel;
	}
	
	
	//DB Insert시 사용
	public int intValue() {
		return value;
	}

	
	public static Level valueOf(int value) {
		switch(value) {
			case 1: return BASIC;
			case 2: return SILVER;
			case 3: return GOLD;
			default: throw new AssertionError("Unknown value:"+value);
		}
	}
	
	
}
