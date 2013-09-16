package slowcookviking;

import instantviking.Expires;

public class AThingWithExpiringMethods
{
    @Expires(day = 1, month = 1, year = 2000, usage = "Expired method, expired in the past")
    public static void anExpiredMethod()
    {
    }

    @Expires(day = 1, month = 1, year = 2222, usage = "Expiring method, expiring in the future")
    public static void anExpiringMethod()
    {
    }
}
