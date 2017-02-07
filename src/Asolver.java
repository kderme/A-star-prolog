import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.ParseException;
import gnu.prolog.io.ReadOptions;
import gnu.prolog.io.TermReader;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import jpl.Atom;
import jpl.Query;
import jpl.Term;
import alice.tuprolog.Prolog;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPTermParser;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.SolveInfo; 
import alice.tuprolog.Theory; 
import alice.tuprolog.Var;

public class Asolver {
	JIPEngine jip;
	JIPTermParser parser;
	
	ArrayList<Suitability> listSuit;
	Hashtable<Long,Node> nodes;
	private ArrayList<Long> arr;
	
	String inputDirPath;
	String outputDirPath;

	public Asolver(String inputDirPath,String outputDirPath,Hashtable<Long,Node> nodes,JIPEngine jip, JIPTermParser parser,ArrayList<Long> arr) {
		/*
		 *		Tried jpl (swi-prolog for java)
		 *  	(failed)
		 *  
		 *  Query q1 =
			    new Query(
			        "consult",
			        new Term[] {new Atom(outputDirPath+"/rules.pl")}
			    );	*/
		listSuit = new ArrayList<Suitability>();
		this.arr=arr;
		this.nodes=nodes;
		this.inputDirPath=inputDirPath;
		this.outputDirPath=outputDirPath;
		this.jip=jip;
		this.parser=parser;
		/*
		 * 		Tried Gnu-prolog for java			
		 *		(failed)
		 *
		Environment environment=new Environment();

		System.out.println("Consulting prolog file for the first time");
		environment.ensureLoaded(AtomTerm.get("common-files/rules2.pl"));
		
		System.out.println("DONE");
 
		Interpreter interpreter;
		interpreter = environment.createInterpreter();
		environment.runInitialization(interpreter);
		for (Object element : environment.getLoadingErrors())
		{
			PrologTextLoaderError err = (PrologTextLoaderError) element;
			System.err.println(err);
			// err.printStackTrace();
		}
		StringReader rd = new StringReader("a(b)");
		TermReader trd = new TermReader(rd, environment);
		// TermWriter out = new TermWriter(new OutputStreamWriter(System.out));
		ReadOptions rd_ops = new ReadOptions(new OperatorSet());
		// WriteOptions wr_ops = new WriteOptions();
		gnu.prolog.term.Term goalTerm=null;
		try {
			goalTerm = trd.readTermEof(rd_ops);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		PrologCode code=null;
		try {
			code = environment.getPrologCode(CompoundTermTag.get((CompoundTerm) goalTerm));
		} catch (PrologException e) {
			e.printStackTrace();
		}
		System.out.println(code);

		
		Term[] args = { AtomTerm.get("b")};
		 CompoundTerm goalTerm = new CompoundTerm(AtomTerm.get("a"),null);
		try {
			interpreter.runOnce(AtomTerm.get("a(b)."));
		} catch (PrologException e) {
			e.printStackTrace();
		} 
	*/	
		
		try {
			System.out.println("Consulting prolog file for the first time");
			System.out.println("This will take a while...");
			
			jip.consultFile(outputDirPath+"/rules.pl");
			
//			jip.consultFile(outputDirPath+"/nodes.pl");
/*				
			jip.consultFile(outputDirPath+"/nextt.pl");
*/			
			jip.consultFile(outputDirPath+"/rest.pl");
	
		} 
		catch (JIPSyntaxErrorException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}
	
	public void solve(){
		JIPQuery jipQuery,jipQueryCl;
		JIPTerm term, termCl;
		Astar astar;
		
		String q="client(X,Y,Xdest,Ydest,Time, Persons, Language, Luggage).";
		System.out.println(q);
	//	jipQuery=jip.getTermParser().parseTerm("?- "+q);
		jipQueryCl = jip.openSynchronousQuery(parser.parseTerm(q));
		termCl = jipQueryCl.nextSolution();
		if(termCl==null){
			System.out.println("Can`t find client");
			System.exit(1);
		}
		System.out.println(termCl.toString());
		
		double Xcl=dget(termCl,"X");
		double Ycl=dget(termCl,"Y");

//		double nidCl=closestNodeOld(Xcl, Ycl);
		
		jipQuery = jip.openSynchronousQuery(parser.parseTerm("taxi(X,Y,Tid,Available,From,To,Languages,Rating,Long_distance,Type,_)"));
		boolean all=true;
		KmlWriter kml = new KmlWriter(outputDirPath, "common-files/map.kml");
		for (term = jipQuery.nextSolution(); term != null;term = jipQuery.nextSolution()) {
			int Tid=iget(term,"Tid");
			double X=dget(term,"X");
			double Y=dget(term,"Y");
			if(arr==null){
				System.out.println("arr==null");
				System.exit(1);
			}
			astar=new Astar(X,Y,Xcl,Ycl,jip, parser, nodes,"Taxi "+Tid,arr);
			System.out.println("Searching path for Taxi "+Tid+"...");
			if(!astar.ASearch()){
				System.out.println("Search failed for taxi "+Tid);
				all=false;
				continue;
			}
			System.out.println("Creating path for Taxi "+Tid+"...");
			astar.createPath();
			System.out.println("OK");
			System.out.println("Path Cost = "+astar.pathCost);
			System.out.println("  (hops="+astar.hops+",explored="+astar.explored+",visited="+astar.visited+")");
			Suitability suit=new Suitability(jip, term, termCl, astar,Tid);
			suit.solve();
			listSuit.add(suit);
//			System.out.println("Path length "+astar.path_length);
			System.out.println();
		}
		double Xdest=dget(termCl,"Xdest");
		double Ydest=dget(termCl,"Ydest");
		updateH(Xdest,Ydest);
		
		astar=new Astar(Xcl,Ycl,Xdest,Ydest,jip, parser, nodes, "Client",arr);
		if(!astar.ASearch()){
			System.out.println("Search Failed for your dest");
			kml.end();
			System.exit(0);
		}
		System.out.println("Creating path for Destination...");
		astar.createPath();
		kml.write(astar,true);
		System.out.println("OK");
		System.out.println("Path Cost = "+astar.pathCost);
		System.out.println("  (hops="+astar.hops+",explored="+astar.explored+",visited="+astar.visited+")");
		System.out.println();
		
		System.out.println("#################   RESULTS     ################");
		System.out.println();
		System.out.println("#####RATINGS#####");
		Comparator<Suitability> comparator = new Suitability.rComparator();
		Collections.sort(listSuit, comparator);
		int i=0;
		for (Suitability s:listSuit){
			i++;
			s.rsay();
			if(i==5)
				break;
		}
	
		System.out.println();
		System.out.println("#####SUITABILITY#####");
		 comparator = new Suitability.dComparator();
		Collections.sort(listSuit, comparator);
		Suitability bestSuit=listSuit.get(0);
		double bestDist=bestSuit.astar.pathCost;
		
		for (Suitability s: listSuit){
			s.distSuitability = 5.0*bestDist/s.astar.pathCost;
			s.suitability+=s.distSuitability;
		}
		
		comparator = new Suitability.sComparator();
		Collections.sort(listSuit, comparator);
		
		i=0;
		for (Suitability s:listSuit){
			i++;
			s.ssay();
			if(i==5)
				break;
		}
		
		for(Suitability s:listSuit){
			if (s.suitable)
				kml.write(s.astar,false);
		}
		
		
		kml.end();
		System.exit(1);
		//KmlWriter kml = new KmlWriter(outputDirPath);
	}
	
	private void updateH(double xdest,double ydest) {
		for(long a:arr){
			Node n=nodes.get(a);
			n.hScore=PrologParser.distance(n.x+"", n.y+"",xdest+"",ydest+"");
			n.parent=null;
			n.gScore=0.0;
			n.fScore=0.0;
		}
		
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
		long val = new BigDecimal(str).longValue();
		return val;
	}
	
	public static double dget(JIPTerm term, String var){
		return Double.valueOf(get(term,var));
	}

	public static long stol (String str) throws NumberFormatException{
		str=str.replace(" ", "");
		if(!str.contains("E")){
				return Long.parseLong(str);
		}
		long val = new BigDecimal(str).longValue();
		return val;
	}
	
	public static double stod(String str){
		str=str.replace(" ", "");
		return Double.valueOf(str);
	}

	public static long getTime(String str){
		String str1= (str+".0").replace(":", "").replace("'","");
		return new BigDecimal(str1).longValue();
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
