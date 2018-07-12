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
	 	/*@REQUIRES��isValidReq(str)&&curTime >= 0;
		  @EFFECTS��(\result.type = FR || \result.type = ER)&&
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
		else//error ���������ܳ���
		{
			System.out.println("no possible");
		}    	
		return req;
	}
	public Boolean inputReqs(RequestList reqlist)//��ȡ��������������������
	{
	 	/*@REQUIRES��reqlist!=null;
		  @MODIFYIED�� reqlist;
		  @EFFECTS��	input string to produce req and put them into reqlist;
		  */ 
		int firstTag=0;
		Request curreq;
		ArrayList<String> errorStr = new ArrayList<String>();
		Scanner sc=new Scanner(System.in);  
    	do {
		String str = sc.nextLine();
    	String str_no_space = str.replaceAll("\\s+", "");//ɾ�����пո�
    	if(str_no_space.length()==0)//����
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
    	//�ȼ���Ƿ�Ϊ�Ϸ���ʽ������
    	Pattern p0=Pattern.compile("\\([EF]R,[+]?(\\d+),((UP|DOWN),)?[+]?(\\d+)\\)");
    	Matcher m0=p0.matcher(str_no_space);
    	if(!m0.matches())
    	{
    		errorStr.add(str_no_space);
    		continue;
    	}
    	curreq = string2Req(str_no_space);//String תReq
    	if(curreq==null)//���Ϸ�
    	{
    		errorStr.add(str_no_space);
    		continue;
    	}
    	if(firstTag==0)//��һ���Ϸ�����û��
    	{
    		//�ǵ�һ���Ϸ�����(FR,1,UP,0)
    		if(curreq.getType()==ReqType.FR&&curreq.getFloorNo()==1&&curreq.getFRdir()==Direction.UP&&curreq.getTime()==0)
    		{
    			firstTag = 1;
    			reqlist.addReq(curreq);
    		}
    		else//invalid ����
    		{
    			new Output().invalidoutput(str_no_space);
    		}
    	}
    	else//��һ���ͷ������Ѿ�����
    	{
    		reqlist.addReq(curreq);
    	}
    	}while (true);
	}
	
	public void deleteInvalidReqs(RequestList reqlist)//ɾ�����в��Ϸ������󣬲����invalid��Ϣ
	{
		for(int i = 1 ;i<reqlist.getListlen();i++)
		{
			if(reqlist.getReq(i).getTime()<reqlist.getReq(i-1).getTime())//���벻�ǰ�ʱ�������
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

	public void findshaodai(int curFloor,Direction dir, Request mainReq, RequestList e_waitlist)//�ӵȴ��������ҵ���ǰ��������Դ������󣬲����
	{
		for(int i =0;i<e_waitlist.getListlen();i++)
		{
			e_waitlist.getReq(i).setStatus(0);
		}
		if(dir == Direction.UP)
		{
			for(int i =0;i<e_waitlist.getListlen();i++)
			{
				//���Դ�
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
				//���Դ�
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
 	public Boolean isSame(Request req, Elevator elevator)//�����same��������Ӧ��ť������false�����same���򷵻�true
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
	/*public Boolean cancelButton(Request req,Elevator ele)//Ϩ������������ť
	{
		if(req.getType()==ReqType.ER)
		{
			if(ele.popButton(req.getFloorNo()-1))//cancel�ɹ�
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
		if((reqlist.getNextindex()==reqlist.getListlen())&&(e_waitlist.getListlen()==0))//ȫ�������Ѿ����벢�ҵȴ�����Ϊ�գ�˵�����������
		{
			System.exit(0);//�����������
		}
		for(int i =reqlist.getNextindex();i<reqlist.getListlen();i++)
		{
			if(reqlist.getReq(i).getTime()<=elevator.getE_time())
			{
				if(isSame(reqlist.getReq(i),elevator))//��ͬ������
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
		RequestList e_waitlist = new RequestList();//�Ѿ�������δ��Ӧ�������б�ר��ĳ������
		Request mainReq;//��ǰ������
		//����e_waitlist,��ɾȥsame����
		update_waitlist(elevator, reqlist,  e_waitlist);
		mainReq= e_waitlist.getReq(0);//���п���
		while(true)
		{
			//��Ӧ�ܹ���Ӧ������
			if(elevator.getState()==ElevatorState.MOVE)
			{
				if(elevator.getCurfloor()<mainReq.getFloorNo())//��û����������Ŀ��¥��
				{
					//�жϵ�ǰ����û�п��Դ�������Ҫ��Ӧ
					int firsttag=1;
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//���Ӵ����������Ӧ
						{
							if(firsttag==1)
							{
								firsttag=0;
								elevator.goOpenClose();
								update_waitlist(elevator, reqlist,  e_waitlist);
							}
							new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.MOVE, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//�����Ӵ���������İ�ť
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
				else if(mainReq.getFloorNo()==elevator.getCurfloor())//����������Ŀ��¥��
				{
					elevator.goOpenClose();
					update_waitlist(elevator, reqlist,  e_waitlist);
					new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.MOVE,elevator.getE_time()-1 );
					//����������Ӧ
					//if(!cancelButton(mainReq,elevator))//��������������İ�ť  ???Ϊ�˽��ͬ�����������ƣ������д�
				//	{
					//	System.out.println("error6");
				//	}
					e_waitlist.deleteReq(mainReq);
					//reqlist.deleteReq(mainReq);
					//������û��ͬ����Ӧ���Ӵ�����
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//���Ӵ����������Ӧ
						{
							new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.MOVE, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//�����Ӵ����������
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					//�ɵ�����������Ӧ��ϣ�ѡ���µ�������
					int hit=0;
					for(int i=0;i<e_waitlist.getListlen();i++)//��������û�о��������δ����Ӵ�����
					{
						if(e_waitlist.getReq(i).getStatus()==1)//���Ӵ����������Ӧ
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
					if(hit==0)//û�о��������δ����Ӵ�������ѡ��ȴ������еĵ�һ������
					{
						if(e_waitlist.getListlen()==0)//�ȴ�����ҲΪ�գ�����ݽ���wfs״̬
						{
							elevator.gowait(0.5);
							update_waitlist(elevator, reqlist,  e_waitlist);
							continue;
						}else//�ȴ����в�Ϊ��
						{
							mainReq = e_waitlist.getReq(0);
							elevator.setState(ElevatorState.STILL);//���õ���״̬Ϊ��ֹ������STILL��֧����
							continue;
						}
					}
					System.out.println("error7");//�����ܵ�����
				}
				else
				{
					System.out.println("error5");
				}
			}
			else if(elevator.getState()==ElevatorState.MOVE)
			{
				if(mainReq.getFloorNo()<elevator.getCurfloor())//��û����������Ŀ��¥��
				{
					//�жϵ�ǰ����û�п��Դ�������Ҫ��Ӧ
					int firsttag=1;
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//���Ӵ����������Ӧ
						{
							if(firsttag==1)
							{
								firsttag=0;
								elevator.goOpenClose();
								update_waitlist(elevator, reqlist,  e_waitlist);
							}
							//new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.DOWN, elevator.getE_time());
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//�����Ӵ���������İ�ť
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
				else if(mainReq.getFloorNo()==elevator.getCurfloor())//����������Ŀ��¥��
				{
					elevator.goOpenClose();
					update_waitlist(elevator, reqlist,  e_waitlist);
					//new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.DOWN,elevator.getE_time()-1);
					//����������Ӧ
					//if(!cancelButton(mainReq,elevator))//��������������İ�ť  ???Ϊ�˽��ͬ�����������ƣ������д�
					//{
					//	System.out.println("error6");
					//}
					e_waitlist.deleteReq(mainReq);
					//reqlist.deleteReq(mainReq);
					//������û��ͬ����Ӧ���Ӵ�����
					for(int i=0;i<e_waitlist.getListlen();i++)
					{
						if(e_waitlist.getReq(i).getStatus()==1&&
								e_waitlist.getReq(i).getFloorNo()==elevator.getCurfloor())//���Ӵ����������Ӧ
						{
							//new Output().output(e_waitlist.getReq(i), elevator.getCurfloor(), ElevatorState.DOWN, elevator.getE_time()-1);
							//if(!cancelButton(e_waitlist.getReq(i),elevator))//�����Ӵ���������İ�ť
							//{
							//	System.out.println("error6");
							//}
							e_waitlist.deleteReq(e_waitlist.getReq(i));
							//reqlist.deleteReq(e_waitlist.getReq(i));
							i--;
						}
					}
					//�ɵ�����������Ӧ��ϣ�ѡ���µ�������
					int hit=0;
					for(int i=0;i<e_waitlist.getListlen();i++)//��������û�о��������δ����Ӵ�����
					{
						if(e_waitlist.getReq(i).getStatus()==1)//���Ӵ����������Ӧ
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
					if(hit==0)//û�о��������δ����Ӵ�������ѡ��ȴ������еĵ�һ������
					{
						if(e_waitlist.getListlen()==0)//�ȴ�����ҲΪ�գ�����ݽ���wfs״̬
						{
							mainReq = null;
							elevator.gowait(0.5);
							update_waitlist(elevator, reqlist,  e_waitlist);
							continue;
						}else//�ȴ����в�Ϊ��
						{
							mainReq = e_waitlist.getReq(0);
							elevator.setState(ElevatorState.STILL);//���õ���״̬Ϊ��ֹ������STILL��֧����
							continue;
						}
					}
					System.out.println("error7");//�����ܵ�����
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
					if(mainReq.getFloorNo()==elevator.getCurfloor())//ͬ������
					{
						elevator.goOpenClose();
						new Output().output(mainReq, elevator.getCurfloor(), ElevatorState.STILL,elevator.getE_time() );
						//����������Ӧ
						e_waitlist.deleteReq(mainReq);
						//reqlist.deleteReq(mainReq);
						update_waitlist(elevator, reqlist,  e_waitlist);
						//if(!cancelButton(mainReq,elevator))//��������������İ�ť
						//{
						//	System.out.println("error6");
						//}
						do {//�ҵ���һ��mainreq
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
					else if(mainReq.getFloorNo()>elevator.getCurfloor())//����
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
				}else//�ȴ�����Ϊ�գ�����WFS
				{
					//System.out.println("error4");
					elevator.gowait(0.5);
					update_waitlist(elevator, reqlist,  e_waitlist);
				}
				
			}
		}//��ѭ��
	}
	
	public static void main(String[] args) {
		// TODO �Զ����ɵķ������
		Boolean ret;
		Scheduler scheduler = new Scheduler();
		RequestList reqlist = new RequestList();
		//Elevator elevator = new Elevator();
		ret = scheduler.inputReqs(reqlist);//�����������󣨳�����ǰ���invalid����
		if(!ret)
		{
			return ;
		}
		scheduler.deleteInvalidReqs(reqlist);//ɾ��invalid����
		//��ʼ����
		//scheduler.ALS_schedul(elevator, reqlist);
		//���Ƚ���
		return;
	}
	
	
}
