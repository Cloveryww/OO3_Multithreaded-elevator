package oo3;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElevatorSys {
	/* @ OVERVIEW:���𴴽����������������̺߳�һ���������̣߳�ͬʱ�����������������ܵ�����ȴ����С�
	 */
	final static int FLOOR_MIN = 1;
	final static int FLOOR_MAX = 20;
	private static PrintWriter printer;//������ļ�����ʹ��resultFile.println()�����Ϣ���ļ�
	private static long startTime;//ϵͳ��ʼʱ��

  
	public static void main(String[] args) {//��ɳ�ʼ�������������������̣߳�ͬʱ������ܵ������������
		/* @ EFFECTS: 
		   @ THREAD_REQUIRES:\locked(reqList_all, ele1, ele2, ele3)
		   @ THREAD_EFFECTS: \locked(reqList_all, ele1, ele2, ele3)
	   */
		int inputtype=1;
		try {
			PipedWriter out = new PipedWriter();  
	        PipedReader in = new PipedReader();  
	        out.connect(in);  
			Scanner input;
			if(inputtype==0)//��Autotest�̻߳�ȡ����
			{
				Autotest autotest = new Autotest(out);
				new Thread(autotest).start();
				input= new Scanner(in);
			}
			else{
				input = new Scanner(System.in);  //�ӿ���̨��ȡ����
			}
            try {//��ֹ���ļ�ʧ��crash
                printer = new PrintWriter(new BufferedWriter(new FileWriter("result.txt")));
            } catch (Exception e) {
                System.exit(0);
            }
            RequestList reqList_all = new RequestList();//��ʼ���ܵ��������
            Floor floor = new Floor(FLOOR_MIN,FLOOR_MAX);//��ʼ��¥���࣬��������¥��İ�ť��Ϣ�������ж�same
                       
            
            //��ʼ��������(�߳�)
            Elevator ele1 = new Elevator(1, floor,printer);
            Elevator ele2 = new Elevator(2, floor,printer);
            Elevator ele3 = new Elevator(3, floor,printer);
            
            //��ʼ����������(�߳�)
            NewScheduler scheduler = new NewScheduler(reqList_all, ele1, ele2, ele3,floor,printer);
            
            ele1.sch = scheduler;
            ele2.sch = scheduler;
            ele3.sch = scheduler;
            
            //�����߳�
            Thread temp;
            temp = new Thread(ele1);
           // temp.setUncaughtExceptionHandler(new UncaughtExceptionHandler());
            temp.start();
            temp = new Thread(ele2);
            //temp.setUncaughtExceptionHandler(new UncaughtExceptionHandler());
            temp.start();
            temp = new Thread(ele3);
            //temp.setUncaughtExceptionHandler(new UncaughtExceptionHandler());
            temp.start();
            temp = new Thread(scheduler);
            //temp.setUncaughtExceptionHandler(new UncaughtExceptionHandler());
            temp.start();
           
            
            //��ʼ���ܿ���̨��������
            String str="";
            int tag = 0;
            long curTime;//��¼���󵽴�����ʱ��(��Ե�һ����������ʱ��)
            int reqNum;//һ�����������������Ϊ10
            DecimalFormat df = new DecimalFormat("0.0");//��ʽ��long
            while (true) {
            	try {
            		str = input.nextLine();
            	}catch(NoSuchElementException e)
            	{
            		System.out.println("Pipe error!");
            		break;
            	}
                if (str.equals("END")) //�������
                	break;
                str = str.replaceAll(" ", "");//ɾ�����пո�
                String[] strs = str.split("[;]", -1);//��;�ָ������󣬱����-1����������������
                if (tag==0) {//��¼��һ����������ʱ��
                	startTime = new Date().getTime();
                	tag = 1;
                	curTime = 0;
                }
                else 
                	curTime = new Date().getTime() - startTime;
                //begin add req to reqList
                reqNum = 0;
                for (int i=0;i<strs.length;i++) 
                {
                    if (isValidReq(strs[i]) && reqNum < 10) {//�ж������Ƿ�Ϸ��������Ƿ�һ������������10
                    	//begin parse strs[i]
                    	Request req = string2Req(strs[i],curTime);
                    	reqList_all.addReq(req);//��������������������  (����Ϊԭ�Ӳ���)
                        reqNum++;
                    } else {//INVALID req
                    	if(reqNum<10)
                    	{
                    		if(isDebug())
                    		{
                    			System.out.println((curTime+ElevatorSys.getStartTime()) + ":INVALID [" + strs[i] + ", " + df.format(curTime / 1000.0) + "]");
                    		}
                    		printer.println((curTime+ElevatorSys.getStartTime()) + ":INVALID [" + strs[i] + ", " + df.format(curTime / 1000.0) + "]");
                    	}
                    }
                }
            }//end while (true)
            input.close();
            printer.close();
            System.exit(0);
        } catch (Exception e) {
        	if(isDebug())
        	{
        		e.printStackTrace();
        		System.out.println("Input error!");
        	}
            System.exit(0);
        }
		
	}//end main
	 public static boolean isValidReq(String str) //�ж��Ƿ�Ϊ�Ϸ�������
	 {
		 /*@REQUIRES��str!=null;
		  @EFFECTS��str is valid ==> \result = ture;
		  @EFFECTS��	str is invalid ==> \result = false;
		  */
	        Pattern pat1 = Pattern.compile("^\\(FR,\\d+,(UP|DOWN)\\)$");//����(FR,2,DOWN)�Ϸ�
	        Pattern pat2 = Pattern.compile("^\\(ER,#\\d+,\\+?\\d+\\)$");//����(ER,#1,6)�Ϸ�
	        try {
	            Matcher matcher1 = pat1.matcher(str);
	            Matcher matcher2 = pat2.matcher(str);
	            if (!matcher1.find() && !matcher2.find()) //��������
	            	return false;
	            //�Ϸ�
	            String[] strs = str.split("[,()]");
	            if (strs.length!=4) //��Ƭ������Ϊ4
	            	return false;
	            if (strs[1].equals("ER")) {
	                if (Integer.parseInt(strs[3])>=Floor.getBottomFloor() && Integer.parseInt(strs[3])<=Floor.getTopFloor() &&
	                        Integer.parseInt(strs[2].substring(1))>0 && Integer.parseInt(strs[2].substring(1))<4) 
	                {
	                    return true;
	                }
	            }
	            if (strs[1].equals("FR")) {
	                if (Integer.parseInt(strs[2])>=Floor.getBottomFloor() && Integer.parseInt(strs[2])<=Floor.getTopFloor() &&(
	                        strs[3].equals("UP") || strs[3].equals("DOWN"))) {
	                    if ((Integer.parseInt(strs[2])==Floor.getBottomFloor() && strs[3].equals("DOWN") ) ||
	                            (Integer.parseInt(strs[2])==Floor.getTopFloor() && strs[3].equals("UP") )) 
	                    	return false;
	                    return true;
	                }
	            }
	        } catch (Exception e) {
	        	if(isDebug())
	        	{
	        		System.out.println("regex error!");
	        	}
	            return false;
	        }
	        return false;
	}
	 public static Request string2Req(String str,long curTime)
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
		if(len==4)
		{
			try {
				if(terms[1].equals("FR"))//(FR,2,DOWN)
				{
					ReqType type = ReqType.FR;
					int floorNo = Integer.parseInt(terms[2]);
					Direction fRdir;
					if(terms[3].equals("UP"))
					{
						fRdir = Direction.UP;
					}
					else
					{
						fRdir = Direction.DOWN;
					}
					req = new Request(type, floorNo,  fRdir,  curTime);
				}
				else {//(ER,#1,6)
					ReqType type = ReqType.ER;
					int floorNo = Integer.parseInt(terms[3]);
					int ele_id = Integer.parseInt(terms[2].substring(1));
					req = new Request(type, ele_id,floorNo, curTime);
				}
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
	public static long getStartTime() {
		/*@EFFECTS�� \result==this.startTime			
		*/
		return startTime;
	}
	public static boolean isDebug(){//�жϵ�ǰ�Ƿ���debugģʽ��debugר��
		return false;
	}

}
