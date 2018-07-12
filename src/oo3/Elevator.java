package oo3;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;

public class Elevator implements Runnable{

	public NewScheduler sch;//调度器的引用
	private int ele_id;
	private Floor floor_class;//记录楼层按钮的楼层类
	private PrintWriter printer;//输出的文件流
	private ElevatorState state;
	private int curfloor;
	private Direction e_dir;//电梯的运动方向，只有电梯状态是MOVE时才有意义
	private long e_time;
	private int targetfloor;//当前电梯运动的目标楼层
	private Boolean[] e_button = new Boolean[21]; //0不用
	private int aom;//Amount of movement(aom) 运动量
	DecimalFormat df = new DecimalFormat("0.0");
	private ArrayList<Request> FRshutdownList = new ArrayList<Request>();//关门时才熄灭按钮，所以需要缓存
    private ArrayList<Request> ERshutdownList = new ArrayList<Request>();//关门时才熄灭按钮，所以需要缓存
	
	//请求等待队列，采用先来先服务的策略（可能会很傻）
	private BlockingQueue<Request> waitQueue = new LinkedBlockingDeque<>();
	//电梯向上运动时的请求队列（包括主请求和可捎带请求，不包括正在等待队列中的请求）
	////默认STILL状态也使用upQueue
	private PriorityBlockingQueue<Request> upQueue = new PriorityBlockingQueue<>(10000, new Comparator<Request>() {
        @Override
        public int compare(Request req1, Request req2) {
            if (req1.getFloorNo()!=req2.getFloorNo()) //目标楼层小的优先
            {
            	return req1.getFloorNo()-req2.getFloorNo();
            }
            return (req1.getTime()-req2.getTime())>0? 1:-1;//如果楼层相同，那么请求发生时间早的优先
        }
    });
	//电梯向下运动时的请求队列（包括主请求和可捎带请求，不包括正在等待队列中的请求）
    private PriorityBlockingQueue<Request> downQueue = new PriorityBlockingQueue<>(10000, new Comparator<Request>() {
        @Override
        public int compare(Request req1, Request req2) {
        	if (req1.getFloorNo()!=req2.getFloorNo()) 
        	{
        		return req2.getFloorNo() - req1.getFloorNo();//目标楼层大的优先
        	}
            return (req1.getTime()-req2.getTime())>0? 1:-1;//如果楼层相同，那么请求发生时间早的优先
        }
    });
    private PriorityBlockingQueue<Request> curQueue;//执行队列的句柄，要注意选择正确的执行队列
    private long tricktime;//用于抵消代码运行时间的时间，解决模拟误差

	public Elevator(int ele_id,Floor floor_class, PrintWriter printer) {
		super();
		this.ele_id = ele_id;
		this.floor_class=floor_class;
		this.printer = printer;
		this.curQueue = upQueue;
		this.state = ElevatorState.WFS;
		this.curfloor = 1;
		for(int i =0 ;i<21;i++)
		{
			this.e_button[i]=false;
		}
	}
	public void goUp(int num){//驱动电梯上num楼
		/*  @ REQUIRES: num >=1 && this.curfloor+num<=10;
 			@ MODIFIES: this.curfloor,this.state,this.e_dir,this.e_time
			@ EFFECTS: 	this.state == MOVE;
						this.state == MOVE;
						this.e_dir == UP;
						this.curfloor == \old(this.curfloor) + num;
						this.e_time== \old(this.e_time) + 0.5 * num;
	 */
		this.state = ElevatorState.MOVE;
		this.e_dir = Direction.UP;
		this.curfloor += num;
		this.e_time+= 0.5 * num;
	}
	public void goDown(int num){//驱动电梯下num楼
		/*  @ REQUIRES: num >=1 && this.curfloor-num>=1;
			@ MODIFIES: this.curfloor,this.state,this.e_dir,this.e_time
			@ EFFECTS: 	this.state == MOVE;
					this.state == MOVE;
					this.e_dir == DOWN;
					this.curfloor == \old(this.curfloor) - num;
					this.e_time== \old(this.e_time) + 0.5 * num;
		 */
		this.state = ElevatorState.MOVE;
		this.e_dir = Direction.DOWN;
		this.curfloor -= num;
		this.e_time += 0.5 * num;
	}
	public void gowait(double waittime)//驱动电梯等待waittime时间
	{
		/*  @ REQUIRES: waittime>0;
			@ MODIFIES: this.state,this.e_time
			@ EFFECTS: 	this.state == WFS;
						this.e_time== \old(this.e_time) + waittime;
		 */
		this.state = ElevatorState.WFS;
		this.e_time += waittime;
	}
	public void goOpenClose()//驱动电梯开关门
	{
		/*  @ MODIFIES: this.state,this.e_time
			@ EFFECTS: 	this.state == STILL;
						this.e_time== \old(this.e_time) + 1;
		 */
		this.state = ElevatorState.STILL;
		this.e_time += 1;
	}
	
