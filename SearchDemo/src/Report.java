import tools.DOMReader;

import java.lang.reflect.Field;

/**
 * Created by ckboss on 16-4-15.
 */
public class Report {

    String checksum;
    String subtype;
    String type;
    String chief_complaint;
    String discharge_diagnosis;
    String year;
    String downlaod_time;
    String update_time;
    String deid;
    String report_text;

    public Report() {
    }

    public Report(String filepath) {
        DOMReader reader = new DOMReader(filepath);
        Field[] fields = this.getClass().getDeclaredFields();
        for(int i=0,len=fields.length;i<len;i++) {
            String name = fields[i].getName();
            try {
                fields[i].set(this,reader.getByName(name));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Report{" +
                "checksum='" + checksum + '\'' +
                ", subtype='" + subtype + '\'' +
                ", type='" + type + '\'' +
                ", chief_complaint='" + chief_complaint + '\'' +
                ", discharge_diagnosis='" + discharge_diagnosis + '\'' +
                ", year='" + year + '\'' +
                ", downlaod_time='" + downlaod_time + '\'' +
                ", update_time='" + update_time + '\'' +
                ", deid='" + deid + '\'' +
                ", report_text='" + report_text + '\'' +
                '}';
    }

    public static void main(String[] args) {
        Report report = new Report("/tmp/report45.xml");
        System.out.println(report);
    }
}
