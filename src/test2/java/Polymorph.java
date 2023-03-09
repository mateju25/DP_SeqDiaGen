public class Polymorph {
    private Base c;
    private Base d = new Overrid();

    public Polymorph() {
        c = new Base();
    }

    public void tmp(Base f) {
        Base e = new Overrid();
        e.foo();
        c.foo();
        d.foo();
        f.foo();
    }
}
