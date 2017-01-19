
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class UsageException extends Exception {
	private static final long serialVersionUID = 1L;

	public UsageException() {
		super(
				"UsageException:\nUsage: java Main [path to input folder]\n"
						+ " If path is not defined ./input1 is the default input folder\n"
						+ " Input folder should have:\n  client.csv\n  nodes.csv\n  taxis.csv\n  lines.csv\n  traffic.csv\n"
						+ " program must have write priviledge at rundir\n");
	}
}

public class Main {
	private static String userdir;
	private static String inputDirPath;
	private static String outputDirPath;
	private static PrologParser myPrologParser;

	
	private static int client;
	private static Hashtable<Long,Node> nodes;
	 
	private static BufferedReader newBuff(String name)
			throws UnsupportedEncodingException, FileNotFoundException {
		String charset = "ISO-8859-7";
		InputStreamReader isr;
		isr = new InputStreamReader(new FileInputStream(inputDirPath + "/"
				+ name + ".csv"), charset);
		BufferedReader in = new BufferedReader(isr);
		return in;
	}

	private static void read_data() throws UnsupportedEncodingException,
			IOException, UsageException {
		String linestr;
		String[] line;
		Scanner sc = null;
		BufferedReader in = null;
		/*
		 * Taxi taxi; Node node; Line line_road; Traffic traffic;
		 */
		// CLIENT
		sc = new Scanner(new File(inputDirPath + "/client.csv"));
		sc.next();
		linestr = sc.next();
		int num=myPrologParser.num("client");
		line = linestr.split(",",num);
		myPrologParser.writePrologFact("client", line, true);
		sc.close();

		// TAXIS
		sc = new Scanner(new File(inputDirPath + "/taxis.csv"));
		sc.next();
		sc.next();
		while (sc.hasNext()) {
			linestr = sc.next();
			sc.skip("	");
			String linestr2 = "";
			String tab = sc.next();
			// taxis end with "\t(Name Name)\n
			tab = tab.substring(1, tab.length());
			while (!(tab.contains(")"))) {
				linestr2 = linestr2+tab+" ";
				tab = sc.next();
			}
			if (tab != null && tab.length() > 1)
				tab = tab.substring(0, tab.length() - 1);
			linestr2 = linestr2 + tab;
			linestr += ",";
			linestr+=linestr2;
			num=myPrologParser.num("taxi");
			line = linestr.split(",",num);
			myPrologParser.writePrologFact("taxi", line, true);
		}
		sc.close();

		// NODES
		nodes = new Hashtable<Long, Node>();
		in = newBuff("nodes");
		linestr = in.readLine();
		linestr = in.readLine();
		while (linestr != null) {
			num=myPrologParser.num("node");
			line = linestr.split(",",num);
			myPrologParser.writePrologFact("node", line, true);
			long l=new Long(line[3]);
			nodes.put(l,new Node(l));
			linestr = in.readLine();
		}
		in.close();

		// LINES
		in = newBuff("lines");
		linestr = in.readLine();
		while (linestr != null) {
			num=myPrologParser.num("line");
			line = linestr.split(",",num);
			myPrologParser.writePrologFact("line", line, true);
			linestr = in.readLine();
		}
		in.close();

		// TRAFFIC
		in = newBuff("traffic");
		linestr = in.readLine();
		while (linestr != null) {
			num=myPrologParser.num("traffic");
			line = linestr.split(",",num);
			myPrologParser.writePrologFact("traffic", line, true);
			linestr = in.readLine();
		}
		in.close();
	}

	private static void _read_data_() throws UsageException {
		try {
			read_data();
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			throw new UsageException();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw new UsageException();
		}
	}

	public static void main(String[] args) {
//		userdir = System.getProperty("user.dir");
		System.out.println(new java.io.File("").getAbsolutePath());
		try {
			// find i/o paths
			if (args.length == 0) {
				inputDirPath = "./input1";
				outputDirPath = "./output1";
			} else if (args.length == 1) {
				if (args[0].equals("-help") || args[0].equals("--help"))
					throw new UsageException();
				inputDirPath = args[0];
				outputDirPath = args[0];
			} else
				throw new UsageException();

			// create outputs folder
			try {
				File outdir;
				if (Files.exists(Paths.get(outputDirPath))){
					outdir = new File(outputDirPath);
					for (File c : outdir.listFiles())
					      Files.delete(Paths.get(c.getAbsolutePath()));
					(new File(outputDirPath)).delete();
				}
				else{
					outdir = new File(outputDirPath);
				}
				outdir.mkdirs();
			} catch (SecurityException s) {
				s.printStackTrace();
				throw new UsageException();
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new UsageException();
			}
			
			// copy genesis.pl to rules.pl and open new file facts.pl to write
			try {
				File from = new File("common-files/discontiguous.pl");
				File to = new File(outputDirPath + "/disc.pl");
				Files.copy(from.toPath(), to.toPath());
				
				from = new File("common-files/rules.pl");
				to = new File(outputDirPath + "/rules.pl");
				Files.copy(from.toPath(), to.toPath());
				
	//			to = new File(outputDirPath + "/facts.pl");
				FileWriter fw = new FileWriter(to, true);
				BufferedWriter bw = new BufferedWriter(fw);
				myPrologParser=new PrologParser(new PrintWriter(bw));
			} catch (IOException e) {
				e.printStackTrace();
				throw new UsageException();
			}

			// append to prolog.pl
			_read_data_();
			myPrologParser.destroy();
			
			System.out
					.println("################################################");
			System.out
					.println("#########   WELCOME TO TARIFAS-APP   ###########");
			System.out
					.println("################################################");
			
			Asolver solver = new Asolver(inputDirPath,outputDirPath,nodes);
			
			solver.solve();
					} catch (UsageException e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
}
