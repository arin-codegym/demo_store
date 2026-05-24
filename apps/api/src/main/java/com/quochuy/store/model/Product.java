package com.quochuy.store.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Product {
	UUID productId;
	String name;
	String company;
	String description;
	boolean featured;
	String image;
	int price;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;
	UUID userId;
}
