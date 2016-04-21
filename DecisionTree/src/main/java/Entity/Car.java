package Entity;

/**
 * Created by ckboss on 16-4-21.
 */
public class Car {

   public int buying,maint,doors,persons,lug_boot,safety,kind;

   @Override
   public String toString() {
      return "Car{" +
              "buying=" + buying +
              ", maint=" + maint +
              ", doors=" + doors +
              ", persons=" + persons +
              ", lug_boot=" + lug_boot +
              ", safety=" + safety +
              ", kind=" + kind +
              '}';
   }
}
