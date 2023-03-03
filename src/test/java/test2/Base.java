package test2;

public class Base {
    public void foo() {
        System.out.println("Base.foo()");
    }
    public void foo(String param) {
        System.out.println("Base.foo()");
        if (param != null)
            System.out.println(param);
    }
}
