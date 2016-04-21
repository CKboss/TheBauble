/**
 * Created by ckboss on 16-4-20.
 */
public class People {

    int id,age,job,house,credit,kind;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", age=" + age +
                ", job=" + job +
                ", house=" + house +
                ", credit=" + credit +
                ", kind=" + kind +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        People people = (People) o;

        if (id != people.id) return false;
        if (age != people.age) return false;
        if (job != people.job) return false;
        if (house != people.house) return false;
        if (credit != people.credit) return false;
        return kind == people.kind;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + age;
        result = 31 * result + job;
        result = 31 * result + house;
        result = 31 * result + credit;
        result = 31 * result + kind;
        return result;
    }

    public static void main(String[] args) {
    }
}
