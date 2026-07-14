namespace UgorjBe.Domain;

public enum OfferCategory
{
    PLAYHOUSE,
    WORKSHOP,
    MOVEMENT,
    SWIMMING,
    SPORT,
    MUSEUM,
    PARENT_CHILD
}

public enum OfferStatus
{
    PUBLISHED,
    WITHDRAWN
}

public enum BookingStatus
{
    CONFIRMED,
    CANCELLED
}

public enum BookingScope
{
    ACTIVE,
    PREVIOUS,
    ALL
}

public enum OfferSort
{
    START_TIME,
    PRICE,
    DISTANCE,
    DISCOUNT
}

public enum SortDirection
{
    ASC,
    DESC
}
