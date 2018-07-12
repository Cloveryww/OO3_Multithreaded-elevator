package oo3;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scheduler {
	private Floor[] floor=new Floor[10];
	Scheduler()
	{
		super();
		for(int i=0;i<10;i++)
		{
			//this.floor[i]= new Floor();
		}
		
	}
	public Request string2Req(String str)
	{
	 	/*@REQUIRES：isValidReq(str)&&curTime >= 0;
		  @EFFECTS：(\result.type = FR || \result.type = ER)&&
		  			(\result.fRdir = UP || \result.fRdir=DOWN)&&
		  			(\result.floorNo >=1 && \result.floorNo <= 20)&&
		  			(\result.e_time = curTime)&&
		  			(\result.type = ER ==> \result.ele_id >=1 && \result.ele_id <=3)	  			
		*/ 
		Request req=null;
		String[] terms=str.split("\\(|\\)|\\,");
		int len=terms.length;
		if(len==5)//(FR,3,DOWN,1000)
		{
			try {
			ReqType type = ReqType.FR;
			int floorNo = Integer.parseInt(terms[2]);
			Direction fRdir;
			int time = Integer.parseInt(terms[4]);
			if(terms[3].equals("UP"))
			{
				fRdir = Direction.UP;
			}
			else
			{
				fRdir = Direction.DOWN;
			}
			req = new Request(type, floorNo,  fRdir,  time);
			} catch (Exception e) {
			    return null;
			}
		}else if(len==4)//(ER,3,1000)
		{
			try {
			ReqType type = ReqType.ER;
			int floorNo = Integer.parseInt(terms[2]);
			int time = Integer.parseInt(terms[3]);
			//req = new Request(type, floorNo,  time);
			} catch (Exception e) {
			    return null;
			}
		}
		else//error 正常不可能出现
		{
			System.out.println("no possible");
		}    	
		return req;
	}
	public Boolean inputReqs(RequestList reqlist)//获取输入的请求，生成请求对象
	{
	 	/*@REQUIRES：reqlist!=null;
		  @MODIFYIED： reqlist;
		  @EFFECTS：	input string to produce req and put them into reqlist;
		  */ 
		int firstTag=0;
		Request curreq;
		ArrayList<String> errorStr = new ArrayList<String>();
		Scanner sc=new Scanner(System.in);  
    	do {
		String str = sc.nextLine();
    	String str_no_space = str.replaceAll("\\s+", "");//删除所有空格
    	if(str_no_space.length()==0)//空行
    	{
    		errorStr.add(str_no_space);
    		continue;
    	} 	
    	if(str_no_space.equals("RUN"))
    	{
    		if(firstTag==0)//error
    		{
    			sc.close();
    			return false;
    		}
    		else
    		{
    			for(int i=0;i<errorStr.size();i++)
    			{
    				new Output().invalidoutput(errorStr.get(i));
    			}
    			sc.close();
    			return true;	
    		}
    	}
    	//先检查是否为合法格式的请求
    	Pattern p0=Pattern.compile("\\([EF]R,[+]?(\\d+),((UP|DOWN),)?[+]?(\\d+)\\)");
    	Matcher m0=p0.matcher(str_no_space);
    	if(!m0.matches())
    	{
    		errorStr.add(str_no_space);
    		continue;
    	}
    	curreq = string2Req(str_no_space);//String 转Req
    	if(curreq==null)//不合法
    	{
    		errorStr.add(str_no_space);
    		continue;
    	}
    	if(firstTag==0)//第一个合法请求还没到
    	{
    		//是第一个合法输入(FR,1,UP,0)
    		if(curreq.getType()==ReqType.FR&&curreq.getFloorNo()==1&&curreq.getFRdir()==Direction.UP&&curreq.getTime()==0)
    		{
    			firstTag = 1;
    			reqlist.addReq(curreq);
    		}
    		else//invalid 输入
    		{
    			new Output().invalidoutput(str_no_space);
    		}
    	}
    	else//第一个和发请求已经到了
    	{
    		reqlist.addReq(curreq);
    	}
    	}while (true);
	}
	
	public void deleteInvalidReqs(RequestList reqlist)//删除所有不合法的请求，并输出invalid信息
	{
		for(int i = 1 ;i<reqlist.getListlen();i++)
		{
			if(reqlist.getReq(i).getTime()<reqlist.getReq(i-1).getTime())//输入不是按时间递增的
			{
				new Output().invalidoutput(reqlist.getReq(i).toString());
				reqlist.deleteReq(i);
				i--;
				continue;
			}
			if(reqlist.getReq(i).getFloorNo()<1||reqlist.getReq(i).getFloorNo()>10)
			{
				new Output().invalidoutput(reqlist.getReq(i).toString());
				reqlist.deleteReq(i);
				i--;
				continue;
			}
			if(reqlist.getReq(i).getType()==ReqType.FR)
			{
				
				if((reqlist.getReq(i).getFRdir()==Direction.UP&&reqlist.getReq(i).getFloorNo()==10)||(reqlist.getReq(i).getFRdir()==Direction.DOWN&&reqlist.getReq(i).getFloorNo()==1))
				{
					new Output().invalidoutput(reqlist.getReq(i).toString());
					reqlist.deleteReq(i);
					i--;
					continue;
				}
			}	
		}
	}

	public void findshaodai(int curFloor,Direction dir, Request mainReq, RequestList e_waitlist)//从等待队列中找到当前主请求可稍待的请求，并标记
	{
		for(int i =0;i<e_waitlist.getListlen();i++)
		{
			e_waitlist.getReq(i).setStatus(0);
		}
		if(dir == Direction.UP)
		{
			for(int i =0;i<e_waitlist.getListlen();i++)
			{
				//可稍待
				if(e_waitlist.getReq(i)==mainReq)
				{
					continue;
				}
				if(((e_waitlist.getReq(i).getType()==ReqType.FR)&&(e_waitlist.getReq(i).getFRdir()==Direction.UP)
						&&(e_waitlist.getReq(i).getFloorNo()>curFloor)&&(e_waitlist.getReq(i).getFloorNo()<=mainReq.getFloorNo()))||
						((e_waitlist.getReq(i).getType()==ReqType.ER)&&(e_waitlist.getReq(i).getFloorNo()>curFloor)))
				{
					e_waitlist.getReq(i).setStatus(1);
				}
			}
		}
		else//DOWN
		{
			for(int i =0;i<e_waitlist.getListlen();i++)
			{
				//可稍待
				if(e_waitlist.getReq(i)==mainReq)
				{
					continue;
				}
				if(((e_waitlist.getReq(i).getType()==ReqType.FR)&&(e_waitlist.getReq(i).getFRdir()==Direction.DOWN)
						&&(e_waitlist.getReq(i).getFloorNo()<curFloor)&&(e_waitlist.getReq(i).getFloorNo()>=mainReq.getFloorNo()))||
						((e_waitlist.getReq(i).getType()==ReqType.ER)&&(e_waitlist.getReq(i).getFloorNo()<curFloor)))
				{
					e_waitlist.getReq(i).setStatus(1);
				}
			}
		}
	}
 	public Boolean isSame(Request req, Elevator elevator)//如果不same，则按下相应按钮，返回false，如果same，则返回true
	{
		if(req.getType()==ReqType.FR)
		{
			
			if(req.getFRdir()==Direction.UP)
			{
				if(this.floor[req.getFloorNo()-1].getUpbutton())
				{
					return true;//same
				}
				else
				{
					this.floor[req.getFloorNo()-1].setUpbutton(true);
					return false;
				}
			}
			else//DOWN
			{
				if(this.floor[req.getFloorNo()-1].getDownbutton())
				{
					return true;//same
				}
				else
				{
					this.floor[req.getFloorNo()-1].setDownbutton(true);
					return false;
				}
			}
			
		}else//ER
		{
			if(elevator.pushButton(req.getFloorNo()-1))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}
	/*public Boolean cancelButton(Request req,Elevator ele)//熄灭请求所按按钮
	{
		if(req.getType()==ReqType.ER)
		{
			if(ele.popButton(req.getFloorNo()-1))//cancel成功
			{
				return true;
			}
			else
			{
				return false;
			}
		}else//FR
		{
			if(req.getFRdir()==Direction.UP)
			{
				if(this.floor[req.getFloorNo()-1].getUpbutton())
				{
					this.floor[req.getFloorNo()-1].setUpbutton(false);
					return true;
			}
				else
				{
					return false;
				}
			}
			else//DOWN
			{
				if(this.floor[req.getFloorNo()-1].getDownbutton())
				{
					this.floor[req.getFloorNo()-1].setDownbutton(false);
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}
	*/
 	public void update_waitlist(Elevator elevator,RequestList reqlist, RequestList e_waitlist)
	{
		if((reqlist.getNextindex()==reqlist.getListlen())&&(e_waitlist.getListlen()==0))//全部请求已经读入并且等待队列为空，说明程序结束了
		{
			System.exit(0);//程序结束！！
		}
		for(int i =reqlist.getNextindex();i<reqlist.getListlen();i++)
		{
			if(reqlist.getReq(i).getTime()<=elevator.getE_time())
			{
				if(isSame(reqlist.getReq(i),elevator))//是同质请求
				{
					new Output().sameoutput(reqlist.getReq(i));
					reqlist.deleteReq(i);
					i--;
					continue;
				}
				e_waitlist.addReq(reqlist.getReq(i));
				reqlist.nextindexadd1();
			}
			else
			{
				break;
			}
		}
	}
	public void ALS_schedul(Elevator elevator, RequestList reqlist)
	{
		RequestList e_waitlist = new RequestList();//已经发生但未响应的请求列表，专属某个电梯
		Request mainReq;//当前主请求
		//更新e_waitlist,并删去same请求
		update_waitlist(elevator, reqlist,  e_waitlist);
		mainReq= e_waitlist.getReq(0);//可有可无
		while(true)
		{
			//响应能够响应的请求
			if(elevator.getState()==ElevatorState.MOVE)
			{
				if(elevator.getCurfloor()<mainReq.getFloorNo())//还没到达主请求目的楼层
				{
					//判断当前层有没有可稍待请求需要响应
					int firsttag=1;
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//有捎带请求可以响应
						{
							if(firsttag==1)
							{
								firsttag=0;
								elevator.goOpenClose();
								update_waitlist(elevator, reqlist,  e_waitlist);
							}
							new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.MOVE, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//灭了捎带请求产生的按钮
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					findshaodai(elevator.getCurfloor(), Direction.UP, mainReq, e_waitlist);
					elevator.goUp(1);
					update_waitlist(elevator, reqlist,  e_waitlist);
				}
				else if(mainReq.getFloorNo()==elevator.getCurfloor())//到达主请求目的楼层
				{
					elevator.goOpenClose();
					update_waitlist(elevator, reqlist,  e_waitlist);
					new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.MOVE,elevator.getE_time()-1 );
					//主请求已响应
					//if(!cancelButton(mainReq,elevator))//灭了主请求产生的按钮  ???为了解决同质问题后移灭灯，可能有错
				//	{
					//	System.out.println("error6");
				//	}
					e_waitlist.deleteReq(mainReq);
					//reqlist.deleteReq(mainReq);
					//看看有没有同层响应的捎带请求
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//有捎带请求可以响应
						{
							new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.MOVE, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//灭了捎带请求产生的
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					//旧的主请求已响应完毕，选择新的主请求
					int hit=0;
					for(int i=0;i<e_waitlist.getListlen();i++)//看看还有没有旧主请求的未完成捎带请求
					{
						if(e_waitlist.getReq(i).getStatus()==1)//有捎带请求可以响应
						{
							hit = 1;
							mainReq = e_waitlist.getReq(i);
							break;
						}
					}
					if(hit==1)
					{
						findshaodai(elevator.getCurfloor(), Direction.UP, mainReq, e_waitlist);
						elevator.goUp(1);
						update_waitlist(elevator, reqlist,  e_waitlist);
						continue;
					}
					if(hit==0)//没有旧主请求的未完成捎带请求，则选择等待队列中的第一个请求
					{
						if(e_waitlist.getListlen()==0)//等待队列也为空，则电梯进入wfs状态
						{
							elevator.gowait(0.5);
							update_waitlist(elevator, reqlist,  e_waitlist);
							continue;
						}else//等待队列不为空
						{
							mainReq = e_waitlist.getReq(0);
							elevator.setState(ElevatorState.STILL);//设置电梯状态为静止，交给STILL分支处理
							continue;
						}
					}
					System.out.println("error7");//不可能到这里
				}
				else
				{
					System.out.println("error5");
				}
			}
			else if(elevator.getState()==ElevatorState.MOVE)
			{
				if(mainReq.getFloorNo()<elevator.getCurfloor())//还没到达主请求目的楼层
				{
					//判断当前层有没有可稍待请求需要响应
					int firsttag=1;
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//有捎带请求可以响应
						{
							if(firsttag==1)
							{
								firsttag=0;
								elevator.goOpenClose();
								update_waitlist(elevator, reqlist,  e_waitlist);
							}
							//new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.DOWN, elevator.getE_time());
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//灭了捎带请求产生的按钮
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					findshaodai(elevator.getCurfloor(), Direction.DOWN, mainReq, e_waitlist);
					elevator.goDown(1);
					update_waitlist(elevator, reqlist,  e_waitlist);
				}
				else if(mainReq.getFloorNo()==elevator.getCurfloor())//到达主请求目的楼层
				{
					elevator.goOpenClose();
					update_waitlist(elevator, reqlist,  e_waitlist);
					//new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.DOWN,elevator.getE_time()-1);
					//主请求已响应
					//if(!cancelButton(mainReq,elevator))//灭了主请求产生的按钮  ???为了解决同质问题后移灭灯，可能有错
					//{
					//	System.out.println("error6");
					//}
					e_waitlist.deleteReq(mainReq);
					//reqlist.deleteReq(mainReq);
					//看看有没有同层响应的捎带请求
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//有捎带请求可以响应
						{
							//new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.DOWN, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//灭了捎带请求产生的按钮
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					//旧的主请求已响应完毕，选择新的主请求
					int hit=0;
					for(int i=0;i<e_waitlist.getListlen();i++)//看看还有没有旧主请求的未完成捎带请求
					{
						if(e_waitlist.getReq(i).getStatus()==1)//有捎带请求可以响应
						{
							hit = 1;
							mainReq = e_waitlist.getReq(i);
							break;
						}
					}
					if(hit==1)
					{
						findshaodai(elevator.getCurfloor(), Direction.DOWN, mainReq, e_waitlist);
						elevator.goDown(1);
						update_waitlist(elevator, reqlist,  e_waitlist);
						continue;
					}
					if(hit==0)//没有旧主请求的未完成捎带请求，则选择等待队列中的第一个请求
					{
						if(e_waitlist.getListlen()==0)//等待队列也为空，则电梯进入wfs状态
						{
							mainReq = null;
							elevator.gowait(0.5);
							update_waitlist(elevator, reqlist,  e_waitlist);
							continue;
						}else//等待队列不为空
						{
							mainReq = e_waitlist.getReq(0);
							elevator.setState(ElevatorState.STILL);//设置电梯状态为静止，交给STILL分支处理
							continue;
						}
					}
					System.out.println("error7");//不可能到这里
				}
				else
				{
					System.out.println("error5");
				}
			}else if(elevator.getState()==ElevatorState.WFS||elevator.getState()==ElevatorState.STILL)
			{
				if(e_waitlist.getListlen()>0)
				{
					mainReq = e_waitlist.getReq(0);
					if(mainReq.getFloorNo()==elevator.getCurfloor())//同层请求
					{
						elevator.goOpenClose();
						new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.STILL,elevator.getE_time() );
						//主请求已响应
						e_waitlist.deleteReq(mainReq);
						//reqlist.deleteReq(mainReq);
						update_waitlist(elevator, reqlist,  e_waitlist);
						//if(!cancelButton(mainReq,elevator))//灭了主请求产生的按钮
						//{
						//	System.out.println("error6");
						//}
						do {//找到下一个mainreq
							if(e_waitlist.getListlen()>0)
							{
								mainReq = e_waitlist.getReq(0);
								break;
							}
							else
							{
								elevator.gowait(0.5);
								update_waitlist(elevator, reqlist,  e_waitlist);
							}
						}while(true);
					}
					else if(mainReq.getFloorNo()>elevator.getCurfloor())//向上
					{
						findshaodai(elevator.getCurfloor(), Direction.UP, mainReq, e_waitlist);
						elevator.goUp(1);
						update_waitlist(elevator, reqlist,  e_waitlist);
					}
					else//mainReq.getFloorNo()<elevator.getCurfloor()
					{
						findshaodai(elevator.getCurfloor(), Direction.DOWN, mainReq, e_waitlist);
						elevator.goDown(1);
						update_waitlist(elevator, reqlist,  e_waitlist);
					}
				}else//等待请求为空，继续WFS
				{
					//System.out.println("error4");
					elevator.gowait(0.5);
					update_waitlist(elevator, reqlist,  e_waitlist);
				}
				
			}
		}//大循环
	}
	
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		Boolean ret;
		Scheduler scheduler = new Scheduler();
		RequestList reqlist = new RequestList();
		//Elevator elevator = new Elevator();
		ret = scheduler.inputReqs(reqlist);//读入所有请求（除了最前面的invalid请求）
		if(!ret)
		{
			return ;
		}
		scheduler.deleteInvalidReqs(reqlist);//删除invalid请求
		//开始调度
		//scheduler.ALS_schedul(elevator, reqlist);
		//调度结束
		return;
	}
	
	
}
