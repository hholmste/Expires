package slowcookviking;

public class ThingUser
{

    static
    {
        AThingWithExpiringMethods.anExpiredMethod(); // TODO: should cause compilation error
    }

    {
        AThingWithExpiringMethods.anExpiringMethod(); // TODO: should cause compilation warning
    }

    public static void main(String[] args)
    {
        AnExpiredThing anExpiredThing = new AnExpiredThing(); // TODO: should cause compilation warning
        anExpiredThing.aMethod(); // should compile fine
        AnExpiredThing.aMethod(); // should cause compilation warning
        AThingWithExpiringMethods.anExpiringMethod(); // should cause compilation warning
        AThingWithExpiringMethods.anExpiredMethod(); // should cause compilation error
    }

}