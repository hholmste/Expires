package slowcookviking;

import instantviking.Expires;

@Expires(day = 1, month = 1, year = 2120, usage = "Expired class, expiring in the future")
public class AnExpiredThing
{

    public static void aMethod()
    {
    }

}