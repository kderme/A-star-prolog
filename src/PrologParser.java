import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPTermParser;

public class PrologParser {
	private final Map<String, Integer> aMap;
	private final String [] known_functors=
			{"client","taxi","node","line","traffic"};
	private final int [] numOfTerms=
			{8,10,5,18,3};
	private Map<String,Integer> speed=null;
	private String last[]={"","","","",""};
	private String client[]=null;
	private Hashtable<Long,Node> nodes=null;

	private boolean checkPrev=true;
	private PrintWriter pwNext;
	
	public static JIPEngine jip;
	public static JIPTermParser parser;
	
	public PrologParser(Hashtable<Long,Node> nodes, PrintWriter pwNext) {
		aMap = new HashMap<String, Integer>();
		int numero=0;
		for(String s :known_functors){
			aMap.put( s, numero++);
		}
		this.pwNext=pwNext;
		jip = new JIPEngine();
		parser = jip.getTermParser();
		initMap();
	}
	
	public int num(String functor){
		return numOfTerms[aMap.get(functor)];
	}

	public static void consult(String file){
		try {
			jip.consultFile(file);
		} catch (JIPSyntaxErrorException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public String stringArrayToString(String start,
			List<String> stringArray, String delimiter, String end) {
		StringBuilder sb = new StringBuilder();
		for (String element : stringArray) {
			if (sb.length() > 0) {
				sb.append(delimiter);
			}
			sb.append(element);
		}
		return start + sb.toString() + end;
	}
	
	private void fixFacts(String functor, String[] line) {
		for (int i = 0; i < line.length; i++) {
			if (line[i].equals("")) {
				line[i] = "x";
				continue;
			}
			if ((line[i].contains("|") || (functor.equals("taxi") && i == 5) || (functor
					.equals("traffic") && i == 2))) {
				line[i] = "[" + line[i].replace("|", ",") + "]";
			}
			if(functor.equals("taxi")&&i==4){
				String s= line[i].replace('-', ',');
				line[i]=s;
			}
			line[i]=line[i].trim();
			if (line[i].contains("%")){
				String s=line[i].replace("%","");
				line[i]=s;
			}
			boolean should = false,good=false;
			char c0=line[i].charAt(0);
			good = Character.isLowerCase(c0);
			good=good || Character.isDigit(c0);
			good=good || c0=='[';
			should=should||!good;
			should=should||(functor.equals("client") && i==4);
			should=should||(functor.equals("node") && i==4);
			should=should||(functor.equals("line") && i==2);
			should=should||(functor.equals("taxi") && i==9);
			should=should||(functor.equals("traffic") && i==1);
			should=should||line[i].contains(" ");
			should=should||(line[i].contains(".")&& !line[i].matches("[-+]?\\d*\\.?\\d+")); //thats a regular expression of a float
			if (line[i].contains("'")){
				should=true;
				String s=line[i].replace("'","`"); 
				line[i]=s;
			}
			
			if (should){
				line[i]='\''+line[i]+'\'';
			}
			
		}
	}
	
	private String fixTerms(String functor,String [] line){
		List<String> n = new ArrayList<String>(Arrays.asList(line));
		int j=aMap.get(functor);
		int num=numOfTerms[j];
		int rest= num-line.length;
		for(int i=0; i<rest;i++){
			n.add("x");
		}
		if (functor.equals("traffic") && n.get(1).equals("x") && n.get(2).equals("x"))
			return "";
		return stringArrayToString("(", n, ",", ").");
		
	}
	
	public void writePrologFact(PrintWriter out_pl,String functor, String[] line) {
		fixFacts(functor, line);
		if (functor=="client"){
			client=line;
		}
		String fact;
		if(functor.equals("node")){
			if (client==null){
				System.out.println("Null Client!!");
				System.exit(1);
			}
			ArrayList<String> newline=new ArrayList<String>(Arrays.asList(line));
			Double dToStart=distance(newline.get(0),newline.get(1),client[0],client[1]);
			Double dToTarget=distance(newline.get(0),newline.get(1),client[2],client[3]);
			
		//	Double val = evaluate(line[2]);
			newline.add(dToStart+"");
			newline.add(dToTarget+"");
			line=new String[newline.size()];
			newline.toArray(line);
			fact=fixTerms(functor, line);
			long nid=new Long(line[3]);
			Node node=nodes.get(nid);
			node.x=Double.parseDouble(line[0]);
			node.y=Double.parseDouble(line[1]);
			node.Lid=new Long(line[2]);
			node.hScore=dToStart/120.0;
			if(checkPrev){
				if(line[2].equals(last[2])){
					double speed=evaluate(line[2]);
					double dist=distance(last[0],last[1],line[0],line[1]);
					Node node2=nodes.get(new Long(last[3]));
					if (speed!=Double.MAX_VALUE){
						if(speed>0){
							String s="next("+last[3]+","+nid+","+dist/speed+").";
							pwNext.println(s);
							node2.add(node,dist/speed);
							
						}
						else{
							String s="next("+nid+","+last[3]+","+dist/speed+").";
							pwNext.println(s);
							node.add(node2,dist/speed);
						}
					}
				}
				last=line;
			}
		}
		else{
			fact=fixTerms(functor, line);
		}
		if (fact!="")
			out_pl.println(functor + fact);
	}

	public static double distance(String x1,String y1,String x2,String y2){
		double dx=Double.parseDouble(x1)-Double.parseDouble(x2);
		double dy=Double.parseDouble(y1)-Double.parseDouble(y2);
		double dist=Math.sqrt(dx*dx+dy*dy);
		return dist;
	}

	private void initMap(){
		speed = new HashMap<String, Integer>();
		speed.put("motorway",120);
		speed.put("motorway_link",70);
		speed.put("primary",80);
		speed.put("secondary",60);
		speed.put("residential",50);
		speed.put("trunk",50);
		speed.put("service",50);
		speed.put("tertiary",30);
		speed.put("tertiary_link",25);
	}
	
	public Double evaluate(String Lid){
		String q=
"line("+Lid+",Highway,'Name',Oneway,Lit,Lanes,Maxspeed,Railway,Boundary,Access,Natural,Barrier,Tunnel,Bridge,Incline,Waterway,Busway,Toll).";		
		//q="canGoAll("+current.Nid+","+targetNid+",X),!.";
		JIPQuery jipQuery = jip.openSynchronousQuery(parser.parseTerm(q));			
		JIPTerm term = jipQuery.nextSolution();
		if(term==null){
			return 50.0;
		}
		
		//one-way
		String strOneway=Asolver.get(term,"Oneway");
		int oneway=0;
		if (strOneway.equals("yes"))
			oneway=1;
		else if (strOneway.equals("'-1'")){
			oneway=-1;
		}
		
		//max-speed
		String strMaxSpeed=Asolver.get(term,"Maxspeed");
		Integer maxSpeed;
		if (strMaxSpeed.equals("x")){
			String Railway=Asolver.get(term,"Highway");
			maxSpeed=speed.get(Railway);
			if (maxSpeed==null)
				return Double.MAX_VALUE;
		}
		else{
			maxSpeed=Integer.parseInt(strMaxSpeed);
		}
		
		//	TODO add traffic
/*		
		if(Asolver.get(term,"Railway").equals("subway")  ||  Asolver.get(term,"Railway").equals("rail"))
		q="traffic("+Lid+",'name',X).";
		JIPQuery jipQuery2 = jip.openSynchronousQuery(parser.parseTerm(q));			
		JIPTerm term2 = jipQuery.nextSolution();
		if(term2!=null ){
		}
*/
		if(Asolver.get(term,"Railway").equals("rails") || Asolver.get(term,"Railway").equals("subway"))
			return Double.MAX_VALUE;
		return (double) (oneway*maxSpeed);
	}

	
	public void setNodes(Hashtable<Long, Node> nodes) {
		this.nodes=nodes;
	}
	
}

	