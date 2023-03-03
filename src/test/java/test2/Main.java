package test2;

public class Main {

    public static void main0() {
        Integer i = 1;
        System.out.println(i);
    }

    public static void main1() {
        Integer i = 1;
        System.out.println(i.getClass().getName());
    }

    private static Integer generateInt() {
        return 1;
    }

    public static void main2() {
        Integer i = generateInt();
        if (i > 0)
            System.out.println(i.getClass().getName());
    }

    public static void main3() {
        Integer i = 1;
        if (i > 0)
            System.out.println(i.getClass().getName());
        else
            System.out.println("i is not greater than 0");
    }

    public static void main4() {
        for (int i = 0; i < 10; i++)
            System.out.println(i);
    }

    public static void main5() {
        var i = 0;
        while (i < 10) {
            System.out.println("Not infinite loop");
            i++;
        }
    }

    private static void main6(int i) {
        if (i < 5) {
            System.out.println(i);
            main6(i + 1);
        }
    }

    public static void main7() {
        main6(0);
    }

    public static void main8() {
        Base b = new Base();
        b.foo();
    }
    
    public static void main9() {
        Implem b = new Implem();
        b.bar();
    }

    public static void main10() {
        Inter b = new Implem();
        b.bar();
    }

    public static void main11() {
        Extend b = new Extend();
        b.baz();
    }

    public static void main12() {
        Extend b = new Extend();
        b.foo();
    }

    public static void main13() {
        Base b = new Overrid();
        b.foo();
    }

    public static void main14() {
        Overrid b = new Overrid();
        b.foo();
    }

    public static void main15() {
        Base b = new Base();
        b.foo();
        b.foo("Hello");
    }

    public static void main16() {
        PlainConstruc b = new PlainConstruc();
    }

    public static void main17() {
        ObjInConstruct b = new ObjInConstruct();
    }

    public static void main18() {
        MethodInConstruc b = new MethodInConstruc();
    }
}
