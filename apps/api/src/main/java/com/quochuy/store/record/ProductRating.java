package com.quochuy.store.record;

import java.util.UUID;
import java.util.function.Supplier;

public record ProductRating(UUID productId, int rating, int count)  {
}
