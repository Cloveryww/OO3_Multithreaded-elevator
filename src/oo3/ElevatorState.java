package oo3;

public enum ElevatorState {//MOVE he FIRSTSTILL 都有方向，而WFS和STILL没有，FIRSTSTILL的方向是之前的运动方向
		MOVE, STILL, WFS, FIRSTSTILL;
	public String EnumToString() 
	{ 
	    String tmpStr = ""; 
	    switch(this) 
	    { 
	        case MOVE: 
	        	tmpStr = "MOVE"; 
	        	break; 
	        case STILL:
	        	tmpStr = "STILL"; 
	        	break;
	        case WFS:
	        	tmpStr = "WFS"; 
	        	break;
	        case FIRSTSTILL:
	        	tmpStr = "FIRSTSTILL";
	        	break;
	        default:
	        	break;
	    } 
	    return tmpStr; 
	} 
}

