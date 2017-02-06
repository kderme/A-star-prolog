import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.PriorityQueue;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPTermParser;


public class Astar {
	double startX,startY,targetX,targetY;
	private JIPEngine jip;
	private JIPTermParser parser;
	private Hashtable<Long,Node> nodes;
	
	private HashSet<Node> closedSet;
	private PriorityQueue<Node> openQ;
	
	public double pathCost;
	private Long startNid,targetNid;
	private Node startNode,targetNode;
	private ArrayList<Pair> path;
	public boolean found=false;
	public String color="blue";
	public String name="";
	
	public Astar
		(double startX, double startY, double targetX, double targetY,
		JIPEngine jip,JIPTermParser parser,Hashtable<Long,Node> nodes, String name)
	{
		this.startX=startX; this.startY=startY;
		this.targetX=targetX; this.targetY=targetY;
		this.jip=jip; this.parser=parser;
		this.nodes=nodes;
		
		this.closedSet = new HashSet<Node>();
		Comparator<Node> comparator = new Node.fComparator();
		this.openQ = new PriorityQueue<Node>(1,comparator);
		//this.openQ = new PriorityQueue<Node>(1,new Node.fComparator());

		this.pathCost=0.0D;
		
		this.startNid=3936338639L;//closestNode(startX,startY);
		this.startNode=nodes.get(startNid);
		if(startNode==null){
			System.out.println("No key for "+startNid);
			System.exit(1);
		}
		this.targetNid=2423063624L;//closestNode(targetX,targetY);
		this.targetNode=nodes.get(targetNid);
		if(targetNode==null){
			System.out.println("No key for "+startNid);
			System.exit(1);
		}
			
		startNode.update(0,0,null);
		startNode.parent=new Node();
		startNode.parent.Nid=0L;
		this.openQ.add(startNode);
		this.name=name;
	}
	
	public boolean ASearch(){
		JIPQuery jipQuery;
		JIPTerm term1;
		ArrayList<JIPTerm> terms;
		double lastf=0.0;
		Node current=null;
		long endTime = System.currentTimeMillis();
		long startTime;
		long totalTime;
		while(!openQ.isEmpty()){
			if (current!=null){
				lastf=current.fScore;
			}
			
			current = openQ.poll();
			
			System.out.println(current.Nid+":("+current.fScore+"="+current.gScore+"+"+current.hScore+"),"+openQ.size()+","+closedSet.size());
			if(current.fScore<lastf){
				System.out.println("current.fScore<lastf");
				System.exit(1);
			}

			closedSet.add(current);
//			current.print();
			if(current==targetNode){
				/*	TARGET FOUND	*/
				//targetNode.parent=current;
				found=true;
				return true;
			}
			String q;
			
			startTime = System.currentTimeMillis();
			
//			q="canGoAll2("+current.Nid+","+current.parent.Nid+","+targetNid+",X,[]),!.";
/*
 * 			findall  method			
 *
			q="canGoAll("+current.Nid+","+current.parent.Nid+","+targetNid+",X,[]),!.";			
			//q="canGoAll("+current.Nid+","+targetNid+",X),!.";
			System.out.println(q);
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));			
			term1=jipQuery.nextSolution();
			if(term1==null){
				continue;
			}
			String res=term1.toStringq(jip);
			System.out.println(res);
			String res1=res.split("\\[")[1];
			res1=res1.split("\\]")[0];
			if(res1.equals("")||res1==null){
				continue;
			}
			System.out.println(res1);
			String [] res2=res1.split(",");
		*
		 * ... findall method	
		 */
			
//			res2[res2.length-1]=res2[res2.length-1].split("]")[0];			
			
/*
			System.out.println(Asolver.listget(term1,"Nss"));
			System.out.println(Asolver.get(term1,"Dss"));
			System.out.println(Asolver.get(term1,"Hss"));
			q="can_go1("+current.Nid+","+targetNid+",N,D,H,[]),!.";
			System.out.println(q);
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
			terms=new ArrayList<JIPTerm>();
			term1=jipQuery.nextSolution();
			terms.add(term1);
			/*
			for (;  (term1 = jipQuery.nextSolution()) != null;  terms.add(term1) )
				;
			long nid2=Asolver.lget(term1, "N");
			term1=jipQuery.nextSolution();
			q="can_go1("+current.Nid+","+targetNid+",N,D,H,["+nid2+"]),!.";
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
			term1=jipQuery.nextSolution();
			terms.add(term1);
*/
			q="can_go("+current.Nid+","+current.parent.Nid+","+targetNid+",N,D,H).";
			System.out.println(q);
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
			terms=new ArrayList<JIPTerm>();
			term1=jipQuery.nextSolution();
			terms.add(term1);
			
			for (;  (term1 = jipQuery.nextSolution()) != null;  terms.add(term1) )
				;
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("			time= "+totalTime);
			
			
//			System.exit(1);

			for (JIPTerm term: terms) {
//	old 		for(Edge e : current.adj){
// findall			for(int i=0; i<res2.length;i++){
/* findall			String [] res3=res2[i].split("/");
				long nid=0L;
				try{
					nid=Asolver.stol(res3[0]);
				}
				catch(NumberFormatException e){
					e.printStackTrace();
					System.out.println("res="+res);
					System.out.println("res1="+res1);
					System.out.println("res2["+i+"]="+res2[2]);
				}
				double distance= Asolver.stod(res3[1]);
				double hScore=Asolver.stod(res3[2]);
*	...findall			
*/			
				long nid=Asolver.lget(term, "N");
				double distance= Asolver.dget(term,"D");
				double hScore=Asolver.dget(term,"H");
				Node neighbor=nodes.get(nid);
				if (neighbor==null){
					System.out.println("get("+nid+")=null");
					System.out.println("nid="+nid);
					System.exit(1);
				}
				else{
//					System.out.println("->"+nid);
				}
				if (closedSet.contains(neighbor))
					/*	IGNORE CLOSED CASES		*/
					continue;
				double gScore = current.gScore + distance;
				if(!openQ.contains(neighbor)){
					neighbor.update(gScore,hScore,current);
					openQ.add(neighbor);
					continue;
				}
				else if (gScore >= neighbor.gScore)
					/*	IS DISCOVERED AND WE FOUND STH WORSE	*/
					continue;
				/*	IS DISCOVERED AND WE FOUND STH BETTER	*/
				openQ.remove(neighbor);
				neighbor.update(gScore,hScore,current);
				openQ.add(neighbor);
			}
		}
		found=false;
		return found;
	}
	
