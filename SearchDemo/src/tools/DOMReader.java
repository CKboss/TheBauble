package tools;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

/**
 * Created by ckboss on 16-4-15.
 */
public class DOMReader {

    File file ;
    SAXReader saxReader;
    Document doc;
    Element root;

    public DOMReader(String filepath) {
        file = new File(filepath);
        saxReader = new SAXReader();
        try {
            doc = saxReader.read(file);
            root = doc.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public String getByName(String name) {
        return root.elementText(name);
    }

    public static void main(String[] args) {
        DOMReader reader = new DOMReader("/tmp/report45.xml");
        System.out.println(reader.getByName("type"));
    }
}
