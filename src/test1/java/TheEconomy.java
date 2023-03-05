public class TheEconomy extends ConcreteSubject {

    public TheEconomy()
    {
        super.setState("The Price of gas is at $5.00/gal");
    }

    public void attach(Observer obj) {
        System.out.println( "Economy attaching " + obj.getClass().getName() );
        observers.add(obj) ;
    }
	 
}

/*

The Price of gas is at $5.00/gal
The New iPad is out today

*/
