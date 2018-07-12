package oo3;

public enum Direction {
	UP,DOWN,STILL;
	public String EnumToString() 
	{ 
	    String tmpStr = ""; 
	    switch(this) 
	    { 
	        case UP: 
	        	tmpStr = "UP"; 
	        	break; 
	        case STILL:
	        	tmpStr = "STILL"; 
	        	break;
	        case DOWN:
	        	tmpStr = "DOWN"; 
	        	break;
	        default:
	        	break;
	    } 
	    return tmpStr; 
	} 
}
