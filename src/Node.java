import java.util.ArrayList;
import java.util.Comparator;

public class Node {
	double x,y;
	public Long Lid;
	public Long Nid;
	public double gScore;
	public double hScore;
	public double fScore;
	public Goto parent;
	
	public ArrayList<Goto> gt;
	
	Node(){
		
	}
	
	Node(long n){
		Nid=n;
		gt=new ArrayList<Goto>();
	}
	
	public void update(double g, Node n, double cost){
		gScore=g;
		parent=new Goto(n,cost);
		
		fScore=gScore+hScore;
	}
	static class fComparator implements Comparator<Node>
	 {
		public int compare(Node one, Node two){
			if(one.fScore-two.fScore>0.0)
				return 1;
			else if(one.fScore-two.fScore<0.0)
				return -1;
			return 0;
		}
	 }
	
	public void add(Node node, double dist){
		gt.add(new Goto(node, dist));
	}
	/*
	 * This class storing edges was added to speedup the process
	 * which was too late with prolog.
	 */
	class Goto{
		Node node;
		double cost;
	
	public Goto(Node node, double cost){
		this.node=node;
		this.cost=cost;
	}
	}
}

