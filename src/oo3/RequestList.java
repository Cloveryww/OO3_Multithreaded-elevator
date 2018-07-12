package oo3;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestList {
	private BlockingQueue<Request>  ReqList;//储存有效请求(可能包括same请求)
	private int nextindex;
	
	RequestList()
	{
		ReqList = new LinkedBlockingQueue<>();
		nextindex = 0;
	}
	
	
	public Request pollReq()//获取队列首部的Req,队列为空则返回null
	{
		/*@REQUIRES：this.ReqList!=null;
		  @EFFECTS：\result = this.ReqList.get(index); 
		  */
		return ReqList.poll();
	}
	public Request getReq(int index)//随机获取第index个Req
	{
		/*@REQUIRES：this.ReqList!=null&&index<this.ReqList.size() &&index>0;
		  @EFFECTS：\result = this.ReqList.get(index); 
		  */
		if(index>=ReqList.size()||index<0)
		{
			return null;
		}
		else//合法的索引
		{
			int cur=0;
            for (Request curreq:ReqList) {
                if (cur==index) 
                	return curreq;
                cur++;
            }
		}
		return null;
	}
	public synchronized void  addReq(Request newReq)//添加新请求
	{
		/*  @ REQUIRES: this!=null&&newReq!=null;
			@ EFFECTS: 	\exit Request req; req in this.ReqList
	 */
		ReqList.offer(newReq);//采用offer的方法，防止队列满了，避免阻塞
	}
	public synchronized void deleteReq(int index)
	{
		/*@REQUIRES：this.ReqList!=null&&index<this.ReqList.size() &&index>0;
		  @MODIFIES：this.ReqList
		  @EFFECTS：\all Request r in this.ReqList;r!=\old(this.ReqList).get(index)!=r; 
		  */
		if(index>=ReqList.size()||index<0)
		{
			return;
		}
		else//合法的索引
		{
			int cur=0;
            for (Request curreq:ReqList) {
                if (cur==index) {
                	ReqList.remove(curreq);
                }
                cur++;
            }
		}
		return;
	}
	public synchronized void deleteReq(Request rq)
	{
		/*@REQUIRES：this.ReqList!=null&&req!=null&&(\exist Request req in this.ReqList; req = rq);
		  @MODIFIES：this.ReqList
		  @EFFECTS：\all Request r in this.ReqList;r!=rq 
		  */
		try {
			ReqList.remove(rq);
		}catch(NoSuchElementException e)
		{
			System.out.println("NoSuchElementException in deleteReq!");
		}
	}
	
	public int getListlen()
	{
		/*@REQUIRES：this.ReqList!=null
		  @EFFECTS：\result = ReqList.size() 
		  */
		return ReqList.size();
	}
	//bellow maybe no use
	public int getNextindex() {
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	\result == this.nextindex;
		 */
		return nextindex;
	}

	public void setNextindex(int nextindex) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.nextindex
			@ EFFECTS: 	this.nextindex ==nextindex;
		 */
		this.nextindex = nextindex;
	}
	public void nextindexadd1() {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.nextindex
			@ EFFECTS: 	this.nextindex ==\old(nextindex) + 1;
		 */
		this.nextindex = this.nextindex+1;
	}
}
