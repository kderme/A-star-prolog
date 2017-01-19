import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class KmlWriter {
	static String start=
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
	
	PrintWriter writer;
	
	public KmlWriter(String outputDirPath){
		try {
			this.writer = new PrintWriter(outputDirPath+"/map.kml", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.println(start);
	}
	
	public void write(Astar astar){
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
