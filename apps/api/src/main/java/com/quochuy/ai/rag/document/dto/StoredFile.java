package com.quochuy.ai.rag.document.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoredFile {
	private final String storagePath;
	private final String originalFileName;
	private final String mimeType;
	private final String checksumSha256;
//	private final byte[] bytes;// bỏ ví nếu lưu vào đây tốn RAM hơn
}
