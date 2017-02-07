import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;


public class KmlWriter {

	String Placemark2=
			  "				</coordinates>\n"
			+ "			</LineString>\n"
			+ "		</Placemark>";

String end=
		  "	</Document>\n"
	+ "</kml>";

	static final String [] colors={"green", "gray", "black", "yellow", "maroon", "aqua", "fuchsia", "purple"};
	static int index=0;
	static final String main= "red";

PrintWriter writer;

	public KmlWriter(String outputDirPath, String initial){
		try {
			File from = new File(initial);
			File to = new File(outputDirPath + "/map.kml");
			Files.copy(from.toPath(), to.toPath());
			FileWriter fw = new FileWriter(to, true);
			BufferedWriter bw = new BufferedWriter(fw);
			this.writer =new PrintWriter(bw);
//			this.writer = new PrintWriter(outputDirPath+"/map.kml", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
			catch (IOException e) {
				e.printStackTrace();
		}
//		writer.println(start);
	}
	
	public void write(Astar astar, boolean isMain){
		giveColor(astar,isMain);
		if(!astar.found)
			return;
		writer.println(Placemark(astar));
		astar.printPath(writer);
		writer.println(Placemark2);
		/*    
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw new UsageException();
		}
		*/
	}
	
	static void giveColor(Astar astar, boolean isMain){
		if (isMain){
			astar.color=main;
		}
		else{
			astar.color=colors[index];
			index++;
			index=index%colors.length;
		}
	}
	
	public void end(){
		writer.println(end);
		writer.close();
	}
	
	static private String Placemark(Astar astar){
		String color=astar.color;
		String name=astar.name;
				return 
				  "		<Placemark>\n"
				+ "			<name>"+name+"</name>\n"
				+ "			<styleUrl>#"+color+"</styleUrl>\n"
				+ "			<LineString>\n"
				+ "				<altitudeMode>relative</altitudeMode>\n"
				+ "				<coordinates>";
	}

}
