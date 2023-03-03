package test2;

public class ObjInConstruct {
    public ObjInConstruct() {
        System.out.println("ObjInConstruct.ObjInConstruct()");
        Base b = new Base();
        b.foo();
    }
}

