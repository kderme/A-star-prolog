import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PrologParser {
	private final PrintWriter out_pl;
	private final Map<String, Integer> aMap;
	private final String [] known_functors=
			{"client","taxi","node","line","traffic"};
	private final int [] numOfTerms=
			{8,10,5,18,3};
	private String lastNid;
	private String lastLid;
	
	public PrologParser(PrintWriter printWriter) {
		out_pl=printWriter;
		aMap = new HashMap<String, Integer>();
		int numero=0;
		for(String s :known_functors){
			aMap.put( s, numero++);
		}
		lastNid="";
		lastLid="";
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
	
	public void writePrologFact(String functor, String[] line,boolean checkPrev) {
		fixFacts(functor, line);
		String fact=fixTerms(functor, line);
		if(checkPrev){
			if(functor.equals("node")){
				if(line[2].equals(lastLid)){
					String s="next("+lastNid+","+line[3]+").";
					out_pl.println(s);
				}
				lastNid=line[3];
				lastLid=line[2];
			}
		}
		if (fact!="")
			out_pl.println(functor + fact);
	}
	
	public void destroy(){
		out_pl.close();
	}
}

	