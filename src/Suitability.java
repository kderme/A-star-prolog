import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPTerm;


//"client(X,Y,Xdest,Ydest,Time, Persons, Language, Luggage)."
//"taxi(X,Y,Tid,Available,From,To,Languages,Rating,Long_distance,Type,_)"


public class Suitability {

	private JIPEngine jip;
	private JIPTerm term;
	private JIPTerm termCl;

	boolean suitable=true;
	public double suitability=5.0;
	public double distSuitability=5.0;
	public double rating;
	public Astar astar;
	
	private ArrayList<String> warnings=null;
	private int Tid;
	
	public Suitability(JIPEngine jip, JIPTerm term,JIPTerm termCl, Astar astar, int Tid){ 
		this.jip=jip;
		this.term=term;
		this.termCl=termCl;
		this.astar=astar;
		this.Tid=Tid;
		this.warnings=new ArrayList<String>();
	}

	public void solve(){
		//	PARSE CLIENT
		String stime=Asolver.get(termCl,"Time");
		long timeCl=Asolver.getTime(stime);
		int personsCl=Integer.parseInt(Asolver.get(termCl,"Persons"));
		String lang=Asolver.get(termCl,"Language");
		int lugg=Integer.parseInt(Asolver.get(termCl,"Luggage"));

		//	PARSE TAXI
		String available=Asolver.get(term,"Available");
		int from=Asolver.iget(term,"From");
		int to=Asolver.iget(term,"To");
	//	TODO
//	ArrayList<String> Languages=
		String res=term.toStringq(jip);
		String res1=res.split("\\[")[1];
		res1=res1.split("\\]")[0];
		String [] res2;
		if(res1.equals("")||res1==null){
			res2=null;
		}
		else{
			res2=res1.split(",");
		}
		this.rating=Asolver.dget(term,"Rating");
		boolean longDistance=Asolver.get(term,"Long_distance").equals("yes");
		String Type=Asolver.get(term,"Type");
		
		//availability
		if(available.equals("no")){
			suitability=0;
			warnings.add("Taxi unavailable!");
			suitable=false;
			return;
		}
		
		//persons
		if(personsCl<from){
			warnings.add("Taxi too big! It takes from "+from+"!");
			suitability-=1.5;
		}
		
		if(personsCl>to){
			warnings.add("Taxi too small! It takes up to "+to+"!");
			suitability-=3;
		}
		
		//language
		if(res1.equals("")||res1==null){
			res=null;
			suitability-=1;
			warnings.add("Taxi driver can`t speak "+lang);
		}
		
		else if(!Arrays.asList(res2).contains(lang)){
			suitability-=1;	
			warnings.add("Taxi driver can`t speak "+lang+"!");
		}

		//luggage
		if(lugg>2 && Type.equals("subcompact")){
			warnings.add("Taxi may be too small for your luggage!");
			suitability-=1;
		}
		
		//long distances
		if(longDistance && astar.pathCost>0.001){
			warnings.add("Taxi is great for big distances!");
			suitability-=4;
		}
		
		if(!longDistance && astar.pathCost>0.001){
			warnings.add("Taxi is not suitable for big distances!");
		}
		
		if (suitability<0)
			suitability=0;
	}

	public void ssay(){
		System.out.println("TAXI "+Tid+"="+suitability+"    cost="+astar.pathCost);
		for(String s: warnings){
			System.out.println(s);
		}
		System.out.println();
	}
	
	public void rsay(){
		System.out.println("TAXI "+Tid+":"+this.rating);
	}
		
	static class sComparator implements Comparator<Suitability>
	 {
		public int compare(Suitability one, Suitability two){
			if(one.suitability-two.suitability>0.0)
				return -1;
			else if(one.suitability-two.suitability<0.0)
				return 1;
			return 0;
		}
	 }
	
	static class dComparator implements Comparator<Suitability>
	 {
		public int compare(Suitability one, Suitability two){
			if(one.astar.pathCost-two.astar.pathCost>0.0)
				return 1;
			else if(one.astar.pathCost-two.astar.pathCost<0.0)
				return -1;
			return 0;
		}
	 }

	static class rComparator implements Comparator<Suitability>
	 {
		public int compare(Suitability one, Suitability two){
			if(one.rating-two.rating>0.0)
				return -1;
			else if(one.rating-two.rating<0.0)
				return 1;
			return 0;
		}
	 }
}