	public int getEle_id() {
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	\result == this.ele_id;
	 */
		return ele_id;
	}
	public void setEle_id(int ele_id) {
		/*  @ REQUIRES: this!=null&&ele_id>=1;
		 *  @ MODIFIES: this.ele_id
			@ EFFECTS: 	this.ele_id ==ele_id;
		 */
		this.ele_id = ele_id;
	}
	public Direction getE_dir() {
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	\result == this.e_dir;
		 */
		return e_dir;
	}
	public void setE_dir(Direction e_dir) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.e_dir
			@ EFFECTS: 	this.e_dir ==e_dir;
		 */
		this.e_dir = e_dir;
	}
	public int getTargetfloor() {
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	\result == this.targetfloor;
	 */
		return targetfloor;
	}
	public void setTargetfloor(int targetfloor) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.targetfloor
			@ EFFECTS: 	this.targetfloor ==targetfloor;
		 */
		this.targetfloor = targetfloor;
	}
	public ElevatorState getState() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.state;
 */
		return state;
	}
	public void setState(ElevatorState state) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.state
			@ EFFECTS: 	this.state ==state;
		 */
		this.state = state;
	}
	public void setCurfloor(int curfloor) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.curfloor
			@ EFFECTS: 	this.curfloor ==curfloor;
		 */
		this.curfloor = curfloor;
	}
	public void setE_time(long e_time) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.e_time
			@ EFFECTS: 	this.e_time ==e_time;
		 */
		this.e_time = e_time;
	}
	public int getCurfloor() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.curfloor;
 */
		return curfloor;
	}
	public long getE_time()
	{
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.e_time;
 */
		return e_time;
	}
	public int getAom() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.aom
 */
		return aom;
	}
	public void setAom(int aom) {
		this.aom = aom;
	}
	public Boolean pushButton(int no)
	{
		/*  @ REQUIRES: no >=1 && no<=20;
	 		@ MODIFIES: this.e_button
			@ EFFECTS: this.e_button[no]=false ==> this.e_button[no]=true&&\result = true;
					   this.e_button[no]=true ==> \result = false;
		 */
		if(this.e_button[no])
		{
			return false;
		}
		else
		{
			this.e_button[no]=true;
			return true;
		}
	}
	public Boolean popButton(int no)
	{
		/*  @ REQUIRES: no >=1 && no<=20;
		 	@ MODIFIES: this.e_button
 			@ EFFECTS: this.e_button[no]=true ==> this.e_button[no]=false&&\result = true;
 					   this.e_button[no]=false ==> \result = false;
		 */
		if(this.e_button[no])
		{
			this.e_button[no]=false;
			return true;
		}
		else
		{
			return false;
		}
	}
	public Boolean isButtonOn(int floorno)//是否该电梯内灯量
	{
		/*  @ REQUIRES: floorno >=1 && floorno<=20;
	 		@ EFFECTS: \result = this.e_button[floorno]
		 */
		return e_button[floorno];
	}
	public synchronized void receiveReq(Request req)//电梯接受请求
	{
		/*  @ REQUIRES: req!=null;
		 	@ MODIFIES: curQueue,waitQueue,this.state,this.e_dir,this.targetfloor,this.tricktime
			@ EFFECTS: canShaodai(req) ==> curQueue.offer(req)
					   !canShaodai(req) ==> waitQueue.offer(req)
		*/
		if (this.state==ElevatorState.WFS) //当前电梯之前处于空闲状态,则把req设置为主请求
		{
			//记录电梯从WFS状态退出时的第一条请求的开始系统时间，用于消除误差
            this.tricktime = req.getTime()+ElevatorSys.getStartTime();
			this.targetfloor = req.getFloorNo();//更新目标楼层，即主请求的楼层
	        if(this.curfloor == req.getFloorNo())//主请求的目标楼层就是当前楼层，则进入STILL状态，开关门
	        {//   WFS => STILL
	        	this.state = ElevatorState.STILL;
	        	this.e_dir = Direction.STILL;
	        }
	        else {//   STILL => MOVE
	        	this.state = ElevatorState.MOVE;
	        	if (this.curfloor < req.getFloorNo())
	        	{
	        		this.e_dir = Direction.UP;
	                curQueue = upQueue;//更改请求队列为最小队列，即队首的请求楼层最先到达
	            }else{
	            	this.e_dir = Direction.DOWN;
	            	curQueue = downQueue;
	            }
	        }
	        curQueue.offer(req);//将请求加入队列
	    } else if (state==ElevatorState.FIRSTSTILL||state==ElevatorState.MOVE)
	    {//如果电梯正在往一个确定的方向运动或临时响应请求停靠以下
	    	if ((this.e_dir==Direction.UP && req.getFloorNo()>this.curfloor) ||(this.e_dir==Direction.DOWN && req.getFloorNo()<this.curfloor))
	    	{
	    		curQueue.offer(req);//与电梯当前运动方向一致，可以捎带完成
	        } else 
	        {
	        	waitQueue.offer(req);//不能捎带，放到等待队列中等带主请求结束
	        }
	    } else {//STILL 直接放到当前队列中，肯定是ER请求，STILL结束后再处理   ####号
	    	curQueue.offer(req);
	    }
	}
	public synchronized boolean canResponse(Request req) {//req肯定是FR请求
		/*  @ REQUIRES: req!=null&&req.type =FR;
			@ EFFECTS: this.state = WFS ==> \result=true;
						(this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =UP &&
						req.getFloorNo()<= targetfloor && req.getFloorNo()>curfloor==>\result ==true;
						(this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =DOWN &&
						req.getFloorNo()>= targetfloor && req.getFloorNo()<curfloor==>\result ==true;
						others ==>\result ==false;			
		*/
		//可以响应有两种情况: 1.电梯是WFS状态  2.电梯可稍待该请求
        if (this.state==ElevatorState.WFS) 
        	return true;
        if (this.e_dir ==req.getFRdir())//this.state==ElevatorState.MOVE&&
        {
            if (this.e_dir==Direction.UP && req.getFloorNo()<= targetfloor && req.getFloorNo()>curfloor)
            	return true;
            if (this.e_dir==Direction.DOWN && req.getFloorNo()>=targetfloor && req.getFloorNo()<curfloor) 
            	return true;
        }
        return false;
    }
    public synchronized boolean canShaodai(Request req) {//req肯定是FR请求
    	/*  @ REQUIRES: req!=null&&req.type =FR;
		@ EFFECTS:  (this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =UP &&
					req.getFloorNo()<= targetfloor && req.getFloorNo()>curfloor==>\result ==true;
					(this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =DOWN &&
					req.getFloorNo()>= targetfloor && req.getFloorNo()<curfloor==>\result ==true;
					others ==>\result ==false;			
	*/
    	if (this.e_dir ==req.getFRdir())//this.state==ElevatorState.MOVE&&
        {
            if (this.e_dir==Direction.UP && req.getFloorNo()<= targetfloor && req.getFloorNo()>curfloor)
            	return true;
            if (this.e_dir==Direction.DOWN && req.getFloorNo()>=targetfloor && req.getFloorNo()<curfloor) 
            	return true;
        }
        return false;
    }
	@Override
	public void run() 
	{
		/* @ REQUIRES: this!=null&&this.curQueue!=null
			@ EFFECTS: monitor elevator to solve Request in Queue
			@ THREAD_REQUIRES:\locked(this)
			@ THREAD_EFFECTS: \locked(this)
		*/
		// TODO 自动生成的方法存根
		long st;
		while (true) {
			
            while (state==ElevatorState.WFS) //如果电梯当前空闲
            {
            	this.e_dir=Direction.STILL;
                if (curQueue.isEmpty()) //执行队列为空
                {
                    if (!waitQueue.isEmpty()) {
                        if (waitQueue.peek().getFloorNo()>this.curfloor)
                        {
                        	curQueue = upQueue;//向上移动
                        }
                        else {
                        	curQueue = downQueue;
                        }
                        curQueue.offer(waitQueue.poll());//将等待队列中的第一项加入执行队列中
                    }
                } else {//执行队列非空，则取出队首的请求作为主请求
                	this.targetfloor = curQueue.peek().getFloorNo();
                    if (curQueue.peek().getFloorNo() == this.curfloor)//WFS=>STILL,本层开关门响应请求
                    {
                    	state = ElevatorState.STILL;
                    }else if(curQueue.peek().getFloorNo() > this.curfloor)//向上运动 WFS=>MOVE
                    {
                    	synchronized(this.sch.lock)
            			{}
                        	this.e_dir = Direction.UP;
                        	state = ElevatorState.MOVE;
                    }else {//向下运动WFS=>MOVE
                    	synchronized(this.sch.lock)
            			{}
                        	this.e_dir = Direction.DOWN;
                        	state = ElevatorState.MOVE;
                    } 
                    //记录电梯从WFS状态退出时的第一条请求的开始系统时间，用于消除误差
                    //tricktime = curQueue.peek().getTime()+ElevatorSys.getStartTime();
                }
            }//end  WFS
            while (this.state==ElevatorState.MOVE) {
                //先检查等待队列中是否有可捎带请求
                boolean flag = true;
                while(flag) {
                	flag = false;
                	for (Request req : waitQueue)
                	{
                		if(req.getType() == ReqType.ER)
                		{
                			if(req.getFloorNo() > this.curfloor && this.e_dir == Direction.UP)
                			{
                				curQueue.offer(req);
                				waitQueue.remove(req);
                                flag = true;
                			}else if(req.getFloorNo() < this.curfloor && this.e_dir == Direction.DOWN)
                			{
                				curQueue.offer(req);
                				waitQueue.remove(req);
                                flag = true;
                			}
                		}else//FR
                		{
                			if(req.getFloorNo() > this.curfloor && this.e_dir == Direction.UP &&req.getFloorNo()<=this.targetfloor)
                			{
                				curQueue.offer(req);
                				waitQueue.remove(req);
                                flag = true;
                			}else if(req.getFloorNo() < this.curfloor && this.e_dir == Direction.DOWN&&req.getFloorNo()>=this.targetfloor)
                			{
                				curQueue.offer(req);
                				waitQueue.remove(req);
                                flag = true;
                			}
                		}
                	}
                }
                st = new Date().getTime();
                long time_wucha = (st-tricktime)%3000;//睡眠时间小于3000毫秒以弥补代码运行所消耗的时间   
                if(ElevatorSys.isDebug())
                {
                	System.out.println("Debug: time_wucha = "+time_wucha+"ms");
                }
                try {
                	if(time_wucha<1000)
                	{
                		Thread.sleep(3000-time_wucha);
                	}else
                	{
                		Thread.sleep(3000);
                	}
                }
                catch (InterruptedException e)
                {
                	System.out.println("elevator move sleep exception");
                }
                st = new Date().getTime();
                //运动一层
                synchronized(this.sch.lock)
    			{}
                this.curfloor = this.e_dir == Direction.UP ? this.curfloor + 1 : this.curfloor - 1;
                this.aom++;//行动量增加1
                FRshutdownList.clear();
                ERshutdownList.clear();
                while (curQueue.peek() != null && this.curfloor == curQueue.peek().getFloorNo()) 
                {//MOVE=>FIRSTSTILL  需要开关门完成请求的响应
                    if (curQueue.peek().getType()==ReqType.FR) 
                    {
                    	FRshutdownList.add(curQueue.peek());
                    }
                    else {
                    	ERshutdownList.add(curQueue.peek());
                    }
                    new Output().output(printer, st, this, curQueue.peek());//输出响应请求信息
                    curQueue.poll();
                    synchronized(this.sch.lock)
        			{}
                    state = ElevatorState.FIRSTSTILL;
                }
            }

            while (this.state==ElevatorState.FIRSTSTILL || this.state==ElevatorState.STILL) {
                st = new Date().getTime();
                long time_wucha = (st-tricktime)%3000;//睡眠时间小于3000毫秒以弥补代码运行所消耗的时间 
                if(ElevatorSys.isDebug())
                {
                	System.out.println("Debug: time_wucha = "+time_wucha+"ms");
                }
                try {
                	if(time_wucha<1000)
                	{
                		Thread.sleep(6000-time_wucha);
                	}else
                	{
                		Thread.sleep(6000);
                	}
                } catch(InterruptedException e)
                {}
                if (state==ElevatorState.STILL) {
                	synchronized(this.sch.lock)
        			{}
                	this.e_dir=Direction.STILL;
                    st = new Date().getTime();
                    //同层请求一次只可能完成一个请求
                    new Output().output(printer, st, this, curQueue.peek());//输出请求响应信息
                    if (curQueue.peek().getType()==ReqType.FR)
                    {
                    	//熄灭按钮
                        floor_class.lightOff(curQueue.peek().getFloorNo(),curQueue.peek().getFRdir());
                    }
                    curQueue.poll();
                }
              //熄灭所有完成响应的楼层按钮 和 楼梯请求按钮
                for (int i=0;i<ERshutdownList.size();i++)
                {
                	this.popButton(ERshutdownList.get(i).getFloorNo());
                }
                for (int i=0;i<FRshutdownList.size();i++)
                {
                	floor_class.lightOff(FRshutdownList.get(i).getFloorNo(), FRshutdownList.get(i).getFRdir());
                }   
                if (curQueue.isEmpty())
                {
                	if (waitQueue.isEmpty()) {
                		synchronized(this.sch.lock)
            			{}
                		state = ElevatorState.WFS;
                	}
                	else {
                		//可捎带集合为空，等待队列非空，选择等待队列中队首的请求作为新的主请求
                		if (waitQueue.peek().getFloorNo()> this.curfloor) {
                			synchronized(this.sch.lock)
                			{}
                			curQueue = upQueue;
                			this.e_dir = Direction.UP;
                			this.state = ElevatorState.MOVE;
                		} else if (waitQueue.peek().getFloorNo()< this.curfloor) 
                		{
                			synchronized(this.sch.lock)
                			{}
                			curQueue = downQueue;
                			this.e_dir = Direction.DOWN;
                			this.state = ElevatorState.MOVE;
                		} else {
                			synchronized(this.sch.lock)
                			{}
                			curQueue = upQueue;//默认STILL状态也使用upQueue
                			this.state = ElevatorState.STILL;
                		}
                        curQueue.offer(waitQueue.poll());
                	}
                } else {//判断是否主请求结束，选择新的主请求，优先选择旧的主请求可捎带集合中的请求
                	//如果没到达主请求目标楼层，则不更换主请求
                	if(this.state==ElevatorState.FIRSTSTILL&&this.curfloor!=this.targetfloor)
                	{
                		//this.targetfloor = curQueue.peek().getFloorNo();//更换主请求
                		synchronized(this.sch.lock)
            			{}
                		if (curQueue.peek().getFloorNo() != this.curfloor) {
                            if (curQueue.peek().getFloorNo() > this.curfloor) 
                            {
                            	this.e_dir  = Direction.UP;
                            }
                            else {
                            	this.e_dir  = Direction.DOWN;
                            }
                            state = ElevatorState.MOVE;
                        } else {
                        	state = ElevatorState.STILL;//特殊处理####号的地方
                        }
                	}else//if((this.state==ElevatorState.FIRSTSTILL&&this.curfloor==this.targetfloor)||(this.state==ElevatorState.STILL))
                	{//到达目标楼层，更换主请求
                		Request temp = curQueue.peek();
                		for (Request req : curQueue)
                		{
                			if(req.getTime()<temp.getTime())
                			{
                				temp = req;
                			}
                		}
                		if(ElevatorSys.isDebug())
                    	{
                    		if(curQueue.peek()!=temp)
                    		{
                    			System.out.println("新主请求不是最近的，而是最早的"+curQueue.peek().toString()+"temp="+temp.toString());
                    		}
                    	}
                		synchronized(this.sch.lock)
            			{}
                		this.targetfloor = temp.getFloorNo();
                		if (temp.getFloorNo() != this.curfloor) {
                            if (temp.getFloorNo() > this.curfloor) 
                            {
                            	this.e_dir  = Direction.UP;
                            }
                            else {
                            	this.e_dir  = Direction.DOWN;
                            }
                            state = ElevatorState.MOVE;
                        } else {
                        	state = ElevatorState.STILL;//特殊处理####号的地方
                        }
                		
                	}
                    
                }
            }
        }
	}
}

