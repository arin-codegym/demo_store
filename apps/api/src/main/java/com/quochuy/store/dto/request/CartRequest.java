package com.quochuy.store.dto.request;

import lombok.Data;

@Data
public class CartRequest {
	private String productId;
	private int amount; // Đảm bảo tên này khớp với phía Frontend gửi lên
}
