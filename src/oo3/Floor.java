package oo3;

public class Floor {
	private static int topFloor;
	private static int bottomFloor;
	private Boolean[] upButton;
	private Boolean[] downButton;
	Floor(int minfloor,int maxfloor) {
		Floor.topFloor = maxfloor;
		Floor.bottomFloor = minfloor;
		this.upButton = new Boolean[maxfloor-minfloor+1+1];//0����
		this.downButton = new Boolean[maxfloor-minfloor+1+1];//0����
		for(int i=0;i<maxfloor-minfloor+1;i++)
		{
			this.upButton[i] = false;
			this.downButton[i] = false;
		}
	}
	public synchronized void lightOn(int i,Direction direct) {//synchronized��֤����ȵĲ�����ԭ�Ӳ���
		if (direct==Direction.UP) 
			upButton[i] = true;
		else 
			downButton[i] = true;
	}
	public synchronized void lightOff(int i,Direction direct) {//synchronized��֤����ȵĲ�����ԭ�Ӳ���
	    if (direct==Direction.UP) 
	    	upButton[i] = false;
	    else 
	    	downButton[i] = false;
    }
    public synchronized boolean islightOn(int i,Direction direction) {//synchronized��֤����ȵĲ�����ԭ�Ӳ���
	    if (direction==Direction.UP) 
	    	return upButton[i];
	    else 
	    	return downButton[i];
    }
	public static int getTopFloor() {
		return topFloor;
	}
	public static int getBottomFloor() {
		return bottomFloor;
	}
	public boolean setDownbutton(boolean b)
	{
		return true;
	}
	public boolean getDownbutton()
	{
		return true;
	}
	public boolean setUpbutton(boolean b)
	{
		return true;
	}
	public boolean getUpbutton()
	{
		return true;
	}
}
