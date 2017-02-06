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
	
	Hashtable<Integer,Astar> liSTars;
	Hashtable<Long,Node> nodes;
	
	String inputDirPath;
	String outputDirPath;
	
	public Asolver(String inputDirPath,String outputDirPath,Hashtable<Long,Node> nodes) {
		/* Query q1 =
			    new Query(
			        "consult",
			        new Term[] {new Atom(outputDirPath+"/rules.pl")}
			    );	*/
		liSTars = new Hashtable<Integer,Astar>();
		this.nodes=nodes;
		jip = new JIPEngine();
		parser = jip.getTermParser();
		this.inputDirPath=inputDirPath;
		this.outputDirPath=outputDirPath;
		System.out.println(System.getProperty("java.library.path"));
/*
		String t0 = "consult('test.pl')";
		if (!Query.hasSolution(t0)) {
			System.out.println(t0 + " failed");
			System.exit(1);
		}
*/		


		Environment environment=new Environment();
		System.out.println("Consulting prolog file for the first time");
		environment.ensureLoaded(AtomTerm.get("common-files/rules2.pl"));
		
		System.out.println("DONE");
//		environment.ensureLoaded(AtomTerm.get(outputDirPath+"/rules.pl"));
		//get(Type);
/*		
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PrologCode code=null;
		try {
			code = environment.getPrologCode(CompoundTermTag.get((CompoundTerm) goalTerm));
		} catch (PrologException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(code);
*/
/*		
		Term[] args = { AtomTerm.get("b")};
		 CompoundTerm goalTerm = new CompoundTerm(AtomTerm.get("a"),null);
		try {
			interpreter.runOnce(AtomTerm.get("a(b)."));
		} catch (PrologException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	*/	
		


		try {
			System.out.println("Consulting prolog file for the first time");
			System.out.println("This will take a while...");
			jip.consultFile(outputDirPath+"/nodes.pl");
			jip.consultFile(outputDirPath+"/nextt.pl");
			jip.consultFile(outputDirPath+"/rest.pl");
			jip.consultFile(outputDirPath+"/rules.pl");
//			jip.consultFile(folder+"/facts.pl");
		} 
		catch (JIPSyntaxErrorException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
		System.out.println();
		
		System.out.println("DONE");
	
	}
	
	public void solve(){
		JIPQuery jipQuery,jipQueryCl;
		JIPTerm term, termCl;
		Astar astar;
		
		String q="client(X,Y,Xdest,Ydest,Time, Persons, Language, Luggage).";
		System.out.println(q);
	//	jipQuery=jip.getTermParser().parseTerm("?- "+q);
		jipQueryCl = jip.openSynchronousQuery(parser.parseTerm("?- "+q));
		termCl = jipQueryCl.nextSolution();
		if(termCl==null){
			System.out.println("Can`t find client");
			System.exit(1);
		}
		System.out.println("0");
		System.out.println(termCl.toString());
		System.out.println("1");
		double Xcl=dget(termCl,"X");
		double Ycl=dget(termCl,"Y");
		System.out.println("2");
//		double nidCl=closestNode(Xcl, Ycl);
		System.out.println("3");
	//	System.exit(1);
		
	//	System.exit(1);
		
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
