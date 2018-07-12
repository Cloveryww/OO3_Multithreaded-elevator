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
	public int dispatchOneFR(Request req)//���Է���һ��FR���� 1 ����ɹ���0����ʧ�ܣ�-1�ܷ��䣬
	//�����Ȳ����䣬sleepһ�����ٷ���
	{ 	/*@REQUIRES��req!=null;
		  @MODIFIES��reqList_all,ele1,ele2,ele3
		  @EFFECTS��req.type = ER  ==> \result = ture&&one ele get req;
		  @EFFECTS��	req.type = FR &&\exist Elevator e;e.canResponse(req) ==> \result = true;
		  @EFFECTS��	req.type = FR &&\all Elevator e;!e.canResponse(req) ==> \result = false;
		  */
		if (ele1.canShaodai(req) || ele2.canShaodai(req)|| ele3.canShaodai(req)) //�����Ӵ�����ĵ���
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
        } else {//û�����Ӵ��ģ�������û������Ӧ��
        	selectQueue.clear();
        	if (ele1.canResponse(req)) 
        		selectQueue.offer(ele1);
        	if (ele2.canResponse(req))
        		selectQueue.offer(ele2);
        	if (ele3.canResponse(req))
        		selectQueue.offer(ele3);
        	if (selectQueue.peek() != null) 
        	{
        		if(tricknum==0)//��һ�β��ܷ���
        		{
        			this.temp = selectQueue.peek().getEle_id();
        			this.tricknum=1;//�´ξͿ��Է�����
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
		// TODO �Զ����ɵķ������
        while (true) {//��ͣ�ĴӶ����л�ȡ���󣬽��зַ�
        	//�Ȳ鿴�ȴ���Ӧ�����Ƿ������������Ӧ��
        	//
        	synchronized(this.lock)//��������ִ�иô����ʱ�����̲߳��ܸı����״̬
			{

        	for(int i=0;i<reqList_wait.getListlen();i++)
        	{
        		Request re = reqList_wait.getReq(i);
        		int hit = 0;//�Ƿ�����ȥ�ı�־
                hit = dispatchOneFR(re);//���䵱ǰFR����
                if(hit!=0)//���ݿ�����Ӧ����������ӵȴ�������ɾ��
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
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}//˯һ�£��ó��߳�
        		continue;
        	}
            Request req = reqList_all.pollReq();//poll �����ˣ��������Ѿ�û�и���
            if (req!=null) {
                if (req.getType()==ReqType.ER) {//��ER����
                    long systime = new Date().getTime();
                    switch(req.getEle_id())//���������һ������
                    {
                    case 1:
                    	if(ele1.isButtonOn(req.getFloorNo()))//������Ϊsame
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele1.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//������
                				}
                			}
                    		ele1.receiveReq(req);
                    	}
                    	break;
                    case 2:
                    	if(ele2.isButtonOn(req.getFloorNo()))//������Ϊsame
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele2.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//������
                				}
                			}
                    		ele2.receiveReq(req);
                    	}
                    	break;
                    case 3:
                    	if(ele3.isButtonOn(req.getFloorNo()))//������Ϊsame
                    	{
                    		new Output().sameoutput(printer,systime,req);
                    	}else
                    	{
                    		boolean re = ele3.pushButton(req.getFloorNo());
                			if(!re)
                			{
                				if(ElevatorSys.isDebug())
                				{
                					System.out.println("Button error!");//������
                				}
                			}
                    		ele3.receiveReq(req);
                    	}
                    	break;
                    default:
                    	System.out.println("ele id error!"+req.getEle_id());
                    }
                } else { //FR���󣬿�ʼ����
                    if (floor.islightOn(req.getFloorNo(), req.getFRdir())) //������Ϊsame
                    {
                        long systime = new Date().getTime();
                        new Output().sameoutput(printer, systime, req);
                    } else {//����same
                    	floor.lightOn(req.getFloorNo(), req.getFRdir());
                        int  hit = 0;//�Ƿ�����ȥ�ı�־
                        hit = dispatchOneFR(req);//���䵱ǰFR����
                        if(hit==0)//û�е��ݿ�����Ӧ������������ӵ��ȴ�������У������ȴ�
                        {
                        	reqList_wait.addReq(req);
                        }   
                        if(hit==-1)
                        {
                        	
                        	try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								// TODO �Զ����ɵ� catch ��
								e.printStackTrace();
							}
                        	hit=dispatchOneFR(req);//��ʱtricknum�϶�����1
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
