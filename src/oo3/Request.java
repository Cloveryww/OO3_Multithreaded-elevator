package oo3;


public class Request {
	private ReqType type;//类型
	private int floorNo;//目的楼层
	private Direction FRdir;//请求方向
	private int ele_id;//电梯的id
	private long time;//相对第一个请求发生的时间
	private int status; //  0: 合法    1: 在捎带集合中   2:same   3:invalid
	
	public Request(ReqType type, int floorNo, Direction fRdir, long time) {
		super();
		this.type = type;
		this.floorNo = floorNo;
		FRdir = fRdir;
		this.time = time;
		this.status = 0;
	}
	public Request(ReqType type, int ele_id, int floorNo, long time) {
		super();
		this.type = type;
		this.ele_id = ele_id;
		this.floorNo = floorNo;
		this.time = time;
		this.status = 0;
	}
	
	public int getEle_id() {
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	\result == this.ele_id
		 */
		return ele_id;
	}
	public void setEle_id(int ele_id) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.ele_id
			@ EFFECTS: 	this.ele_id ==ele_id;
		 */
		this.ele_id = ele_id;
	}
	public int getStatus() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.status
	 */
		return status;
	}
	public void setStatus(int status) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.status
			@ EFFECTS: 	this.status ==status;
		 */
		this.status = status;
	}
	public ReqType getType() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.type
	 */
		return type;
	}
	public int getFloorNo() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.loorNo
	 */
		return floorNo;
	}
	public Direction getFRdir() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.FRdir
	 */
		return FRdir;
	}
	public long getTime() {
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	\result == this.time
	 */
		return time;
	}
	public void setType(ReqType type) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.type
			@ EFFECTS: 	this.type ==type;
		 */
		this.type = type;
	}
	public void setFloorNo(int floorNo) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.floorNo
			@ EFFECTS: 	this.floorNo ==floorNo;
		 */
		this.floorNo = floorNo;
	}
	public void setFRdir(Direction fRdir) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.fRdir
			@ EFFECTS: 	this.fRdir ==fRdir;
		 */
		FRdir = fRdir;
	}
	public void setTime(long time) {
		/*  @ REQUIRES: this!=null;
		 *  @ MODIFIES: this.time
			@ EFFECTS: 	this.time ==time;
		 */
		this.time = time;
	}
	public String toString()//多进程用的toString
	{
		/*  @ REQUIRES: this!=null;
			@ EFFECTS: 	print this;
		 */
		String str="";
		if(this.type==ReqType.FR)//(FR,1,UP)
		{
			if(this.FRdir==Direction.UP)
			{
				str = "(FR,"+Integer.toString(this.floorNo)+",UP)";
			}
			else
			{
				str = "(FR,"+Integer.toString(this.floorNo)+",DOWN)";
			}
			return str;
		}
		//(ER,#1,5)
		str = "(ER,#"+Integer.toString(this.getEle_id())+","+Integer.toString(this.getFloorNo())+")";
		return str;
	}

	public String toString3()
	{
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	print this;
	 */
		String str = "";
		if(this.type==ReqType.FR)//(FR,1,UP,0)
		{
			if(this.FRdir==Direction.UP)
			{
				str = "(FR,"+Integer.toString(this.floorNo)+",UP,"+Long.toString(this.time)+")";
			}
			else
			{
				str = "(FR,"+Integer.toString(this.floorNo)+",DOWN,"+Long.toString(this.time)+")";
			}
			return str;
		}
		if(this.FRdir==Direction.UP)
		{
			str = "(ER,"+Integer.toString(this.floorNo)+",UP,"+Long.toString(this.time)+")";
		}
		else
		{
			str = "(ER,"+Integer.toString(this.floorNo)+","+Long.toString(this.time)+")";
		}
		return str;
	}
	public String toString2()//[request]  such as  :[FR,1,UP,0]
	{
		/*  @ REQUIRES: this!=null;
		@ EFFECTS: 	print this;
	 */
		String str = "";
		if(this.type==ReqType.FR)//(FR,1,UP,0)
		{
			if(this.FRdir==Direction.UP)
			{
				str = "[FR,"+Integer.toString(this.floorNo)+",UP,"+Long.toString(this.time)+"]";
			}
			else
			{
				str = "[FR,"+Integer.toString(this.floorNo)+",DOWN,"+Long.toString(this.time)+"]";
			}
			return str;
		}
		if(this.FRdir==Direction.UP)
		{
			str = "[ER,"+Integer.toString(this.floorNo)+",UP,"+Long.toString(this.time)+"]";
		}
		else
		{
			str = "[ER,"+Integer.toString(this.floorNo)+","+Long.toString(this.time)+"]";
		}
		return str;
		
	}

}