	public void printPath(PrintWriter writer){
		for (Pair p:path){
			writer.println("					"+p.left+"	"+p.right);
		}
	}
	
	public void createPath(){
		JIPQuery jipQuery;
		JIPTerm term;
		
		path=new ArrayList<Pair>();
		path.add(new Pair(Double.toString(targetX),Double.toString(targetY)));
		for (Node n=targetNode; n!=startNode;n=n.parent){
			String q="node(X,Y,"+n.Nid+",_,_),!.";
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
			if ((term = jipQuery.nextSolution())!=null){
				String X=Asolver.get(term,"X");
				String Y=Asolver.get(term,"Y");
				path.add(new Pair(X,Y));
			}
		}
		path.add(new Pair(Double.toString(startX),Double.toString(startY)));
		Collections.reverse(path);
	}
	
/*	
	public void path(){
		path= new ArrayList<Node>();
		for (Node n=targetNode; n!=startNode; n=n.parent){
			path_length+=n.parent.distance;
			path.add(n);
//			System.out.println(path.size());
		}
		path.add(startNode);
		Collections.reverse(path);
	}
*/
	
	private long closestNode(double x,double y){
		JIPQuery jipQuery;
		JIPTerm term;

		String q="findClosestNode("+x+","+y+",N,D).";
		System.out.println(q);
		double minD=100.0D,	 tempD;
		long minN=0L,  tempN;
		jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
		for(term = jipQuery.nextSolution(); term!=null;term = jipQuery.nextSolution()){
			tempD=Asolver.dget(term,"D");
			tempN=Asolver.lget(term,"N");
			if (tempD<minD){
				minN=tempN;
				minD=tempD;
			}
		}
		if(minN==0L){
				
		}
		System.out.println(minN+","+minD);
		return minN;
	}
	
	private class Pair {
		  private final String left;
		  private final String right;

		  public Pair(String left, String right) {
		    this.left = left;
		    this.right = right;
		  }
	}
}
