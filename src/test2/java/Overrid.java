

public class Overrid extends Base {
    public void foo() {
        System.out.println("Overrid.foo()");
        String s = this.getClass().getName();
        Integer i = generateInt();
    }

    private Integer generateInt() {
        return 1;
    }
}
