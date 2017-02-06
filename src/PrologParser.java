import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class PrologParser {
	private final Map<String, Integer> aMap;
	private final String [] known_functors=
			{"client","taxi","node","line","traffic"};
	private final int [] numOfTerms=
			{8,10,5,18,3};
	private String last[]={"","","","",""};
	private String client[]=null;
	private static Hashtable<Long,Node> nodes;
	private boolean cheat=true;
	
	public PrologParser(Hashtable<Long,Node> nodes) {
		aMap = new HashMap<String, Integer>();
		int numero=0;
		for(String s :known_functors){
			aMap.put( s, numero++);
		}
		this.nodes=nodes;
	}
	
	public int num(String functor){
		return numOfTerms[aMap.get(functor)];
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
	
	public void writePrologFact(PrintWriter out_pl,PrintWriter out_pl2, String functor, String[] line,boolean checkPrev) {
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
			newline.add(dToStart+"");
			newline.add(dToTarget+"");
			line=new String[newline.size()];
			newline.toArray(line);
			fact=fixTerms(functor, line);
			if(checkPrev){
				if(line[2].equals(last[2])){
					double dist=distance(last[0],last[1],line[0],line[1]);
					String s="next("+last[3]+","+line[3]+","+dist+").";
					out_pl2.println(s);
					if(false){
						Node node=nodes.get(new Long(line[3]));
						node.n.add(new Long(last[3]));
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
	
	
	private double distance(String x1,String y1,String x2,String y2){
		double dx=Double.parseDouble(x1)-Double.parseDouble(x2);
		double dy=Double.parseDouble(y1)-Double.parseDouble(y2);
		double dist=Math.sqrt(dx*dx+dy*dy);
		return dist;
	}
}

	