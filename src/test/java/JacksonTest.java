import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import light.rpc.util.json.JacksonHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by jiangzhiwen on 17/2/14.
 */
public class JacksonTest {
    public static enum Sex{
        MALE, FEMALE;
    }

    public static class C2{
        private String a = "a";
        private int b = 1;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    public static class C1{
        private String name = "clazz";
        private int age = 10;
        private Sex sex = Sex.MALE;
        private C2 c2;
        private List<C2> list;
        private Map<Integer, C2> map;
        private Map<Sex, C2> map2;
        private Set<C2> set;
        /*
        private LocalDateTime time = LocalDateTime.now();
        private LocalDate date = LocalDate.now();
        */
        private BigDecimal big;
        private Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

        public C1() {
            list = new ArrayList<>();
            list.add(new C2());
            list.add(new C2());

            map = new HashMap<>();
            map.put(1, new C2());

            map2 = new HashMap<>();
            map2.put(Sex.FEMALE, new C2());

            set = new HashSet<>();
            set.add(new C2());

            big = new BigDecimal(123);
        }

        public C1(int a, int b) {
            list = new ArrayList<>();
            list.add(new C2());
            list.add(new C2());

            map = new HashMap<>();
            map.put(1, new C2());

            map2 = new HashMap<>();
            map2.put(Sex.FEMALE, new C2());

            set = new HashSet<>();
            set.add(new C2());

            big = new BigDecimal(123);
        }

        public BigDecimal getBig() {
            return big;
        }

        public void setBig(BigDecimal big) {
            this.big = big;
        }

        public Map<Sex, C2> getMap2() {
            return map2;
        }

        public void setMap2(Map<Sex, C2> map2) {
            this.map2 = map2;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        /*
        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
*/

        public List<C2> getList() {
            return list;
        }

        public void setList(List<C2> list) {
            this.list = list;
        }

        public Map<Integer, C2> getMap() {
            return map;
        }

        public void setMap(Map<Integer, C2> map) {
            this.map = map;
        }

        public Set<C2> getSet() {
            return set;
        }

        public void setSet(Set<C2> set) {
            this.set = set;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Sex getSex() {
            return sex;
        }

        public void setSex(Sex sex) {
            this.sex = sex;
        }

        public C2 getC2() {
            return c2;
        }

        public void setC2(C2 c2) {
            this.c2 = c2;
        }

    }

    public static void main(String[] args) throws IOException {
        /*
        main1(args);
        System.exit(0);
        */

        C1 c1 = new C1(1,1);
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(c1);
        System.out.println(jsonStr);

        //C1 c11 = mapper.readValue(jsonStr, C1.class);
        C1 c11 = mapper.readValue(jsonStr, mapper.getTypeFactory().constructType(C1.class));

        System.out.println("");
        String s = mapper.writeValueAsString(c11);
        System.out.println(s);

    }

    public static void main1(String[] args) throws IOException {
        C1 c1 = new C1(1,1);
        ObjectMapper mapper = JacksonHelper.getMapper();
        String s = mapper.writeValueAsString(c1);
        System.out.println(s);

        C1 c11 = mapper.readValue(s, JacksonHelper.genJavaType(C1.class));
        s = mapper.writeValueAsString(c11);
        System.out.println(s);
    }



}
