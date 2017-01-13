import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.util.regex.Pattern;


class UsageException extends Exception
{
 	private static final long serialVersionUID = 1L;

public UsageException()
   {
     super("Usage: java Main [path to input folder]\n"+
		"If path is not defined ./TN2016-CW1 is the default input folder\n"+
		"Input folder should have:\n client.csv\n nodes.csv\n taxis.csv");
   }
}

public class Main{
	private static String userdir;
	private static String input_path;
	private static String output_path;
	private static PrintWriter out_pl;
/*	
	private static Client client;
	private static ArrayList<Taxi> taxis;

	private static ArrayList<Node> nodes;
	private static ArrayList<Line> lines;
	private static ArrayList<Traffic> traffic;
*/
	
	public static String stringArrayToString( String start, String[] stringArray, String delimiter, String end ) {
	    StringBuilder sb = new StringBuilder();
	    for ( String element : stringArray ) {
	        if (sb.length() > 0) {
	            sb.append( delimiter );
	        }
	        sb.append( element );
	    }
	    return start+sb.toString()+end;
	}
	
	private static void fixAtoms(String querry, String [] line){
		for(int i=0; i<line.length; i++){
			if (line[i].equals("")){
				line[i]="null";
			}
			if ((line[i].contains("|")  ||  (querry.equals("taxi") && i==5)  || (querry.equals("traffic") && i==2) )){
				String[] tolist=line[i].split("|");
				line[i]="["+line[i].replace("|",",")+"]";
			}
			line[i].replace(' ', '-');
		}
	}
	
	private static void toPrologQuerry(String querry,String [] line){
		fixAtoms(querry, line);
		out_pl.println(querry+stringArrayToString("(",line,",",")."));
		//out_pl.println("a");
	}
	
	private static BufferedReader newBuff(String name) throws UnsupportedEncodingException, FileNotFoundException{
		String charset = "ISO-8859-1";
		InputStreamReader isr;
		isr=new InputStreamReader 
				(new FileInputStream(input_path+"/"+name+".csv"), charset);
		BufferedReader in = new BufferedReader(isr);
		return in;
	}
	
	private static void read_data() throws UnsupportedEncodingException, IOException, UsageException{
		String linestr;
		String [] line;
		Scanner sc = null;
		BufferedReader in=null;
/*		
		Taxi taxi;
		Node node;
		Line line_road;
		Traffic traffic;
*/		
		//CLIENT
		sc=new Scanner(new File(input_path+"/client.csv"));
		sc.next();
		linestr=sc.next();
		line=linestr.split(",");
		toPrologQuerry("client",line);
//		client=new Client(line);
		sc.close();

		//TAXIS
//		taxis = new ArrayList<Taxi>();
		sc=new Scanner(new File(input_path+"/taxis.csv"));
		sc.next();
		sc.next();
		while(sc.hasNext()){
			linestr=sc.next();
			System.out.println(linestr);
			sc.skip("	");
			String linestr2="";
			String tab=sc.next();
			tab=tab.substring(1,tab.length());
			System.out.println(tab);
			while(!(tab.contains(")"))){
				linestr2=linestr2+"-";
				linestr2+=tab;
				tab=sc.next();
			}
			if(tab!= null && tab.length()>1)
				tab=tab.substring(0,tab.length()-1);
			linestr2=linestr2+tab;
/*			
			byte[] byteArray = new byte[100];
			String s="";
			byte b=sc.nextByte();
			for(int i=0; b!=Character.getDirectionality(')'); i++){
				byteArray[i]=b;
				b=sc.nextByte();
			}
			
			String linestr2=new String(byteArray);
//			linestr2=linestr2.substring(1, linestr2.length()-1);
 * */
 
			linestr+=",";
			linestr+=linestr2;
			System.out.println(linestr);
			line=linestr.split(",");
			toPrologQuerry("taxi",line);
//			taxi = new Taxi(linestr);
//			taxis.add(line);
			System.out.println(linestr);
		}
		sc.close();
		
		//NODES
//		nodes = new ArrayList<Node>();
		in=newBuff("nodes");
		linestr=in.readLine();
		while(linestr!=null){
			line=linestr.split(",");
			toPrologQuerry("node",line);
//			node=new Node(linestr);
//			nodes.add(node);
			linestr=in.readLine();
		}
		in.close();

		//LINES
//		nodes = new ArrayList<Node>();
		in=newBuff("lines");
		linestr=in.readLine();
		while(linestr!=null){
			line=linestr.split(",");
			toPrologQuerry("line",line);
//			node=new Node(linestr);
//			nodes.add(node);
			linestr=in.readLine();
		}
		in.close();
		
		//TRAFFIC
//		nodes = new ArrayList<Node>();
		in=newBuff("traffic");
		linestr=in.readLine();
		while(linestr!=null){
			line=linestr.split(",");
			toPrologQuerry("traffic",line);
//			node=new Node(linestr);
//			nodes.add(node);
			linestr=in.readLine();
		}
		in.close();
	}

	private static void _read_data_() throws UsageException{
	try {	read_data();	}
	catch(UnsupportedEncodingException e){
		System.out.println(e.getMessage());
		throw new UsageException();
	}
	catch(IOException e){
		System.out.println(e.getMessage());
		throw new UsageException();
	}

	}

