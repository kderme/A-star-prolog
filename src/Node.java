import java.util.ArrayList;
import java.util.Comparator;

public class Node {
	public Long Nid;
	public double gScore;
	public double hScore;
	public double fScore;
	public Node parent;
	
	public ArrayList<Long> n;
	
	Node(){
		
	}
	
	Node(long n){
		Nid=n;
	}
	
	public void update(double g, double h, Node p){
		gScore=g;
		hScore=h;
		parent=p;
		
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
}
