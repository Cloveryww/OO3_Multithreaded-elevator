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

	public NewScheduler sch;//������������
	private int ele_id;
	private Floor floor_class;//��¼¥�㰴ť��¥����
	private PrintWriter printer;//������ļ���
	private ElevatorState state;
	private int curfloor;
	private Direction e_dir;//���ݵ��˶�����ֻ�е���״̬��MOVEʱ��������
	private long e_time;
	private int targetfloor;//��ǰ�����˶���Ŀ��¥��
	private Boolean[] e_button = new Boolean[21]; //0����
	private int aom;//Amount of movement(aom) �˶���
	DecimalFormat df = new DecimalFormat("0.0");
	private ArrayList<Request> FRshutdownList = new ArrayList<Request>();//����ʱ��Ϩ��ť��������Ҫ����
    private ArrayList<Request> ERshutdownList = new ArrayList<Request>();//����ʱ��Ϩ��ť��������Ҫ����
	
	//����ȴ����У����������ȷ���Ĳ��ԣ����ܻ��ɵ��
	private BlockingQueue<Request> waitQueue = new LinkedBlockingDeque<>();
	//���������˶�ʱ��������У�����������Ϳ��Ӵ����󣬲��������ڵȴ������е�����
	////Ĭ��STILL״̬Ҳʹ��upQueue
	private PriorityBlockingQueue<Request> upQueue = new PriorityBlockingQueue<>(10000, new Comparator<Request>() {
        @Override
        public int compare(Request req1, Request req2) {
            if (req1.getFloorNo()!=req2.getFloorNo()) //Ŀ��¥��С������
            {
            	return req1.getFloorNo()-req2.getFloorNo();
            }
            return (req1.getTime()-req2.getTime())>0? 1:-1;//���¥����ͬ����ô������ʱ���������
        }
    });
	//���������˶�ʱ��������У�����������Ϳ��Ӵ����󣬲��������ڵȴ������е�����
    private PriorityBlockingQueue<Request> downQueue = new PriorityBlockingQueue<>(10000, new Comparator<Request>() {
        @Override
        public int compare(Request req1, Request req2) {
        	if (req1.getFloorNo()!=req2.getFloorNo()) 
        	{
        		return req2.getFloorNo() - req1.getFloorNo();//Ŀ��¥��������
        	}
            return (req1.getTime()-req2.getTime())>0? 1:-1;//���¥����ͬ����ô������ʱ���������
        }
    });
    private PriorityBlockingQueue<Request> curQueue;//ִ�ж��еľ����Ҫע��ѡ����ȷ��ִ�ж���
    private long tricktime;//���ڵ�����������ʱ���ʱ�䣬���ģ�����

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
	public void goUp(int num){//����������num¥
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
	public void goDown(int num){//����������num¥
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
	public void gowait(double waittime)//�������ݵȴ�waittimeʱ��
	{
		/*  @ REQUIRES: waittime>0;
			@ MODIFIES: this.state,this.e_time
			@ EFFECTS: 	this.state == WFS;
						this.e_time== \old(this.e_time) + waittime;
		 */
		this.state = ElevatorState.WFS;
		this.e_time += waittime;
	}
	public void goOpenClose()//�������ݿ�����
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
	public Boolean isButtonOn(int floorno)//�Ƿ�õ����ڵ���
	{
		/*  @ REQUIRES: floorno >=1 && floorno<=20;
	 		@ EFFECTS: \result = this.e_button[floorno]
		 */
		return e_button[floorno];
	}
	public synchronized void receiveReq(Request req)//���ݽ�������
	{
		/*  @ REQUIRES: req!=null;
		 	@ MODIFIES: curQueue,waitQueue,this.state,this.e_dir,this.targetfloor,this.tricktime
			@ EFFECTS: canShaodai(req) ==> curQueue.offer(req)
					   !canShaodai(req) ==> waitQueue.offer(req)
		*/
		if (this.state==ElevatorState.WFS) //��ǰ����֮ǰ���ڿ���״̬,���req����Ϊ������
		{
			//��¼���ݴ�WFS״̬�˳�ʱ�ĵ�һ������Ŀ�ʼϵͳʱ�䣬�����������
            this.tricktime = req.getTime()+ElevatorSys.getStartTime();
			this.targetfloor = req.getFloorNo();//����Ŀ��¥�㣬���������¥��
	        if(this.curfloor == req.getFloorNo())//�������Ŀ��¥����ǵ�ǰ¥�㣬�����STILL״̬��������
	        {//   WFS => STILL
	        	this.state = ElevatorState.STILL;
	        	this.e_dir = Direction.STILL;
	        }
	        else {//   STILL => MOVE
	        	this.state = ElevatorState.MOVE;
	        	if (this.curfloor < req.getFloorNo())
	        	{
	        		this.e_dir = Direction.UP;
	                curQueue = upQueue;//�����������Ϊ��С���У������׵�����¥�����ȵ���
	            }else{
	            	this.e_dir = Direction.DOWN;
	            	curQueue = downQueue;
	            }
	        }
	        curQueue.offer(req);//������������
	    } else if (state==ElevatorState.FIRSTSTILL||state==ElevatorState.MOVE)
	    {//�������������һ��ȷ���ķ����˶�����ʱ��Ӧ����ͣ������
	    	if ((this.e_dir==Direction.UP && req.getFloorNo()>this.curfloor) ||(this.e_dir==Direction.DOWN && req.getFloorNo()<this.curfloor))
	    	{
	    		curQueue.offer(req);//����ݵ�ǰ�˶�����һ�£������Ӵ����
	        } else 
	        {
	        	waitQueue.offer(req);//�����Ӵ����ŵ��ȴ������еȴ����������
	        }
	    } else {//STILL ֱ�ӷŵ���ǰ�����У��϶���ER����STILL�������ٴ���   ####��
	    	curQueue.offer(req);
	    }
	}
	public synchronized boolean canResponse(Request req) {//req�϶���FR����
		/*  @ REQUIRES: req!=null&&req.type =FR;
			@ EFFECTS: this.state = WFS ==> \result=true;
						(this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =UP &&
						req.getFloorNo()<= targetfloor && req.getFloorNo()>curfloor==>\result ==true;
						(this.state = MOVE ||this.state = FIRSTSTILL)&&req.FRdir = this.e_dir =DOWN &&
						req.getFloorNo()>= targetfloor && req.getFloorNo()<curfloor==>\result ==true;
						others ==>\result ==false;			
		*/
		//������Ӧ���������: 1.������WFS״̬  2.���ݿ��Դ�������
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
    public synchronized boolean canShaodai(Request req) {//req�϶���FR����
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
		// TODO �Զ����ɵķ������
		long st;
		while (true) {
			
            while (state==ElevatorState.WFS) //������ݵ�ǰ����
            {
            	this.e_dir=Direction.STILL;
                if (curQueue.isEmpty()) //ִ�ж���Ϊ��
                {
                    if (!waitQueue.isEmpty()) {
                        if (waitQueue.peek().getFloorNo()>this.curfloor)
                        {
                        	curQueue = upQueue;//�����ƶ�
                        }
                        else {
                        	curQueue = downQueue;
                        }
                        curQueue.offer(waitQueue.poll());//���ȴ������еĵ�һ�����ִ�ж�����
                    }
                } else {//ִ�ж��зǿգ���ȡ�����׵�������Ϊ������
                	this.targetfloor = curQueue.peek().getFloorNo();
                    if (curQueue.peek().getFloorNo() == this.curfloor)//WFS=>STILL,���㿪������Ӧ����
                    {
                    	state = ElevatorState.STILL;
                    }else if(curQueue.peek().getFloorNo() > this.curfloor)//�����˶� WFS=>MOVE
                    {
                    	synchronized(this.sch.lock)
            			{}
                        	this.e_dir = Direction.UP;
                        	state = ElevatorState.MOVE;
                    }else {//�����˶�WFS=>MOVE
                    	synchronized(this.sch.lock)
            			{}
                        	this.e_dir = Direction.DOWN;
                        	state = ElevatorState.MOVE;
                    } 
                    //��¼���ݴ�WFS״̬�˳�ʱ�ĵ�һ������Ŀ�ʼϵͳʱ�䣬�����������
                    //tricktime = curQueue.peek().getTime()+ElevatorSys.getStartTime();
                }
            }//end  WFS
            while (this.state==ElevatorState.MOVE) {
                //�ȼ��ȴ��������Ƿ��п��Ӵ�����
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
                long time_wucha = (st-tricktime)%3000;//˯��ʱ��С��3000�������ֲ��������������ĵ�ʱ��   
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
                //�˶�һ��
                synchronized(this.sch.lock)
    			{}
                this.curfloor = this.e_dir == Direction.UP ? this.curfloor + 1 : this.curfloor - 1;
                this.aom++;//�ж�������1
                FRshutdownList.clear();
                ERshutdownList.clear();
                while (curQueue.peek() != null && this.curfloor == curQueue.peek().getFloorNo()) 
                {//MOVE=>FIRSTSTILL  ��Ҫ����������������Ӧ
                    if (curQueue.peek().getType()==ReqType.FR) 
                    {
                    	FRshutdownList.add(curQueue.peek());
                    }
                    else {
                    	ERshutdownList.add(curQueue.peek());
                    }
                    new Output().output(printer, st, this, curQueue.peek());//�����Ӧ������Ϣ
                    curQueue.poll();
                    synchronized(this.sch.lock)
        			{}
                    state = ElevatorState.FIRSTSTILL;
                }
            }

            while (this.state==ElevatorState.FIRSTSTILL || this.state==ElevatorState.STILL) {
                st = new Date().getTime();
                long time_wucha = (st-tricktime)%3000;//˯��ʱ��С��3000�������ֲ��������������ĵ�ʱ�� 
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
                    //ͬ������һ��ֻ�������һ������
                    new Output().output(printer, st, this, curQueue.peek());//���������Ӧ��Ϣ
                    if (curQueue.peek().getType()==ReqType.FR)
                    {
                    	//Ϩ��ť
                        floor_class.lightOff(curQueue.peek().getFloorNo(),curQueue.peek().getFRdir());
                    }
                    curQueue.poll();
                }
              //Ϩ�����������Ӧ��¥�㰴ť �� ¥������ť
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
                		//���Ӵ�����Ϊ�գ��ȴ����зǿգ�ѡ��ȴ������ж��׵�������Ϊ�µ�������
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
                			curQueue = upQueue;//Ĭ��STILL״̬Ҳʹ��upQueue
                			this.state = ElevatorState.STILL;
                		}
                        curQueue.offer(waitQueue.poll());
                	}
                } else {//�ж��Ƿ������������ѡ���µ�����������ѡ��ɵ���������Ӵ������е�����
                	//���û����������Ŀ��¥�㣬�򲻸���������
                	if(this.state==ElevatorState.FIRSTSTILL&&this.curfloor!=this.targetfloor)
                	{
                		//this.targetfloor = curQueue.peek().getFloorNo();//����������
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
                        	state = ElevatorState.STILL;//���⴦��####�ŵĵط�
                        }
                	}else//if((this.state==ElevatorState.FIRSTSTILL&&this.curfloor==this.targetfloor)||(this.state==ElevatorState.STILL))
                	{//����Ŀ��¥�㣬����������
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
                    			System.out.println("��������������ģ����������"+curQueue.peek().toString()+"temp="+temp.toString());
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
                        	state = ElevatorState.STILL;//���⴦��####�ŵĵط�
                        }
                		
                	}
                    
                }
            }
        }
	}
}