	public static void main(String[] args)
	{
		userdir=System.getProperty("user.dir");
		System.out.println(new java.io.File("").getAbsolutePath());
	try{
		//find paths
		if(args.length==0){
			input_path="./input1";
			output_path="./output1";
		}
		else if(args.length==1){
			if (args[0].equals("-help") || args[0].equals("--help"))
				throw new UsageException();
			input_path=args[0];
			output_path=args[0];
		}
		else
			throw new UsageException();
	
		//create outputs folders 
		if (!(new File(output_path).mkdirs())) {
			System.out.println("Output folder failed to be created");
		//	throw new UsageException();
		}
//		Files.copy("initial.pl", output_path+"prolog.pl");
		
		//
		try{
			FileWriter fw = new FileWriter(output_path+"/prolog.pl", true);
			BufferedWriter bw = new BufferedWriter(fw);
			out_pl = new PrintWriter(bw);
		}
		catch (IOException e) {
			System.out.println("Failed to open prolog file");
		}
		
		_read_data_();
		
		System.out.println("################################################");
		System.out.println("#########   WELCOME TO TARIFAS-APP   ###########");
		System.out.println("################################################");
/*		
		System.out.println("Client is at:");
		client.print();
		System.out.println("Taxis are at is at:");
		for(Taxi t :taxis){
			t.print();
		}
		
		client.FindClosestNode(nodes);
		System.out.println("Client closest Node is at:");
		client.closestN.print();
		System.out.println("Taxis closest Nodes are at:");
		for(Taxi t : taxis){
			t.FindClosestNode(nodes);
			t.closestN.print();
		}
		
		for(Node n : nodes){
			n.hEuclDist = n.distance(client.closestN);
		}
		
		Node.findAdj(nodes);
		boolean all=true;
		for(Taxi t: taxis){
			System.out.println();
			System.out.println("Searching path for Taxi "+t.id+"...");
			t.Astar = new A_star (t.closestN, client.closestN, nodes);
			if(!(t.found=t.Astar.ASearch())){
				all=false;
				System.out.println("Search failed for taxi "+t.id+"");
				continue;
			}
			System.out.println("Creating path for Taxi "+t.id+"...");
			t.Astar.path();
			System.out.println("Path length "+t.Astar.path_length);
			System.out.println("OK");
//			t.Astar.print_path();
		}
		System.out.println();
		if(all)
			System.out.println("All done.\nLets find the best taxi...");
		else
			System.out.println("Failed to find route for some taxis!\n"
					+ "You may want to give a bigger value for max_dist in Node.java.\n"
					+ "This value is used to avoid creating too long edges\n"
					+ "Lets find the best taxi from the succesfull...");
		Taxi t=Taxi.best(taxis);
		t.isbest=true;
		System.out.println("Best Taxi had id: "+t.id+" and path length:"+t.Astar.path_length);
		System.out.println();
		CreateKml();
		System.out.println("You can find the kml output file at "+path+"/map.kml");
*/		
	}
	catch (UsageException e){
			System.out.println(e.getMessage());
			System.exit(0);
	}
	}
	}
/*	
	static private String Placemark(int i){
		String color=taxis.get(i).color();
		int j=taxis.get(i).id;
				return 
				  "		<Placemark>\n"
				+ "			<name>Taxi "+j+"</name>\n"
				+ "			<styleUrl>#"+color+"</styleUrl>\n"
				+ "			<LineString>\n"
				+ "				<altitudeMode>relative</altitudeMode>\n"
				+ "				<coordinates>";
	}
	
	static private void CreateKml() throws UsageException{
		String start=
				  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "	<kml xmlns=\"http://earth.google.com/kml/2.1\">\n"
				+ "	<Document>\n"
				+ "		<name>Taxi Routes</name>\n"
				+ "		<Style id=\"green\">\n"
				+ "			<LineStyle>\n"
				+ "				<color>ff009900</color>\n"
				+ "				<width>4</width>\n"
				+ "			</LineStyle>\n"
				+ "		</Style>\n"
				+ "		<Style id=\"red\">\n"
				+ "			<LineStyle>\n"
				+ "				<color>ff0000ff</color>\n"
				+ "				<width>4</width>\n"
				+ "			</LineStyle>\n"
				+ "		</Style>";
		
		String Placemark2=
				  "				</coordinates>\n"
				+ "			</LineString>\n"
				+ "		</Placemark>";
		
		String end=
				  "	</Document>\n"
				+ "</kml>";
		
		try{
			
		    PrintWriter writer = new PrintWriter(path+"/map.kml", "UTF-8");
		    
		    writer.println(start);
		    for(int i=0; i<taxis.size();i++){
		    	Taxi t=taxis.get(i);
		    	if(!t.found)
		    		continue;
		    	writer.println(Placemark(i));
		    	t.Astar.print_path(writer,client,taxis.get(i));
		    	writer.println(Placemark2);
		    }
		    writer.println(end);
		    
		    writer.close();
		    
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw new UsageException();
		}
		
		}
	
}
*/