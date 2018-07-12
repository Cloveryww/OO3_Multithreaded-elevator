package oo3;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

public class NewScheduler extends Scheduler implements Runnable {
	
	private RequestList reqList_all;
	private RequestList reqList_wait;
	private Floor floor;
    private PrintWriter printer;
	private Elevator ele1;
	private Elevator ele2;
	private Elevator ele3;
	public Boolean lock;
	private int tricknum;
	private int temp;//debug
	
	private PriorityBlockingQueue<Elevator> selectQueue = new PriorityBlockingQueue<>(4, new Comparator<Elevator>() 
	{
        @Override
        public int compare(Elevator e1, Elevator e2) {
            return e1.getAom()-e2.getAom();
        }
    });
	
	public NewScheduler(RequestList reqList_all, Elevator ele1, Elevator ele2, Elevator ele3, Floor floor,
			PrintWriter printWriter) {
		super();
		this.reqList_all = reqList_all;
		this.reqList_wait = new RequestList();
		this.ele1 = ele1;
		this.ele2 = ele2;
		this.ele3 = ele3;
		this.floor = floor;
		this.printer = printWriter;
		this.lock = new Boolean(true);
		this.tricknum = 0;
		this.temp=-1;
	}
	public int dispatchOneFR(Request req)//尝试分配一个FR请求 1 分配成功，0分配失败，-1能分配，
	//但是先不分配，sleep一豪秒再分配
	{ 	/*@REQUIRES：req!=null;
		  @MODIFIES：reqList_all,ele1,ele2,ele3
		  @EFFECTS：req.type = ER  ==> \result = ture&&one ele get req;
		  @EFFECTS：	req.type = FR &&\exist Elevator e;e.canResponse(req) ==> \result = true;
		  @EFFECTS：	req.type = FR &&\all Elevator e;!e.canResponse(req) ==> \result = false;
		  */
		if (ele1.canShaodai(req) || ele2.canShaodai(req)|| ele3.canShaodai(req)) //有能捎带请求的电梯
		{
			selectQueue.clear();
			if (ele1.canShaodai(req)) 
				selectQueue.offer(ele1);
            if (ele2.canShaodai(req)) 
            	selectQueue.offer(ele2);
            if (ele3.canShaodai(req)) 
            	selectQueue.offer(ele3);
            if (selectQueue.peek() != null) {
            	selectQueue.peek().receiveReq(req);
            	if(ElevatorSys.isDebug())
            	{
            		System.out.println("Debug: "+req.toString()+"==shaodai>>"+selectQueue.peek().getEle_id());
            	}
            	return 1;
            }
        } else {//没有能捎带的，看看有没有能响应的
        	selectQueue.clear();
        	if (ele1.canResponse(req)) 
        		selectQueue.offer(ele1);
        	if (ele2.canResponse(req))
        		selectQueue.offer(ele2);
        	if (ele3.canResponse(req))
        		selectQueue.offer(ele3);
        	if (selectQueue.peek() != null) 
        	{
        		if(tricknum==0)//第一次不能分配
        		{
        			this.temp = selectQueue.peek().getEle_id();
        			this.tricknum=1;//下次就可以分配了
        			return -1;
        		}else//tricknum==1
        		{
        			if(ElevatorSys.isDebug())
        			{
        				if(this.temp!=selectQueue.peek().getEle_id())
        				{
        					System.out.println("trick succuss");
        				}
        			}
        			this.tricknum = 0;
        		}
        		selectQueue.peek().receiveReq(req);
        		if(ElevatorSys.isDebug())
            	{
            		System.out.println("Debug: "+req.toString()+"==WFS>>"+selectQueue.peek().getEle_id());
            	}
        		return 1;
        	}
        }
		return 0;
	}
	@Override
	public synchronized void run() {
		/* @ REQUIRES:this!=null; 
		   @ MODIFIES:this.reqList_all,ele1,ele2,ele3 
		   @ EFFECTS: \all Request req in reqList_all;dispatch req to ele1 || dispatch req to ele2 || dispatch req to ele3
		   @ THREAD_REQUIRES:\locked(reqList_all, ele1, ele2, ele3)
		   @ THREAD_EFFECTS: \locked(reqList_all, ele1, ele2, ele3)
		*/
		// TODO 自动生成的方法存根
        while (true) {//不停的从队列中获取请求，进行分发
        	//先查看等待响应队列是否有请求可以响应。
        	//
        	synchronized(this.lock)//调度器在执行该代码段时电梯线程不能改变电梯状态
			{

        	for(int i=0;i<reqList_wait.getListlen();i++)
        	{
        		Request re = reqList_wait.getReq(i);
        		int hit = 0;//是否分配出去的标志
                hit = dispatchOneFR(re);//分配当前FR请求
                if(hit!=0)//电梯可以响应，将该请求从等待队列中删除
                {
                	if(hit==-1)
                	{
                		break;
                	}
                	reqList_wait.deleteReq(re);
                	if(ElevatorSys.isDebug())
                	{
                		System.out.println("Debug: "+re.toString()+"==can Res>> one ele"+i);
                	}
                	break;
                }   
        	}
			}
        	if(this.tricknum==1)
        	{
        		
        		try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}//睡一下，让出线程
        		continue;
        	}
            Request req = reqList_all.pollReq();//poll 出来了，队列中已经没有该项
            if (req!=null) {
                if (req.getType()==ReqType.ER) {//是ER请求
                    long systime = new Date().getTime();
                    switch(req.getEle_id())//分配给其中一个电梯
                    {
                    case 1:
                    	if(ele1.isButtonOn(req.getFloorNo()))//灯亮，为same
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele1.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//出错了
                				}
                			}
                    		ele1.receiveReq(req);
                    	}
                    	break;
                    case 2:
                    	if(ele2.isButtonOn(req.getFloorNo()))//灯亮，为same
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele2.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//出错了
                				}
                			}
                    		ele2.receiveReq(req);
                    	}
                    	break;
                    case 3:
                    	if(ele3.isButtonOn(req.getFloorNo()))//灯亮，为same
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele3.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//出错了
                				}
                			}
                    		ele3.receiveReq(req);
                    	}
                    	break;
                    default:
                    	System.out.println("ele id error!"+req.getEle_id());
                    }
                } else { //FR请求，开始调度
                    if (floor.islightOn(req.getFloorNo(), req.getFRdir())) //灯亮，为same
                    {
                        long systime = new Date().getTime();
                        new Output().sameoutput(printer, systime, req);
                    } else {//不是same
                    	floor.lightOn(req.getFloorNo(), req.getFRdir());
                        int  hit = 0;//是否分配出去的标志
                        hit = dispatchOneFR(req);//分配当前FR请求
                        if(hit==0)//没有电梯可以响应，将该请求添加到等待请求队列，继续等待
                        {
                        	reqList_wait.addReq(req);
                        }   
                        if(hit==-1)
                        {
                        	
                        	try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								// TODO 自动生成的 catch 块
								e.printStackTrace();
							}
                        	hit=dispatchOneFR(req);//此时tricknum肯定等于1
                        	if(hit!=1)
                        	{
                        		if(ElevatorSys.isDebug())
                        		{
                        			System.out.println("trick error!");
                        		}
                        	}
                        }
                    }//end non same else
                }//end FR else
            }//end non null if
        }
		
	}
}
