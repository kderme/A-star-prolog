import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
		this.openQ = new PriorityQueue<Node>(1);

		this.pathCost=0.0D;
		this.startNid=this.closestNode(startX,startY);
		this.targetNid=this.closestNode(targetX,targetY);
		this.targetNode=nodes.get(targetNid);
		this.startNode=nodes.get(targetNid);
			if(startNode==null){
				System.out.println("No key for "+startNid);
				System.exit(1);
			}
			 startNode.update(0,0,null);
		this.openQ.add(startNode);
		this.name=name;
	}
	
	public boolean ASearch(){
		JIPQuery jipQuery;
		JIPTerm term;	
			
		while(!openQ.isEmpty()){
			Node current = openQ.poll();
			System.out.println(current.Nid);
			closedSet.add(current);
//			current.print();
			if(current==targetNode){
				/*	TARGET FOUND	*/
				targetNode.parent=current;
				found=true;
				return true;
			}
			String q="can_go("+current+","+targetNid+",N,D,H).";
			jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
			for (term = jipQuery.nextSolution();  term != null;  term = jipQuery.nextSolution()) {
			//for(Edge e : current.adj){
				long nid=Asolver.lget(term, "N");
				double distance= Asolver.dget(term,"D");
				double hScore=Asolver.dget(term,"H");
				Node neighbor=nodes.get(nid);
				if (closedSet.contains(neighbor))
					/*	IGNORE CLOSED CASES		*/
					continue;
				double gScore = current.gScore + distance;
				if(!openQ.contains(neighbor)){
					openQ.add(neighbor);
					neighbor.update(gScore,hScore,current);
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
	public long closestNode(double x,double y){
		JIPQuery jipQuery;
		JIPTerm term;

		String q="findClosestNode("+x+","+y+",N,D).";
		double minD=100.0D,	 tempD;
		long minN=0L,  tempN;
		jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
		for(term = jipQuery.nextSolution(); term!=null;term = jipQuery.nextSolution()){
			tempD=Asolver.dget(term,"D");
			tempN=Asolver.lget(term,"N");
			if (tempD<minD){
				minN=tempN;
			}
		}
		if(minN==0L){
			
		}
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


