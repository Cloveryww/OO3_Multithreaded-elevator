package oo3;

public enum ElevatorState {//MOVE he FIRSTSTILL ���з��򣬶�WFS��STILLû�У�FIRSTSTILL�ķ�����֮ǰ���˶�����
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

