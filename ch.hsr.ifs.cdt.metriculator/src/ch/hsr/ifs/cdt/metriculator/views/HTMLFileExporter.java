package ch.hsr.ifs.cdt.metriculator.views;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.hsr.ifs.cdt.metriculator.MetriculatorPluginActivator;
import ch.hsr.ifs.cdt.metriculator.model.AbstractMetric;
import ch.hsr.ifs.cdt.metriculator.model.converters.ModelToXMLConverter;
import ch.hsr.ifs.cdt.metriculator.model.nodes.AbstractNode;

public class HTMLFileExporter extends FileExporter {

	public String theme = "simple";
	
	public HTMLFileExporter(Path export_location, AbstractNode root, Collection<AbstractMetric> metrics) {
		super(export_location, root, metrics);
	}

	@Override
	public void run() {
		ModelToXMLConverter x = new ModelToXMLConverter();
		x.convert(root, metrics);

		Document doc = x.getResult();
		injectProperties(doc);
		
		if (export_location.isEmpty()) {
			export_location = Path.fromOSString(System.getProperty("user.home")).append("metriculator-export");
		}
		
		String xml = x.getXML();
		System.out.println(xml);
		StreamSource xmlStream = new StreamSource(new StringReader(xml));
		StreamSource xslStream = new StreamSource(getProjectFile("export-resources/html/html.xslt"));
		IPath htmlFilename = export_location.append("index").addFileExtension("html");
		
		copyResourcesTo(export_location);
		transform(xmlStream, xslStream, new StreamResult(htmlFilename.toOSString()));
		open(htmlFilename);
	}

	private void injectProperties(Document doc) {
		//CheckersRegistry.getInstance().getRefProblems(metrics.iterator().next().getChecker());
		Element propEl = doc.createElement("properties");
		doc.getDocumentElement().appendChild(propEl);
		
		// inject theme property
		Element themeEl = doc.createElement("theme");
		themeEl.setAttribute("name", theme);
		propEl.appendChild(themeEl);
	}

	private void transform(StreamSource xmlStream, StreamSource xslStream, StreamResult result) {
		try {			
			TransformerFactory factory = TransformerFactory.newInstance();
			Templates template = factory.newTemplates(xslStream);
			Transformer transformer = template.newTransformer();
			transformer.transform(xmlStream, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void open(IPath filename) {
		File file = new File(filename.toOSString());
		try {
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				String cmd = "rundll32 url.dll,FileProtocolHandler " + file.getCanonicalPath();
				Runtime.getRuntime().exec(cmd);
			} else {
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyResourcesTo(IPath export_location) {
        try {
			File source = getProjectFile("export-resources/html");
			File dest = new File(export_location.toOSString());
			copyFolder(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getProjectFile(String projectrelativePath) {
		Bundle bundle = MetriculatorPluginActivator.getDefault().getBundle();
        URL resource = bundle.getEntry(projectrelativePath);
		try {
			return new File(FileLocator.resolve(resource).toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see http://www.mkyong.com/java/how-to-copy-directory-in-java/
	 * */
	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				System.out.println("Directory copied from " + src + "  to "+ dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			System.out.println("File copied from " + src + " to " + dest);
		}
	}
}
