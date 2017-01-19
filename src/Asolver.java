import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPTermParser;


public class Asolver {
	JIPEngine jip;
	JIPTermParser parser;
	
	Hashtable<Integer,Astar> liSTars;
	Hashtable<Long,Node> nodes;
	
	String inputDirPath;
	String outputDirPath;
	
	public Asolver(String inputDirPath,String outputDirPath,Hashtable<Long,Node> nodes) {
		jip = new JIPEngine();
		this.inputDirPath=inputDirPath;
		this.outputDirPath=outputDirPath;
		try {
			System.out.println("Consulting prolog file for the first time");
			System.out.println("This will take a while...");
			jip.consultFile(outputDirPath+"/rules.pl");
//			jip.consultFile(folder+"/facts.pl");
		} 
		catch (JIPSyntaxErrorException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
		System.out.println();
		parser = jip.getTermParser();
		liSTars = new Hashtable<Integer,Astar>(); 	
		this.nodes=nodes;
	}
	
	public void solve(){
		JIPQuery jipQuery,jipQueryCl;
		JIPTerm term, termCl;
		Astar astar;
		
		jipQueryCl = jip.openSynchronousQuery(parser.parseTerm("client(X,Y,Xdest,Ydest,Time, Persons, Language, Luggage)."));
		termCl = jipQueryCl.nextSolution();
		if(termCl==null){
			System.out.println("Can`t find client");
			System.exit(1);
		}
		double Xcl=dget(termCl,"X");
		double Ycl=dget(termCl,"Y");
		double nidCl=closestNode(Xcl, Ycl);
		jipQuery = jip.openSynchronousQuery(parser.parseTerm("taxi(X,Y,Tid,_,_,_,_,_,_,_,_)."));
		boolean all=true;
		for (term = jipQuery.nextSolution(); term != null;term = jipQuery.nextSolution()) {
			int Tid=iget(term,"Tid");
			double X=dget(term,"X");
			double Y=dget(term,"Y");
			astar=new Astar(X,Y,Xcl,Ycl,jip, parser, nodes,"Taxi "+Tid);
			System.out.println("Searching path for Taxi "+Tid+"...");
			if(!astar.ASearch()){
				System.out.println("Search failed for taxi "+Tid);
				System.exit(0);
				all=false;
				continue;
			}
			System.out.println("Creating path for Taxi "+Tid+"...");
			astar.createPath();
			System.out.println("OK");
			System.out.println("Path length: "+astar.pathCost);
			liSTars.put(Tid,astar);
//			System.out.println("Path length "+astar.path_length);
			KmlWriter kml = new KmlWriter(outputDirPath);
			kml.write(astar);
			kml.end();
			System.exit(0);
		}
		int Xdest=iget(termCl,"Xdest");
		int Ydest=iget(termCl,"Ydest");
		astar=new Astar(Xcl,Ycl,Xdest,Ydest,jip, parser, nodes, "Client");
		if(!astar.ASearch()){
			System.out.println("Search Failed for your dest");
			System.exit(0);
		}
		astar.createPath();
		
		//KmlWriter kml = new KmlWriter(outputDirPath);
	}
	
	public static String get(JIPTerm term, String var){
		String str=term.getVariablesTable().get(var).toString();
		return str;
	}
	
	public static int iget(JIPTerm term, String var){
		return Integer.parseInt(get(term,var));
	}
	
	public static long lget(JIPTerm term, String var){
		String str=get(term,var);
		if(!str.contains("E"))
			return new Long(str);
		String [] spl=str.split("E");
		double d=Double.valueOf(spl[0]);
		int times=Integer.parseInt(spl[1]);
		return (long) d*times;
		
	}
	
	public static double dget(JIPTerm term, String var){
		return Double.valueOf(get(term,var));
	}
	
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
		return minN;
	}



	public double closestNodeOld(double x,double y){
		JIPQuery jipQuery;
		JIPTerm term;
		
		String q="closestNode("+x+","+y+",N,D).";
		jipQuery=jip.openSynchronousQuery(parser.parseTerm(q));
		term = jipQuery.nextSolution();
		if (term != null) {
			long Nid=new Long(term.getVariablesTable().get("N").toString());
			double D=Double.valueOf(term.getVariablesTable().get("D").toString());
			if (D>20){
				System.out.println("closest node is too far:"+D+" metres away" );
				return 0L;
			}
			return Nid;
		}
		return 0L;
	}
	
}
