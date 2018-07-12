package oo3;

import java.io.PrintWriter;
import java.text.DecimalFormat;

public class Output {
	
	
	public void output(Request rq, int curFloor,  ElevatorState e_state, double time)//[request]/（n,STILL,t）
	{
			System.out.println(rq.toString2()+"/("+Integer.toString(curFloor)+","+e_state.EnumToString()+","+Double.toString(time)+")");
	}
	
	
	/*
	 * if(ElevatorSys.isDebug())
                    {
                    	System.out.println(st+ ":["+curQueue.peek().toString()+", "+df.format(curQueue.peek().getTime()/1000.0)+
                    		"] / (#"+Integer.toString(this.ele_id)+", " +Integer.toString(this.curfloor)+
                            ", "+this.e_dir+", "+Integer.toString(this.aom)+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
                    }
                    printer.println(st+ ":["+curQueue.peek().toString()+", "+df.format(curQueue.peek().getTime()/1000.0)+
                    		"] / (#"+Integer.toString(this.ele_id)+", " +Integer.toString(this.curfloor)+
                            ", "+this.e_dir+", "+Integer.toString(this.aom)+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
	 */
	public void output(PrintWriter pw,long st, Elevator ele,Request req)//多线程输出
	{
		DecimalFormat df = new DecimalFormat("0.0");//格式化long
		if(ElevatorSys.isDebug())
        {
        	System.out.println(st+":["+req.toString()+", "+df.format(req.getTime()/1000.0)+
        		"] / (#"+Integer.toString(ele.getEle_id())+", " +Integer.toString(ele.getCurfloor())+
                ", "+ele.getE_dir()+", "+Integer.toString(ele.getAom())+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
        }
        pw.println(st+":["+req.toString()+", "+df.format(req.getTime()/1000.0)+
        		"] / (#"+Integer.toString(ele.getEle_id())+", " +Integer.toString(ele.getCurfloor())+
                ", "+ele.getE_dir()+", "+Integer.toString(ele.getAom())+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
	}
	public void invalidoutput(String rq)//可能格式有问题
	{
		System.out.println("INVALID ["+rq+"]");
	}
	public void sameoutput(Request rq)
	{
		System.out.println("# SAME "+rq.toString2());
	}
	public void sameoutput(PrintWriter pw,long systime, Request req)//多线程same输出
	{
		DecimalFormat df = new DecimalFormat("0.0");//格式化long
		if(ElevatorSys.isDebug())
		{
			System.out.println("#"+systime+":SAME [" +req.toString()+", "+df.format(req.getTime() / 1000.0)+"]");
		}
		pw.println("#"+systime+":SAME [" +req.toString()+", "+df.format(req.getTime() / 1000.0)+"]");
	}
	

}
