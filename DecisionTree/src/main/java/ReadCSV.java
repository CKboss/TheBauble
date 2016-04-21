import Entity.Car;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by ckboss on 16-4-20.
 */

public class ReadCSV {

    String filepath ;

    ReadCSV() {
    }

    ReadCSV(String filepath) {
        this.filepath = filepath;
    }

    <T> ArrayList<T> Read(Class T,String path) {
        this.filepath = path;
        return Read(T);
    }

    <T> ArrayList<T> Read(Class T) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(this.filepath));

            boolean firstline = true;
            String[] titles;
            Field[] fields = T.getDeclaredFields();
            ArrayList<T> ret = new ArrayList<>();

            while(br.ready()) {

                String line = br.readLine();
                if(firstline) {
                    titles = line.split(",");
                    firstline = false;
                } else {
                    String[] words = line.split(",");
                    int cnt=0;
                    T t = (T) T.newInstance();
                    for(String word : words) {

                        Field field = fields[cnt];
                        field.set(t,Integer.valueOf(word));

                        cnt++;
                    }
                    ret.add(t);
                }
            }

            return ret;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {

        /*
        ReadCSV readCSV = new ReadCSV("/home/ckboss/Documents/MachineLearning/数据集/Toydataset/t1.csv");
        ArrayList<People> peoples = readCSV.Read(People.class);

        for(People people : peoples ) {
            System.out.println(people);
        }
        */

        ReadCSV readCSV = new ReadCSV("/home/ckboss/Documents/MachineLearning/数据集/car/car.csv");
        ArrayList<Car> cars = readCSV.Read(Car.class);

        for(Car car : cars) {
            System.out.println(car);
        }
    }
}
